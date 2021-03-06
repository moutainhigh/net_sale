package org.xxjr.busi.util;

import java.io.Serializable;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.DateUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;

public class NetSaleCfgUtil {
	
	public static final String redis_key_netSaleCfg = "redis_key_netSaleCfg";
	
	/** 缓存7天*/
	public static final int base_cache_time = 60*60*24*7;
	
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getNetSaleCfg () {
		
		List<Map<String, Object>> result = (List<Map<String, Object>>) RedisUtils.getRedisService().get(redis_key_netSaleCfg);
		if (result == null ) {
			result = refreshNetSaleCfg();
		}
		return result;
	}
	
	public static List<Map<String, Object>> refreshNetSaleCfg () {
		AppParam param = new AppParam("netSaleCfgService", "query");
		param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
		param.addAttr("enable", "1");
		AppResult result = new AppResult();
		if (SpringAppContext.getBean("netSaleCfgService") == null) {
			result = RemoteInvoke.getInstance().callNoTx(param);
		}else {
			result = SoaManager.getInstance().invoke(param);
		}
		if (result.getRows().size() > 0) {
			List<Map<String, Object>> rows = result.getRows();
			RedisUtils.getRedisService().set(redis_key_netSaleCfg, (Serializable) rows,base_cache_time);
			return rows;
		}
		return null;
	}
	
	/**
	 * 转移网销池中是新单的数据到挂卖
	 */
	public static void transferNetDataToSale (Object processId) {
		List<Map<String, Object>> netSaleCfg = getNetSaleCfg();
		AppParam queryParam = new AppParam("netStorePoolService", "queryTransferData");
		queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		for (Map<String, Object> item : netSaleCfg) {
			AppResult queryResult = new AppResult();
			queryParam.clearAttr();
			queryParam.addAttr("saleCount", item.get("saleCount"));
			queryParam.addAttrs(item);
			int minHours = NumberUtil.getInt(item.get("minHours"), 7);
			int maxHours = NumberUtil.getInt(item.get("maxHours"), 3);
			String startTime = DateUtil.toStringByParttern(DateUtil.minu(LocalDateTime.now(),  minHours, DateUtil.ChronoUnit_HOURS), "yyyy-MM-dd hh");
			String endTime = DateUtil.toStringByParttern(DateUtil.plus(LocalDateTime.now(),  maxHours, DateUtil.ChronoUnit_HOURS), "yyyy-MM-dd hh");
			
			queryParam.addAttr("startTime", (startTime + ":00:00"));
			queryParam.addAttr("endTime", (endTime + ":59:59"));
			queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
			transferSale(queryResult.getRows(), processId);
		}
	}
	
	private static void transferSale (List<Map<String, Object>> rows, Object processId) {
		if (rows.size() > 0) {
			for (Map<String, Object> map : rows) {
				AppParam param = new AppParam("salePoolService", "save");
				param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				param.addAttrs(map);
				AppResult result = RemoteInvoke.getInstance().call(param);
				if (result.isSuccess()) {
					param.setService("netStorePoolService");//修改为已转挂卖
					param.setMethod("updateTransStatus");
					RemoteInvoke.getInstance().call(param);
				}
			}
		}
	}
	
	/**
	 * 根据传入的类型，修改或删除对应的相关 信息
	 * @param applyId
	 * @param type 1.网销跟进 ,2挂卖成功
	 */
	public static AppResult updateSaleOrNetStatus (String applyId, int type) {
		AppResult result = new AppResult();
		if (StringUtils.isEmpty(applyId)) {
			result.setSuccess(false);
			result.setMessage("申请ID不能为空!");
			return result;
		}
		if (type == 1) {//当前为网销跟进 操作,需要修改贷超相关信息
			AppParam saleParam = new AppParam("salePoolService", "delete");
			saleParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			saleParam.addAttr("applyId", applyId);
			if (SpringAppContext.getBean("salePoolService") == null) {
				result = RemoteInvoke.getInstance().call(saleParam);
			}else {
				result = SoaManager.getInstance().invoke(saleParam);
			}
			int count = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Delete_SIZE), 0);
			if (count > 0) {
				return result;//成功代表数据还在贷超池中直接返回
			}

		}else if (type == 2) {//当前为挂卖成功操作,需要修改网销相关操作
			AppParam netParam = new AppParam("netStorePoolService", "updateNetStatus");
			netParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			netParam.addAttr("applyId", applyId);
			if (SpringAppContext.getBean("salePoolService") == null) {
				result = RemoteInvoke.getInstance().call(netParam);
			}else {
				result = SoaManager.getInstance().invoke(netParam);
			}
		}
		return result;
	}
	
	/***
	 * https连接处理
	 */
	public static void trustEveryone() {  
	        try {  
	            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() { 
					@Override
					public boolean verify(String arg0, SSLSession arg1) {
						return true;
					}  
	            }); 
	            SSLContext context = SSLContext.getInstance("TLS");  
	            context.init(null, new X509TrustManager[] { new X509TrustManager() {

					@Override
					public void checkClientTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
						
					}
					@Override
					public void checkServerTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
						
					}

					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}  
	                
	            } }, new SecureRandom());  
	            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());  
	        } catch (Exception e) {  
	        }  
	    }
}
