package org.xxjr.mq.listener.receiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.ddq.active.mq.XxjrMqSendUtil;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;
import org.llw.mq.rabbitmq.RabbitMqConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.mq.listener.consumer.RabbitMqConsumer;
import org.xxjr.mq.listener.util.XxjrInitAnnotation;
import org.xxjr.sms.SendSmsByUrl;
import org.xxjr.sms.SmsJuheSendUtil;
import org.xxjr.sms.SmsTenXunSendUtil;
import org.xxjr.sys.util.SmsConfigUtil;
import org.xxjr.sys.util.SysParamsUtil;
import org.xxjr.sys.util.ValidUtils;
import org.xxjr.sys.util.message.MessageConstants;
import org.xxjr.sys.util.message.MessageTemplateUtil;

@Component
@XxjrInitAnnotation(beanName="custMessageReceiver",initMethod="init")
public class CustMessageReceiver extends RabbitMqConsumer {
	
	@Autowired
	private RabbitMqConfig rabbitMqConfig;
	
	@Value("${rabbit.queue.custMessage}")
	private String queueName;

	@SuppressWarnings("unchecked")
	public void onMessage(Map<String, Object> messageInfo) {
		try {
			LogerUtil.log("CustMessageReceiver params:" + messageInfo.toString());
			String messageType = (String) messageInfo.get("messageType");
			Map<String, Object> messageTemplate = MessageTemplateUtil.getMessageTemplate(messageType);
			if(messageTemplate==null){
				LogerUtil.error(CustMessageReceiver.class, "not found messageType:" + messageType);
				return;
			}
			
			int flagWx = Integer.valueOf(messageTemplate
					.get("flagWx") ==null ? "0" : messageTemplate.get("flagWx").toString());
			int flagWxTemplate = Integer.valueOf(messageTemplate
					.get("flagWxTemplate") ==null ? "0" : messageTemplate.get("flagWxTemplate").toString());
			int flagSms = Integer.valueOf(messageTemplate
					.get("flagSms")==null ? "0" : messageTemplate.get("flagSms").toString());
			int flagEmail = Integer.valueOf(messageTemplate
					.get("flagEmail") ==null ? "0" : messageTemplate.get("flagEmail").toString());
			int flagMessage = Integer.valueOf(messageTemplate
					.get("flagMessage") ==null ? "0" : messageTemplate.get("flagMessage").toString());
			int isUser = Integer.valueOf(messageTemplate
					.get("isUser") ==null ? "0" : messageTemplate.get("isUser").toString());
			Object customerId = messageInfo.get("customerId");
			String content = (String) messageTemplate.get("content");
			String title = (String) messageTemplate.get("title");
			String emailContent = (String) messageTemplate.get("emailContent");
			Object openid =null;//相庆的openid
			Object gzhId =null;//公众号
			Map<String,Object> params = (Map<String, Object>) messageInfo.get("params"); //使用的参数信息
			if(params==null){
				params = new HashMap<String,Object>();
			}
			if (customerId != null) {
				Map<String,Object> customerInfo  = CustomerIdentify.getCustIdentify(customerId.toString());
				openid = customerInfo.get("openid");
				gzhId = StringUtils.isEmpty(customerInfo.get("gzhId"))? "11":customerInfo.get("gzhId").toString();
				
				params.putAll(customerInfo);
				params.put("customerId", customerId);
			}
			content = MessageTemplateUtil.getMessageContent(params, content);
			//微信发送信息
			if (flagWx == 1 && flagWxTemplate == 0 && !StringUtils.isEmpty(openid)) {
				sendWxMessage(openid, gzhId, content, messageInfo);
			}
			
//			if (flagWxTemplate == 1 && !StringUtils.isEmpty(openid)) {
//				sendWxTemplateMessage(openid, gzhId, messageType, params);
//			}
			//发送sms
			if (flagSms == 1) {
				sendHttpSMS(params.get("telephone").toString(), 
						StringUtils.isEmpty(messageTemplate.get("smsContent")) ? content
								: MessageTemplateUtil.getMessageContent(params,
										(String) messageTemplate
												.get("smsContent")), params,messageTemplate);
			}
			//发送email
			if (flagEmail == 1) {
				emailContent = MessageTemplateUtil.getMessageContent(params, content);
				sendHttpEmail(params.get("email"), title, emailContent, messageInfo);
			}
			//发送站内信
			if (flagMessage == 1 && customerId != null) {
				sendCustMessage(
					StringUtils.isEmpty(messageTemplate.get("smsContent"))
							? content:
								MessageTemplateUtil.getMessageContent(params, 
										(String)messageTemplate.get("smsContent")), messageType, title, params);
			}
			if(isUser ==1  && customerId != null){
				sendAdmminMessage((String)messageTemplate.get("userContent"), messageType, title, params);
			}
		} catch (Exception e) {
			LogerUtil.error(CustMessageReceiver.class, e, "CustMessageReceiver mq execute error!");
			XxjrMqSendUtil.saveFailureLog("CustMessageReceiver", messageInfo);
		}

	}
		
	/***
	 * 发送微信通知
	 * @param openid
	 * @param gzhId
	 * @param content
	 * @param messageInfo
	 * @return
	 */
	private static boolean sendWxMessage(Object openid,Object gzhId,String content,Map<String, Object> messageInfo ){
		AppParam mqParam = new AppParam();
		mqParam.setService("sendMqMessageService");
		mqParam.setMethod("sendTextMessage");
		mqParam.addAttr("content", content);
		mqParam.addAttr("openid", openid);
		mqParam.addAttr("gzhId", gzhId);
		mqParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "wx"));
		try {
			
			if (SpringAppContext.getBean("sendMqMessageService") != null) {
				SoaManager.getInstance().invoke(mqParam);;
			}else{
//				mqParam.setRmiServiceName(AppProperties
//						.getProperties(DuoduoConstant.RMI_SERVICE_START +
//								ServiceKey.Key_wx));
				RemoteInvoke.getInstance().call(mqParam);
			}
			return true;
		} catch (Exception e1) {
			LogerUtil.error(CustMessageReceiver.class, e1, "CustMessageReceiver sendWxMessage execute error!");
			XxjrMqSendUtil.saveFailureLog("sendWxMessage", messageInfo);
		}
		return false;
	}
	
	/**
	 * 发送微信模版消息
	 * @param openid
	 * @param gzhId
	 * @param messageType
	 * @param messageInfo
	 * @return
	 */
//	private static boolean sendWxTemplateMessage(Object openid, Object gzhId, String messageType, Map<String, Object> messageInfo){
//		Map<String, Object> templateInfo = MessageTemplateUtil.getWxTemplateInfo(messageType);
//		if(templateInfo == null){
//			return false;
//		}
//		Map<String,Object> paramMap = MessageUtil.getWXTemplateContent(templateInfo, messageInfo);
//
//		Map<String,Object> msgparams = new HashMap<String, Object>(); 
//		msgparams.put("paramMap", paramMap);
//		msgparams.put("url", MessageTemplateUtil.getMessageContent(messageInfo, StringUtil.getString(templateInfo.get("templateUrl"))));
//		msgparams.put("templateId", templateInfo.get("templateId"));
//		msgparams.put("openid", openid);
//		msgparams.put("gzhId", gzhId);
//		
//		AppParam mqParam = new AppParam();
//		mqParam.setService("sendMqMessageService");
//		mqParam.setMethod("sendTemplateMessage");
//		mqParam.addAttrs(msgparams);
//		mqParam.setRmiServiceName(AppProperties
//				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_wx));
//		try {
//			if (SpringAppContext.getBean("sendMqMessageService") != null) {
//				SoaManager.getInstance().invoke(mqParam);;
//			}else{
//				mqParam.setRmiServiceName(AppProperties
//						.getProperties(DuoduoConstant.RMI_SERVICE_START +
//								ServiceKey.Key_wx));
//				RemoteInvoke.getInstance().call(mqParam);
//			}
//			return true;
//		} catch (Exception e1) {
//			LogerUtil.error(CustMessageReceiver.class, e1, "CustMessageReceiver sendWxTemplateMessage execute error!");
//			XxjrMqSendUtil.saveFailureLog("sendWxTemplateMessage", messageInfo);
//		}
//		return false;
//	}
	
	/***
	 * 发送短信通知
	 * @param telephone
	 * @param content
	 * @param messageInfo
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static boolean sendHttpSMS(String telephone,String content,Map<String, Object> messageInfo,Map<String,Object> templateInfo){
		boolean isSendSucc = false;
		try{
			// 半小时之内同一手机号仅发一次短信
			String sendFlag = (String)RedisUtils.getRedisService().get("isSendSms" + telephone);
			if("1".equals(sendFlag)){
				return true;
			}
			// 模板ID|appid|appkey|短信签名|短信厂商
			String templateIdStr = StringUtil.getString(templateInfo.get("templateId"));
			if(!StringUtils.isEmpty(templateIdStr)){
				
				String[] tempParams = templateIdStr.split("\\|");
				if(tempParams.length == 5){
					String tempId = tempParams[0];
					String appid = tempParams[1];
					String appkey = tempParams[2];
					String signName = tempParams[3];
					String smsType = tempParams[4];
					String params = StringUtil.getString(templateInfo.get("params"));
					String paramVals = MessageTemplateUtil.getMessageContent(messageInfo, params);
					if("TengXunYun".equalsIgnoreCase(smsType)){//腾讯云
						String[] paramArr  = org.apache.commons.lang.StringUtils.stripAll(paramVals.split(",|，"));
						ArrayList<String> paramList = new ArrayList();
						Collections.addAll(paramList, paramArr);
						AppResult sendResult = SmsTenXunSendUtil.
								sendTplSms(Integer.parseInt(appid), appkey, signName, telephone, 
										tempId, paramList);
						isSendSucc = sendResult.isSuccess();
					}
					
					if("JuHe".equalsIgnoreCase(smsType)){//聚合
						AppResult sendResult = SmsJuheSendUtil.sendSmsText(telephone, paramVals,
								tempId , appkey);
						isSendSucc = sendResult.isSuccess();
					}

				}
			
			}else{
				String smsUrl = SmsConfigUtil.getSMSInfo(content, telephone);
				AppResult smsResult = SendSmsByUrl.sendSMSInfo(smsUrl, content, telephone, null);
				isSendSucc = smsResult.isSuccess();
			}

			if(isSendSucc){
				RedisUtils.getRedisService().set("isSendSms" + telephone, "1", 60 * 30);
			}
		} catch (Exception e1) {
			LogerUtil.error(CustMessageReceiver.class, e1, "sendHttpSMS execute error!");
			XxjrMqSendUtil.saveFailureLog("sendHttpSMS", messageInfo);
		}
		return isSendSucc;
	}
	
	/***
	 * 发送邮件
	 * @param email
	 * @param title
	 * @param emailConent
	 * @param messageInfo
	 * @return
	 */
	private static boolean sendHttpEmail(Object email,String title,String emailConent,Map<String, Object> messageInfo){
		try{
			if(!StringUtils.isEmpty(email) && ValidUtils.validateEmail(email.toString())){
				//EmailSendUtil.sendHtmlMail(email.toString(), title, emailConent,(String) messageInfo.get("messageType"));
				return true;
			}
		} catch (Exception e1) {
			LogerUtil.error(CustMessageReceiver.class, e1, "sendHttpEmail execute error!");
			XxjrMqSendUtil.saveFailureLog("sendHttpEmail", messageInfo);
		}
		return false;
	}
	
	/**
	 *添加用户消息
	 * @param content 消息内容
	 * @param messageType 消息类型
	 * @param title 标题
	 * @param param 参数
	 * @return
	 */
	private static AppResult sendCustMessage(String content ,String messageType,String title,Map<String, Object> param) {
		
		AppParam messageInfo  = new AppParam();
		param.put("createDate", DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM));
		messageInfo.addAttrs(param);
				
		//添加站内信息
		AppParam addMessage  = new AppParam();
		addMessage.setMethod("insert");
		addMessage.setService("custMessageService");
		addMessage.addAttr("msgTo", param.get("userName"));
		addMessage.addAttr("customerId", param.get("customerId"));
		addMessage.addAttr("msgFrom", param.get("sendBy")==null?"admin":"小小金融");
		addMessage.addAttr("sendTime", new Date());
		addMessage.addAttr("subject", title);
		addMessage.addAttr("content", content);
		addMessage.addAttr("flag", MessageConstants.messageFlag_1);
		addMessage.addAttr("sendFlag", "1");
		addMessage.addAttr("sendParams", "");
		addMessage.addAttr("messageType", messageType);
		AppResult result = new AppResult();
		try{
			if (SpringAppContext.getBean("custMessageService") == null) {
				addMessage.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
				result = RemoteInvoke.getInstance().call(addMessage);
			}else{
				result = SoaManager.getInstance().invoke(addMessage);
			}
		}catch(Exception e){
			LogerUtil.error(CustMessageReceiver.class, e, "sendCustMessage execute error!");
			XxjrMqSendUtil.saveFailureLog("sendCustMessage", param);
		}
		return result;
	}
	/**
	 * 添加管理员消息
	 * @param content 消息内容
	 * @param messageType 消息类型
	 * @param title 标题
	 * @param param 参数
	 * @return
	 */
	private static AppResult sendAdmminMessage(String content ,String messageType,String title,Map<String, Object> param) {
		
		AppParam messageInfo  = new AppParam();
		param.put("createDate", DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM));
		messageInfo.addAttrs(param);
		
		//添加站内信息
		AppParam addMessage  = new AppParam();
		addMessage.setMethod("insert");
		addMessage.setService("userMessageService");
		addMessage.addAttr("msgTo", param.get("userName"));
		addMessage.addAttr("userId", param.get("userId")==null?SysParamsUtil.getStringParamByKey("adminUser", "1"):
			param.get("userId"));
		addMessage.addAttr("msgFrom", param.get("sendBy")==null?"admin":"小小金融");
		addMessage.addAttr("sendTime", new Date());
		addMessage.addAttr("subject", title);
		addMessage.addAttr("content", content);
		addMessage.addAttr("flag", MessageConstants.messageFlag_1);
		addMessage.addAttr("sendFlag", "1");
		addMessage.addAttr("sendParams", "");
		addMessage.addAttr("messageType", messageType);
		AppResult result = new AppResult();
		try{
			if (SpringAppContext.getBean("userMessageService") == null) {
				addMessage.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + "sys"));
				result = RemoteInvoke.getInstance().call(addMessage);
			}else{
				result = SoaManager.getInstance().invoke(addMessage);
			}
		}catch(Exception e){
			LogerUtil.error(CustMessageReceiver.class , e, "sendAdmminMessage execute error!");
			XxjrMqSendUtil.saveFailureLog("sendAdmminMessage", param);
		}
		return result;
	}

	@Override
	public void init(String queueName ,RabbitMqConfig rabbitMqConfig) {
		super.init(queueName,rabbitMqConfig);
	}
	
    public void init() {
		init(queueName,rabbitMqConfig);
	}
	
	@PreDestroy
	public void destroy(){
		reaseResource();
	}
	
}
