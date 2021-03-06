package org.xxjr.job.listener.busi.sum.util;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.job.util.JobUtil;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.StoreConstant;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.OrgUtils;
import org.xxjr.sys.util.ServiceKey;


/**
 * 基本数据统计工具类
 * @author hwf
 *
 */
public class BaseSumUtil {
	/**
	 * 渠道基本数据统计
	 * @param processId
	 * @param today
	 */
	public static void channelBase(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("BaseSumUtil channelBase>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("sumUtilExtService","channelBase");
			queryParam.addAttr("today", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = 0;
			if(dataList.size()>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumChannelBaseService","save");
				insertParam.addAttr("today", today);
				insertParam.addAttr("list", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size =NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("BaseSumUtil channelBase >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil channelBase error");
			JobUtil.addProcessExecute(processId, "统计渠道基本数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 客服基本数据统计
	 * @param processId
	 * @param today
	 */
	public static void kfBase(Object processId, String today, String isSummaryKf) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("BaseSumUtil kfBase>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("sumUtilExtService","kfBase");
			queryParam.addAttr("isSummaryKf", isSummaryKf);
			queryParam.addAttr("today", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = 0;
			if(dataList.size()>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumKfBaseService","save");
				insertParam.addAttr("today", today);
				insertParam.addAttr("list", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size =NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("BaseSumUtil channelBase >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil kfBase error");
			JobUtil.addProcessExecute(processId, "统计客服基本数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 门店经理基本数据统计
	 * @param processId
	 * @param today
	 */
	public static void storeBase(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("BaseSumUtil storeBase>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计的门店列表
			List<Map<String, Object>> orgList = OrgUtils.getIsCountOrgList();
			int size = 0;
			for(Map<String, Object> map : orgList){
				//门店ID
				int orgId = NumberUtil.getInt(map.get("orgId"),0);
				if(0 == orgId){
					continue;
				}
				//获取统计数据
				AppParam queryParam = new AppParam("sumUtilExtService","storeBase");
				queryParam.addAttr("today", today);
				queryParam.addAttr("orgId", orgId);
				queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(queryParam);
				List<Map<String,Object>> dataList = result.getRows();
				if(dataList.size()>0){
					//将统计数据插入统计表
					AppParam insertParam = new AppParam("sumStoreBaseService","save");
					insertParam.addAttr("today", today);
					insertParam.addAttr("orgId", orgId);
					insertParam.addAttr("list", dataList);
					insertParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
					result = RemoteInvoke.getInstance().call(insertParam);
					size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
				}
			}
			
			LogerUtil.log("BaseSumUtil storeBase >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil storeBase error");
			JobUtil.addProcessExecute(processId, "统计门店经理基本数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 门店基本数据统计
	 * @param processId
	 * @param today
	 */
	public static void orgBase(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("BaseSumUtil orgBase>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("sumUtilExtService","orgBase");
			queryParam.addAttr("today", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = 0;
			if(dataList.size()>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumOrgBaseService","save");
				insertParam.addAttr("today", today);
				insertParam.addAttr("list", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size =NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("BaseSumUtil orgBase >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil orgBase error");
			JobUtil.addProcessExecute(processId, "统计门店基本数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 汇总基本数据统计
	 * @param processId
	 * @param today
	 */
	public static void totalBase(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("BaseSumUtil totalBase>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("sumUtilExtService","totalBase");
			queryParam.addAttr("today", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = 0;
			if(dataList.size()>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumTotalBaseService","save");
				insertParam.addAttr("today", today);
				insertParam.addAttr("list", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size =NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("BaseSumUtil channelBase >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil totalBase error");
			JobUtil.addProcessExecute(processId, "统计汇总基本数据 报错：" + e.getMessage() );
		}
	}
	
	
	/**
	 * kf跟进的信贷员充值统计
	 * @param processId
	 * @param today
	 */
	public static void successChargeByKf(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("BaseSumUtil successChargeByKf>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("lendSuccessRechargeService","successChargeByKf");
			queryParam.addAttr("recordDate", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = 0;
			if(dataList.size()>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumRechargeKfService","save");
				insertParam.addAttr("recordDate", today);
				insertParam.addAttr("list", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size =NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("BaseSumUtil successChargeByKf >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil successChargeByKf error");
			JobUtil.addProcessExecute(processId, "kf跟进的信贷员充值统计报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 门店经理基本数据月度统计
	 * @param processId
	 * @param today
	 */
	public static void storeBaseMonth(Object processId, String toMonth) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("BaseSumUtil storeBaseMonth>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计的门店列表
			List<Map<String, Object>> orgList = OrgUtils.getIsCountOrgList();
			int size = 0;
			for(Map<String, Object> map : orgList){
				//门店ID
				int orgId = NumberUtil.getInt(map.get("orgId"),0);
				if(0 == orgId){
					continue;
				}
				//获取统计数据
				AppParam queryParam = new AppParam("sumUtilExtService","storeBaseMonth");
				queryParam.addAttr("toMonth", toMonth);
				queryParam.addAttr("orgId", orgId);
				queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(queryParam);
				List<Map<String,Object>> dataList = result.getRows();
				if(dataList.size()>0){
					//将统计数据插入统计表
					AppParam insertParam = new AppParam("sumStoreBaseMonthService","save");
					insertParam.addAttr("toMonth", toMonth);
					insertParam.addAttr("list", dataList);
					insertParam.addAttr("orgId", orgId);
					insertParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
					result = RemoteInvoke.getInstance().call(insertParam);
					size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
				}
			}
			LogerUtil.log("BaseSumUtil storeBaseMonth >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil storeBaseMonth error");
			JobUtil.addProcessExecute(processId, "统计月度门店人员基本数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 风控基本数据统计
	 * @param processId
	 * @param today
	 */
	public static void riskBase(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("BaseSumUtil riskBase>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("sumUtilExtService","riskBase");
			queryParam.addAttr("today", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			int size = 0;
			if(result.getRows().size() > 0 && !StringUtils.isEmpty(result.getRow(0))){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumRiskBaseService","save");
				insertParam.addAttr("today", today);
				insertParam.addAttrs(result.getRow(0));
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("BaseSumUtil riskBase >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil totalBase error");
			JobUtil.addProcessExecute(processId, "风控基本数据 报错：" + e.getMessage());
		}
	}
	
	/**
	 * 订单状态数据统计
	 */
	public static void dealOrderTypeSum(Object processId,String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("BaseSumUtil dealOrderTypeSum>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			AppParam queryParam = new AppParam("storeListOptExtService","queryDealOrderTypeToday");
			queryParam.addAttr("recordDate", today);
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			int sucSize=0;
			List<Map<String,Object>> dataList = result.getRows();
			if(dataList.size()>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumDealOrderTypeService","save");
				insertParam.addAttr("recordDate", today);
				insertParam.addAttr("list", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				sucSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("BaseSumUtil dealOrderTypeSum >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +sucSize);
		} catch (Exception e) {
			LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil dealOrderTypeSum error");
			JobUtil.addProcessExecute(processId, "订单状态统计 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 订单评分数据统计
	 */
	public static void orderRateSum(Object processId,String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("BaseSumUtil orderRateSum>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			AppParam queryParam = new AppParam("storeApplyExtService","queryRateToday");
			queryParam.addAttr("recordDate", today);
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			int sucSize=0;
			List<Map<String,Object>> dataList = result.getRows();
			if(dataList.size()>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumOrderRateService","save");
				insertParam.addAttr("recordDate", today);
				insertParam.addAttr("list", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				sucSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("BaseSumUtil orderRateSum >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +sucSize);
		} catch (Exception e) {
			LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil orderRateSum error");
			JobUtil.addProcessExecute(processId, "订单评分数据统计 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 订单状态渠道统计
	 */
	public static void channelDealOrderTypeSum(Object processId,String today){
		AppResult result = new AppResult();
		try {
			LogerUtil.log("BaseSumUtil channelDealOrderTypeSum>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			AppParam queryParam = new AppParam("storeApplyExtService","queryOrderChannelToday");
			queryParam.addAttr("recordDate", today);
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			
			int sucSize=0;
			List<Map<String,Object>> dataList = result.getRows();
			if(dataList.size()>0){
				AppParam insertParam = new AppParam("sumChannelDealordertypeService","save");
				insertParam.addAttr("recordDate", today);
				insertParam.addAttr("list", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				sucSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			
			LogerUtil.log("BaseSumUtil channelDealOrderTypeSum >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +sucSize);
		} catch (Exception e) {
			LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil channelDealOrderTypeSum error");
			JobUtil.addProcessExecute(processId, "订单状态渠道统计 报错：" + e.getMessage() );
		}
	}
	/**
	 * 订单状态分组统计（门店）
	 */
	public static void orgDealOrderSum(Object processId,String today){
		AppResult result = new AppResult();
		try {
			LogerUtil.log("BaseSumUtil orgDealOrderSum>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			AppParam queryParam = new AppParam("storeListOptExtService","queryOrgDealOrderToday");
			queryParam.addAttr("recordDate", today);
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			
			int sucSize=0;
			List<Map<String,Object>> dataList = result.getRows();
			if(dataList.size()>0){
				AppParam insertParam = new AppParam("sumOrgDealOrderService","save");
				insertParam.addAttr("recordDate", today);
				insertParam.addAttr("list", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				sucSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			
			LogerUtil.log("BaseSumUtil orgDealOrderSum >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +sucSize);
		} catch (Exception e) {
			LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil orgDealOrderSum error");
			JobUtil.addProcessExecute(processId, "订单状态分组统计（门店）统计 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 查询门店人员暂停分单加入暂停表
	 */
	public static void querystorePauseAddPool(Object processId){
		try {
			LogerUtil.log("BaseSumUtil querystorePauseAddPool>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			AppParam queryParam = new AppParam("custLevelService","query");
			queryParam.addAttr("isAllotOrder", StoreConstant.STORE_ALLOT_STATUS_2);
			queryParam.addAttr("allotDesc", "您当前回款数未达到要求");
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
			int sucSize = 0;
			for(Map<String, Object> map : queryResult.getRows()){
				try{
					String customerId = StringUtil.getString(map.get("customerId"));
					AppParam allotParam = new AppParam("storePauseAllotService","queryCount");
					allotParam.addAttr("customerId", customerId);
					allotParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START
									+ ServiceKey.Key_busi_in));
					AppResult allotResult = RemoteInvoke.getInstance().callNoTx(allotParam);
					int totalSize = NumberUtil.getInt(allotResult.getAttr(DuoduoConstant.TOTAL_SIZE),0);
					if(totalSize == 0){
						//获取用户信息
						Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
						String pauseDate = DateUtil.getSimpleFmt(new Date());
						String orgId = StringUtil.getString(custInfo.get("orgId"));
						//加入暂停分单表
						AppParam insertParam = new AppParam("storePauseAllotService","saveOrUpdate");
						insertParam.addAttr("customerId", customerId);
						insertParam.addAttr("orgId", orgId);
						insertParam.addAttr("pauseDate",pauseDate);
						insertParam.setRmiServiceName(AppProperties
								.getProperties(DuoduoConstant.RMI_SERVICE_START
										+ ServiceKey.Key_busi_in));
						AppResult result = RemoteInvoke.getInstance().call(insertParam);
						if(result.isSuccess()){
							sucSize ++;
						}
						//加入暂停分单统计表
						AppParam sumParam = new AppParam("sumStorePauseAllotService","saveOrUpdate");
						sumParam.addAttr("customerId", customerId);
						sumParam.addAttr("orgId", orgId);
						sumParam.addAttr("pauseDate",pauseDate);
						sumParam.setRmiServiceName(AppProperties
								.getProperties(DuoduoConstant.RMI_SERVICE_START
										+ ServiceKey.Key_sum));
						RemoteInvoke.getInstance().call(sumParam);
					}
				}catch (Exception e) {
					LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil querystorePauseAddPool error");
				}
			}
			LogerUtil.log("BaseSumUtil querystorePauseAddPool >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +sucSize);
		} catch (Exception e) {
			LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil querystorePauseAddPool error");
			JobUtil.addProcessExecute(processId, "查询门店人员暂停分单加入暂停表 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 查询门店人员暂停分单统计
	 */
	public static void storePauseAllotCount(Object processId){
		try {
			LogerUtil.log("BaseSumUtil storePauseAllotCount>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			AppParam queryParam = new AppParam("storePauseAllotService","query");
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
			int sucSize = 0;
			for(Map<String, Object> map : queryResult.getRows()){
				try{
					String customerId = StringUtil.getString(map.get("customerId"));
					String roleType = StoreUserUtil.getCustomerRole(customerId);
					//离职的门店人员删除暂停分单信息
					if(CustConstant.CUST_ROLETYPE_0.equals(roleType)){
						//删除门店人员暂停分单信息
						AppParam deleteParam = new AppParam("storePauseAllotService","delete");
						deleteParam.addAttr("customerId", customerId);
						deleteParam.setRmiServiceName(AppProperties
								.getProperties(DuoduoConstant.RMI_SERVICE_START
										+ ServiceKey.Key_busi_in));
						RemoteInvoke.getInstance().call(deleteParam);
						//删除门店人员暂停分单统计信息
						AppParam deleteSumParam = new AppParam("sumStorePauseAllotService","deleteByCustId");
						deleteSumParam.addAttr("customerId", customerId);
						deleteSumParam.setRmiServiceName(AppProperties
								.getProperties(DuoduoConstant.RMI_SERVICE_START
										+ ServiceKey.Key_sum));
						RemoteInvoke.getInstance().call(deleteSumParam);
						continue;
					}
					String pauseDate = StringUtil.getString(map.get("pauseDate"));
					String createTime = StringUtil.getString(map.get("createTime"));
					AppParam pasueParam = new AppParam("storePauseAllotService","storePauseAllotCount");
					pasueParam.addAttr("customerId", customerId);
					pasueParam.addAttr("pauseDate", pauseDate);
					pasueParam.addAttr("pauseDateTime", createTime);
					pasueParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
					AppResult pasueResult = RemoteInvoke.getInstance().callNoTx(pasueParam);
					if(pasueResult.getRows().size() > 0){
						AppParam updateParam = new AppParam("storePauseAllotService","update");
						updateParam.addAttrs(pasueResult.getRow(0));
						updateParam.setRmiServiceName(AppProperties
								.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
						AppResult updateResult = RemoteInvoke.getInstance().call(updateParam);
						sucSize += NumberUtil.getInt(updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE), 0);
					}
				}catch (Exception e) {
					LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil storePauseAllotCount error");
				}
			}
			LogerUtil.log("BaseSumUtil storePauseAllotCount >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end update count=" +sucSize);
		} catch (Exception e) {
			LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil storePauseAllotCount error");
			JobUtil.addProcessExecute(processId, "查询门店人员暂停分单统计 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 查询门店人员暂停分单统计
	 */
	public static void storePauseAllotSum(Object processId){
		try {
			LogerUtil.log("BaseSumUtil storePauseAllotSum>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			AppParam queryParam = new AppParam("storePauseAllotService","query");
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
			int sucSize = 0;
			for(Map<String, Object> map : queryResult.getRows()){
				try {
					String customerId = StringUtil.getString(map.get("customerId"));
					String pauseDate = StringUtil.getString(map.get("pauseDate"));
					String createTime = StringUtil.getString(map.get("createTime"));
					AppParam pasueParam = new AppParam("storePauseAllotService","storePauseAllotCount");
					pasueParam.addAttr("customerId", customerId);
					pasueParam.addAttr("pauseDate", pauseDate);
					pasueParam.addAttr("pauseDateTime", createTime);
					pasueParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
					AppResult pasueResult = RemoteInvoke.getInstance().callNoTx(pasueParam);
					if(pasueResult.getRows().size() > 0){
						AppParam updateParam = new AppParam("sumStorePauseAllotService","update");
						updateParam.addAttrs(pasueResult.getRow(0));
						updateParam.setRmiServiceName(AppProperties
								.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
						AppResult updateResult = RemoteInvoke.getInstance().call(updateParam);
						sucSize += NumberUtil.getInt(updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE), 0);
					}
				}catch (Exception e) {
					LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil storePauseAllotSum error");
				}
			}
			LogerUtil.log("BaseSumUtil storePauseAllotSum >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end update count=" +sucSize);
		} catch (Exception e) {
			LogerUtil.error(BaseSumUtil.class,e, "BaseSumUtil storePauseAllotSum error");
			JobUtil.addProcessExecute(processId, "查询门店人员暂停分单统计 报错：" + e.getMessage() );
		}
	}
}
