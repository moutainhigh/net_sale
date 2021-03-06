package org.xxjr.busi.util.push;

import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.SysException;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractInsureUtil implements PushUtil{
	
	@Override
	public int pushData(Map<String, Object> row, Integer pushType) {
		int status = 2;
		try {
			Map<String, Object> config = PushPlatformUtils.getConfigByCode(pushType);
			if ((!PushPlatformUtils.checkChannelOpen(config))) {
				addPushRecord(row, pushType, 3, (config.get("pushName") + "推送未打开"));
				return 3;
			}
			if ((!PushPlatformUtils.checkChannelMaxCount(config))) {
				addPushRecord(row, pushType, 3, (config.get("pushName") + "推送已达上限"));
				return 3;
			}
			if ((!PushPlatformUtils.getInsureCondition(row, config))) {
				addPushRecord(row, pushType, 4, (config.get("pushName") + "筛选条件无法通过"));
				return 4;
			}
			Map<String, Object> response = response(row);//推送数据由子类实现
			
			if (StringUtils.isEmpty(response.get("status"))) {
				throw new SysException("缺少必须返回的参数!");
			}
			
			status = NumberUtil.getInt(response.get("status"), 2);
			
			AppParam updateParam = new AppParam("insurancePushPoolService", "updateStatus");
			updateParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			updateParam.addAttrs(row);
			updateParam.addAttr("pushType", pushType);
			updateParam.addAttr("message", response.get("message"));
			updateParam.addAttr("status", status);
			RemoteInvoke.getInstance().call(updateParam);
			log.info(config.get("pushName") + "数据推送任务" + getClass().getName() + " end");
		} catch (Exception e) {
			log.error(getClass().getName() + " >>>>>>>>>>>>>>>>>>error", e);
		}
		return status;
	}
	
	/**
	 * 数据推出方法
	 * @param row(申请信息)
	 * @return status(返回的状态 1:成功 2:失败,必须返回) message(第三方返回的信息)
	 */
	public abstract Map<String, Object> response(Map<String, Object> row) throws Exception;
}
