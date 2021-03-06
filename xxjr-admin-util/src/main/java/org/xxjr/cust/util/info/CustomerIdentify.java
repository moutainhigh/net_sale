package org.xxjr.cust.util.info;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;

/**
 * 
 * 用户的 实名，邮箱，手机号码 的 基本数据
 * @author Administrator
 *
 */
public class CustomerIdentify {
	/**已经认证的身分信息**/
	public static String CacheKey_PASS = "CustIdentify";
	
	/** 微信用户状态 **/
	public static String CacheKey_WX_STATUS = "CustWXStatus";
	
	/**
	 * 获取用户 实名验证的数据
	 * @param customerId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getCustIdentify(String customerId){
		Map<String, Object> userMap = (Map<String, Object>) RedisUtils.getRedisService().get(CacheKey_PASS + customerId);
		if(userMap == null || userMap.isEmpty()){
			userMap = refreshIdentifyById(customerId);
		}
		return userMap;
	}
	
	/**
	 * 获取用户微信状态
	 * @param customerId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getWxStatus(Object gzhId, Object openid){
		Map<String, Object> wxMap = (Map<String, Object>) RedisUtils.getRedisService().get(CacheKey_WX_STATUS + openid);
		if(wxMap == null){
			wxMap = refreshWxStatus(gzhId, openid);
		}
		return wxMap;
	}
	
	/**
	 * 刷新用户实名验证 根据session
	 * @param custId
	 * @return
	 */
	public static Map<String, Object> refreshIdentifyBySession(String customerId){ 
		Map<String,Object> sessionData = DuoduoSession.getUser().getSessionData();
		Map<String, Object> userMap = getDBUserInfo(customerId, sessionData);
		RedisUtils.getRedisService().set(CacheKey_PASS + customerId, (Serializable)userMap, CustomerUtil.CACHE_TIME_7_DAYS);
		return userMap;
	}
	
	/**
	 * 根据数据库查出的信息刷新缓存
	 * @param custId
	 * @return
	 */
	public static Map<String, Object> refreshIdentifyByExists(String customerId,Map<String,Object> sessionData){ 
		Map<String, Object> userMap = getDBUserInfo(customerId, sessionData);
		RedisUtils.getRedisService().set(CacheKey_PASS + customerId, (Serializable)userMap, CustomerUtil.CACHE_TIME_7_DAYS);
		return userMap;
	}
	
	/**
	 * zhijie 刷新信息缓存
	 * @param custId
	 * @return
	 */
	public static void refreshIdentifyByExistsDate(String customerId,Map<String,Object> sessionData){ 
		
		RedisUtils.getRedisService().set(CacheKey_PASS + customerId, (Serializable)sessionData, CustomerUtil.CACHE_TIME_7_DAYS);
		
	}
	/**
	 * 刷新用户实名验证的数据，根据最新数据
	 * @param custId
	 * @return
	 */
	public static Map<String, Object> refreshIdentifyById(String customerId){ 
		AppParam param = new AppParam();
		param.setService("customerService");
		param.setMethod("loginQuery");
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		param.addAttr("customerId", customerId);
		Map<String, Object> userMap = new HashMap<String,Object>();
		AppResult result = null;
		
		//若没有相应的对象，使用远程调用 
		if (SpringAppContext.getBean("customerService") == null) {
			result = RemoteInvoke.getInstance().call(param);
		}else{
			result = SoaManager.getInstance().invoke(param);
		}
		if(result.getRows().size()>0){
			Map<String,Object> resultMap = result.getRow(0);
			userMap = getDBUserInfo(customerId, resultMap);
		}
		RedisUtils.getRedisService().set(CacheKey_PASS + customerId, (Serializable)userMap, CustomerUtil.CACHE_TIME_7_DAYS);
		return userMap;
	}
	
	/**
	 * 刷新用户微信状态
	 * @param openid
	 * @param gzhId
	 * @return
	 */
	public static Map<String,Object> refreshWxStatus(Object gzhId, Object openid){
		Map<String, Object> baseInfo = new HashMap<String,Object>();
		AppParam param = new AppParam();
		param.setService("custWxinfoService");
		param.setMethod("queryInfoByOpenid");
		param.addAttr("openid", openid);
		param.addAttr("gzhId", gzhId);
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "wx"));
		AppResult result = null;
		if (SpringAppContext.getBean("custWxinfoService") == null) {
			result = RemoteInvoke.getInstance().call(param);
		}else{
			result = SoaManager.getInstance().invoke(param);
		}
		if(result.getRows().size() > 0){
			baseInfo = result.getRow(0);
			baseInfo.put("nickName", StringUtil.decodeStr(baseInfo.get("nickName")));
		}
		RedisUtils.getRedisService().set(CacheKey_WX_STATUS + openid, (Serializable)baseInfo, CustomerUtil.CACHE_TIME);
		return baseInfo;
	}

	/***
	 * 获取用户基本信息 ，去掉email,telephone,cardNo,realName
	 * @param customerId
	 * @return
	 */
	public static Map<String, Object> getNoShowInfo(String customerId) {
		Map<String,Object> map = getCustIdentify(customerId);
		map.remove("email");
		map.remove("telephone");
		map.remove("cardNo");
		map.remove("realName");
		return map;
	}
	
	/***
	 * 获取用户基本信息
	 * @param customerId
	 * @param resultMap
	 * @return
	 */
	private static Map<String, Object> getDBUserInfo(String customerId,Map<String, Object> resultMap) {
		String email = (String) resultMap.get("email");
		String telephone = (String) resultMap.get("telephone");
		String userName = (String) resultMap.get("userName");
		Map<String, Object> userMap = new HashMap<String,Object>();
		
		userMap.put("telephone", telephone);
		userMap.put("hideTelephone", StringUtil.getHideTelphone(telephone));
		userMap.put("email", email);
		userMap.put("hideEmail", StringUtil.getHideEmail(email));
		userMap.put("custDesc", resultMap.get("custDesc"));
		userMap.put("userType", resultMap.get("userType"));
		userMap.put("vipGrade", resultMap.get("vipGrade"));
		userMap.put("vipEndDate", resultMap.get("vipEndDate"));
		//设置推荐人
		userMap.put("refererTwo", resultMap.get("refererTwo"));
		userMap.put("referer", resultMap.get("referer"));
		userMap.put("openid", resultMap.get("openid"));
		userMap.put("unionid", resultMap.get("unionid"));
		userMap.put("gzhId", resultMap.get("gzhId"));
		userMap.put("userImage", resultMap.get("userImage"));
		userMap.put("qrcodeImgUrl", resultMap.get("qrcodeImgUrl"));
		userMap.put("imName", resultMap.get("imName"));
		userMap.put("status", resultMap.get("status"));
		userMap.put("sourceType", resultMap.get("sourceType"));
		userMap.put("orgId", resultMap.get("orgId"));
		userMap.put("orgNo", resultMap.get("orgNo"));
		userMap.put("authType", resultMap.get("authType"));
		userMap.put("kfAuthType", resultMap.get("kfAuthType"));
		userMap.put("userOrgs", resultMap.get("userOrgs"));
		userMap.put("roleType", resultMap.get("roleType"));
		userMap.put("groupName", resultMap.get("groupName"));
		userMap.put("teamName", resultMap.get("teamName"));
		userMap.put("remindStatus", resultMap.get("remindStatus"));
		userMap.put("customerId", customerId);
		//所在城市
		userMap.put("provice", resultMap.get("provice"));
		userMap.put("cityName", resultMap.get("cityName"));
		userMap.put("cityArea", resultMap.get("cityArea"));
		userMap.put("address", resultMap.get("address"));
		//工作认证情况
		userMap.put("lenderType", resultMap.get("lenderType"));
		userMap.put("company", resultMap.get("company"));
		userMap.put("cardStatus", resultMap.get("cardStatus"));
		userMap.put("compType", resultMap.get("compType"));
		userMap.put("companyDesc", resultMap.get("companyDesc"));
		userMap.put("compId", resultMap.get("compId"));
		//实名信息
		String cardNo = (String) resultMap.get("cardNo");
		String realName = (String) resultMap.get("realName");
		userMap.put("realName", realName);
		userMap.put("cardNo", cardNo);
		userMap.put("userName", userName);
		userMap.put("identifyStatus", resultMap.get("identifyStatus"));
		userMap.put("autoAuditCount", resultMap.get("autoAuditCount"));
		userMap.put("registerTime", resultMap.get("registerTime"));
		//头像状态
		userMap.put("headStatus", resultMap.get("headStatus"));
		
		
		if (!StringUtils.isEmpty(resultMap.get("cardNo")) && 
				!StringUtils.isEmpty(resultMap.get("realName"))) {
			userMap.put("hideCardNo", StringUtil.getHideIdentify(cardNo));
			userMap.put("hideRealName", StringUtil.getHideRealname(realName));
			userMap.put("hideName", StringUtil.getHideUserName(realName));
		}
		return userMap;
	}
	
	
}
