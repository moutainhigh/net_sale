package org.xxjr.sys.util.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.ServiceKey;


public class MessageTemplateUtil {
	
	/***
	 * 更新所有模板信息
	 * @return
	 */
	public static List<Map<String, Object>> refreshList(){
		AppParam messageContext = new AppParam();
		messageContext.setService("msgTemplateService");
		messageContext.setMethod("query");
		AppResult messageResult =null;
		if (SpringAppContext.getBean("msgTemplateService") == null) {
			messageResult = RemoteInvoke.getInstance().call(messageContext);
		}else{
			messageResult = SoaManager.getInstance().invoke(messageContext);
		}
		
		List<Map<String, Object>> list = messageResult.getRows();
		if(messageResult.getRows().size()>0){
			RedisUtils.getRedisService().set(MessageConstants.Key_configMessageKey,
					(Serializable)list);
		}
		return list;
	}
	
	/***
	 * 获取所有的消息 模板
	 * @param isUser 当 isUser=1 返回用户可以配置发送不发送的模板
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getUseConfigMessage(Integer isUser){
		List<Map<String, Object>> list = 
				(List<Map<String, Object>>)RedisUtils.getRedisService().get(MessageConstants.Key_configMessageKey);
		if (list == null) {
			list = refreshList();
		}
		if (isUser == null) {
			return list;
		}
		List<Map<String, Object>> isUserList = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> userMap: list ){
			if(Integer.valueOf(userMap.get("isUser").toString()).intValue()  == isUser.intValue()){
				isUserList.add(userMap);
			}
		}
		return isUserList;
	}
	
	/***
	 * 获取消息模板
	 * @param messageType 模板关键字
	 * @return
	 */
	public static Map<String, Object> getMessageTemplate(String messageType) {
		List<Map<String, Object>> list = getUseConfigMessage(null);
		if (list == null) {
			return null;
		}
		for (Map<String, Object> message : list) {
			if (messageType.equals(message.get("messageType").toString())) {
				return message;
			}
		}
		return null;
	}
	
	/***
	 * 获取SMS 需要的参数
	 * @param messageKey
	 * @param telephone
	 * @param param
	 * @return
	 */
	public static AppParam getParamSMS(String messageKey,
			String telephone, Map<String, Object> param) {
		AppParam AppParam = new AppParam();
		AppParam.addAttr(MessageConstants.KEY_CELL_PHONE, telephone);
		AppParam.addAttr(MessageConstants.KEY_MESSAGE_TYPE, messageKey);
		AppParam.addAttrs(param);
		return AppParam;
	}
	
	/***
	 * 获取Email 需要的参数
	 * @param messageKey
	 * @param telephone
	 * @param param
	 * @return
	 */
	public static AppParam getParamEmail(String messageKey,
			String mailto, Map<String, Object> param) {
		AppParam AppParam = new AppParam();
		AppParam.addAttr(MessageConstants.KEY_EMAIL_TO,mailto);
		AppParam.addAttr(MessageConstants.KEY_MESSAGE_TYPE, messageKey);
		AppParam.addAttrs(param);
		return AppParam;
	}
	
	/***
	 * 根据String 获取Map
	 * @param sendParams 以 | 隔开，key=Value
	 * @return
	 */
	public static Map<String,Object> getParamByString(String sendParams){
		Map<String,Object> messParam = new HashMap<String,Object>();
		if(!StringUtils.isEmpty(sendParams)){
			for(String param:sendParams.split("&")){
				if (param.trim().length() == 0) {
					continue;
				}
				String[] paraV = param.split("=");
				String key = paraV[0];
				String value = "";
				if (paraV.length >= 2) {
					value = paraV[1];
				}
				messParam.put(key, value);
			}
		}
		return messParam;
	}
	
	/***
	 * 根据 Map获取 String
	 * @param sendParams 以 | 隔开，key=Value
	 * @return
	 */
	public static String getParamByMap(Map<String, ?> messParam) {
		StringBuffer params = null;
		if (messParam != null) {
			for(String key:messParam.keySet()){
				if (messParam.get(key) != null) {
					if (params == null) {
						params = new StringBuffer(key + "=" + messParam.get(key));
					}else{
						params.append( "&" + key + "=" + messParam.get(key));
					}
				}
			}
		}
		return params ==null ? "":params.toString();
	}
	
	
	
	/***
	 * 添加消息/***
	 * 获取message的内容
	 * @param messages  消息参数
	 * @param content 内容
	 * @return
	 */
	public static String getMessageContent(Map<String,Object> messages,
			String content) {
		if (messages != null) {
			for(String key:messages.keySet()){
				if(messages.get(key)!=null){
					content = content.replace("{" + key + "}", messages.get(key).toString());
				}
			}
		}
		return content;
	}
	
	/***
	 * 更新所有微信模板信息配置
	 * @return
	 */
	public static List<Map<String, Object>> refreshWxTemplateList(){
		AppParam messageContext = new AppParam();
		messageContext.setService("wxTemplateService");
		messageContext.setMethod("query");
		AppResult messageResult =null;
		if (SpringAppContext.getBean("wxTemplateService") == null) {
			messageResult = RemoteInvoke.getInstance().call(messageContext);
		}else{
			messageResult = SoaManager.getInstance().invoke(messageContext);
		}
		
		List<Map<String, Object>> list = messageResult.getRows();
		if(messageResult.getRows().size()>0){
			RedisUtils.getRedisService().set(MessageConstants.Key_WXTemplateConfig,
					(Serializable)list);
		}
		return list;
	}
	
	/**
	 * 获取所有微信模版
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getAllWXTemplateInfo(){
		List<Map<String, Object>> list = 
				(List<Map<String, Object>>)RedisUtils.getRedisService().get(MessageConstants.Key_WXTemplateConfig);
		if (list == null) {
			list = refreshWxTemplateList();
		}
		return list;
	}
	
	/***
	 * 根据模版消息类型获取消息模板
	 * @param messageType 模板类型
	 * @return
	 */
	public static Map<String, Object> getWxTemplateInfo(String messageType) {
		List<Map<String, Object>> list = getAllWXTemplateInfo();
		if (list == null) {
			return null;
		}
		for (Map<String, Object> message : list) {
			if (messageType.equals(message.get("messageType").toString())) {
				return message;
			}
		}
		return null;
	}
	/**
	 * 填充模板消息内容
	 * @param templateInfo
	 * @param params
	 * @return
	 */
	public static Map<String,Object> getWXTemplateContent(Map<String, Object> templateInfo, Map<String,Object> params){
		Map<String,Object> infoMap = new HashMap<String, Object>(); 
		infoMap.put("first", MessageTemplateUtil.getMessageContent(params, templateInfo.get("first").toString()));
		if(!StringUtils.isEmpty(params.get("firstOpt1"))){
			infoMap.put("first", MessageTemplateUtil.getMessageContent(params, templateInfo.get("firstOpt1").toString()));
		}
		if(!StringUtils.isEmpty(params.get("firstOpt2"))){
			infoMap.put("first", MessageTemplateUtil.getMessageContent(params, templateInfo.get("firstOpt2").toString()));
		}
		infoMap.put("keyword1", MessageTemplateUtil.getMessageContent(params, templateInfo.get("keyword1").toString()));
		infoMap.put("keyword2", MessageTemplateUtil.getMessageContent(params, templateInfo.get("keyword2").toString()));
		if(!StringUtils.isEmpty(templateInfo.get("keyword3"))){
			infoMap.put("keyword3", MessageTemplateUtil.getMessageContent(params, templateInfo.get("keyword3").toString()));
		}
		if(!StringUtils.isEmpty(templateInfo.get("keyword4"))){
			infoMap.put("keyword4", MessageTemplateUtil.getMessageContent(params, templateInfo.get("keyword4").toString()));
		}
		infoMap.put("remark", MessageTemplateUtil.getMessageContent(params, templateInfo.get("remark").toString()));
		return infoMap;
	}
	
	public static void main(String[] args){
		String strInfo = "key=12121&myInfo=23232&myValue=";
		Map<String,Object> info = getParamByString(strInfo);
		System.out.println(getParamByMap(info));
	}
}
