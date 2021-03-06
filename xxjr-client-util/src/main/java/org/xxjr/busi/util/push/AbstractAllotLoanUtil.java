package org.xxjr.busi.util.push;

import java.time.LocalDate;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.BorrowChannelUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;

import lombok.extern.slf4j.Slf4j;

/**
 * 针对参与分单的小贷数据特殊处理工具类
 * @author Administrator
 *
 */
@Slf4j
public abstract class AbstractAllotLoanUtil extends AbstractLoanUtil {

	@Override
	public int pushData(Map<String, Object> row, Integer pushType) {
		int status = 2;
		try {
			Map<String, Object> config = PushPlatformUtils.getConfigByCode(pushType);
			
			if ((!PushPlatformUtils.checkChannelOpen(config))) {
				addPushRecord(row, pushType, 3, (config.get("pushName") + "推送未打开"));
				return 3;
			}
			
			if (!PushPlatformUtils.checkOffDayStopPush(StringUtil.objectToStr(config.get("pushCode")))) {
				log.info(config.get("pushName") + "今天是休息日不推送!");
				return 3;
			}
			
			if (!PushPlatformUtils.isPushDateTime(StringUtil.objectToStr(config.get("pushCode")))) {
				log.info(config.get("pushName") + "还未到推送时间!");
				return 3;
			}
			
			if ((!PushPlatformUtils.checkChannelMaxCount(config))) {
				addPushRecord(row, pushType, 3, config.get("pushName") + "推送已达上限");
				return 3;
			}
			
			if ((!PushPlatformUtils.checkChannelLimit(row, config))) {
				addPushRecord(row, pushType, 4, (config.get("pushName") + "筛选条件无法通过"));
				return 4;
			}
			
			if ((!checkAllotChannel(row, config))) {//这个需要最后判断，先判断总量有没有到，再判断每个的占比
				return 4;
			}
			
			Map<String, Object> response = response(row);//推送数据由子类实现
			
			if (StringUtils.isEmpty(response.get("status"))) {
				throw new SysException("缺少必须返回的参数!");
			}
			
			status = NumberUtil.getInt(response.get("status"), 2);
			AppParam updateParam = new AppParam("thirdPushPoolService", "updateStatus");
			updateParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			updateParam.addAttrs(row);
			updateParam.addAttr("pushType", pushType);
			updateParam.addAttr("status", status);
			updateParam.addAttr("message", response.get("message"));
			RemoteInvoke.getInstance().call(updateParam);
			log.info("推送" + config.get("pushName") + "数据任务 "+ getClass().getName() +" end");
		} catch (Exception e) {
			log.error(getClass().getName() + " >>>>>>>>>>>>>>>>>>error", e);
		}
		return status;
	}
	
	/**
	 * 参与分单的小贷存在信息流渠道和api渠道比例分配问题需要特殊处理
	 * @return
	 */
	public boolean checkAllotChannel (Map<String, Object> row, Map<String, Object> config){
		boolean flag = false;
		AppParam param = new AppParam("borrowApplyPushService", "queryChannelProportion");
		param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
		param.addAttr("nowDate", LocalDate.now().toString());
		param.addAttr("pushType", config.get("pushCode"));
		AppResult result = RemoteInvoke.getInstance().callNoTx(param);
		int apiCount = NumberUtil.getInt(result.getAttr("apiCount"), 0);
		int otherCount = NumberUtil.getInt(result.getAttr("otherCount"), 0);
		
		double apiScale = 100;
		if (NumberUtil.getInt(config.get("apiScale"), 100) > 0) {
			apiScale = (NumberUtil.getInt(config.get("apiScale"), 100) / 100.0);
		}else {
			apiScale = 0;
		}
		
		int totalCount = NumberUtil.getInt(config.get("totalCount"), 500);
		
		int needApiCount = (int) (totalCount * apiScale);
		int needOtherCount = totalCount - needApiCount;
		
		
		String channelCode = StringUtil.objectToStr(row.get("channelCode"));
		Map<String, Object> channelInfo = BorrowChannelUtil.getChannelByCode(channelCode);
		
		boolean isApi = (NumberUtil.getInt(channelInfo.get("type"), 2) == 3);
		
		if (!isApi && otherCount < needOtherCount) {
			flag = true;
		} else if (isApi && apiCount < needApiCount && otherCount >= needOtherCount) {//需要信息流到上限了再推api的
			flag = true;
		}
		
		if (!flag) {
			if (isApi && apiCount >= needApiCount) {
				RedisUtils.getRedisService().set((PushPlatformUtils.REDIS_MAX_API_COUNT_PREFIX + config.get("pushCode")), true, PushPlatformUtils.lastNowSeconds());
			} else if((!isApi) && otherCount >= needOtherCount) {
				RedisUtils.getRedisService().set((PushPlatformUtils.REDIS_MAX_OTHER_COUNT_PREFIX + config.get("pushCode")), true, PushPlatformUtils.lastNowSeconds());
			}
		}
		
		return flag;
	}
}
