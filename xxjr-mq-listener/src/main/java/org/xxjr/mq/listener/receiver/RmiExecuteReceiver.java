package org.xxjr.mq.listener.receiver;

import java.util.Map;

import javax.annotation.PreDestroy;

import org.ddq.active.mq.XxjrMqSendUtil;
import org.ddq.common.context.AppParam;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.mq.rabbitmq.RabbitMqConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xxjr.mq.listener.consumer.RabbitMqConsumer;
import org.xxjr.mq.listener.util.XxjrInitAnnotation;


/***
 * 执行远程服务指定方法
 * @author fangrq
 *
 */
@Component
@XxjrInitAnnotation(beanName="rmiExecuteReceiver",initMethod="init")
public class RmiExecuteReceiver extends RabbitMqConsumer{
	
	@Autowired
	private RabbitMqConfig rabbitMqConfig;
	
	@Value("${rabbit.queue.rmiService}")
	private String queueName;

	public void onMessage(Map<String, Object> param) {
		try {
			LogerUtil.log("execute params:" + param.toString());
			AppParam appParam = new AppParam();
			for (String name : param.keySet()) {
				if(StringUtils.isEmpty(name)){
					continue;
				}
				
				Object value = param.get(name);
				if ("service".equals(name)) {
					appParam.setService(StringUtil.getString(value));
				} else if ("method".equals(name)) {
					appParam.setMethod(StringUtil.getString(value));
				} else if ("rmiServiceName".equals(name)) {
					appParam.setRmiServiceName(StringUtil.getString(value));
				} else {
					appParam.addAttr(name, value);
				}
			}
			if(StringUtils.isEmpty(appParam.getService()) 
					|| StringUtils.isEmpty(appParam.getMethod())){
				return;
			}
			RemoteInvoke.getInstance().call(appParam);
		} catch (Exception e) {
			LogerUtil.error(RmiExecuteReceiver.class, e, "MqExecuteReceiver execute error!");
			XxjrMqSendUtil.saveFailureLog("MqExecuteReceiver", param);
		}
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
