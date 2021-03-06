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
 * 
 * 挂卖统计工具类
 * @author ZQH
 *
 */
public class RetSumUtil {

	/**
	 * 渠道回款统计
	 * @param processId
	 * @param today
	 */
	public static void channelRet(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("RetSumUitl channelRet>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("sumUtilExtService","retByChannel");
			queryParam.addAttr("recordDate", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = 0;
			if(dataList.size()>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumRetChannelService","save");
				insertParam.addAttr("today", today);
				insertParam.addAttr("list", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().callNoTx(insertParam);
				size =NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("RetSumUitl channelRet >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(RetSumUtil.class,e, "RetSumUitl channelRet error");
			JobUtil.addProcessExecute(processId, "统计渠道回款数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 客服回款统计
	 * @param processId
	 * @param today
	 */
	public static void kfRet(Object processId, String today, String isSummaryKf) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("RetSumUitl kfRet>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			if(StringUtils.isEmpty(today) || StringUtils.isEmpty(isSummaryKf)){
				LogerUtil.log("RetSumUitl kfRet>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 传入的参数有误：today="+today+",isSummaryKf="+isSummaryKf);
				return;
			}
			//获取统计数据
			AppParam queryParam = new AppParam("sumUtilExtService","retSumaryByKf");
			queryParam.addAttr("isSummaryKf", isSummaryKf);
			queryParam.addAttr("recordDate", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = 0;
			if(dataList.size()>0){
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumRetKfService","save");
				insertParam.addAttr("today", today);
				insertParam.addAttr("list", dataList);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().callNoTx(insertParam);
				size =NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("RetSumUitl kfRet >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(RetSumUtil.class,e, "RetSumUitl kfRet error");
			JobUtil.addProcessExecute(processId, "统计客服回款数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 回款基本统计
	 * @param processId
	 * @param today
	 */
	public static void retByBase(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("RetSumUitl timeRet>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			//获取统计数据
			AppParam queryParam = new AppParam("sumUtilExtService","retByBase");
			queryParam.addAttr("recordDate", today);
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(queryParam);
			List<Map<String,Object>> dataList = result.getRows();
			int size = 0;
			if(dataList.size()>0){
				Map<String,Object> paramsMap = dataList.get(0);
				//将统计数据插入统计表
				AppParam insertParam = new AppParam("sumRetBaseService","save");
				insertParam.addAttrs(paramsMap);
				insertParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().callNoTx(insertParam);
				size =NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			}
			LogerUtil.log("RetSumUitl RetByBase >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(RetSumUtil.class,e, "RetSumUitl RetByBase error");
			JobUtil.addProcessExecute(processId, "按按处理回款统计数据 报错：" + e.getMessage() );
		}
	}
	
	/**
	 * 按门店回款统计
	 * @param processId
	 * @param today
	 */
	public static void storeRet(Object processId, String today) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("RetSumUitl storeRet>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
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
				AppParam queryParam = new AppParam("sumUtilExtService","retSumaryByStore");
				queryParam.addAttr("recordDate", today);
				queryParam.addAttr("orgId", orgId);
				queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().call(queryParam);
				List<Map<String,Object>> dataList = result.getRows();
				if(dataList.size()>0){
					//将统计数据插入统计表
					AppParam insertParam = new AppParam("sumRetStoreService","save");
					insertParam.addAttr("today", today);
					insertParam.addAttr("orgId", orgId);
					insertParam.addAttr("list", dataList);
					insertParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
					result = RemoteInvoke.getInstance().callNoTx(insertParam);
					size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
				}
			}
			LogerUtil.log("RetSumUitl storeRet >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(RetSumUtil.class,e, "RetSumUitl storeRet error");
			JobUtil.addProcessExecute(processId, "统计门店经理回款数据 报错：" + e.getMessage() );
		}
	}
	
	
	/**
	 * 按门店回款统计
	 * @param processId
	 * @param today
	 */
	public static void storeRetMonth(Object processId, String toMonth) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("RetSumUitl storeRetMonth>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
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
				AppParam queryParam = new AppParam("sumUtilExtService","storeRetMonth");
				queryParam.addAttr("toMonth", toMonth);
				queryParam.addAttr("orgId", orgId);
				queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().call(queryParam);
				List<Map<String,Object>> dataList = result.getRows();
				if(dataList.size()>0){
					//将统计数据插入统计表
					AppParam insertParam = new AppParam("sumRetStoreMonthService","save");
					insertParam.addAttr("toMonth", toMonth);
					insertParam.addAttr("orgId", orgId);
					insertParam.addAttr("list", dataList);
					insertParam.setRmiServiceName(AppProperties
							.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
					result = RemoteInvoke.getInstance().callNoTx(insertParam);
					size += NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
				}
			}
			LogerUtil.log("RetSumUitl storeRetMonth >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" +size);
		} catch (Exception e) {
			LogerUtil.error(RetSumUtil.class,e, "RetSumUitl storeRetMonth error");
			JobUtil.addProcessExecute(processId, "月统计门店经理回款数据 报错：" + e.getMessage() );
		}
	}
}
