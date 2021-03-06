package org.xxjr.job.listener.busi;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
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
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

/**
 * 订单回收自动化任务
 * @author Administrator
 *
 */
@Lazy
@Component
public class AutoOrderRecyclingJob implements BaseExecteJob{
	
	/**
	 * 订单回收自动化任务
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
			newOrderRecycling(processId,cityList);//新订单回收
			againOrderRecling(processId,cityList);//再分配订单回收
			LogerUtil.log("AutoOrderRecyclingJob >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end");
		} catch (Exception e) {
			LogerUtil.error(AutoOrderRecyclingJob.class, e, "OrderRecyclingAutoJob >>>>>>>>>>>>>>>>>>error");
			JobUtil.addProcessExecute(processId, "订单回收自动化任务" + e.getMessage() );
		}
		return result;
	}
	
	/**
	 * 新订单回收自动化任务
	 * @param processId
	 * @return
	 */
	public static AppResult newOrderRecycling(Object processId,List<Map<String, Object>> cityList){
		AppResult result = new AppResult();
		try {
			int orderRecyclingStatus = SysParamsUtil.getIntParamByKey("orderRecyclingStatus", 1);
			if(orderRecyclingStatus == 0){
				result.setMessage("订单回收自动化任务未开启!");
				result.setSuccess(false);
				return result;
			}
			LogerUtil.log("newOrderRecycling >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
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
			//没处理插入分配池的订单数
			int notDealCount = 0;
			if(cityList != null && cityList.size() > 0){
				for(Map<String, Object> resultMap : cityList ){
					//根据规则查询没处理的订单
					String  allotDate = OrderRecyclingUtil.getNotDealAllotDate2(notHandl,resultMap);
					AppParam applyParam = new AppParam();
					applyParam.setService("storeHandleExtService");
					applyParam.setMethod("queryNotDealOrder");
					applyParam.addAttr("orderStatus", "-1");//-1是订单状态为未处理
					applyParam.addAttr("status", "2");//2 门店锁定中
					applyParam.addAttr("orderType", 1);//1是新订单
					applyParam.addAttr("allotDate", allotDate);
					applyParam.addAttr("orgId", resultMap.get("orgId"));//门店ID
					applyParam.addAttr("allotStartDate", DateUtil.getSimpleFmt(DateUtil.getNextDay(new Date(), -30)));
					applyParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_busi_in));
					AppResult applyResult = RemoteInvoke.getInstance().callNoTx(applyParam);
					if(applyResult.isSuccess() && applyResult.getRows().size() > 0){
						AppParam allotParam = new AppParam();
						allotParam.setService("netStorePoolService");
						allotParam.setMethod("insertAllot");
						allotParam.addAttr("orderList", applyResult.getRows());
						allotParam.addAttr("orderType", 1);
						allotParam.addAttr("desc", "新订单超时没处理被系统自动回收");
						allotParam.setRmiServiceName(AppProperties
								.getProperties(DuoduoConstant.RMI_SERVICE_START
										+ ServiceKey.Key_busi_in));
						AppResult allotResult = RemoteInvoke.getInstance().call(allotParam);
						if(allotResult.isSuccess()){
							notDealCount = Integer.valueOf(StringUtil.getString(allotResult.getAttr("allotCount")));
						}
						
					}
				}
			}
			
			//根据规则查询没上门的订单
			String  bookDate = OrderRecyclingUtil.getNotVisitAllotDate(notVisitor);
			AppParam bookParam = new AppParam();
			bookParam.setService("storeHandleExtService");
			bookParam.setMethod("queryNotVisitOrder");
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
			int notVisitCount = 0;
			if(bookResult.isSuccess() && bookResult.getRows().size() > 0){
				AppParam allotParam = new AppParam();
				allotParam.setService("netStorePoolService");
				allotParam.setMethod("insertAllot");
				allotParam.addAttr("orderType", 2);
				allotParam.addAttr("desc", "新订单超时没上门被系统自动回收");
				allotParam.addAttr("orderList", bookResult.getRows());
				allotParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				AppResult allotResult = RemoteInvoke.getInstance().call(allotParam);
				if(allotResult.isSuccess()){
					notVisitCount = Integer.valueOf(StringUtil.getString(allotResult.getAttr("allotCount")));
				}
			}
			//根据规则查询没签单的订单
			String  signDate = DateUtil.toStringByParttern(DateUtil.getNextDay(new Date(), -notSign), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
			AppParam signParam = new AppParam();
			signParam.setService("storeHandleExtService");
			signParam.setMethod("queryNotSignOrder");
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
			int notSignCount = 0;
			if(signResult.isSuccess() && signResult.getRows().size() > 0){
				AppParam allotParam = new AppParam();
				allotParam.setService("netStorePoolService");
				allotParam.setMethod("insertAllot");
				allotParam.addAttr("orderType", 2);
				allotParam.addAttr("notSign", 1);
				allotParam.addAttr("desc", "新订单超时没签单被系统自动回收");
				allotParam.addAttr("orderList", signResult.getRows());
				allotParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				AppResult allotResult = RemoteInvoke.getInstance().call(allotParam);
				if(allotResult.isSuccess()){
					notSignCount = Integer.valueOf(StringUtil.getString(allotResult.getAttr("allotCount")));
				}
			}
			
			LogerUtil.log("新订单回收:没处理订单笔数："+ notDealCount + ",没上门订单笔数：" + notVisitCount +",没签单订单笔数：" + notSignCount);
			JobUtil.addProcessExecute(processId, "新订单回收 newOrderRecycling msg:没处理订单笔数："+ notDealCount + ",没上门订单笔数：" + notVisitCount +",没签单订单笔数：" + notSignCount);
			LogerUtil.log("newOrderRecycling >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end");
		} catch (Exception e) {
			LogerUtil.error(AutoOrderRecyclingJob.class, e, "newOrderRecycling >>>>>>>>>>>>>>>>>>error");
		}
		
		return result;
	}
	
	/**
	 * 再分配订单回收自动化任务
	 * @param processId
	 * @return
	 */
	public static AppResult againOrderRecling(Object processId,List<Map<String, Object>> cityList){
		AppResult result = new AppResult();
		try {
			int againOrderRecycling = SysParamsUtil.getIntParamByKey("againOrderRecycling", 1);
			if(againOrderRecycling == 0){
				result.setMessage("再分配订单回收自动化任务未开启!");
				result.setSuccess(false);
				return result;
			}
			LogerUtil.log("againOrderRecling >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
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
			//没处理插入分配池的订单数
			int notDealCount = 0;
			if(cityList != null && cityList.size() > 0){
				for(Map<String, Object> resultMap : cityList ){
					//根据规则查询没处理的订单
					String  allotDate = OrderRecyclingUtil.getNotDealAllotDate2(notHandl,resultMap);
					AppParam applyParam = new AppParam();
					applyParam.setService("storeHandleExtService");
					applyParam.setMethod("queryNotDealOrder");
					applyParam.addAttr("orderStatus", "-1");//-1是订单状态为未处理
					applyParam.addAttr("status", "2");//2 门店锁定中
					applyParam.addAttr("orderType", 2);//2是再分配单
					applyParam.addAttr("allotDate", allotDate);
					applyParam.addAttr("orgId", resultMap.get("orgId"));//门店ID
					applyParam.addAttr("allotStartDate", DateUtil.getSimpleFmt(DateUtil.getNextDay(new Date(), -30)));
					applyParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_busi_in));
					AppResult applyResult = RemoteInvoke.getInstance().callNoTx(applyParam);
					
					if(applyResult.isSuccess() && applyResult.getRows().size() > 0){
						AppParam allotParam = new AppParam();
						allotParam.setService("netStorePoolService");
						allotParam.setMethod("insertAllot");
						allotParam.addAttr("desc", "再分配订单超时没处理被系统自动回收");
						allotParam.addAttr("orderList", applyResult.getRows());
						allotParam.setRmiServiceName(AppProperties
								.getProperties(DuoduoConstant.RMI_SERVICE_START
										+ ServiceKey.Key_busi_in));
						AppResult allotResult = RemoteInvoke.getInstance().call(allotParam);
						if(allotResult.isSuccess()){
							notDealCount = Integer.valueOf(StringUtil.getString(allotResult.getAttr("allotCount")));
						}
						
					}
				}
			}
			
			
			//根据规则查询没上门的订单
			String  bookDate =  OrderRecyclingUtil.getNotVisitAllotDate(notVisitor);
			AppParam bookParam = new AppParam();
			bookParam.setService("storeHandleExtService");
			bookParam.setMethod("queryNotVisitOrder");
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
			int notVisitCount = 0;
			if(bookResult.isSuccess() && bookResult.getRows().size() > 0){
				AppParam allotParam = new AppParam();
				allotParam.setService("netStorePoolService");
				allotParam.setMethod("insertAllot");
				allotParam.addAttr("desc", "再分配订单超时没上门被系统自动回收");
				allotParam.addAttr("orderList", bookResult.getRows());
				allotParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				AppResult allotResult = RemoteInvoke.getInstance().call(allotParam);
				if(allotResult.isSuccess()){
					notVisitCount = Integer.valueOf(StringUtil.getString(allotResult.getAttr("allotCount")));
				}
			}
			//根据规则查询没签单的订单
			String  signDate = DateUtil.toStringByParttern(DateUtil.getNextDay(new Date(), -notSign), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
			AppParam signParam = new AppParam();
			signParam.setService("storeHandleExtService");
			signParam.setMethod("queryNotSignOrder");
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
			int notSignCount = 0;
			if(signResult.isSuccess() && signResult.getRows().size() > 0){
				AppParam allotParam = new AppParam();
				allotParam.setService("netStorePoolService");
				allotParam.setMethod("insertAllot");
				allotParam.addAttr("notSign", 1);
				allotParam.addAttr("desc", "再分配订单超时没签单被系统自动回收");
				allotParam.addAttr("orderList", signResult.getRows());
				allotParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				AppResult allotResult = RemoteInvoke.getInstance().call(allotParam);
				if(allotResult.isSuccess()){
					notSignCount = Integer.valueOf(StringUtil.getString(allotResult.getAttr("allotCount")));
				}
			}
			
			LogerUtil.log("再分配订单回收:没处理订单笔数："+ notDealCount + ",没上门订单笔数：" + notVisitCount +",没签单订单笔数：" + notSignCount);
			JobUtil.addProcessExecute(processId, "再分配订单回收 againOrderRecling msg:没处理订单笔数："+ notDealCount + ",没上门订单笔数：" + notVisitCount +",没签单订单笔数：" + notSignCount);
			LogerUtil.log("againOrderRecling >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end");
		} catch (Exception e) {
			LogerUtil.error(AutoOrderRecyclingJob.class, e, "againOrderRecling >>>>>>>>>>>>>>>>>>error");
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
}
