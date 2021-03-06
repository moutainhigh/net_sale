package org.xxjr.job.listener.mq;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.llw.mq.rabbitmq.RabbitMqConfig;
import org.springframework.util.SerializationUtils;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public abstract  class JobMqConsumer implements Consumer {
	
	private RabbitMqConfig rabbitMqConfig;
	
	private String queueName;
	
	private Channel channel;
	
	private int prefetchCount = 1;
	
	private static boolean runFlag = true;
	
	private  volatile static Connection consRabbitConnection;
	
	public abstract void onMessage(Map<String,Object> messageInfo);
	
	public JobMqConsumer(){
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
			log.error("rease Resource",e);
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
			log.error("JobMqConsumer start error:queueName :" + queueName,e);
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
			if(runFlag && messageInfo != null && !messageInfo.isEmpty()){
				onMessage(messageInfo);
			}
		}catch(Exception e){
			log.error("JobMqConsumer handleDelivery error:queueName :" + queueName, e);
		}finally{
			channel.basicAck(env.getDeliveryTag(), false);
		}
	}

	/**
	 * Called when consumer is registered.
	 */
	public void handleConsumeOk(String consumerTag) {
		//System.out.println("Consumer "+consumerTag +" registered");		
	}

	
	public void handleCancel(String consumerTag) {
		log.info("consumerTag " + consumerTag + " handleCancel");
		if(this.channel != null && runFlag){
			try {
				if(this.channel.isOpen()){
					channel.queueDeclare(queueName, true, false, false, null);
					channel.basicQos(prefetchCount);//设置同一时间最大服务转发消息数量
					channel.basicConsume(queueName, false, this);
				}
			} catch (IOException e) {
				log.error("JobMqConsumer handleCancel consumerTag :" + consumerTag, e);
			}
		}
	}
	
	public void handleCancelOk(String consumerTag) {
		
	}
	
	public void handleRecoverOk(String consumerTag) {
		
	}
	
	public void handleShutdownSignal(String consumerTag, ShutdownSignalException cause) {
		
		log.error("handleShutdownSignal  causeReason" + cause.getReason() + "isHardError=" + cause.isHardError(),cause);
		if(this.channel != null && runFlag){
			if(!this.channel.isOpen()){
				try {
					init(queueName,rabbitMqConfig);
				} catch (Exception e) {
					log.error("JobMqConsumer handleShutdownSignal :" + consumerTag, e);
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
