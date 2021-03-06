package org.xxjr.job.listener.busi;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ddq.active.mq.message.CustMessageSend;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.job.core.BaseExecteJob;
import org.llw.job.util.JobConstant;
import org.llw.job.util.JobUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.StoreAgainCfgUtils;
import org.xxjr.busi.util.store.OrderRecyclingUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

/**
 * 查询即将回收订单自动化任务
 * @author Administrator
 *
 */
@Lazy
@Component
public class AutoQueryWillRecyclingJob implements BaseExecteJob{
	
	/**
	 * 查询即将回收订单自动化任务
	 * @param processId
	 * @return
	 */
	@Override
	public AppResult executeJob(AppParam param){
		AppResult result = new AppResult();
		Object processId = param.getAttr(JobConstant.KEY_processId);
		try {
			LogerUtil.log("AutoOrderRecyclingJob >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			List<Map<String, Object>> cityList = getCityWorkTimeList();//配置过工作时间的城市列表
			newOrderWillRecycling(processId,cityList);//新订单即将回收
			againOrderWillRecling(processId,cityList);//再分配订单即将回收
			LogerUtil.log("AutoQueryWillRecyclingJob >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end");
			
			// 专属单未处理通知
			notDealNotify(processId);
		} catch (Exception e) {
			LogerUtil.error(AutoQueryWillRecyclingJob.class, e, "AutoQueryWillRecyclingJob >>>>>>>>>>>>>>>>>>error");
			JobUtil.addProcessExecute(processId, "即将回收订单自动化任务" + e.getMessage() );
		}
		return result;
	}
	
	/**
	 * 新订单即将回收自动化任务
	 * @param processId
	 * @return
	 */
	public static AppResult newOrderWillRecycling(Object processId,List<Map<String, Object>> cityList){
		AppResult result = new AppResult();
		try {
			int orderWillRecyclingStatus = SysParamsUtil.getIntParamByKey("orderWillRecyclingStatus", 1);
			if(orderWillRecyclingStatus == 0){
				result.setMessage("新订单即将回收自动化任务未开启!");
				result.setSuccess(false);
				LogerUtil.log("新订单即将回收自动化任务未开启!");
				return result;
			}
			LogerUtil.log("newOrderWillRecycling >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			int notHandl = 0 ; //多久没处理再分配
			int notVisitor = 0; //多久没上门再分配
			int notSign = 0; // 多久没签约再分配
			Map<String,Object> queryMap = StoreAgainCfgUtils.getNewAllotCfg();
			if(!StringUtils.isEmpty(queryMap)){
				notHandl = Integer.valueOf(StringUtil.getString(queryMap.get("notHandl")));
				notVisitor = Integer.valueOf(StringUtil.getString(queryMap.get("notVisitor")));
				notSign = Integer.valueOf(StringUtil.getString(queryMap.get("notSign")));
			}else{
				result.setSuccess(false);
				result.setMessage("缺少相关等级配置参数");
				return result;
			}
			//没处理的订单发送微信消息通知
			CustMessageSend messageSend = (CustMessageSend) SpringAppContext.getBean(CustMessageSend.class);
			int notDealCount = 0;
			if(notHandl >= 90){
				if(cityList != null && cityList.size() > 0){
					long nowTime = new Date().getTime();
					for(Map<String, Object> resultMap : cityList ){
						String amEndWorkTime = StringUtil.getString(resultMap.get("amEndWordTime"));//中午下班时间
						String amEndWorkTimeStr = StringUtils.isEmpty(amEndWorkTime) ? "00:00" : amEndWorkTime;
						long amEndTime = getLongTimes(amEndWorkTimeStr);
						String pmBeginWorkTime = StringUtil.getString(resultMap.get("pmBeginWorkTime"));//下午上班时间
						long pmStartTime = getLongTimes(StringUtils.isEmpty(pmBeginWorkTime) ? "00:00" : pmBeginWorkTime);
						String evesBeginWorkTime = StringUtil.getString(resultMap.get("evesBeginWorkTime"));
						String evesEndWorkTime = StringUtil.getString(resultMap.get("evesEndWordTime"));//晚上下班时间
						String evesEndWorkTimeStr = StringUtils.isEmpty(evesEndWorkTime) ? "00:00":evesEndWorkTime;
						long evesEndTime = getLongTimes(evesEndWorkTimeStr);
						if((nowTime > amEndTime && nowTime < pmStartTime) || StringUtils.isEmpty(evesBeginWorkTime) || StringUtils.isEmpty(evesEndWorkTime) || nowTime > evesEndTime){
							continue;
						}
						//根据规则查询没处理的订单
						String  allotDate = OrderRecyclingUtil.getNotDealAllotDate2(notHandl,resultMap);
						Date tempDate = DateUtil.toDateByString(allotDate, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
						allotDate = OrderRecyclingUtil.getMessageNitoceAlloDate(resultMap,tempDate);
						AppParam applyParam = new AppParam();
						applyParam.setService("storeHandleExtService");
						applyParam.setMethod("queryNotDealOrderGroup");
						applyParam.addAttr("orderStatus", "-1");//-1是订单状态为未处理
						applyParam.addAttr("status", "2");//2 门店锁定中
						applyParam.addAttr("orderType", 1);//1是新订单
						applyParam.addAttr("allotDate", allotDate);
						applyParam.addAttr("orgId", resultMap.get("orgId"));
						applyParam.addAttr("allotStartDate", DateUtil.getSimpleFmt(DateUtil.getNextDay(new Date(), -30)));
						applyParam.setRmiServiceName(AppProperties
								.getProperties(DuoduoConstant.RMI_SERVICE_START
										+ ServiceKey.Key_busi_in));
						AppResult applyResult = RemoteInvoke.getInstance().callNoTx(applyParam);
						notDealCount = applyResult.getRows().size();
						//没处理的订单发送微信消息通知
//						CustMessageSend messageSend = (CustMessageSend) SpringAppContext.getBean(CustMessageSend.class);
						if(applyResult.isSuccess() && applyResult.getRows().size() > 0){
							for(Map<String,Object> storeMap : applyResult.getRows()){
								String lastStore = StringUtil.getString(storeMap.get("lastStore"));
								String realName = StringUtil.getString(storeMap.get("realName"));
								String orderCount = StringUtil.getString(storeMap.get("orderCount"));
								String allApplyName = StringUtil.getString(storeMap.get("allApplyName"));
								Map<String, Object> paramsMap = new HashMap<String, Object>();
								paramsMap.put("orderCount", orderCount);
								paramsMap.put("realName", realName);
								paramsMap.put("descType", "新订单超时没处理");
								paramsMap.put("allApplyName", allApplyName);
								paramsMap.put("noticeTime", DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS));
								// 微信消息
								messageSend.sendCustMessage(lastStore,"willRecyclingOrder", paramsMap);
							}
						}
					}
				}
			}
			
			
			//根据规则查询没上门的订单
			String  bookDate = OrderRecyclingUtil.getNotVisitAllotDate(notVisitor);
			Date bookTempDate = DateUtil.toDateByString(bookDate, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
			bookDate = DateUtil.toStringByParttern(DateUtil.getNextHour(bookTempDate, 1), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
			AppParam bookParam = new AppParam();
			bookParam.setService("storeHandleExtService");
			bookParam.setMethod("queryNotVisitOrderGroup");
			bookParam.addAttr("status", "2");//2 门店锁定中
			bookParam.addAttr("orderStatusIn", "-1,0,1"); // 未上门
			bookParam.addAttr("orderType", 1);
			bookParam.addAttr("allotDate", bookDate);
			bookParam.addAttr("allotStartDate", DateUtil.getSimpleFmt(DateUtil.getNextDay(new Date(), -30)));
			bookParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult bookResult = RemoteInvoke.getInstance().callNoTx(bookParam);
			
			//没上门插入分配池的订单数
			int notVisitCount = bookResult.getRows().size();
			if(bookResult.isSuccess() && bookResult.getRows().size() > 0){
				for(Map<String,Object> storeMap : bookResult.getRows()){
					String lastStore = StringUtil.getString(storeMap.get("lastStore"));
					String realName = StringUtil.getString(storeMap.get("realName"));
					String orderCount = StringUtil.getString(storeMap.get("orderCount"));
					String allApplyName = StringUtil.getString(storeMap.get("allApplyName"));
					Map<String, Object> paramsMap = new HashMap<String, Object>();
					paramsMap.put("orderCount", orderCount);
					paramsMap.put("realName", realName);
					paramsMap.put("descType", "新订单超时没上门");
					paramsMap.put("allApplyName", allApplyName);
					paramsMap.put("noticeTime", DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS));
					// 微信消息
					messageSend.sendCustMessage(lastStore,"willRecyclingOrder", paramsMap);
				}
			}
			//根据规则查询没签单的订单
			String  signDate = DateUtil.toStringByParttern(DateUtil.getNextDay(new Date(), -notSign), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
			Date signTempDate = DateUtil.toDateByString(signDate, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
			signDate = DateUtil.toStringByParttern(DateUtil.getNextHour(signTempDate, 1), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
			AppParam signParam = new AppParam();
			signParam.setService("storeHandleExtService");
			signParam.setMethod("queryNotSignOrderGroup");
			signParam.addAttr("orderType",1);
			signParam.addAttr("status", "2");//2 门店锁定中
			signParam.addAttr("orderStatusIn", "-1,0,1,2"); // 处理中
			signParam.addAttr("allotDate", signDate);
			signParam.addAttr("allotStartDate", DateUtil.getSimpleFmt(DateUtil.getNextDay(new Date(), -30)));
			signParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult signResult = RemoteInvoke.getInstance().callNoTx(signParam);
			//没签单插入分配池的订单数
			int notSignCount = signResult.getRows().size();
			if(signResult.isSuccess() && signResult.getRows().size() > 0){
				for(Map<String,Object> storeMap : signResult.getRows()){
					String lastStore = StringUtil.getString(storeMap.get("lastStore"));
					String realName = StringUtil.getString(storeMap.get("realName"));
					String orderCount = StringUtil.getString(storeMap.get("orderCount"));
					String allApplyName = StringUtil.getString(storeMap.get("allApplyName"));
					Map<String, Object> paramsMap = new HashMap<String, Object>();
					paramsMap.put("orderCount", orderCount);
					paramsMap.put("realName", realName);
					paramsMap.put("descType", "新订单超时没签单");
					paramsMap.put("allApplyName", allApplyName);
					paramsMap.put("noticeTime", DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS));
					// 微信消息
					messageSend.sendCustMessage(lastStore,"willRecyclingOrder", paramsMap);
				}
			}
			
			LogerUtil.log("新订单即将回收:发送没处理订单的消息："+ notDealCount + "条,发送没上门订单的消息：" + notVisitCount +"条,发送没签单订单的消息：" + notSignCount+"条。");
			JobUtil.addProcessExecute(processId, "新订单即将回收 newOrderWillRecycling msg:发送没处理订单的消息："+ notDealCount + ",发送没上门订单的消息：" + notVisitCount +",发送没签单订单的消息：" + notSignCount);
			LogerUtil.log("newOrderWillRecycling >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end");
		} catch (Exception e) {
			LogerUtil.error(AutoQueryWillRecyclingJob.class, e, "newOrderWillRecycling >>>>>>>>>>>>>>>>>>error");
		}
		
		return result;
	}
	
	/**
	 * 再分配订单即将回收自动化任务
	 * @param processId
	 * @return
	 */
	public static AppResult againOrderWillRecling(Object processId,List<Map<String, Object>> cityList){
		AppResult result = new AppResult();
		try {
			int againWillOrderRecycling = SysParamsUtil.getIntParamByKey("againWillOrderRecycling", 1);
			if(againWillOrderRecycling == 0){
				result.setMessage("再分配订单即将回收自动化任务未开启!");
				result.setSuccess(false);
				LogerUtil.log("再分配订单即将回收自动化任务未开启!");
				return result;
			}
			LogerUtil.log("againOrderWillRecling >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			int notHandl = 0 ; //多久没处理再分配
			int notVisitor = 0; //多久没上门再分配
			int notSign = 0; // 多久没签约再分配
			Map<String,Object> queryMap = StoreAgainCfgUtils.getAgainAllotCfg();
			if(!StringUtils.isEmpty(queryMap)){
				notHandl = Integer.valueOf(StringUtil.getString(queryMap.get("notHandl")));
				notVisitor = Integer.valueOf(StringUtil.getString(queryMap.get("notVisitor")));
				notSign = Integer.valueOf(StringUtil.getString(queryMap.get("notSign")));
			}else{
				result.setSuccess(false);
				result.setMessage("缺少相关等级配置参数");
				return result;
			}
			CustMessageSend messageSend = (CustMessageSend) SpringAppContext.getBean(CustMessageSend.class);
			int notDealCount = 0;
			if(notHandl >= 90){
				if(cityList != null && cityList.size() > 0){
					long nowTime = new Date().getTime();
					for(Map<String, Object> resultMap : cityList ){
						String amEndWorkTime = StringUtil.getString(resultMap.get("amEndWordTime"));//中午下班时间
						String amEndWorkTimeStr = StringUtils.isEmpty(amEndWorkTime) ? "00:00" : amEndWorkTime;
						long amEndTime = getLongTimes(amEndWorkTimeStr);
						String pmBeginWorkTime = StringUtil.getString(resultMap.get("pmBeginWorkTime"));//下午上班时间
						long pmStartTime = getLongTimes(StringUtils.isEmpty(pmBeginWorkTime) ? "00:00" : pmBeginWorkTime);
						String evesBeginWorkTime = StringUtil.getString(resultMap.get("evesBeginWorkTime"));
						String evesEndWorkTime = StringUtil.getString(resultMap.get("evesEndWordTime"));//晚上下班时间
						String evesEndWorkTimeStr = StringUtils.isEmpty(evesEndWorkTime) ? "00:00":evesEndWorkTime;
						long evesEndTime = getLongTimes(evesEndWorkTimeStr);
						if((nowTime > amEndTime && nowTime < pmStartTime) || StringUtils.isEmpty(evesBeginWorkTime) || StringUtils.isEmpty(evesEndWorkTime) || nowTime > evesEndTime){
							continue;
						}
						//根据规则查询没处理的订单
						String  allotDate = OrderRecyclingUtil.getNotDealAllotDate2(notHandl,resultMap);
						Date tempDate = DateUtil.toDateByString(allotDate, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
						allotDate = OrderRecyclingUtil.getMessageNitoceAlloDate(resultMap,tempDate);
						AppParam applyParam = new AppParam();
						applyParam.setService("storeHandleExtService");
						applyParam.setMethod("queryNotDealOrderGroup");
						applyParam.addAttr("orderStatus", "-1");//-1是订单状态为未处理
						applyParam.addAttr("status", "2");//2 门店锁定中
						applyParam.addAttr("orderType", 2);//2是再分配单
						applyParam.addAttr("allotDate", allotDate);
						applyParam.addAttr("orgId", resultMap.get("orgId"));
						applyParam.addAttr("allotStartDate", DateUtil.getSimpleFmt(DateUtil.getNextDay(new Date(), -30)));
						applyParam.setRmiServiceName(AppProperties
								.getProperties(DuoduoConstant.RMI_SERVICE_START
										+ ServiceKey.Key_busi_in));
						AppResult applyResult = RemoteInvoke.getInstance().callNoTx(applyParam);
//						CustMessageSend messageSend = (CustMessageSend) SpringAppContext.getBean(CustMessageSend.class);
						//没处理插入分配池的订单数
						notDealCount = applyResult.getRows().size();
						if(applyResult.isSuccess() && applyResult.getRows().size() > 0){
							for(Map<String,Object> storeMap : applyResult.getRows()){
								String lastStore = StringUtil.getString(storeMap.get("lastStore"));
								String realName = StringUtil.getString(storeMap.get("realName"));
								String orderCount = StringUtil.getString(storeMap.get("orderCount"));
								String allApplyName = StringUtil.getString(storeMap.get("allApplyName"));
								Map<String, Object> paramsMap = new HashMap<String, Object>();
								paramsMap.put("orderCount", orderCount);
								paramsMap.put("realName", realName);
								paramsMap.put("descType", "再分配订单超时没处理");
								paramsMap.put("allApplyName", allApplyName);
								paramsMap.put("noticeTime", DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS));
								// 微信消息
								messageSend.sendCustMessage(lastStore,"willRecyclingOrder", paramsMap);
							}
						}
					}
				}
			}
			//根据规则查询没上门的订单
			String  bookDate =  OrderRecyclingUtil.getNotVisitAllotDate(notVisitor);
			Date bookTempDate = DateUtil.toDateByString(bookDate, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
			bookDate = DateUtil.toStringByParttern(DateUtil.getNextHour(bookTempDate, 1), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
			AppParam bookParam = new AppParam();
			bookParam.setService("storeHandleExtService");
			bookParam.setMethod("queryNotVisitOrderGroup");
			bookParam.addAttr("orderType", 2);
			bookParam.addAttr("status", "2");//2 门店锁定中
			bookParam.addAttr("orderStatusIn", "-1,0,1"); // 未上门
			bookParam.addAttr("allotDate", bookDate);
			bookParam.addAttr("allotStartDate", DateUtil.getSimpleFmt(DateUtil.getNextDay(new Date(), -30)));
			bookParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult bookResult = RemoteInvoke.getInstance().callNoTx(bookParam);
			//没上门插入分配池的订单数
			int notVisitCount = bookResult.getRows().size();
			if(bookResult.isSuccess() && bookResult.getRows().size() > 0){
				for(Map<String,Object> storeMap : bookResult.getRows()){
					String lastStore = StringUtil.getString(storeMap.get("lastStore"));
					String realName = StringUtil.getString(storeMap.get("realName"));
					String orderCount = StringUtil.getString(storeMap.get("orderCount"));
					String allApplyName = StringUtil.getString(storeMap.get("allApplyName"));
					Map<String, Object> paramsMap = new HashMap<String, Object>();
					paramsMap.put("orderCount", orderCount);
					paramsMap.put("realName", realName);
					paramsMap.put("descType", "再分配订单超时没上门");
					paramsMap.put("allApplyName", allApplyName);
					paramsMap.put("noticeTime", DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS));
					// 微信消息
					messageSend.sendCustMessage(lastStore,"willRecyclingOrder", paramsMap);
				}
			}
			//根据规则查询没签单的订单
			String  signDate = DateUtil.toStringByParttern(DateUtil.getNextDay(new Date(), -notSign), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
			Date signTempDate = DateUtil.toDateByString(signDate, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
			signDate = DateUtil.toStringByParttern(DateUtil.getNextHour(signTempDate, 1), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
			AppParam signParam = new AppParam();
			signParam.setService("storeHandleExtService");
			signParam.setMethod("queryNotSignOrderGroup");
			signParam.addAttr("orderType",2);
			signParam.addAttr("status", "2");//2 门店锁定中
			signParam.addAttr("orderStatusIn", "-1,0,1,2"); // 处理中
			signParam.addAttr("allotDate", signDate);
			signParam.addAttr("allotStartDate", DateUtil.getSimpleFmt(DateUtil.getNextDay(new Date(), -30)));
			signParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult signResult = RemoteInvoke.getInstance().callNoTx(signParam);
			//没签单插入分配池的订单数
			int notSignCount = signResult.getRows().size();
			if(signResult.isSuccess() && signResult.getRows().size() > 0){
				for(Map<String,Object> storeMap : signResult.getRows()){
					String lastStore = StringUtil.getString(storeMap.get("lastStore"));
					String realName = StringUtil.getString(storeMap.get("realName"));
					String orderCount = StringUtil.getString(storeMap.get("orderCount"));
					String allApplyName = StringUtil.getString(storeMap.get("allApplyName"));
					Map<String, Object> paramsMap = new HashMap<String, Object>();
					paramsMap.put("orderCount", orderCount);
					paramsMap.put("realName", realName);
					paramsMap.put("descType", "再分配订单超时没签单");
					paramsMap.put("allApplyName", allApplyName);
					paramsMap.put("noticeTime", DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS));
					// 微信消息
					messageSend.sendCustMessage(lastStore,"willRecyclingOrder", paramsMap);
				}
			}
			
			LogerUtil.log("再分配订单即将回收:发送没处理订单的消息："+ notDealCount + "条,发送没上门订单的消息：" + notVisitCount +"条,发送没签单订单的消息：" + notSignCount+"条。");
			JobUtil.addProcessExecute(processId, "再分配订单即将回收 againOrderWillRecling msg:发送没处理订单的消息："+ notDealCount + ",发送没上门订单的消息：" + notVisitCount +",发送没签单订单的消息：" + notSignCount);
			LogerUtil.log("againOrderWillRecling >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end");
		} catch (Exception e) {
			LogerUtil.error(AutoQueryWillRecyclingJob.class, e, "againOrderWillRecling >>>>>>>>>>>>>>>>>>error");
		}
		
		return result;
	}
	
	
	/**
	 * 获取所有配置过的城市工作时间
	 * @return
	 */
	public List<Map<String, Object>> getCityWorkTimeList(){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			params.setService("worktimeCfgService");
			params.setMethod("query");
			params.setOrderBy("createTime");
			params.setOrderValue("desc");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
		}catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "getCityWorkTimeList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result.getRows();
	}
	
	
	/**
	 * 时间转换成long类型
	 * @param workTime
	 * @return
	 */
	public static long getLongTimes(String workTime){
		String[] timeStr = workTime.split(":");
		long time = 0L;
		if(timeStr.length > 0){
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(timeStr[0]));
			cal.set(Calendar.MINUTE, Integer.valueOf(timeStr[1]));
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND,0);                                                                                                                                                                                       
			time = cal.getTime().getTime(); 
		}
		return time;
	}
	
	/***
	 * 专属单未处理通知
	 * @param processId
	 */
	private static void notDealNotify(Object processId){
		int totalSucSize = 0;
		AppParam updateParam = new AppParam("storeExcluesiveNotifyService","notDealNotify");
		updateParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult result = RemoteInvoke.getInstance().call(updateParam);
		if(result.isSuccess()){
			totalSucSize = NumberUtil.getInt(result.getAttr("sucSize"), 0);
		}
		LogerUtil.log("专属单7天未处理通知总笔数为:" + totalSucSize);
		JobUtil.addProcessExecute(processId, "专属单7天未处理通知总笔数为:"+ totalSucSize);
	}
}
