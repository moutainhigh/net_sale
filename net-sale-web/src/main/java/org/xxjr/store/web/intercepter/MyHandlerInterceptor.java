package org.xxjr.store.web.intercepter;


import java.io.Serializable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.ThreadLogUtil;

/***
 * handler page Interceptor
 * 
 * @author Administrator
 * 
 */
public class MyHandlerInterceptor extends HandlerInterceptorAdapter {
	private static Logger log = LogManager
			.getLogger(MyHandlerInterceptor.class);
	
	//特殊Action
	private String exceptActions = "/account/";
	//服务器名称
	private  String serverName;

	public String getServerName() {
		if (this.serverName == null) {
			return "default";
		}
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getExceptActions() {
		return exceptActions;
	}

	public void setExceptActions(String exceptActions) {
		this.exceptActions = exceptActions;
	}

	/***
	 * 判断是否需要登录
	 * @param url
	 * @return
	 */
	private boolean needLogin(String url) {
		if (StringUtils.isEmpty(exceptActions)) {
			return false;
		}
		for (String action : exceptActions.split(",")) {
			if (url.indexOf(action) >= 0) {
				return true;
			}
		}
		return false;
	}


	@Override
	public boolean preHandle(HttpServletRequest request,HttpServletResponse response, Object handler)  {
		String url = request.getRequestURI();
		String baseUrl = url;
		// 判断是否需要登录
		boolean needLogin =this.needLogin(url);//false;
		String signId = request.getParameter("signId");
		String params = RequestUtil.getRequestParams(request, true);
		String ip = DuoduoSession.getIpAddress(request);
		if(log.isInfoEnabled()){
			log.info("reuqest url:" + url+"," + ip +"," + params);
		}
		Map<String,Object> custInfo = null;
		// 需要登录
		if (needLogin) {
			url = StringUtils.substringBetween(url, "/account/", "/");
			if (StringUtils.isEmpty(signId)) {
				response.setHeader("signId", "needLogin");
				return false;
			}
			request.setAttribute("signId", signId);
			Object userId = RedisUtils.getRedisService().get(signId);
			if (userId == null || StringUtils.isEmpty((String) userId)) {
				response.setHeader("signId", "needLogin");// 需要登录
				//存日志
				DuoduoSession.setMessage("offline needLogin");
				this.setAndsave(baseUrl, params, ip, signId, custInfo, request);
				return false;
			}

			if (userId != null) {
				String oldSinId = (String) RedisUtils.getRedisService().get(StoreUserUtil.USER_KEY +userId);
				if (!signId.equals(oldSinId)) {
					response.setHeader("signId", "offline");// 已在其他地方登录
					//存日志
					DuoduoSession.setMessage("offline other login");
					this.setAndsave(baseUrl, params, ip, signId, custInfo, request);
					return false;
				}
				
				if(baseUrl.indexOf("/account/user/notify/queryNotifyAllList") < 0
						&& baseUrl.indexOf("/account/work/applyInfo/saveAllInfo") < 0 
						&& baseUrl.indexOf("/account/work/waitDeal/queryNewApplay") < 0
						&& baseUrl.indexOf("/account/work/againAllot/queryAgainAllot") < 0
						&& baseUrl.indexOf("/account/work/waitRecognized/queryWaitRecognCount") < 0){//定时请求通知过滤
					//设置用户缓存
					RedisUtils.getRedisService().set(signId, (Serializable) userId,StoreUserUtil.user_cache_time);//signId
					RedisUtils.getRedisService().set(StoreUserUtil.USER_KEY +userId, signId,StoreUserUtil.user_cache_time);//signId
				}
				custInfo = CustomerIdentify.getCustIdentify(userId.toString());
				// 设置用户信息到request中
				CustomerUtil.setExistsSession(custInfo, request);
				DuoduoSession.web2Service(request);
				
				//设置头部signId
				response.setHeader("signId", signId);
			}
			
			boolean hasRole = StoreUserUtil.validateURL(url, (String) userId,baseUrl);
			if (!hasRole) {
				response.setHeader("signId", "noRole");//没有权限
				//存日志
				DuoduoSession.setMessage("noRole is null");
				this.setAndsave(url, params, ip, signId, custInfo, request);
				return false;
			}
		}else{
			DuoduoSession.web2Service(request);
		}
		// 设置日志（新）
		ThreadLogUtil.setLogParam(baseUrl, params, ip, signId, custInfo, request);

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

	}

	
	@Override
	public void afterCompletion(javax.servlet.http.HttpServletRequest request,
			javax.servlet.http.HttpServletResponse response,
			java.lang.Object handler, java.lang.Exception ex) {
		try {
			//保存日志信息
			//ThreadLogUtil.addLogByNewThread(DuoduoSession.getShowLog(),ThreadLogEnum.STORE_LOG);
			// 清除threadlocal信息
			DuoduoSession.clearLocalData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/***
	 * 保存和修改数据
	 * @param url
	 * @param params
	 * @param ip
	 * @param signId
	 * @param custInfo
	 * @param request
	 */
	private void setAndsave(String url,String params,String ip,
			String signId,Map<String, Object> custInfo,HttpServletRequest request){
		//记录日志为出错
		DuoduoSession.setStatus("2");
		//清除数据
		DuoduoSession.clearLocalData();
	}
}
