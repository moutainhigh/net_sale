package org.llw.job.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.LogerUtil;
import org.llw.job.util.JobUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class ScheduleManager {
	
	@Autowired
	private ThreadPoolTaskScheduler threadPoolTaskScheduler;
	
	/**保存系统中的 运行记录**/
	private ConcurrentMap<String,ScheduledFuture<?>> futures = 
			new ConcurrentHashMap<String,ScheduledFuture<?>>();
	
	@Bean
	public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		return new ThreadPoolTaskScheduler();
	}
	
	private static ScheduleManager scheduleManager =null;
	
	
	/***
	 * 初始化执行任务
	 */
	public void initSchedule(){
		AppParam param = new AppParam();
		param.setService("jobService");
		param.setMethod("query");
		param.addAttr("enable", 1);
		AppResult jobList = RemoteInvoke.getInstance().callNoTx(param);
		for(Map<String, Object> jobInfo:jobList.getRows()){
			BaseJob baseJob = JobUtil.getBaseJob(jobInfo);
			try {
				//bean找不到，不做处理
				Object bean = SpringAppContext.getBean(baseJob.getJobInfo().getDealBean());
				if (bean == null) {
					LogerUtil.error(ScheduleManager.class,"Schedule job:" + jobInfo.get("jobName") 
							+ " not found bean:" + baseJob.getJobInfo().getDealBean());
					continue;
				}
				ScheduleManager.getInstance().addAndScheduleJob(baseJob);
			} catch (Exception e) {
				LogerUtil.error(ScheduleManager.class,e,"Schedule job:"
						+ baseJob.getJobInfo().getDealBean() + " is error!");
			}
		}
	}

	public static ScheduleManager getInstance() {
		if (scheduleManager == null) {
			scheduleManager = SpringAppContext.getBean(ScheduleManager.class);
			
		}
		return scheduleManager;
	}

	private ScheduleManager() {
		
	}
	
	
	/***
	 * 停止原来的任务
	 * @param key
	 * @return
	 */
	public boolean stopCron(String key) {
		if (futures.get(key) != null) {
			// 取消定时任务
			return futures.get(key).cancel(true);
		}else {
			LogerUtil.log("not found job key:" + key);
			//不存在，直接返回true
			return true;
		}
	}
	
	
	/**
	 * 添加JoB
	 * @param jobEntry
	 */
	public synchronized void addAndScheduleJob(BaseJob baseJob) throws Exception {
		if (baseJob == null || baseJob.getJobInfo().getDealBean()==null) {
			throw new SysException("配置的规则不能为空！");
		}
		String jobId = baseJob.getJobInfo().getJobId().toString();
		if(futures.get(jobId)!=null) {
			futures.get(jobId).cancel(true);
		}
		ScheduledFuture<?> future = threadPoolTaskScheduler.schedule(
				baseJob,
				JobUtil.getTrigger(baseJob));
		futures.put(jobId, future);
	}
	
	/**
	 * 添加JoB立即执行
	 * @param jobEntry
	 */
	public synchronized void executeQuick(BaseJob baseJob) throws Exception {
		String jobId = baseJob.getJobInfo().getJobId().toString();
		//取消原来的执行计划
		if(futures.get(jobId)!=null) {
			futures.get(jobId).cancel(true);
			futures.remove(jobId);
		}
		ScheduledFuture<?> future = threadPoolTaskScheduler.schedule(
				baseJob,
				JobUtil.getExecuteNow());
		//添加执行计划
		futures.put(baseJob.getJobInfo().getJobId().toString(), future);
	}
	

	/***
	 * 删除JOB
	 * remove Job
	 * 
	 * @param jobkey
	 * @throws SchedulerException
	 */
	public void removeJob(String jobId) {
		//取消原来的执行计划
		if(futures.get(jobId)!=null) {
			futures.get(jobId).cancel(true);
			futures.remove(jobId);
		}
	}

	/***
	 * 判断job是否存在
	 * checkExists
	 * 
	 * @param jobkey
	 * @throws SchedulerException
	 */
	public boolean checkExists(String jobId) {
		return futures.get(jobId)!=null;
	}
	
	
	/***
	 * 关闭处理
	 * shutdown
	 * @throws SchedulerException
	 */
	public void shutdown() {
		threadPoolTaskScheduler.destroy();
	}
}
