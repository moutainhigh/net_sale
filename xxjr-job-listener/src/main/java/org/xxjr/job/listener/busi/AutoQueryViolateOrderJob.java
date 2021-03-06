package org.xxjr.job.listener.busi;

import java.util.Date;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateUtil;
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
 * 查询门店星标与处理状态不一致暂停分单机制自动化任务
 * @author Administrator
 *
 */
@Lazy
@Component
public class AutoQueryViolateOrderJob implements BaseExecteJob{
	
	/**
	 * 查询门店星标与处理状态不一致暂停分单机制自动化任务
	 * @param processId
	 * @return
	 */
	@Override
	public AppResult executeJob(AppParam param){
		AppResult result = new AppResult();
		Object processId = param.getAttr(JobConstant.KEY_processId);
		try {
			LogerUtil.log("AutoQueryViolateOrderJob >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			violateOrderUpdateAllot(processId);
			LogerUtil.log("AutoQueryViolateOrderJob >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end");
		} catch (Exception e) {
			LogerUtil.error(AutoQueryViolateOrderJob.class, e, "AutoQueryViolateOrderJob >>>>>>>>>>>>>>>>>>error");
			JobUtil.addProcessExecute(processId, "查询门店星标与处理状态不一致暂停分单机制自动化任务" + e.getMessage() );
		}
		return result;
	}
	
	/**
	 * 查询门店星标与处理状态不一致暂停分单机制
	 * @param processId
	 * @return
	 */
	public static AppResult violateOrderUpdateAllot(Object processId){
		AppResult result = new AppResult();
		try {
			int violateOrderAllotStatus = SysParamsUtil.getIntParamByKey("violateOrderAllotStatus", 1);
			if(violateOrderAllotStatus == 0){
				result.setMessage("查询门店星标与处理状态不一致暂停分单自动化任务未开启!");
				result.setSuccess(false);
				return result;
			}
			LogerUtil.log("violateOrderUpdateAllot >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			String violateOrderStartDate = SysParamsUtil.getStringParamByKey("violateOrderStartDate","2018-03-01");
			String  startAllotTime = "";
			if(!StringUtils.isEmpty(violateOrderStartDate) && violateOrderStartDate.length() == 10){
				startAllotTime = violateOrderStartDate;
			}
			AppParam applyParam = new AppParam("borrowStoreApplyService","queryViolateOrder");
			applyParam.addAttr("startAllotTime", startAllotTime);
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult applyResult = RemoteInvoke.getInstance().callNoTx(applyParam);
			int insertCount = 0;
			AppParam insertParam = new AppParam("orderViolateRecordService","insert");
			if(applyResult.isSuccess() && applyResult.getRows().size() > 0){
				for(Map<String,Object> map : applyResult.getRows()){
					int applyId = NumberUtil.getInt(StringUtil.getString(map.get("applyId")));
					int lastStore = NumberUtil.getInt(StringUtil.getString(map.get("lastStore")));
					AppParam recordParam = new AppParam("orderViolateRecordService","query");
					recordParam.addAttr("applyId", applyId);
					recordParam.addAttr("lastStore", lastStore);
					recordParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_busi_in));
					AppResult recordAppResult = RemoteInvoke.getInstance().call(recordParam);
					if(recordAppResult.getRows().size() > 0){
						continue;
					}
					int custLabel = NumberUtil.getInt(StringUtil.getString(map.get("custLabel")),0);
					int orderStatus = NumberUtil.getInt(StringUtil.getString(map.get("orderStatus")),-1);
					insertParam.addAttr("applyId", applyId);
					insertParam.addAttr("lastStore", lastStore);
					insertParam.addAttr("custLabel", custLabel);
					insertParam.addAttr("orderStatus", orderStatus);
					insertParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_busi_in));
					AppResult insertResult = RemoteInvoke.getInstance().call(insertParam);
					int insertSize = NumberUtil.getInt(StringUtil.getString(insertResult.getAttr(DuoduoConstant.DAO_Insert_SIZE)),0);
					if(insertSize == 1){
						insertCount += insertSize;
						AppParam queryParam = new AppParam("custLevelService","query");
						queryParam.addAttr("customerId", lastStore);
						queryParam.setRmiServiceName(AppProperties
								.getProperties(DuoduoConstant.RMI_SERVICE_START
										+ ServiceKey.Key_busi_in));
						AppResult queryAppResult= RemoteInvoke.getInstance().call(queryParam);
						Map<String,Object> custMap = null;
						if(queryAppResult.getRows().size() > 0 && !StringUtils.isEmpty(StringUtil.getString(queryAppResult.getRow(0)))){
							custMap = queryAppResult.getRow(0);
							String oldStopAllotDate = StringUtil.getString(custMap.get("stopAllotDate"));
							String oldIsAllotOrder = StringUtil.getString(custMap.get("isAllotOrder"));
							String stopAllotDate = "";
							if(StringUtils.isEmpty(oldStopAllotDate)){
								stopAllotDate = DateUtil.toStringByParttern(DateUtil.getNextDay(new Date(), 1), DateUtil.DATE_PATTERN_YYYY_MM_DD);
							}else {
								Date oldAllotDate = DateUtil.toDateByString(oldStopAllotDate, DateUtil.DATE_PATTERN_YYYY_MM_DD);
								stopAllotDate = DateUtil.toStringByParttern(DateUtil.getNextDay(oldAllotDate, 1), DateUtil.DATE_PATTERN_YYYY_MM_DD);
							}
							AppParam updateParam = new AppParam("custLevelService","updateAllortTime");
							updateParam.addAttr("stopAllotDate", stopAllotDate);
							updateParam.addAttr("allotDesc", "您有不规范操作，客户星级和处理状态不一致");
							updateParam.addAttr("customerId", lastStore);
							updateParam.setRmiServiceName(AppProperties
									.getProperties(DuoduoConstant.RMI_SERVICE_START
											+ ServiceKey.Key_busi_in));
							if("2".equals(oldIsAllotOrder)){
								RemoteInvoke.getInstance().call(updateParam);
							}else{
								updateParam.addAttr("isAllotOrder", "2"); //暂停分单
								updateParam.addAttr("updateAllotOrderFlag", "1"); //更改暂停分单标识
								RemoteInvoke.getInstance().call(updateParam);
							}
						}
					}
				}
			}
			LogerUtil.log("查询门店星标与处理状态不一致暂停分单插入记录:"+ insertCount);
			JobUtil.addProcessExecute(processId, "查询门店星标与处理状态不一致暂停分单插入记录:"+ insertCount+"条。");
			LogerUtil.log("violateOrderUpdateAllot >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end");
		} catch (Exception e) {
			LogerUtil.error(AutoQueryViolateOrderJob.class, e, "violateOrderUpdateAllot >>>>>>>>>>>>>>>>>>error");
		}
		
		return result;
	}
}
