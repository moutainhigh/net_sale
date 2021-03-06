package org.xxjr.busi.util.store;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.SysException;
import org.ddq.common.security.md5.Md5;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

/***
 * 跟踪平台用户相关工具类
 * 
 * @author sty
 *
 */
public class StoreUserUtil {

	public static final String USER_KEY = "store_userKey_";

	/** 跟进平台登录用户菜单树缓存key **/
	public static final String USER_TREE_MENUS_KEY = "store_userMenusTreeKey_";

	/** 跟进平台登录用户基本菜单缓存key **/
	public static final String USER_BASE_MENUS_KEY = "store_userBaseMenusKey_";

	/** 跟进平台登录用户审核权限缓存key **/
	public static final String USER_CHECK_MENUS_KEY = "store_userCheckMenusKey_";

	/** 跟进平台登录用户修改权限缓存key **/
	public static final String USER_MODIFY_MENUS_KEY = "store_userModifyMenusKey_";
	
	/** APP与PC绑定 **/
	public static final String APP_PC_CON ="apppccon";
	
	/** 今日统计缓存key **/
	public static final String STORE_TODAY_COUNT_KEY = "store_todayCountKey_";
	
	/** 本周统计缓存key **/
	public static final String STORE_THIS_WEEK_COUNT_KEY = "store_thisWeekCountKey_";
	
	/** 本月统计缓存key **/
	public static final String STORE_TO_MONTH_COUNT_KEY = "store_toMonthCountKey_";
	
	/** 首页滚动系统通知缓存key **/
	public static final String STORE_SYS_SCROLL_NOTIFY_KEY = "store_sysScrollNotifyKey_";
	
	/** 首页系统通知缓存key **/
	public static final String STORE_SYS_NOTIFY_KEY = "store_sysNotifyKey_";
	
	/** 首页系统反馈缓存key **/
	public static final String STORE_SYS_FEED_BACK_KEY = "store_sysFeedBackKey_";

	/** 用户缓存时间 **/
	public static final int user_cache_time = 60 * 30;
	
	/** 通知缓存时间 **/
	public static final int store_notify_cache_time = 60 * 60 * 24 * 3;

	/***
	 * 获取session用户id
	 * 
	 * @return
	 */
	public static String getCustomerId(HttpServletRequest request) {
		String signId = request.getParameter("signId");
		if (StringUtils.isEmpty(signId)) {
			return "-1";
		}
		String userId = (String) RedisUtils.getRedisService().get(signId);
		if (StringUtils.isEmpty(userId)) {
			return "-1";
		}
		return userId;
	}

	/***
	 * 获取session用户id
	 * 
	 * @return
	 */
	public static String getUserBySignId(String singnId) {
		if (StringUtils.isEmpty(singnId)) {
			return "-1";
		}
		String userId = (String) RedisUtils.getRedisService().get(singnId);
		if (StringUtils.isEmpty(userId)) {
			return "-1";
		}
		return userId;
	}

	/**
	 * 获取用户URL权限
	 * 
	 * @return
	 */
	public static String getUserMenuUrls(String roleId) {
		String menuUrls = (String) RedisUtils.getRedisService().get(
				USER_BASE_MENUS_KEY + roleId);
		if (StringUtils.isEmpty(menuUrls)) {
			menuUrls = refreshUserMenuUrls(roleId);
		}
		return menuUrls;
	}

	/**
	 * 刷新用户URL权限
	 * 
	 * @return
	 */
	public static String refreshUserMenuUrls(String roleId) {

		AppParam param = new AppParam();
		param.setService("sysStoreMenuService");
		param.setMethod("queryUserMenuUrls");
		param.addAttr("roleId", roleId);
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_cust));
		AppResult menuResult = RemoteInvoke.getInstance().callNoTx(param);

		String menuUrls = "";
		if (!StringUtils.isEmpty(menuResult.getRow(0))) {
			menuUrls = (String) menuResult.getRow(0).get("menuUrls");
			RedisUtils.getRedisService().set(USER_BASE_MENUS_KEY + roleId,
					menuUrls, 30 * 60);
		}

		return menuUrls;
	}

	/**
	 * 获取用户审核URL权限
	 * 
	 * @return
	 */
	public static String getUserCheckUrls(String roleId) {
		String menuUrls = (String) RedisUtils.getRedisService().get(
				USER_CHECK_MENUS_KEY + roleId);
		if (StringUtils.isEmpty(menuUrls)) {
			menuUrls = refreshUserCheckUrls(roleId);
		}
		return menuUrls;
	}

	/**
	 * 刷新用户URL权限
	 * 
	 * @return
	 */
	public static String refreshUserCheckUrls(String roleId) {

		AppParam param = new AppParam();
		param.setService("sysStoreMenuService");
		param.setMethod("queryUserCheckUrls");
		param.addAttr("roleId", roleId);
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_cust));
		AppResult menuResult = RemoteInvoke.getInstance().callNoTx(param);

		String menuUrls = "";
		if (!StringUtils.isEmpty(menuResult.getRow(0))) {
			menuUrls = (String) menuResult.getRow(0).get("menuUrls");
			RedisUtils.getRedisService().set(USER_CHECK_MENUS_KEY + roleId,
					menuUrls, 30 * 60 * 12);
		}

		return menuUrls;
	}

	/**
	 * 获取用户审核URL权限
	 * 
	 * @return
	 */
	public static String getUserModifyUrls(String roleId) {
		String menuUrls = (String) RedisUtils.getRedisService().get(
				USER_MODIFY_MENUS_KEY + roleId);
		if (StringUtils.isEmpty(menuUrls)) {
			menuUrls = refreshUserModifyUrls(roleId);
		}
		return menuUrls;
	}

	/**
	 * 刷新用户URL权限
	 * 
	 * @return
	 */
	public static String refreshUserModifyUrls(String roleId) {

		AppParam param = new AppParam();
		param.setService("sysStoreMenuService");
		param.setMethod("queryUserModifyUrls");
		param.addAttr("roleId", roleId);
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_cust));
		AppResult menuResult = RemoteInvoke.getInstance().callNoTx(param);

		String menuUrls = "";
		if (!StringUtils.isEmpty(menuResult.getRow(0))) {
			menuUrls = (String) menuResult.getRow(0).get("menuUrls");
			RedisUtils.getRedisService().set(USER_MODIFY_MENUS_KEY + roleId,
					menuUrls, 30 * 60 * 12);
		}

		return menuUrls;
	}

	/**
	 * 获取用户菜单树
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> getUserMenusTree(String roleId) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> menus = (List<Map<String, Object>>) RedisUtils.getRedisService().get(USER_TREE_MENUS_KEY + roleId);
		
		if (menus == null || menus.size() == 0) {
			menus = refreshUserMenusTree(roleId);
		}
	
		return menus;
	}

	/**
	 * 刷新用户主菜单权限
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> refreshUserMenusTree(String roleId) {

		AppParam params = new AppParam();
		params.setService("sysStoreMenuService");
		params.setMethod("queryUserMenusTree");
		params.addAttr("roleId", roleId);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_cust));
		AppResult menuResult = RemoteInvoke.getInstance().callNoTx(params);

		List<Map<String, Object>> menuList = null;
		List<Map<String, Object>> mainMenu = new ArrayList<Map<String, Object>>();
		if (menuResult.getRows().size() > 0) {
			menuList = menuResult.getRows();
			// 转换成菜单树
			for (Map<String, Object> map : menuList) {
				if (StringUtils.isEmpty(map.get("parentId"))) {
					mainMenu.add(map);
				}
			}
			for (Map<String, Object> map : mainMenu) {
				List<Map<String, Object>> subList = getChild(map.get("menuId")
						.toString(), menuList);
				if (subList != null) {
					map.put("subMenus", subList);
				}
			}
			
			RedisUtils.getRedisService().set(USER_TREE_MENUS_KEY + roleId,(Serializable) mainMenu, 60 * 30 * 12);
			
		}

		return mainMenu;
	}

	/**
	 * 递归查找子菜单
	 * 
	 * @param id
	 *            当前菜单id
	 * @param rootMenu
	 *            要查找的列表
	 * @return
	 */
	private static List<Map<String, Object>> getChild(String id,
			List<Map<String, Object>> rootMenu) {
		// 子菜单
		List<Map<String, Object>> childList = new ArrayList<>();
		for (Map<String, Object> map : rootMenu) {
			// 遍历所有节点，将父菜单id与传过来的id比较
			if (!StringUtils.isEmpty(map.get("parentId"))) {
				if (map.get("parentId").toString().equals(id)) {
					childList.add(map);
				}
			}
		}
		// 把子菜单的子菜单再循环一遍
		for (Map<String, Object> menu : childList) {
			// 递归
			List<Map<String, Object>> subList = getChild(menu.get("menuId")
					.toString(), rootMenu);
			if (subList != null) {
				menu.put("subMenus", subList);
			}
		} // 递归退出条件
		if (childList.size() == 0) {
			return null;
		}
		return childList;
	}

	/**
	 * 菜单权限判断
	 * 
	 * @param url
	 * @param userId
	 * @return
	 */
	public static boolean validateURL(String url, String userId, String baseUrl) {
		if ("user".equals(url)) {
			return true;
		}
		
		String keyVal = "/account/";

		String btnUrl = baseUrl.substring(baseUrl.indexOf(keyVal)
				+ keyVal.length() - 1);// 方法url
		String btnUrl2 = btnUrl.substring(btnUrl.lastIndexOf("/") + 1);
		String menuUrl = btnUrl.substring(0, btnUrl.lastIndexOf("/"));// 菜单url
		
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(userId);
		String roleId = "";
		if(custInfo != null){
			roleId =   StringUtil.getString(custInfo.get("authType"));
		}
		// 用户url权限
		String menuRole = getUserMenuUrls(roleId);
		String checkRole = getUserCheckUrls(roleId);
		String modifyRole = getUserModifyUrls(roleId);

		if (btnUrl2.contains("query")) {
			if (!menuRole.contains(menuUrl)) {
				return false;
			}
		} else if (btnUrl2.contains("check")) {
			if (!checkRole.contains(menuUrl)) {
				return false;
			}
		} else {
			if (!modifyRole.contains(menuUrl)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 隐藏用户敏感信息
	 */
	public static void hideInfo(AppResult result) {
		if (result.getRows().size() > 0) {
			List<Map<String, Object>> rows = result.getRows();
			Map<String, Object> map = rows.get(0);
			map.remove("userId");
			map.remove("roleId");
			map.remove("loginStatus");
			map.remove("password");
			map.put("telephone",
					org.ddq.common.util.StringUtil.getHideTelphone(map.get(
							"telephone").toString()));
			rows.set(0, map);
			result.setRows(rows);
		}
	}

	/**
	 * 
	 * @param customerId
	 * @param loginStatus
	 *            0-离线 1-在线
	 */
	public static void updateUserLoginStatus(Object customerId, int loginStatus) {
		// 更新客服登陆状态
		AppParam updateParam = new AppParam("customerService", "newUpdate");
		updateParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_cust));

		updateParam.addAttr("customerId", customerId);
		updateParam.addAttr("loginStatus", loginStatus);
		RemoteInvoke.getInstance().call(updateParam);
		
		// 更新客服登陆状态
		AppParam custParam = new AppParam("busiCustService", "update");
		custParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));

		custParam.addAttr("customerId", customerId);
		custParam.addAttr("loginStatus", loginStatus);
		RemoteInvoke.getInstance().call(custParam);
	}



	/**
	 * 清除用户所有缓存
	 * @param customerId
	 */
	public static void clearCustAllRedis(Object customerId) {
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId.toString());
		String roleId = "";
		if(custInfo != null){
			roleId =   StringUtil.getString(custInfo.get("authType"));
		}
		RedisUtils.getRedisService().del(CustomerIdentify.CacheKey_PASS + customerId);
		RedisUtils.getRedisService().del(StoreUserUtil.USER_KEY + customerId);
		RedisUtils.getRedisService().del(StoreUserUtil.USER_TREE_MENUS_KEY+roleId);
		RedisUtils.getRedisService().del(StoreUserUtil.USER_BASE_MENUS_KEY+roleId);
		RedisUtils.getRedisService().del(StoreUserUtil.USER_CHECK_MENUS_KEY+roleId);
		RedisUtils.getRedisService().del(StoreUserUtil.USER_MODIFY_MENUS_KEY+roleId);
	}

	/**
	 * 获取用户信息
	 * @param customerId
	 * @return
	 */
	public static Map<String, Object> getCustomerInfo(Object customerId) {
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId.toString());
		return custInfo;
	}
	
	
	/**
	 * 获取用户角色
	 * @param customerId
	 * @return
	 */
	public static String getCustomerRole(String customerId) {
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
		if(custInfo !=null){
			String roleType = StringUtil.getString(custInfo.get("roleType"));
			return roleType;
		}
		return "";
	}

	/**
	 * 刷新用户信息
	 * @param customerId
	 * @return
	 */
	public static Map<String, Object> refreshCustomerInfo(Object customerId) {
		AppParam loginParam = new AppParam();
		loginParam.setService("customerService");
		loginParam.setMethod("loginQuery");
		loginParam.addAttr("customerId", customerId);
		loginParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_cust));
		AppResult loginResult = RemoteInvoke.getInstance().call(loginParam);
		Map<String, Object> customMap = null;
		if (loginResult.getRows().size() > 0) {
			customMap = loginResult.getRow(0);
		}
		return customMap;
	}


	/**
	 * 创建新的sessionId
	 * 
	 * @param type
	 * @return
	 */
	public static String createNewSessionId(String sessionId) {
		StringBuffer bf = new StringBuffer();
		bf.append(sessionId);
		bf.append(DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERNYYYYMMDDHHMMSSSSS));
		sessionId =  Md5.getInstance().encrypt(bf.toString());
		return sessionId;
	}

	/***
	 *公共处理方法
	 * @param param
	 * @param customerId
	 */
	public static void dealUserAuthParam(AppParam param, String customerId,String paramType){
		//获取用户信息
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
		//开启渠道类型标识
		int openStoreChannelFlag = SysParamsUtil.getIntParamByKey("openStoreChannelFlag", 0);
		if(openStoreChannelFlag == 1){
			//设置查询渠道类型
			String queryChannelType = SysParamsUtil.getStringParamByKey("storeQueryChannelType", "2");
			param.addAttr("queryChannelType", queryChannelType);
		}
		if(custInfo != null){
			String authType =   StringUtil.getString(custInfo.get("roleType"));
			String userOrgs =  StringUtil.getString(custInfo.get("userOrgs"));
			String groupName = StringUtil.getString(custInfo.get("groupName"));//组
			String teamName = StringUtil.getString(custInfo.get("teamName"));//队
			String orgId = StringUtil.getString(custInfo.get("orgId"));//门店
			
			//门店管理员和管理可以查看门店
			if(CustConstant.CUST_ROLETYPE_1.equals(authType)
					||CustConstant.CUST_ROLETYPE_6.equals(authType)
					||CustConstant.CUST_ROLETYPE_7.equals(authType)){
				if(!"all".equals(userOrgs) && !StringUtils.isEmpty(userOrgs)){
					if("1".equals(StringUtil.getString(param.getAttr("audioType")))){ // 录音权限
						param.addAttr("manaOrgs",userOrgs);
						param.addAttr("isManager", true);
					}else{
						param.addAttr("userOrgs",userOrgs);
					}
				}else if(StringUtils.isEmpty(userOrgs)){
					throw new SysException("没有门店管理权限");
				}
				
			}else if(CustConstant.CUST_ROLETYPE_8.equals(authType)){//门店主管
				if(StringUtils.isEmpty(groupName) || StringUtils.isEmpty(orgId)){
					throw new SysException("没有门店主管权限");
				}
				param.addAttr("groupName",groupName);
				param.addAttr("orgId",orgId);
			}else if(CustConstant.CUST_ROLETYPE_9.equals(authType)){//门店副主管
				if(StringUtils.isEmpty(groupName) || StringUtils.isEmpty(teamName) || StringUtils.isEmpty(orgId) ){
					throw new SysException("没有门店副主管权限");
				}
				param.addAttr("groupName",groupName);
				param.addAttr("teamName",teamName);
				param.addAttr("orgId",orgId);
			}else{//门店业务员
				param.addAttr(paramType, customerId);
			}
		}
	}
	
	
	/**
	 * 添加系统登录登出记录 
	 * @param customerId
	 * @param loginStatus
	 */
	public static void addStoreOnlineRecord(String customerId, int loginStatus,String statusDesc) {
		Date now = new Date();
		AppParam param = new AppParam("storeOnlineRecordService", "insert");
		param.addAttr("customerId", customerId);
		param.addAttr("createTime", now);
		param.addAttr("loginStatus", loginStatus);
		param.addAttr("statusDesc", statusDesc);
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		RemoteInvoke.getInstance().call(param);
		addOnlineTimeCount(customerId,loginStatus,now);
	}
	/**
	 * 增加在线时长统计
	 * @param customerId
	 * @param loginStatus
	 */
	public static void addOnlineTimeCount(String customerId, int loginStatus,Date now) {
		try{
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			String roleType = StringUtil.getString(custInfo.get("roleType"));
			if((0 == loginStatus || 4 == loginStatus) && CustConstant.CUST_ROLETYPE_1.equals(roleType)){
				String recordDate = DateUtil.getSimpleFmt(now);
				AppParam queryParam = new AppParam("storeOnlineRecordService", "query");
				queryParam.addAttr("customerId", customerId);
				queryParam.addAttr("loginStatus", 1);
				queryParam.addAttr("startDate", recordDate);
				queryParam.setOrderBy("createTime");
				queryParam.setOrderValue("desc");
				queryParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
				if(queryResult.isSuccess() && queryResult.getRows().size() > 0){
					String createTime = StringUtil.getString(queryResult.getRow(0).get("createTime"));
					if(!StringUtils.isEmpty(createTime)){
						LocalDateTime createDate = DateUtil.toLocalDateTime(createTime, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS);
						LocalDateTime nowDate = DateUtil.toLocalDateTimeByDate(now);
						long onlineTime = DateUtil.betweenTwoTime(createDate, nowDate, ChronoUnit.MINUTES);
						String realName = StringUtil.getString(custInfo.get("realName"));
						AppParam saveParam = new AppParam("sumHandleRecordService","save");
						saveParam.addAttr("recordDate", recordDate);
						saveParam.addAttr("customerId", customerId);
						saveParam.addAttr("realName", realName);
						saveParam.addAttr("onlineTime", onlineTime);
						saveParam.setRmiServiceName(AppProperties
								.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
						RemoteInvoke.getInstance().call(saveParam);
					}
				}
			}
		}catch(Exception e){
			LogerUtil.error(StoreUserUtil.class, e, "增加在线时长统计报错 customerId=" + customerId);
		}
	}
	/**
	 * 查询首页今日统计
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> getToDayCase(AppParam params) {
		String currLoginCustId = StringUtil.getString(params.getAttr("currLoginCustId"));
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) RedisUtils.getRedisService().get(STORE_TODAY_COUNT_KEY + currLoginCustId);
		if (list == null || list.size() == 0) {
			list = refreshToDayCase(params);
		}
		return list;
	}
	
	/**
	 * 刷新首页今日统计
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> refreshToDayCase(AppParam params) {
		List<Map<String, Object>>  listMap = new ArrayList<Map<String,Object>>();
		String currLoginCustId = StringUtil.getString(params.getAttr("currLoginCustId"));
		params.setService("sumUtilExtService");
		params.setMethod("queryToDayWork");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		if(result.isSuccess() && result.getRows().size() > 0){
			listMap = result.getRows();
			Map<String, Object> map = new HashMap<String,Object>();
			map.put("queryTime", DateUtil.toStringByParttern(new Date(),DateUtil.DATE_PATTERN_YYYY_MM_DD));
			listMap.add(map);
			RedisUtils.getRedisService().set(STORE_TODAY_COUNT_KEY + currLoginCustId, (Serializable) listMap, user_cache_time);
		}
		return listMap;
	}
	
	/**
	 * 查询首页本周统计
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> getThisWeekCase(AppParam params) {
		String currLoginCustId = StringUtil.getString(params.getAttr("currLoginCustId"));
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) RedisUtils.getRedisService().get(STORE_THIS_WEEK_COUNT_KEY + currLoginCustId);
		if (list == null || list.size() == 0) {
			list = refreshThisWeekCase(params);
		}
		return list;
	}
	
	/**
	 * 刷新首页本周统计
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> refreshThisWeekCase(AppParam params) {
		List<Map<String, Object>>  listMap = new ArrayList<Map<String,Object>>();
		String currLoginCustId = StringUtil.getString(params.getAttr("currLoginCustId"));
		params.setService("sumUtilExtService");
		params.setMethod("queryThisWeekCase");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		if(result.isSuccess() && result.getRows().size() > 0){
			listMap = result.getRows();
			Map<String, Object> map = new HashMap<String,Object>();
			map.put("queryTime", DateUtil.toStringByParttern(new Date(),DateUtil.DATE_PATTERN_YYYY_MM_DD));
			listMap.add(map);
			RedisUtils.getRedisService().set(STORE_THIS_WEEK_COUNT_KEY + currLoginCustId, (Serializable) listMap, user_cache_time);
		}
		return listMap;
	}
	
	/**
	 * 查询首页本月统计
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> getToMonthCase(AppParam params) {
		String currLoginCustId = StringUtil.getString(params.getAttr("currLoginCustId"));
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) RedisUtils.getRedisService().get(STORE_TO_MONTH_COUNT_KEY + currLoginCustId);
		if (list == null || list.size() == 0) {
			list = refreshToMonthCase(params);
		}
		return list;
	}
	
	/**
	 * 刷新首页本月统计
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> refreshToMonthCase(AppParam params) {
		List<Map<String, Object>>  listMap = new ArrayList<Map<String,Object>>();
		String currLoginCustId = StringUtil.getString(params.getAttr("currLoginCustId"));
		params.setService("sumStoreBaseMonthService");
		params.setMethod("queryToMonthWork");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		if(result.isSuccess() && result.getRows().size() > 0){
			listMap = result.getRows();
			Map<String, Object> map = new HashMap<String,Object>();
			map.put("queryTime", DateUtil.toStringByParttern(new Date(),DateUtil.DATE_PATTERN_YYYY_MM_DD));
			listMap.add(map);
			RedisUtils.getRedisService().set(STORE_TO_MONTH_COUNT_KEY + currLoginCustId, (Serializable) listMap, user_cache_time);
		}
		return listMap;
	}
	
	/**
	 * 查询首页滚动系统通知
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getScrollNotifyList(AppParam params) {
		List<Map<String, Object>> list = (List<Map<String, Object>>) RedisUtils.getRedisService().get(STORE_SYS_SCROLL_NOTIFY_KEY);
		if (list == null || list.size() == 0) {
			list = refreshScrollNotifyList(params);
		}
		return list;
	}
	
	/**
	 * 刷新首页滚动系统通知
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> refreshScrollNotifyList(AppParam params) {
		List<Map<String, Object>>  listMap = new ArrayList<Map<String,Object>>();
		params.addAttr("status", "1"); //查询有效状态的通知
		params.addAttr("messNotifyType", "1"); // 1系统通知消息
		params.addAttr("isScroll", "1"); // 1滚动
		params.setService("sysNotifyService");
		params.setMethod("query");
		params.setOrderBy("notifyDate,createTime");
		params.setOrderValue("desc,desc");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		if(result.isSuccess() && result.getRows().size() > 0){
			listMap = result.getRows();
			RedisUtils.getRedisService().set(STORE_SYS_SCROLL_NOTIFY_KEY, (Serializable) result.getRows(), store_notify_cache_time);
		}
		return listMap;
	}
	
	/**
	 * 查询首页系统通知
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static AppResult getSysNotifyList(AppParam params) {
		AppResult result = new AppResult();
		List<Map<String, Object>> list = (List<Map<String, Object>>) RedisUtils.getRedisService().get(STORE_SYS_NOTIFY_KEY);
		if (list == null || list.size() == 0) {
			result = refreshSysNotifyList(params);
		}else{
			result.addRows(list);
		}
		return result;
	}
	
	/**
	 * 刷新首页系统通知
	 * 
	 * @return
	 */
	public static AppResult refreshSysNotifyList(AppParam params) {
		List<Map<String, Object>>  listMap = new ArrayList<Map<String,Object>>();
		params.setService("sysNotifyService");
		params.setMethod("queryShow");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		if(result.isSuccess() && result.getRows().size() > 0){
			listMap = result.getRows();
			int totalRecords = NumberUtil.getInt(result.getPage().getTotalRecords(),1);
			int totalPages = NumberUtil.getInt(result.getPage().getTotalPage(),1);
			Map<String, Object> map = new HashMap<String,Object>();
			map.put("totalRecords", totalRecords);
			map.put("totalPages", totalPages);
			listMap.add(map);
			RedisUtils.getRedisService().set(STORE_SYS_NOTIFY_KEY, (Serializable) listMap, store_notify_cache_time);
		}
		return result;
	}
	
	/**
	 * 查询首页系统反馈
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static AppResult getFeedBackList(AppParam params) {
		AppResult result = new AppResult();
		List<Map<String, Object>> list = (List<Map<String, Object>>) RedisUtils.getRedisService().get(STORE_SYS_FEED_BACK_KEY);
		if (list == null || list.size() == 0) {
			result = refreshFeedBackList(params);
		}else{
			result.addRows(list);
		}
		return result;
	}
	
	/**
	 * 刷新首页系统反馈
	 * 
	 * @return
	 */
	public static AppResult refreshFeedBackList(AppParam params) {
		List<Map<String, Object>>  listMap = new ArrayList<Map<String,Object>>();
		params.setService("sysFeedbackService");
		params.setMethod("queryShow");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		if(result.isSuccess() && result.getRows().size() > 0){
			listMap = result.getRows();
			int totalRecords = NumberUtil.getInt(result.getPage().getTotalRecords(),1);
			int totalPages = NumberUtil.getInt(result.getPage().getTotalPage(),1);
			Map<String, Object> map = new HashMap<String,Object>();
			map.put("totalRecords", totalRecords);
			map.put("totalPages", totalPages);
			listMap.add(map);
			RedisUtils.getRedisService().set(STORE_SYS_FEED_BACK_KEY, (Serializable) listMap, store_notify_cache_time);
		}
		return result;
	}
	
	/***
	 * 参数转换公共处理方法
	 * @param param
	 * @param customerId
	 */
	public static void dealOrderTypeParam(AppParam param,String columnName){
		// noCondition 无条件  smallLoansCount 微粒贷/小贷  carCount 车  insuranceCount 保险  replaceCount 代发  houseCount 本地房
		// rejectCallCount 未接/拒接  notOneselfCount 非本人  emptyCount 异地/空号  noneedCount 不需要  notDealCount 未了解 
		// taxesEntLoanCount 税金企业贷 accumulationFundCount 公积金标记  otherHouseCount 外地房
		if("noCondition".equals(columnName)){
			param.addAttr("dealOrderType", "0");
		}else if("smallLoansCount".equals(columnName)){
			param.addAttr("dealOrderType", "1");
		}else if("carCount".equals(columnName)){
			param.addAttr("dealOrderType", "2");
		}else if("insuranceCount".equals(columnName)){
			param.addAttr("dealOrderType", "3");
		}else if("replaceCount".equals(columnName)){
			param.addAttr("dealOrderType", "4");
		}else if("houseCount".equals(columnName)){
			param.addAttr("dealOrderType", "5");
		}else if("rejectCallCount".equals(columnName)){
			param.addAttr("dealOrderType", "6");
		}else if("notOneselfCount".equals(columnName)){
			param.addAttr("dealOrderType", "7");
		}else if("emptyCount".equals(columnName)){
			param.addAttr("dealOrderType", "8");
		}else if("noneedCount".equals(columnName)){
			param.addAttr("dealOrderType", "9");
		}else if("notDealCount".equals(columnName)){
			param.addAttr("dealOrderType", "10");
		}else if("taxesEntLoanCount".equals(columnName)){
			param.addAttr("dealOrderType", "11");
		}else if("accumulationFundCount".equals(columnName)){
			param.addAttr("dealOrderType", "12");
		}else if("otherHouseCount".equals(columnName)){
			param.addAttr("dealOrderType", "13");
		}
	}
}
