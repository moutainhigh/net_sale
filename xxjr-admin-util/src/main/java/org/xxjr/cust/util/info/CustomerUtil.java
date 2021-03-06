package org.xxjr.cust.util.info;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.RandomStringUtils;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.security.MD5Util;
import org.ddq.common.security.md5.Md5;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.DuoduoUser;
import org.ddq.common.web.session.ThreadConstants;
import org.llw.common.web.util.IdentifyUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.CustTokenConstant;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;


public class CustomerUtil {


	/**用户数据保留时长为 1小时**/
	public static Integer CACHE_TIME = 60*60*1;
	
	/**用户数据保留时长为 7天**/
	public static Integer CACHE_TIME_7_DAYS = 7*24*60*60;
	
	/**用户手机数据保留时长为 30天**/
	public static Integer USER_LOGIN_TIME = 30*24*60*60;
	
	/** 客服管理登录用户信息缓存key  **/
	public static final String KF_USER_SESSION = "KF_SESS_";

	
	/**
	 * 保留小数点后4位
	 */
	public static DecimalFormat df1 = new DecimalFormat("#,##0.0000");	
	/**
	 * 保留小数点后两位
	 */
	public static DecimalFormat df2 = new DecimalFormat("0.##");
	/**
	 * 用户手机ID
	 */
	public static final String AUTOZ_LOGIN_KEY = "antoLogin_";
	
	private static final char[] randomChs = new char[] { '1', '2', '3', '4','5', '6', '7', '8', '9', '0','a','f','e','y','z' };
	
	/** 连连支付商户用户标识 **/
	public static final String LIANLIAN_USER_PRE ="xxjrlluid";
	
	/** 连连支付测试商户用户标识 **/
	public static final String LIANLIAN_TEST_USER_PRE ="testxxjrlluid";
		
	public static String getLianUserId(){
		if(AppProperties.isDebug()){
			return LIANLIAN_TEST_USER_PRE + CustomerUtil.getCustId();
		}else{
			return LIANLIAN_USER_PRE + CustomerUtil.getCustId();
		}
	}
	
	public static AppResult retErrorMsg(String errMsg){
		AppResult result = new AppResult();
		result.setSuccess(false);
		result.setMessage(errMsg);
		return result;
	}
	
	/**
	 * 创建tokenId
	 * 
	 * @param type
	 * @return
	 */
	public static String createTokenId(String type) {
		StringBuffer bf = new StringBuffer();
		String random = RandomStringUtils.random(8);
		bf.append(random);
		bf.append(DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERNYYYYMMDDHHMMSSSSS));
		String token = type + Md5.getInstance().encrypt(bf.toString());
		return token;
	}

	public static String setToken(String loginType, String custId,int seconds) {
		String signId= CustomerUtil.createTokenId(loginType);
		RedisUtils.getRedisService().set(signId, (Serializable)custId, seconds);
		return signId;
	}
	
	/**
	 * app保存token
	 * @param loginType
	 * @param custId
	 * @param request
	 * @return
	 */
	public static String setAppToken(String loginType, String custId,
			HttpServletRequest request) {
		String signId= createTokenId(loginType);
		Map<String, Object> tokenInfo = new HashMap<String, Object>();
		tokenInfo.put(CustTokenConstant.CUST_LOGINSTATUS,
				CustTokenConstant.CUST_LOGINSTATUS_LOGININ);
		tokenInfo.put(CustTokenConstant.USER_TOKENID, signId);
		tokenInfo.put(CustTokenConstant.CUST_LOGINTYPE, loginType);
		tokenInfo.put(CustTokenConstant.CUST_ID,custId);
		RedisUtils.getRedisService().set(signId, (Serializable)tokenInfo, 60 * 60 *24 *30);
		return signId;
	}

	/***
	 * 用户登录操作
	 * 
	 * @param context
	 * @param request
	 * @return
	 */
	public static AppResult doLogin(AppParam context,
			HttpServletRequest request) {
		String phoneUUID = request.getParameter("UUID");
		context.setService("customerService");
		context.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		context.setMethod("login");
		AppResult result = new AppResult();
		AppResult loginResult = RemoteInvoke.getInstance().call(context);
		if (loginResult.getRows().size() == 1) {
			Map<String, Object> custMap = loginResult.getRow(0);
			String uid = custMap.get("customerId").toString();
			String status = custMap.get("status").toString();
			if (status.equals("2")) {
				result.setMessage("该用户帐号已被管理员锁定，请与客服人员联系。");
				result.setSuccess(false);
				return result;
			} else if (status.equals("3")) {
				result.setMessage("该用户帐号已失效，请与客服人员联系。");
				result.setSuccess(false);
				return result;
			}
			custMap = CustomerIdentify.getCustIdentify(uid);
			setExistsSession(custMap, request);
			DuoduoSession.web2Service(request);
			if (!StringUtils.isEmpty(phoneUUID)) {
				RedisUtils.getRedisService().set(
						CustomerUtil.AUTOZ_LOGIN_KEY + "-" + uid, phoneUUID,
						USER_LOGIN_TIME);
			}
			result.putAttr("customerId", CustomerUtil.getEncrypt(uid));
			result.putAttr("roleType", custMap.get("roleType"));
			result.putAttr("isNewer",
					StringUtils.isEmpty(StringUtil.objectToStr(custMap.get("realName")).trim()) ? "1" : "0");
		} else {
			result.setMessage("用户名或密码不正确");
			result.setSuccess(false);
		}
		return result;
	}
	
	public static void refreshSession(HttpServletRequest request){
		Map<String,Object> custMap = CustomerIdentify.refreshIdentifyById(getCustId());
		setExistsSession(custMap,request);
	}
	
	/***
	 * 设置用户session 非登录处理
	 * @param context
	 * @param request
	 * @return
	 */
	public static void setExistsSession(Map<String, Object> custMap,HttpServletRequest request){
		DuoduoUser duoduoUser = new DuoduoUser();
		if(StringUtils.isEmpty(custMap.get("userName"))){
			duoduoUser.setUsername(custMap.get("customerId")+"-" +
					StringUtil.getHideTelphone(org.ddq.common.util.StringUtil.getString(custMap.get("telephone"))));
		}
		String platform = request.getParameter("platform");
		if(!StringUtils.isEmpty(platform)){
			custMap.put("platform", StringUtils.isEmpty(platform) ? "0" : platform);
		}
		duoduoUser.setSessionData(custMap);
		request.setAttribute(ThreadConstants.DUODUO_USER, duoduoUser);
		request.getSession().setAttribute(ThreadConstants.DUODUO_USER, duoduoUser);
	}
	
	public static void refreshRedis(HttpServletRequest request){
		CustomerIdentify.refreshIdentifyById(getCustId());
	}
	
	/***
	 * 用户更改手机号
	 * @param context
	 * @param request
	 * @return
	 */
	public static AppResult changeTelephone(String newTelephone,String customerId){
		// 更改用户手机信息
		AppParam phoneParam = new AppParam();
		phoneParam.setService("customerService");
		phoneParam.setMethod("update");
		phoneParam.addAttr("telephone", newTelephone);
		phoneParam.addAttr("customerId", customerId);
		phoneParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		RemoteInvoke.getInstance().call(phoneParam);
		//修改busi用户手机号信息
		AppParam busiParam = new AppParam();
		busiParam.setService("busiCustService");
		busiParam.setMethod("update");
		busiParam.addAttr("telephone", newTelephone);
		busiParam.addAttr("customerId", customerId);
		busiParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "busi"));
		RemoteInvoke.getInstance().call(busiParam);

		return new AppResult();
	}
	
	/***
	 * 解析手机号码归属地
	 * @param context
	 * @param request
	 * @return
	 */
	public static Map<String,Object> parseAreaInfo(String telephone){
		Map<String,Object> areaInfo = new HashMap<String,Object>();
		if(StringUtils.isEmpty(telephone) ||telephone.length() < 7){
			return areaInfo;
		}
		AppParam phoneParam = new AppParam();
		phoneParam.setService("mobileService");
		phoneParam.setMethod("query");
		phoneParam.addAttr("mobileNumber", telephone.substring(0, 7));
		phoneParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult result =RemoteInvoke.getInstance().call(phoneParam);
		if(result.getRows().size() > 0){
			return result.getRow(0);
		}
		return areaInfo;
	}

	/***
	 * 获取session用户id
	 * @return
	 */
	public static String getCustId(){
		return DuoduoSession.getUser().getSessionData().get("customerId").toString();
	}
	
	public static String getCustomerId2(){
		DuoduoUser user = DuoduoSession.getUser();
		if(user == null || StringUtils.isEmpty(user.getSessionData().get("customerId")))
			return "";
		else
		return  user.getSessionData().get("customerId").toString();
	}
	
	/***
	 * 根据用户openid查客户信息
	 * @param openid
	 * @return
	 */
	public static Map<String, Object> queryInfoByOpenid(String openid){
		if(StringUtils.isEmpty(openid)){
			return null;
		}
		AppParam param = new AppParam();
		param.addAttr("openid", openid);
		param.setService("customerService");
		param.setMethod("login");
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = RemoteInvoke.getInstance().call(param);
		if(result.getRows().size() > 0){
			return result.getRow(0);
		}
		return null;
	}	
	
	
	/***
	 * 根据用户unionid查客户信息
	 * @param openid
	 * @return
	 */
	public static Map<String, Object> queryInfoByUnionid(Object unionid){
		if(StringUtils.isEmpty(unionid)){
			return null;
		}
		AppParam param = new AppParam();
		param.addAttr("unionid", unionid);
		param.setService("customerService");
		param.setMethod("loginQuery");
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = RemoteInvoke.getInstance().call(param);
		if(result.getRows().size() > 0){
			return result.getRow(0);
		}
		return null;
	}
	
	
	/**
	 * 修改(绑定)用户表openid
	 * @param customerId
	 * @param openid
	 * @return
	 */
	public static AppResult updateOpenid(Object customerId,Object openid){
		AppParam updateParam = new AppParam();
		updateParam.addAttr("openid", openid);
		updateParam.addAttr("customerId", customerId);
		updateParam.setService("customerService");
		updateParam.setMethod("updateOpenid");
		updateParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		return RemoteInvoke.getInstance().call(updateParam);
	}
	
	/**
	 * 填充unionid信息
	 * @param customerId
	 * @param openid
	 * @param unionid
	 * @return
	 */
	public static AppResult updateUnionid(Object customerId,Object openid,Object unionid){
		AppParam updateParam = new AppParam();
		updateParam.addAttr("unionid", unionid);
		updateParam.addAttr("customerId", customerId);
		updateParam.setService("customerService");
		updateParam.setMethod("updateUnionid");
		updateParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		return RemoteInvoke.getInstance().call(updateParam);
	}
	
	/***
	 * 根据手机号或推荐人ID查用户ID
	 * @param userName
	 * @return
	 */
	public static String queryCustId(String telephone,String refererId){
		if(StringUtils.isEmpty(telephone) && StringUtils.isEmpty(refererId)){
			return null;
		}
		AppParam  queryCust = new AppParam();
		queryCust.addAttr("telephone", telephone);
		queryCust.addAttr("customerId", refererId);
		queryCust.setService("customerService");
		queryCust.setMethod("queryCustName");
		queryCust.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult result = RemoteInvoke.getInstance().callNoTx(queryCust);
		if(result.getRows().size()>0){
			return result.getRow(0).get("customerId").toString();
		}
		return null;
	}
	
	/**
	 * 根据gzhId和openid查询customerID
	 * @param gzhId
	 * @param openid
	 * @return
	 */
	public static Object queryCustIdByOpenid(String gzhId, String openid){
		AppParam queryParam = new AppParam();
		queryParam.setService("customerService");
		queryParam.setMethod("query");
		queryParam.addAttr("gzhId", gzhId);
		queryParam.addAttr("openid", openid ==null ?"0000":openid);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult queryResult = RemoteInvoke.getInstance().call(queryParam);
		if(queryResult.getRows().size()>0){
			return queryResult.getRow(0).get("customerId");
		}
		return null;
	}
	
	/**
	 * 从t_openid_relation根据customerId查询openid
	 * @param openid
	 * @return
	 */
	public static Object queryOpenidByCustId(Object gzhId, Object customerId){
		AppParam queryParam = new AppParam();
		queryParam.setService("customerService");
		queryParam.setMethod("query");
		queryParam.addAttr("customerId", customerId);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult queryResult = RemoteInvoke.getInstance().call(queryParam);
		if(queryResult.getRows().size()>0){
			return queryResult.getRow(0).get("openid");
		}
		return null;
	}
	
	/**
	 * 查询用户是否已设置密码
	 * @param customerId
	 * @return
	 */
	public static Object queryCustPwd(String customerId){
		if(StringUtils.isEmpty(customerId)){
			return null;
		}
		AppParam  queryCust = new AppParam();
		queryCust.addAttr("customerId", customerId);
		queryCust.setService("customerService");
		queryCust.setMethod("queryCustPwd");
		queryCust.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult result = RemoteInvoke.getInstance().callNoTx(queryCust);
		if(result.getRows().size() > 0){
			return result.getRow(0).get("password");
		}
		return null;
	}
	
	/**
	 * 查询用户是否已设置交易密码
	 * @param customerId
	 * @return
	 */
	public static Object queryCustJyPwd(String customerId){
		if(StringUtils.isEmpty(customerId)){
			return null;
		}
		AppParam  queryCust = new AppParam();
		queryCust.addAttr("customerId", customerId);
		queryCust.setService("customerService");
		queryCust.setMethod("queryCustPwd");
		queryCust.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult result = RemoteInvoke.getInstance().callNoTx(queryCust);
		if(result.getRows().size() > 0){
			return result.getRow(0).get("tradePwd");
		}
		return null;
	}
	
	/**
	 * 查询用户信息(公共方法)
	 * @param params
	 * @return
	 */
	public static Map<String,Object> queryCustInfo(AppParam params){
		params.setService("customerService");
		params.setMethod("query");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult result = null;
		if (SpringAppContext.getBean("customerService") == null) {
			result = RemoteInvoke.getInstance().callNoTx(params);
		}else {
			result = SoaManager.getInstance().invoke(params);
		}
		
		if(result.getRows().size() > 0){
			return result.getRow(0);
		}
		return null;
	}
	
	/**
	 * 用户Id加密
	 * @param custId
	 * @return
	 */
	public static String getEncrypt(String custId){
		String start = getRandBefore();
		String newCust = getCustId(custId);
		return  start + StringUtil.encodeBase64(newCust);
	}
	
	
	/***
	 * 用户Id解密
	 * @param custId
	 * @return
	 */
	public static String getDecrypt(String custId){
		try{
			if (custId == null || custId.equals("")) {
				return null;
			}
			int size = SysParamsUtil.getIntParamByKey("USERID", 8);
			if(custId.length()<=size){
				return null;
			}
			custId = custId.substring(size);
			String descId = StringUtil.decoderBase64(custId);
			return new Integer(descId).toString();
		}catch(Exception e){
			
		}
		return null;
	}
	
	private static String getCustId(String custId){
		StringBuffer str = new StringBuffer(custId);
		if(str.length()<11){
			for(int i=custId.length();i<=11;i++){
				str.insert(0,"0");
			}
		}
		return str.toString();
	}
	
	private static String getRandBefore(){
		return IdentifyUtil.getRandValue(SysParamsUtil.getIntParamByKey("USERID", 8));
	}
	
	//用户自动注册
	public static String custAutoRegister(AppParam regParams){
		Object sourceType = regParams.getAttr("sourceType");
		regParams.setService("customerService");
		regParams.setMethod("register");
		regParams.addAttr("password",MD5Util.getEncryptPassword("123456"));
		regParams.addAttr("sourceType",
				StringUtils.isEmpty(sourceType) ? CustConstant.CUST_sourceType_XXJR: sourceType);
		regParams.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START +ServiceKey.Key_cust));
		
		AppResult result = RemoteInvoke.getInstance().call(regParams);
		
		return result.isSuccess() ? result.getAttr("customerId").toString() : "";
	}
	
	public static void sumRegister (Object telephone) {
		if (StringUtils.isEmpty(telephone)) {
			return;
		}
		AppParam updateParam = new AppParam("sumUserRegisterService", "update");
		updateParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		updateParam.addAttr("telephone", telephone);
		updateParam.addAttr("register", "1");
		if (SpringAppContext.getBean("sumUserRegisterService") == null) {
			RemoteInvoke.getInstance().call(updateParam);
		}else {
			SoaManager.getInstance().invoke(updateParam);
		}
	}
	
	/**
	 * 用户自动注册
	 * @param regParams
	 * @return
	 */
	public static String applyCustAutoRegister(AppParam regParams){
		Object sourceType = regParams.getAttr("sourceType");
		regParams.setService("borrowCustService");
		regParams.setMethod("register");
		regParams.addAttr("sourceType",StringUtils.isEmpty(sourceType) ? CustConstant.CUST_sourceType_XXJR: sourceType);
		regParams.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = RemoteInvoke.getInstance().call(regParams);
		return result.isSuccess() ? result.getAttr("customerId").toString() : "";
	}
	
	public static String queryBorrowCustId(String telephone,String refererId){
		if(StringUtils.isEmpty(telephone) && StringUtils.isEmpty(refererId)){
			return null;
		}
		AppParam  queryCust = new AppParam();
		queryCust.addAttr("telephone", telephone);
		queryCust.addAttr("customerId", refererId);
		queryCust.setService("borrowCustService");
		queryCust.setMethod("query");
		queryCust.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = RemoteInvoke.getInstance().callNoTx(queryCust);
		if(result.getRows().size()>0){
			return result.getRow(0).get("customerId").toString();
		}
		return null;
	}
	
	/**
	 * 生成唯一标识
	 * 
	 * @return
	 */
	public  static String getUUID(String preStr) {
		String random = RandomStringUtils.random(5, randomChs);
		String seriaNo = DateUtil.toStringByParttern(new Date(), "yyyyMMddHHmmssSSS")+ random;
		String key = Md5.getInstance().encrypt(seriaNo);
		return preStr != null ? preStr + key : key;
	}
	
	/**
	 * 更新用户信息(设置用户环信账号等)
	 * @param params
	 * @return
	 */
	public static AppResult updateInfo(AppParam params) {
		AppResult result = new AppResult();
		params.setService("customerService");
		params.setMethod("update");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		if (SpringAppContext.getBean("customerService") == null) {
			result = RemoteInvoke.getInstance().call(params);
		}else{
			result = SoaManager.getInstance().invoke(params);
		}
		return result;
	}
	
	
	/**
	 * 用户是否管理员
	 * @param custoemrId
	 * @return
	 */
	public static boolean isAdmin(String customerId){
		return CustConstant.CUST_ROLETYPE_1.equals(getRoleType(customerId));
	}
	/**
	 * 获取用户类型
	 * @param customerId
	 * @return
	 */
	public static String getRoleType(String customerId){
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
		return StringUtil.getString(custInfo.get("roleType"));
	}
	
	/**
	 * 获取signid
	 * @param request
	 * @return
	 */
	public static String getSignId(HttpServletRequest request) {
		String signId = request.getParameter(CustTokenConstant.USER_SIGNID);
		if (signId == null || signId=="") {
			signId = request.getHeader(CustTokenConstant.USER_SIGNID);
		}
		request.setAttribute(CustTokenConstant.USER_SIGNID,signId);
		return signId;
	}
	
	/**
	 * 返回用户信息
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getTokenInfo(String signId) {
		Map<String, Object> tokenInfo = new HashMap<String, Object>();
		if (signId != null) {
			tokenInfo = (Map<String, Object>) RedisUtils.getRedisService().get(
					signId);
		}
		if(null == tokenInfo){
			tokenInfo = new HashMap<String, Object>();
		}
		return tokenInfo;
	}
	
	/**
	 * 返回用户信息
	 * 
	 * @param request
	 * @return
	 */
	public static Map<String, Object> getTokenInfo(HttpServletRequest request) {
		String signId = (String) request.getAttribute(CustTokenConstant.USER_SIGNID);
		return getTokenInfo(signId);
	}
	
	/**
	 * 获取登录用户id
	 * @param request
	 * @return
	 */
	public static  Object getCustIdFromToken(HttpServletRequest request){
		Map<String, Object> tokenInfo = getTokenInfo(request);
		return tokenInfo.get(CustTokenConstant.CUST_ID);
	}
	
	/**
	 * 获取登录用户id
	 * @param request
	 * @return
	 */
	public static  Object getOpenIdFromToken(HttpServletRequest request){
		Map<String, Object> tokenInfo = getTokenInfo(request);
		return tokenInfo.get(CustConstant.USER_OPENID);
	}

	/**
	 * 获取互动吧用户id
	 * @param request
	 * @return
	 */
	public static  Object getHdbIdFromToken(HttpServletRequest request){
		Map<String, Object> tokenInfo = getTokenInfo(request);
		return tokenInfo.get(CustTokenConstant.HDB_CUST_ID);
	}
	
	/**
	 * 查询用户密码
	 * @param params
	 * @return
	 */
	public static AppResult verifyPass(AppParam params){
		String password = StringUtil.getString(params.removeAttr("password"));
		String encryptPwd = MD5Util.getEncryptPassword(password);
		params.addAttr("password", encryptPwd);
		params.setService("customerService");
		params.setMethod("queryCustPwd");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		return result;
	}
	
	/***
	 * 设置用户session 非登录处理
	 * @param context
	 * @param request
	 * @return
	 */
	public static AppResult logout(HttpServletRequest request){
		AppResult result = new AppResult();
		String customerId = getCustId();
		
		HttpSession session = request.getSession(false);
		if (session != null) {
			DuoduoSession.clearSessionData(request);
			session.invalidate();
			result.setSuccess(true);
			RedisUtils.getRedisService().del(CustomerUtil.AUTOZ_LOGIN_KEY + "-" + customerId);
		}else{
			result.setSuccess(false);
		}
		return result;
		
	}
	
	/***
	 * 微信端退出登录
	 * @param context
	 * @param request
	 * @return
	 */
	public static AppResult wxLogout(HttpServletRequest request){
		AppResult result = new AppResult();
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(ThreadConstants.DUODUO_USER);
		}
		return result;
		
	}
	
	/**
	 * 获取小程序保存的openid和gzhId
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,String> getXcxInfo(HttpServletRequest request) {
		String signId = request.getParameter(CustTokenConstant.USER_SIGNID);
		if (StringUtils.isEmpty(signId)) {
			signId = request.getHeader(CustTokenConstant.USER_SIGNID);
		}
		if (StringUtils.isEmpty(signId)) {
			signId = org.ddq.common.util.StringUtil.objectToStr(request
					.getAttribute(CustTokenConstant.USER_SIGNID));
		}
		Map<String, String> xcxInfo = (Map<String, String>) RedisUtils
				.getRedisService()
				.get(CustTokenConstant.XCX_SIN_Token + signId);
		return xcxInfo;
	}
	
	/**
	 * 将用户信息添加到小程序缓存中
	 */
	@SuppressWarnings("unchecked")
	public static void setXcxUserId(HttpServletRequest request,String customerId) {
		String signId = request.getParameter(CustTokenConstant.USER_SIGNID);
		if (StringUtils.isEmpty(signId)) {
			signId = request.getHeader(CustTokenConstant.USER_SIGNID);
		}
		if (StringUtils.isEmpty(signId)) {
			signId = org.ddq.common.util.StringUtil.objectToStr(request
					.getAttribute(CustTokenConstant.USER_SIGNID));
		}
		Map<String, String> xcxInfo = (Map<String, String>) RedisUtils
				.getRedisService()
				.get(CustTokenConstant.XCX_SIN_Token + signId);
		xcxInfo.put("customerId",customerId);
	}
	
	/**
	 * 查询绑定的客服信息
	 */
	public static Map<String,Object> getBindingKf(String custId){
		AppResult result = new AppResult();
		AppParam param = new AppParam();
		param.setService("kfCustService");
		param.setMethod("queryBindingKf");
		param.addAttr("customerId", custId);
		if(SpringAppContext.getBean("kfCustService") == null) {
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(param);
		}else{
			result = SoaManager.getInstance().invoke(param);
		}
		if(result.getRows().size() > 0){
			return result.getRow(0);
		}
		return new HashMap<String,Object>();
	}
	
	/**
	 * 添加活动记录
	 * @param params
	 */
	public static void addActivityRecord(AppParam params){
		params.setService("custActivityRecordService");
		params.setMethod("insert");
		params.addAttr("customerId", params.getAttr("customerId"));
		params.addAttr("referer", params.getAttr("referer"));
		params.addAttr("registerTime", new Date());
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		if (SpringAppContext.getBean("custActivityRecordService") == null) {
			RemoteInvoke.getInstance().call(params);
		}else{
			SoaManager.getInstance().invoke(params);
		}
	}
	
	/**
	 * 更新奖励记录
	 * @param params
	 */
	public static void updateRewardRecord(AppParam params) {
		String rewardType = StringUtil.getString(params.getAttr("rewardType"));
		AppParam param = new AppParam();
		// 阶梯奖励，记录好友是否已奖
		if ("rewardReferer".equals(rewardType)) {
			int refererNums = NumberUtil.getInt(params.getAttr("refererNums"), 0);
			if (refererNums == 3) {
				param.addAttr("friends3Reward", 1);
			}else if(refererNums == 5){
				param.addAttr("friends5Reward", 1);
			}else if (refererNums  == 10) {
				param.addAttr("friends10Reward", 1);
			}
		}
		String operator = StringUtil.getString(params.getAttr("operator"));// 操作类型 firstRecharge-好友首次充值、 identifyCard-好友工作认证
		// 好友完成首次充值或工作认证，记录推荐人张数
		if ("firstRecharge".equals(operator)) {
			param.addAttr("refererRechargeReward", 1);// 好友首次充值，记录推荐人奖励张数(v4.0奖励邀请人1张免单券)
		}else if ("identifyCard".equals(operator)) {
			param.addAttr("refererIdentifyCardReward", 1);// 好友工作认证，记录推荐人奖励张数(v4.0奖励邀请人1张5折券)
		}
		param.setService("custActivityRecordService");
		param.setMethod("saveOrUpdate");
		param.addAttr("customerId", params.getAttr("customerId"));
		param.addAttr("selfId", params.getAttr("selfId"));
		param.addAttr("operator", operator);
		param.addAttr("activityType", 2);
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		if (SpringAppContext.getBean("custActivityRecordService") == null) {
			RemoteInvoke.getInstance().call(param);
		}else{
			SoaManager.getInstance().invoke(param);
		}
	}
	
	/**
	 * 拦截状态不是正常的用户
	 * @param status
	 * @return
	 */
	public static String errStatus(int status) {
		String errMsg = null;
		switch (status) {
		case 1:
			break;
		case 2:
			errMsg = "该账户被锁定";
			break;
		case 3:
			errMsg = "该账户已失效";
			break;
		case 4:
			errMsg = "该账户永久锁定";
			break;
		default:
			break;
		}
		return errMsg;
	}
}
