package org.xxjr.store.web.action.account.config;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.active.mq.store.StoreAppSend;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.llw.model.cache.RedisUtils;
import org.ddq.common.core.service.RemoteInvoke;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;

/**
 * 系统通知配置
 * @author Administrator
 *
 */
@Controller()
@RequestMapping("/account/config/sysNotify/")
public class SysNotifycCfgAction {
	
	/**
	 * 查询系统通知信息
	 * @param request
	 * @return
	 */
	@RequestMapping("querySysNotify")
	@ResponseBody
	public AppResult querySysNotify(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.addAttr("messNotifyType", "1"); // 1系统通知消息
			params.setService("sysNotifyService");
			params.setMethod("queryShow");
			params.setOrderBy("notifyDate,createTime");
			params.setOrderValue("desc,desc");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(params);
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "querySysNotify error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 新增系统通知
	 * @param request
	 * @return
	 */
	@RequestMapping("addSysNotify")
	@ResponseBody
	public AppResult addSysNotify(HttpServletRequest request){
		AppResult result = new AppResult();
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.addAttr("messNotifyType", "1"); // 1系统通知消息
			params.setService("sysNotifyService");
			params.setMethod("insert");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			int insertSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE),0);
			if(insertSize > 0){
				Map<String, Object> sendParam = new HashMap<String, Object>();
				String customerId = StringUtil.getString(params.getAttr("customerId"));
				String uuid = "";
				if(!StringUtils.isEmpty(customerId)){
					sendParam.put("customerId", customerId);
					sendParam.put("notifyType", "1");
					sendParam.put("cmdName", "0007"); //系统个人消息
					uuid = StringUtil.getString(RedisUtils.getRedisService().get("app" + customerId));
				}else{
					sendParam.put("notifyType", "2");
					sendParam.put("cmdName", "0007"); //系统消息
				}
				StoreAppSend  storeAppSend = SpringAppContext.getBean(StoreAppSend.class);//发送消息通知
				sendParam.put("message", params.getAttr("notifyText"));
				sendParam.put("success", "true");
				storeAppSend.sendAppMessage(uuid,"storeCmdType", sendParam);
				//删除缓存
				RedisUtils.getRedisService().del(StoreUserUtil.STORE_SYS_SCROLL_NOTIFY_KEY);
				RedisUtils.getRedisService().del(StoreUserUtil.STORE_SYS_NOTIFY_KEY);
			}
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "addSysNotify error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 删除系统通知
	 * @param request
	 * @return
	 */
	@RequestMapping("delete")
	@ResponseBody
	public AppResult delete(HttpServletRequest request){
		AppResult result = new AppResult();
		String notifyId = request.getParameter("notifyId");
		if (StringUtils.isEmpty(notifyId)) {
			result.setSuccess(false);
			result.setMessage("通知ID不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sysNotifyService");
			params.setMethod("delete");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			if(result.isSuccess()){
				//删除缓存
				RedisUtils.getRedisService().del(StoreUserUtil.STORE_SYS_SCROLL_NOTIFY_KEY);
				RedisUtils.getRedisService().del(StoreUserUtil.STORE_SYS_NOTIFY_KEY);
			}
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "deleteSysNotify error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 更新系统通知
	 * @param request
	 * @return
	 */
	@RequestMapping("updateSysNotify")
	@ResponseBody
	public AppResult updateSysNotify(HttpServletRequest request){
		AppResult result = new AppResult();
		String notifyId = request.getParameter("notifyId");
		if (StringUtils.isEmpty(notifyId)) {
			result.setSuccess(false);
			result.setMessage("通知ID不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			String orgId = request.getParameter("orgId");
			if (StringUtils.isEmpty(orgId)) {
				params.addAttr("orgId", 0);
			}
			String customerId = request.getParameter("customerId");
			if (StringUtils.isEmpty(customerId)) {
				params.addAttr("customerId", 0);
			}
			params.setService("sysNotifyService");
			params.setMethod("update");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(params);
			if(result.isSuccess()){
				//删除缓存
				RedisUtils.getRedisService().del(StoreUserUtil.STORE_SYS_SCROLL_NOTIFY_KEY);
				RedisUtils.getRedisService().del(StoreUserUtil.STORE_SYS_NOTIFY_KEY);
			}
			
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "updateSysNotify error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
