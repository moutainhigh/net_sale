package org.xxjr.store.web.action.common;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ddq.active.mq.store.StoreTaskSend;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.exception.SysException;
import org.ddq.common.security.MD5Util;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.model.cache.RedisUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.BorrowChannelUtil;
import org.xxjr.busi.util.StoreConstant;
import org.xxjr.busi.util.TeamConfigUtil;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerPwdUtil;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.store.web.util.Key_SMS;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.OrgUtils;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;
import org.xxjr.sys.util.ValidUtils;

import lombok.extern.slf4j.Slf4j;



@Controller
@RequestMapping("/user/")
@Slf4j
public class UserAction {

	/**
	 * 密码登录，若不存在用户就注册
	 * @param request
	 * @return
	 */
	@RequestMapping("login")
	@ResponseBody
	public AppResult login(HttpServletRequest request, HttpServletResponse response){
		AppResult result = new AppResult();
		String telephone = request.getParameter("telephone");
		String password = request.getParameter("password");
		// 验证手机号  
		if(!ValidUtils.validateTelephone(telephone)){
			result.setSuccess(false);
			result.setMessage("请输入正确的手机号码");
			return result;
		}
		if(StringUtils.isEmpty(password)){
			result.setSuccess(false);
			result.setMessage("密码不能为空");
			return result;
		}
		try {
			
		//	判断是否超过3次密码错误
			if(CustomerPwdUtil.getCustLoginErrorCount(telephone)>=20){
				result.setMessage("用户名或密码错误超过3次,请用其他方式登录！");
				result.setSuccess(false);
				result.putAttr("errorMore", "1");
				return result;
			}
			AppParam custPwdParam = new AppParam();
			custPwdParam.addAttr("telephone", telephone);
			custPwdParam.addAttr("password", password);
			AppResult custInfoResult = CustomerUtil.verifyPass(custPwdParam);
			if(!custInfoResult.isSuccess()){
				return custInfoResult;
			}
			if(custInfoResult.getRows().size() <= 0){
				// 缓存登录密码错误次数+1
				CustomerPwdUtil.setCustLoginErrorCount(telephone);
				result.setMessage("用户名或密码不正确");
				result.setSuccess(false);
				return result;
			}
			
			Map<String, Object> custInfo = custInfoResult.getRow(0);
			String errMsg = CustomerUtil.errStatus(NumberUtil.getInt(custInfo.get("status"),-1));
			if (!StringUtils.isEmpty(errMsg)) {
				return CustomerUtil.retErrorMsg(errMsg);
			}
			
			AppParam loginParam = new AppParam();
			loginParam.setService("customerService");
			loginParam.setMethod("loginQuery");
			loginParam.addAttr("telephone", telephone);
			loginParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			AppResult loginResult = RemoteInvoke.getInstance().call(loginParam);
			
			if(loginResult.getRows().size() == 1){
				Map<String, Object> custMap = loginResult.getRow(0);
				if (!validLogin(custMap, result)) {
					return result;
				}
				//限制门店人员登录时间
				limitCustLogin(custMap);
				Map<String, Object> cust= new HashMap<String,Object>();
				cust.put("userName", custMap.get("realName"));
				cust.put("userRole", custMap.get("roleType"));
				cust.put("authRole", custMap.get("authType"));
				cust.put("allOrgs", custMap.get("userOrgs"));
				cust.put("userOrgId", custMap.get("orgId"));
				result.addRow(cust);

				String customerId = custMap.get("customerId").toString();
				String sessionId = request.getSession().getId();
				sessionId = StoreUserUtil.createNewSessionId(sessionId);
				response.setHeader("signId", sessionId);
				//设置用户的缓存
				RedisUtils.getRedisService().set(sessionId, customerId, StoreUserUtil.user_cache_time);
				RedisUtils.getRedisService().set(StoreUserUtil.USER_KEY +customerId, sessionId,StoreUserUtil.user_cache_time);
				//更新用户缓存
				CustomerIdentify.refreshIdentifyById(customerId);
				//更新登录状态
				StoreUserUtil.updateUserLoginStatus(customerId, 1);
				//添加登录记录
				StoreUserUtil.addStoreOnlineRecord(customerId, 1, "验证码登录");
				
				//创建任务对象调用mq
				StoreTaskSend storeSend = (StoreTaskSend)SpringAppContext.getBean(StoreTaskSend.class);
				Map<String, Object> msgParam = new HashMap<String, Object>();
				msgParam.put("recordDate", DateUtil.getSimpleFmt(new Date()));
				storeSend.sendStoreMessage(customerId,"countDealType" , msgParam);
				
			}else{
				result.setMessage("用户名或密码不正确");
				result.setSuccess(false);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "pwdlogin error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 快捷登录，若不存在用户就注册
	 * @param request
	 * @return
	 */
	@RequestMapping("kjLogin")
	@ResponseBody
	public AppResult kjLogin(HttpServletRequest request, HttpServletResponse response){
		AppResult result = new AppResult();
		String telephone = request.getParameter("telephone");
		String randomNo = request.getParameter("randomNo");
		// 验证手机号  
		if(!ValidUtils.validateTelephone(telephone)){
			result.setSuccess(false);
			result.setMessage("请输入正确的手机号码");
			return result;
		}
		if(StringUtils.isEmpty(randomNo)){
			result.setSuccess(false);
			result.setMessage("短信验证码不能为空");
			return result;
		}
		try {
			//短信验证码验证
			if(!ValidUtils.validateRandomNo(randomNo, Key_SMS.Key_SMS_STORE_KJ_LOGIN, telephone)){
				result.setSuccess(false);
				result.setMessage("短信验证码不正确");
				return result;
			}else {//验证成功，删除发送短信的计数key
				String smsCountKey = SysParamsUtil.getParamByKey
						(Key_SMS.Key_SMS_STORE_KJ_LOGIN, true)+telephone
						+Key_SMS.SMS_COUNT_FIX;
				
				ValidUtils.removeCache(smsCountKey);
			}
			
			AppParam loginParam = new AppParam();
			loginParam.setService("customerService");
			loginParam.setMethod("loginQuery");
			loginParam.addAttr("telephone", telephone);
			loginParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			AppResult loginResult = RemoteInvoke.getInstance().call(loginParam);
			
			if(loginResult.getRows().size() == 1){
				Map<String, Object> custMap = loginResult.getRow(0);
				if (!validLogin(custMap, result)) {
					return result;
				}
				//限制门店人员登录时间
				limitCustLogin(custMap);
				Map<String, Object> cust= new HashMap<String,Object>();
				cust.put("userName", custMap.get("realName"));
				cust.put("userRole", custMap.get("roleType"));
				cust.put("authRole", custMap.get("authType"));
				cust.put("allOrgs", custMap.get("userOrgs"));
				cust.put("userOrgId", custMap.get("orgId"));
				result.addRow(cust);

				String customerId = custMap.get("customerId").toString();
				String sessionId = request.getSession().getId();
				sessionId = StoreUserUtil.createNewSessionId(sessionId);
				response.setHeader("signId", sessionId);
				//设置用户的缓存
				RedisUtils.getRedisService().set(sessionId, customerId, StoreUserUtil.user_cache_time);
				RedisUtils.getRedisService().set(StoreUserUtil.USER_KEY +customerId, sessionId,StoreUserUtil.user_cache_time);
				//更新用户缓存
				CustomerIdentify.refreshIdentifyById(customerId);
				//更新登录状态
				StoreUserUtil.updateUserLoginStatus(customerId, 1);
				//添加登录记录
				StoreUserUtil.addStoreOnlineRecord(customerId, 1, "验证码登录");
				
				//创建任务对象调用mq
				StoreTaskSend storeSend = (StoreTaskSend)SpringAppContext.getBean(StoreTaskSend.class);
				Map<String, Object> msgParam = new HashMap<String, Object>();
				msgParam.put("recordDate", DateUtil.getSimpleFmt(new Date()));
				storeSend.sendStoreMessage(customerId,"countDealType" , msgParam);
				
			}else{
				result.setMessage("用户名不存在！");
				result.setSuccess(false);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "kjlogin error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 登出
	 * @param request
	 * @return
	 */
	@RequestMapping("logout")
	@ResponseBody
	public AppResult logout(HttpServletRequest request){
		AppResult result = new AppResult();
		String signId = request.getParameter("signId");
		try{
			if(!StringUtils.isEmpty(signId)){
				String customerId = StoreUserUtil.getCustomerId(request);
				RedisUtils.getRedisService().del(signId);
				RedisUtils.getRedisService().del(StoreUserUtil.USER_KEY +customerId);
				//添加登出记录
				StoreUserUtil.addStoreOnlineRecord(customerId, 0, "登出");
				//更新用户登陆状态
				StoreUserUtil.updateUserLoginStatus(customerId, 0);
			}
		}catch(Exception e){
			LogerUtil.error(this.getClass(), e, "logout");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	
	/**
	 * 在线、离开、忙碌、退出
	 * @param request
	 * @return
	 */
	@RequestMapping("statusChange")
	@ResponseBody
	public AppResult statusChange(HttpServletRequest request){
		AppResult result = new AppResult();
		String signId = request.getParameter("signId");
		//用户在线状态 0退出 1在线  2忙碌 3离开 
		String userStatus = request.getParameter("userStatus");
		try{
			if(!StringUtils.isEmpty(signId)){
				String customerId = StoreUserUtil.getCustomerId(request);
				//判断是否获取到customerId customerId未获取到默认返回-1
				if(!StoreConstant.STORE_NO_OBTAIN_CUSTOMER.equals(customerId)){
					if(0 == NumberUtil.getInt(userStatus)){//退出
						RedisUtils.getRedisService().del(signId);
						RedisUtils.getRedisService().del(StoreUserUtil.USER_KEY +customerId);
						//添加登出记录
						StoreUserUtil.addStoreOnlineRecord(customerId, 0, "退出");
						//更新用户登陆状态
						StoreUserUtil.updateUserLoginStatus(customerId, 0);
					}else{
						String desc = null;
						if(1 == NumberUtil.getInt(userStatus)){
							desc = "在线";
						}else if(2 == NumberUtil.getInt(userStatus)){
							desc = "忙碌";
						}else if(3 == NumberUtil.getInt(userStatus)){
							desc = "离开";
						}else if(4 == NumberUtil.getInt(userStatus)){
							desc = "无操作自动退出";
						}
						//添加登出记录
						StoreUserUtil.addStoreOnlineRecord(customerId, NumberUtil.getInt(userStatus), desc);
						//更新用户登陆状态
						StoreUserUtil.updateUserLoginStatus(customerId, NumberUtil.getInt(userStatus));
					}
				}
			}
		}catch(Exception e){
			LogerUtil.error(this.getClass(), e, "logout");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}


	/***
	 * 验证用户是否可以操作
	 * @param custMap
	 * @param loginResult
	 * @return
	 */
	private boolean validLogin(Map<String,Object> custMap,AppResult loginResult){
		String status = custMap.get("status").toString();
		if(status.equals("2")){
			loginResult.setMessage("该用户帐号已被管理员锁定，请与客服人员联系。");
			loginResult.setSuccess(false);
			return false;
		}else if(status.equals("3")){
			loginResult.setMessage("该用户帐号已失效，请与客服人员联系。");
			loginResult.setSuccess(false);
			return false;
		}
		String roleType = StringUtil.getString(custMap.get("roleType"));
		if(StringUtils.isEmpty(roleType) || (!CustConstant.CUST_ROLETYPE_1.equals(roleType)
				&& !CustConstant.CUST_ROLETYPE_2.equals(roleType)
				&& !CustConstant.CUST_ROLETYPE_3.equals(roleType)
				&& !CustConstant.CUST_ROLETYPE_6.equals(roleType)
				&& !CustConstant.CUST_ROLETYPE_7.equals(roleType)
				&& !CustConstant.CUST_ROLETYPE_8.equals(roleType)
				&& !CustConstant.CUST_ROLETYPE_9.equals(roleType)
				&& !CustConstant.CUST_ROLETYPE_10.equals(roleType))){
			loginResult.setSuccess(false);
			loginResult.setMessage("只有门店及管理员才能登录系统!");
			return false;
		}
		return true;
	}
	
	/***
	 * 限制业务员登录方法(登录时间 7:00-22:00)
	 * @param param
	 * @param customerId
	 */
	public static void limitCustLogin(Map<String,Object> custInfo){
		if(custInfo != null){
			String authType = StringUtil.getString(custInfo.get("roleType"));
			String orgId = StringUtil.getString(custInfo.get("orgId"));
			Map<String,Object> orgInfo = OrgUtils.getOrgByOrgId(orgId);
			int isNet = NumberUtil.getInt(orgInfo.get("isNet"),-1);
			String orgNo = StringUtil.getString(orgInfo.get("orgNo"));
			
			if(isNet != 1 && !"999".equals(orgNo) && !"000".equals(orgNo) ) {
				throw new SysException("你所属门店已被禁止登陆");
			}
			//门店业务员、门店主管、副主管限制登录
			if(CustConstant.CUST_ROLETYPE_3.equals(authType)
					||CustConstant.CUST_ROLETYPE_8.equals(authType)
					||CustConstant.CUST_ROLETYPE_9.equals(authType)){
				//设置开始登录时间
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, 7);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				long startLoginTime = cal.getTime().getTime();
				
				//设置最后登录时间
				Calendar calend = Calendar.getInstance();
				calend.set(Calendar.HOUR_OF_DAY, 22);
				calend.set(Calendar.MINUTE, 0);
				calend.set(Calendar.SECOND, 0);
				long lastLoginTime = calend.getTime().getTime();
				
				//当前时间
				long nowTime = new Date().getTime();
				if(nowTime < startLoginTime || nowTime > lastLoginTime){
					throw new SysException("当前时间不允许登录，正常登录时间为7:00-22:00");
				}
				
				//客户端IP
				String clientIp = DuoduoSession.getClientIp();
				
				log.info(custInfo.get("userName") + "客户端Ip:" + clientIp);
				
				String OrgIdsByIpLimit = SysParamsUtil.getStringParamByKey("OrgIdsByIpLimit","-1");
				String orgIPAddress = SysParamsUtil.getStringParamByKey("orgIpAddresses","0.0.0.1");
			
				if(StringUtils.hasText(OrgIdsByIpLimit) 
						&& StringUtils.hasText(orgIPAddress)) {
					if(OrgIdsByIpLimit.indexOf(orgId) >=0 && orgIPAddress.indexOf(clientIp) <0){
						throw new SysException("抱歉，您没有权限外网访问系统!");
					}
				}	
			}
		}
	}
	
	/**
	 * 查询所有团队
	 * @param request
	 * @return
	 */
	@RequestMapping("team/queryTeamAll")
	@ResponseBody
	public AppResult getTeamAll (HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			List<Map<String, Object>> teamAll = TeamConfigUtil.getTeamAll();
			if (teamAll != null && teamAll.size() > 0) {
				result.addRows(teamAll);
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryTeamAll error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	/***
	 * 查询所有渠道大类
	 * @param request
	 * @returnøøØ
	 */
	@RequestMapping("code/queryAll")
	@ResponseBody
	public AppResult queryAll(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			result.addRows(BorrowChannelUtil.getAllChannel());
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryAll error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	public static void main(String[] args) {
		System.out.println(MD5Util.getEncryptPassword("123456"));
	}
}
