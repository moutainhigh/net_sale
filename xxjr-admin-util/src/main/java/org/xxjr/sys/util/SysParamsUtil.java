package org.xxjr.sys.util;

import java.io.Serializable;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
/***
 * 参数配置
 * @author Administrator
 *
 */
public class SysParamsUtil {
	public final static String StartParamKey="Param_";

	/**测试状态**/
	public static final String KEY_systemTest = "systemTest";
	/**版本号**/
	public static final String KEY_systemVersion = "systemVersion";
	
	/**测试的手机号码**/
	public static final String KEY_systemTelphone = "systemTelphone";
	/**测试的邮箱号码**/
	public static final String KEY_systemEmail = "systemEmail";
	/** 公司地址 */
	public static String KEY_COMPANY_ADDRESS = "companyAddress";
	/** 公司名称 */
	public static String KEY_COMPANY_NAME = "companyName";
	/** 公司客服电话 */
	public static String KEY_COMPANY_SERVICE_TEL = "serviceTel";
	/**短信发送状态**/
	public static final String KEY_sendStatus = "sendStatus";
	/** 微信支付功能 **/
	public static final String KEY_wxPayUsable = "wxPayUsable";
	/*** SMS保留 时长 **/
	public final static String Key_SMS_CACHE_TIME = "defautSmsTime";
	/** 小小金融工作时间 **/
	public static final String KEY_xxjr_work_time = "xxjr_work_time";
	
	/** 邀请好友活动开始时间 **/
	public static final String KEY_invite_start_date = "invite_start_date";
	/** 邀请好友活动结束时间 **/
	public static final String KEY_invite_end_date = "invite_end_date";
	/** 邀请信贷经理参与免单券活动开始时间 **/
	public static final String KEY_invite_freeTicket_start_date = "invite_freeTicket_start_date";
	/** 邀请信贷经理参与免单券活动结束时间**/
	public static final String KEY_invite_freeTicket_end_date = "invite_freeTicket_end_date";
	/** 信贷经理邀请5月30日活动开始时间 **/
	public static final String KEY_invite_530_start_date = "invite_530_start_date";
	/** 信贷经理邀请5月30日活动结束时间**/
	public static final String KEY_invite_530_end_date = "invite_530_end_date";
	/** 信贷经理邀请关注小小攒钱、办卡活动开始时间 **/
	public static final String KEY_invite_xxzq_startdate = "invite_xxzq_start_date";
	/** 信贷经理邀请关注小小攒钱、办卡活动结束时间 **/
	public static final String KEY_invite_xxzq_enddate = "invite_xxzq_end_date";
	/** 信用卡分销系统公众号ID **/
	public static final String KEY_FX_GZHID = "fxGzhId";
	
	
	/**
	 * 获取参数
	 * @param key
	 */
	public static String refreshValue(String key, String value){
		if (value == null) {
			AppParam queryParams = new AppParam();
			queryParams.setService("sysParamsService");
			queryParams.setMethod("query");
			queryParams.addAttr("paramCode", key);
			
			//若没有相应的对象，使用远程调用 
			if (SpringAppContext.getBean("sysParamsService") == null) {
				AppResult paramsResult = RemoteInvoke.getInstance().call(queryParams);
				if (paramsResult.getRows().size() > 0) {
					value = (String)paramsResult.getRow(0).get("paramValue");
				}
			}else{
				AppResult paramsResult = SoaManager.getInstance().invoke(queryParams);
				if (paramsResult.getRows().size() > 0) {
					value = (String)paramsResult.getRow(0).get("paramValue");
				}
			}
			RedisUtils.getRedisService().set(SysParamsUtil.StartParamKey + key, (Serializable) value);
		}else{
			RedisUtils.getRedisService().set(SysParamsUtil.StartParamKey + key, (Serializable) value);
		}
		return value;
	}
	/**
	 * 获取参数
	 * @param key
	 */
	public static String getParamByKey(String key){
		Object value =null;
		try{
			value = RedisUtils.getRedisService().get(SysParamsUtil.StartParamKey + key);
		}catch(Exception e){
			return null;
		}
		if (value == null) {
			value = refreshValue(key, null);
		}
		return value ==null ? null: value.toString();
	}
	
	/**
	 * 获取参数，如果获取不到，直接返回key
	 * @param key
	 * @param retunKey
	 * @return
	 */
	public static String getParamByKey(String key, boolean retunKey){
		String value = getParamByKey(key);
		if(StringUtils.isEmpty(value)){
			RedisUtils.getRedisService().set(SysParamsUtil.StartParamKey + key, key);
			return key;
		}
		return value;
	}
	
	/**
	 * 获取参数，如果获取不到，直接返回defaultValue
	 * @param key 
	 * @param defaultValue
	 * @return
	 */
	public static int getIntParamByKey(String key, int defaultValue){
		String value = getParamByKey(key);
		if(StringUtils.isEmpty(value)){
			try{
				RedisUtils.getRedisService().set(SysParamsUtil.StartParamKey + key, String.valueOf(defaultValue));
			}catch(Exception e){
				
			}
			return defaultValue;
		}
		return Integer.valueOf(value);
	}
	
	/**
	 * 获取参数，如果获取不到，直接返回defaultValue
	 * @param key 
	 * @param defaultValue
	 * @return
	 */
	public static String getStringParamByKey(String key, String defaultValue){
		String value = getParamByKey(key);
		if(StringUtils.isEmpty(value)){
			try{
				RedisUtils.getRedisService().set(SysParamsUtil.StartParamKey + key, defaultValue);
			}catch(Exception e){
				
			}
			return defaultValue;
		}
		return value;
	}
	
	/**
	 * 系统测试
	 * @param key 
	 * @param defaultValue
	 * @return
	 */
	public static boolean getBoleanByKey(String key, boolean defaultValue){
		String value = getParamByKey(key);
		if(StringUtils.isEmpty(value)){
			try{
				RedisUtils.getRedisService().set(SysParamsUtil.StartParamKey + key, String.valueOf(defaultValue));
			}catch(Exception e){
				
			}
			return defaultValue;
		}
		return Boolean.valueOf(value);
	}
	
}
