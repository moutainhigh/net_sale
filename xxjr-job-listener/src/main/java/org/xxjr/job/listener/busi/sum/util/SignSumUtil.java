package org.xxjr.job.listener.busi.sum.util;

import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.LogerUtil;
import org.llw.job.util.JobUtil;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.OrgUtils;
import org.xxjr.sys.util.ServiceKey;

/**
 * 签单统计工具类
 * @author hwf
 *
 */
public class SignSumUtil {

	/**
	 * 渠道签单统计
	 * @param processId
	 * @param today
	 */
	public static void channelSign(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("SignSumUtil channelSign>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("sumUtilExtService","channelSign");
			queryParam.addAttr("today", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = 0;
			if(dataList.size()>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumSignChannelService","save");
				insertParam.addAttr("today", today);
				insertParam.addAttr("list", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size =NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("SignSumUtil channelSign >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(SignSumUtil.class,e, "SignSumUtil channelSign error");
			JobUtil.addProcessExecute(processId, "统计渠道签单数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 客服签单统计
	 * @param processId
	 * @param today
	 */
	public static void kfSign(Object processId, String today, String isSummaryKf) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("SignSumUtil kfSign>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			if(StringUtils.isEmpty(today) || StringUtils.isEmpty(isSummaryKf)){
				LogerUtil.log("SignSumUtil kfSign>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 传入的参数有误：today="+today+",isSummaryKf="+isSummaryKf);
				return;
			}
			//获取统计数据
			AppParam queryParam = new AppParam("sumUtilExtService","kfSign");			
			queryParam.addAttr("isSummaryKf", isSummaryKf);
			queryParam.addAttr("today", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = 0;
			if(dataList.size()>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumSignKfService","save");
				insertParam.addAttr("today", today);
				insertParam.addAttr("list", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size =NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("SignSumUtil kfSign >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(SignSumUtil.class,e, "SignSumUtil kfSign error");
			JobUtil.addProcessExecute(processId, "统计客服签单数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 门店经理签单统计
	 * @param processId
	 * @param today
	 */
	public static void storeSign(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("SignSumUtil storeSign>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
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
				AppParam queryParam = new AppParam("sumUtilExtService","storeSign");
				queryParam.addAttr("today", today);
				queryParam.addAttr("orgId", orgId);
				queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(queryParam);
				List<Map<String,Object>> dataList = result.getRows();
				if(dataList.size()>0){
					//将统计数据插入统计表
					AppParam insertParam = new AppParam("sumSignStoreService","save");
					insertParam.addAttr("today", today);
					insertParam.addAttr("list", dataList);
					insertParam.addAttr("orgId", orgId);
					insertParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
					result = RemoteInvoke.getInstance().call(insertParam);
					size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
				}
			}
			LogerUtil.log("SignSumUtil storeSign >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(SignSumUtil.class,e, "SignSumUtil storeSign error");
			JobUtil.addProcessExecute(processId, "统计门店经理签单数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 总的签单统计(按处理时间) 
	 * @param processId
	 * @param today
	 */
	public static void sumTotalSign(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("SignSumUtil sumTotalSign>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("sumUtilExtService","sumTotalSign");
			queryParam.addAttr("today", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = dataList.size();
			if(size>0){
				Map<String,Object> paramsMap = dataList.get(0);
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumSignBaseService","save");
				insertParam.addAttrs(paramsMap);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				size =NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("SignSumUtil sumTotalSign >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(BookSumUtil.class,e, "SignSumUtil sumTotalSign error");
			JobUtil.addProcessExecute(processId, "统计总的签单统计(按处理时间)  报错：" + e.getMessage() );
		}
	}
	
	
	/**
	 * 门店经理本月签单统计
	 * @param processId
	 * @param today
	 */
	public static void storeSignMonth(Object processId, String toMonth) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("SignSumUtil storeSignMonth>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
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
				AppParam queryParam = new AppParam("sumUtilExtService","storeSignMonth");
				queryParam.addAttr("toMonth", toMonth);
				queryParam.addAttr("orgId", orgId);
				queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().callNoTx(queryParam);
				List<Map<String,Object>> dataList = result.getRows();
				if(dataList.size()>0){
					//将统计数据插入统计表
					AppParam insertParam = new AppParam("sumSignStoreMonthService","save");
					insertParam.addAttr("toMonth", toMonth);
					insertParam.addAttr("list", dataList);
					insertParam.addAttr("orgId", orgId);
					insertParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
					result = RemoteInvoke.getInstance().call(insertParam);
					size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
				}
			}
			LogerUtil.log("SignSumUtil storeSignMonth >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(SignSumUtil.class,e, "SignSumUtil storeSignMonth error");
			JobUtil.addProcessExecute(processId, "月统计门店经理签单数据 报错：" + e.getMessage() );
		}
	}

	/***
	 * 签单失败原因统计
	 * @param processId
	 * @param today
	 */
	public static void signFailChannel(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("SignSumUtil signFailChannel>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			AppParam queryParam = new AppParam("storeApplyExtService","querySignFailToday");
			queryParam.addAttr("recordDate", today);
			queryParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			int sucSize=0;
			List<Map<String,Object>> dataList = result.getRows();
			if(dataList.size()>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumSignFailChannelService","save");
				insertParam.addAttr("recordDate", today);
				insertParam.addAttr("list", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().call(insertParam);
				sucSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("SignSumUtil signFailChannel >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +sucSize);
		} catch (Exception e) {
			LogerUtil.error(SignSumUtil.class,e, "SignSumUtil signFailChannel error");
			JobUtil.addProcessExecute(processId, "签单失败原因统计 报错：" + e.getMessage() );
		}
	}
}
