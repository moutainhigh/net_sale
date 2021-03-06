package org.xxjr.job.listener.busi;

import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.job.core.BaseExecteJob;
import org.llw.job.util.JobConstant;
import org.llw.job.util.JobUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

/**
 * 每五分钟统计各门店在线人员自动化任务
 * @author Administrator
 *
 */
@Lazy
@Component
public class AutoCountOnlineJob implements BaseExecteJob{
	
	/**
	 * 每五分钟统计各门店在线人数
	 * @param processId
	 * @return
	 */
	@Override
	public AppResult executeJob(AppParam param){
		AppResult result = new AppResult();
		Object processId = param.getAttr(JobConstant.KEY_processId);
		try {
			LogerUtil.log("AutoCountOnlineJob >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			countOnlinePerson(processId);//统计在线人数
			LogerUtil.log("AutoCountOnlineJob >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end");
		} catch (Exception e) {
			LogerUtil.error(AutoCountOnlineJob.class, e, "AutoCountOnlineJob >>>>>>>>>>>>>>>>>>error");
			JobUtil.addProcessExecute(processId, "每五分钟统计各门店在线人员自动化任务" + e.getMessage() );
		}
		return result;
	}
	
	/**
	 * 统计各门店在线人数
	 * @param processId
	 * @return
	 */
	public static AppResult countOnlinePerson(Object processId){
		AppResult result = new AppResult();
		try {
			int countOnlineStatus = SysParamsUtil.getIntParamByKey("countOnlineStatus", 1);
			if(countOnlineStatus == 0){
				result.setMessage("统计各门店在线人数自动化任务未开启!");
				result.setSuccess(false);
				return result;
			}
			LogerUtil.log("countOnlinePerson >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			AppParam applyParam = new AppParam("sumUtilExtService","queryOrgOnline");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult applyResult = RemoteInvoke.getInstance().callNoTx(applyParam);
			int insertCount = 0;
			int updateCount = 0;
			AppParam updateParam = new AppParam("sumOrgBaseService","updateOnlineCount");
			if(applyResult.isSuccess() && applyResult.getRows().size() > 0){
				for(Map<String,Object> map : applyResult.getRows()){
					int orgId = NumberUtil.getInt(StringUtil.getString(map.get("orgId")));
					String recordDate = StringUtil.getString(map.get("recordDate"));
					String orgName = StringUtil.getString(map.get("orgName"));
					String cityName = StringUtil.getString(map.get("cityName"));
					int isNet = NumberUtil.getInt(StringUtil.getString(map.get("isNet")));
					int onlineCount = NumberUtil.getInt(StringUtil.getString(map.get("onlineCount")),0);
					updateParam.addAttr("orgId", orgId);
					updateParam.addAttr("recordDate", recordDate);
					updateParam.addAttr("orgName", orgName);
					updateParam.addAttr("cityName", cityName);
					updateParam.addAttr("isNet", isNet);
					updateParam.addAttr("onlineCount", onlineCount);
					updateParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_sum));
					AppResult updateResult = RemoteInvoke.getInstance().call(updateParam);
					if(StringUtils.isEmpty(updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE))){
						insertCount += NumberUtil.getInt(StringUtil.getString(updateResult.getAttr(DuoduoConstant.DAO_Insert_SIZE)));
					}else{
						updateCount += NumberUtil.getInt(StringUtil.getString(updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE)));
					}
				}
			}
			LogerUtil.log("统计各门店在线人数:insert count="+ insertCount + ",update count="+ updateCount);
			JobUtil.addProcessExecute(processId, "统计各门店在线人数:update count="+ updateCount + ",insert count="+ insertCount);
			LogerUtil.log("countOnlinePerson >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end");
		} catch (Exception e) {
			LogerUtil.error(AutoCountOnlineJob.class, e, "countOnlinePerson >>>>>>>>>>>>>>>>>>error");
		}
		
		return result;
	}
}
