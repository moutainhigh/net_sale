package org.xxjr.busi.util.store;

import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.LogerUtil;
import org.xxjr.sys.util.ServiceKey;

public class BusiCustUtil {
	
	public static AppResult setBusiCustIn(Map<String, Object> custInfo,String key) {
		AppParam params = new AppParam();
		params.addAttrs(custInfo);
		if(key!=null){
			params.addAttr(key, "1");
		}
		//保存或修改个人信息抢单
		params.setMethod("saveOrUpdate");
		params.setService("busiCustService");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult result = new AppResult();
		try{
			result = RemoteInvoke.getInstance().call(params);
			LogerUtil.log("setBusiCust: " + result.isSuccess()  + custInfo.get("customerId") + "," + result.getMessage());
		}catch(Exception e){
			LogerUtil.error(BusiCustUtil.class, e, "setBusiCust error:" + custInfo.get("customerId") );
			result.setSuccess(Boolean.FALSE);
			result.setMessage(e.getMessage());
		}
		return result;
		
	}
	
	/***
	 * 更新sum中的用户信息
	 * @param custInfo
	 * @param key
	 * @return
	 */
	public static AppResult setBusiCustSum(Map<String, Object> custInfo,String key) {
		AppParam params = new AppParam();
		params.addAttrs(custInfo);
		if(key!=null){
			params.addAttr(key, "1");
		}
		//保存或修改个人信息抢单
		params.setMethod("saveOrUpdate");
		params.setService("sumBusiCustService");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		AppResult result = new AppResult();
		try{
			result = RemoteInvoke.getInstance().call(params);
			LogerUtil.log("setBusiCustSum: " + result.isSuccess()  + custInfo.get("customerId") + "," + result.getMessage());
		}catch(Exception e){
			LogerUtil.error(BusiCustUtil.class, e, "setBusiCustSum error:" + custInfo.get("customerId") );
			result.setSuccess(Boolean.FALSE);
			result.setMessage(e.getMessage());
		}
		return result;
		
	}
	
}
