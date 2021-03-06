package org.ddq.active.mq;

import java.util.Date;
import java.util.Map;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.NetUtil;
import org.llw.mq.rabbitmq.AsyncRabbitMqTemplate;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * MQ活动引擎
 * @author Administrator
 *
 */

@Slf4j
public class XxjrMqSendUtil {
	/**MQ的的模板*/
	private static AsyncRabbitMqTemplate mqTemplate = null;
	
	/***
	 * 获取MQname
	 * @param key
	 * @return
	 */
	private static String getQname(String key) {
		String nameKey = AppProperties.getProperties(MQNames.MQ_NAME_START + key);
		if(StringUtils.isEmpty(nameKey)) {
			throw new SysException("相应的MessageKey不存在:"  + MQNames.MQ_NAME_START + key);
		}
		return nameKey;
	}
	/**
	 * 发送消息
	 * @param map
	 */
	public static void sendMessage(Map<String,Object> map, String messageKey){
		if(mqTemplate==null) {
			mqTemplate = SpringAppContext.getBean(AsyncRabbitMqTemplate.class);
		}
		try {
			mqTemplate.sendDefaultMsg(getQname(messageKey), map);
		} catch (Exception e) {
			log.error("sendMessage error:" + messageKey, e);
		}
	}
	
	/**
	 * 广播消息
	 * @param map
	 */
	public static void sendFanOutMsg(Map<String,Object> map,String messageKey){
		if(mqTemplate==null) {
			mqTemplate = SpringAppContext.getBean(AsyncRabbitMqTemplate.class);
		}
		try {
			mqTemplate.sendFanOutMsg(getQname(messageKey), map);
		} catch (Exception e) {
			log.error("sendFanOutMsg error:" + messageKey, e);
		}
	}
	
	/***
	 * MQ发送失败时保存信息
	 * @param busiName 业务名称
	 * @param map 执行参数
	 */
	public static void saveFailureLog(String busiName, Map<String,Object> map){
		AppParam mqLogParam = new AppParam();
		mqLogParam.setService("activityLogService");
		mqLogParam.setMethod("insert");
		mqLogParam.addAttr("createTime", new Date());
		// 信息类型(0  首次发送失败 1 接受处理失败)
		mqLogParam.addAttr("sendStatus", "0");
		mqLogParam.addAttr("busiName",busiName +":" + NetUtil.getInetAddress());
		mqLogParam.addAttr("param", JsonUtil.getInstance().object2JSON(map));
		try{
			RemoteInvoke.getInstance().call(mqLogParam);
		}catch(Exception e){
			LogerUtil.error(XxjrMqSendUtil.class, e, "saveFailureLog:" + JsonUtil.getInstance().object2JSON(map));
		}
	}
	
}
