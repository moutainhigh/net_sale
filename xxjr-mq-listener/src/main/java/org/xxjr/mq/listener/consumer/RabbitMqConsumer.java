package org.xxjr.mq.listener.consumer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.ddq.common.util.LogerUtil;
import org.llw.mq.rabbitmq.RabbitMqConfig;
import org.springframework.util.SerializationUtils;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public abstract class RabbitMqConsumer implements Consumer{
	
	private RabbitMqConfig rabbitMqConfig;
	
	private String queueName;
	
	private Channel channel;
	
	private int prefetchCount = 1;
	
	private static boolean runFlag = true;
	
	private  volatile static Connection consRabbitConnection;
	
	public abstract void onMessage(Map<String,Object> messageInfo);
	
	public RabbitMqConsumer(){
		runFlag = true;
	}
	/**
	 * 获取rabbit 连接
	 * @return
	 * @throws IOException
	 * @throws TimeoutException
	 */
    public Connection getConn() throws IOException, TimeoutException{
    	if(consRabbitConnection == null || !consRabbitConnection.isOpen()){
    		consRabbitConnection = rabbitMqConfig.getConsRabbitConnection();
    	}
    	return  consRabbitConnection;
    	
    }
    
    /***
	 * 释放资源
	 */
	public void reaseResource() {
		try {		
			if (consRabbitConnection != null) {
				consRabbitConnection.close();
				consRabbitConnection = null;
				runFlag = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	
	}
	
	/**
	 * 初始化
	 */
	public void init(String queueName , RabbitMqConfig rabbitMqConfig){
		init(queueName, rabbitMqConfig, prefetchCount);
	}
	
	/**
	 * 初始化
	 * @param rabbitMqConfig 
	 */
	public void init(String queueName ,RabbitMqConfig rabbitMqConfig, int prefetchCount){
		this.queueName = queueName;
		this.rabbitMqConfig = rabbitMqConfig;
		try{
			channel = getConn().createChannel();
			channel.queueDeclare(queueName, true, false, false, null);
			channel.basicQos(prefetchCount);//设置同一时间最大服务转发消息数量
			channel.basicConsume(queueName, false, this);
		}catch(Exception e){
			LogerUtil.error(this.getClass(), e, "rabbitmq创建channel出错，queueName" + queueName);
		}
	}
	
	/**
	 * Called when new message is available.
	 */
	@SuppressWarnings("unchecked")
	public void handleDelivery(String consumerTag, Envelope env,
			BasicProperties props, byte[] body) throws IOException {
		
		try{
			if(!runFlag){
				channel.basicCancel(consumerTag);
				return;
			}
			Map<String,Object> messageInfo = (Map<String,Object>)SerializationUtils.deserialize(body);
			if(runFlag && messageInfo != null){
				onMessage(messageInfo);
			}
		}catch(Exception e){
			LogerUtil.error(this.getClass(), e, "rabbitmq处理消息出错，queueName" +queueName);
		}finally{
			channel.basicAck(env.getDeliveryTag(), false);
		}
	}

	/**
	 * Called when consumer is registered.
	 */
	public void handleConsumeOk(String consumerTag) {
		LogerUtil.log("consumerTag " + consumerTag + " registered");
	}
	
	public void handleCancel(String consumerTag) {
		LogerUtil.log("consumerTag " + consumerTag + " handleCancel");
		if(this.channel != null && runFlag){
			try {
				if(this.channel.isOpen()){
					channel.queueDeclare(queueName, true, false, false, null);
					channel.basicQos(prefetchCount);//设置同一时间最大服务转发消息数量
					channel.basicConsume(queueName, false, this);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void handleCancelOk(String consumerTag) {
		LogerUtil.log("consumerTag " + consumerTag + " handleCancelOk");
	}
	
	public void handleRecoverOk(String consumerTag) {
		LogerUtil.log("consumerTag " + consumerTag + " handleRecoverOk");
	}
	
	public void handleShutdownSignal(String consumerTag, ShutdownSignalException cause) {
		LogerUtil.error(this.getClass(), cause, "handleShutdownSignal  causeReason" + cause.getReason() + "isHardError=" + cause.isHardError());
		if(this.channel != null && runFlag){
			if(!this.channel.isOpen()){
				try {
					init(queueName,rabbitMqConfig);
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}
		
	}
	

	public String getQueueName() {
		return queueName;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
}
