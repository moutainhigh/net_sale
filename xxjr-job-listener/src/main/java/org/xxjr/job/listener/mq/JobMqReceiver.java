package org.xxjr.job.listener.mq;

import java.util.Map;

import javax.annotation.PreDestroy;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.StringUtil;
import org.llw.job.core.BaseJob;
import org.llw.job.core.ScheduleManager;
import org.llw.job.util.JobUtil;
import org.llw.mq.rabbitmq.RabbitMqConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xxjr.sys.util.ServiceKey;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JobMqReceiver extends JobMqConsumer{
	@Autowired
	private RabbitMqConfig rabbitMqConfig;
	
	@Value("${rabbit.queue.resetJob}")
	private String queueName;
	
	@Override
	public void onMessage(Map<String, Object> messageInfo) {
		log.info("JobMqReceiver params:" + messageInfo.toString());
		try {
			String messageType = StringUtil.getString(messageInfo.get("messageType"));
			log.info("messageType:" + messageType);
			if("jobMqType".equals(messageType)){ //job重置
				dealJobMessage(messageInfo);
			}else if("executeQuick".equals(messageType)){
				executeQuickJob(messageInfo);
			}
		} catch (Exception e) {
			log.error("SeverCallReceiver mq execute error", e);
		}
	}
	
	/***
	 * 处理MQ操作
	 * @param messageInfo
	 */
	private static void dealJobMessage(Map<String, Object> messageInfo) {
		AppParam param = new AppParam();
		param.setService("jobService");
		param.setMethod("query");
		param.addAttr("enable", 1);
		param.addAttr("jobId", messageInfo.get("jobId"));
		param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_cust));
		AppResult resultJob = RemoteInvoke.getInstance().call(param);
		if(resultJob.getRows().size() > 0){
			Map<String,Object> jobInfo = resultJob.getRow(0);
			BaseJob baseJob = JobUtil.getBaseJob(jobInfo);
			try {
				//bean找不到，不做处理
				Object bean = SpringAppContext.getBean(baseJob.getJobInfo().getDealBean());
				if (bean == null) {
					log.error("Schedule job:" + jobInfo.get("jobName") 
							+ " not found bean:" + baseJob.getJobInfo().getDealBean());
				}
				ScheduleManager.getInstance().addAndScheduleJob(baseJob);
			} catch (Exception e) {
				log.error("Schedule job:" + 
			baseJob.getJobInfo().toString() + " is error!", e);
			}
		}
		
	}
	
	/***
	 * 立即执行任务
	 * @param messageInfo
	 */
	private static void executeQuickJob(Map<String, Object> messageInfo) {
		AppParam param = new AppParam();
		param.setService("jobService");
		param.setMethod("query");
		param.addAttr("enable", 1);
		param.addAttr("jobId", messageInfo.get("jobId"));
		param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_cust));
		AppResult resultJob = RemoteInvoke.getInstance().callNoTx(param);
		if(resultJob.getRows().size() > 0){
			Map<String,Object> jobInfo = resultJob.getRow(0);
			BaseJob baseJob = JobUtil.getBaseJob(jobInfo);
			try {
				//bean找不到，不做处理
				Object bean = SpringAppContext.getBean(baseJob.getJobInfo().getDealBean());
				if (bean == null) {
					log.error("Schedule job:" + jobInfo.get("jobName") 
							+ " not found bean:" + baseJob.getJobInfo().getDealBean());
				}
				ScheduleManager.getInstance().executeQuick(baseJob);
			} catch (Exception e) {
				log.error("Schedule job:" + baseJob.toString() + " is error!", e);
			}
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

