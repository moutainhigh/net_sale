package org.llw.job.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateTimeUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.NumberUtil;
import org.ddq.common.util.PropertiesXXUtils;
import org.llw.job.core.BaseJob;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.StringUtils;

public class JobUtil {
	
	/***
	 * 根据jobTable内容获取BaseJob
	 * @param jobInfo
	 * @return
	 */
	public static BaseJob getBaseJob(Map<String,Object> jobInfo){
		JobParamMo jobParam =new JobParamMo(
				NumberUtil.getLong(jobInfo.get(JobConstant.KEY_JOBID)),
				(String)jobInfo.get(JobConstant.KEY_TYPE_CODE),
				Integer.valueOf(jobInfo.get(JobConstant.KEY_EXECUTE_TYPE).toString()),
				jobInfo.get(JobConstant.KEY_EXECUTE_TIME) == null ? null :
					DateTimeUtil.toDateByString(jobInfo.get(JobConstant.KEY_EXECUTE_TIME).toString(), 
							DateTimeUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS),
				Integer.valueOf(jobInfo.get(JobConstant.KEY_END_TYPE).toString()),
				jobInfo.get(JobConstant.KEY_END_DATE) == null ? null :
					DateTimeUtil.toDateByString(jobInfo.get(JobConstant.KEY_END_DATE).toString(), 
							DateTimeUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS),
				(String)jobInfo.get(JobConstant.KEY_RECURRENT_VALUE),
				(String)jobInfo.get(JobConstant.KEY_UPDATE_BY),
				(String)jobInfo.get(JobConstant.KEY_jobParams));
		
		BaseJob baseJob = new BaseJob(jobParam);
		return baseJob;
	}
	
	
	/***
	 * 添加处理明细
	 * @param processId
	 * @param executeDesc
	 */
	public static void addProcessExecute(Object processId,String executeDesc){
		AppParam context = new AppParam();
		context.addAttr("processId", processId);
		context.addAttr("executeTime", new Date());
		context.addAttr("executeDesc", executeDesc);
		context.setService("jobExecuteService");
		context.setMethod("insert");
		try{
			context.setRmiServiceName(PropertiesXXUtils.getInstance().getProperty(
					DuoduoConstant.REMOTE_SERVICE_START+DuoduoConstant.REMOTE_SERVICE_ADMIN));
			
			RemoteInvoke.getInstance().call(context);
			
		}catch(Exception e){
			LogerUtil.error(JobUtil.class,e,"Add process execute ");
		}
	}
	/**
	 * 添加任务锁
	 * @param paramMo
	 * @return
	 */
	public static AppResult insertLock(JobParamMo paramMo ){
		AppParam context = new AppParam();
		context.addAttr(JobConstant.KEY_businessId, paramMo.getJobId());
		context.addAttr(JobConstant.KEY_businessType, paramMo.getDealBean());
		context.addAttr(JobConstant.KEY_startTime, new Date());
		context.setService("jobLockService");
		context.setMethod("insert");
		try{
			AppResult result = RemoteInvoke.getInstance().call(context);
			return result;
		}catch(Exception e){
			LogerUtil.error(JobUtil.class,e,"insertLock error");
		}
		return new AppResult();
	}

	/***
	 * 获取job Trigger
	 * @param baseJob
	 * @return
	 */
	public static Trigger getTrigger(BaseJob baseJob){
		JobParamMo jobInfo = baseJob.getJobInfo();
		int executeType  = jobInfo.getExecuteType();
		Trigger trigger = null;
		switch(executeType){
			//立即执行
			case JobConstant.JOB_EXECUTE_TYPE_0:
				trigger = getExecuteNow();
				break;
			//执行一次	
			case JobConstant.JOB_EXECUTE_TYPE_1:
				trigger = getExecuteDate(jobInfo.getExecuteDate());
				break;
			case JobConstant.JOB_EXECUTE_TYPE_2:
				trigger = getExecuteCrons(jobInfo.getCrons(),jobInfo.getJobId());
				break;
		}
		return trigger;
	}
	/**
	 * 立即 执行
	 * @return
	 */
	public static Trigger getExecuteNow() {
		return  new Trigger() {
			@Override
			public Date nextExecutionTime(TriggerContext triggerContext) {
				return new Date();
			}
		};
	}
	/**
	 * 固定时间执行
	 * @return
	 */
	public static Trigger getExecuteDate(Date executeDate) {
		return  new Trigger() {
			@Override
			public Date nextExecutionTime(TriggerContext triggerContext) {
				return executeDate;
			}
		};
	}
	/**
	 * 循环执行
	 * @param crons
	 * @param jobId
	 * @return
	 */
	public static Trigger getExecuteCrons(String crons,Long jobId) {
		return  new Trigger() {
			@Override
			public Date nextExecutionTime(TriggerContext triggerContext) {
				// 定时任务触发，可修改定时任务的执行周期
				CronTrigger trigger = new CronTrigger(crons);
				Date nextExecDate = trigger.nextExecutionTime(triggerContext);
				LogerUtil.log("job:" + jobId + " nextExecDate:" +
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nextExecDate));
				return nextExecDate;
			}
		};
	}
	/***
	 * 修改运行状态
	 * @param paramMo
	 * @param executeStatus
	 * @return
	 */
	public static AppResult inertProcessInfo(JobParamMo paramMo,int executeStatus){
		AppParam context = new AppParam();
		context.addAttr(JobConstant.KEY_JOBID, paramMo.getJobId());
		context.addAttr(JobConstant.KEY_CREATE_BY, paramMo.getCreateBy());
		context.addAttr(JobConstant.KEY_executeStatus, executeStatus);
		context.addAttr(JobConstant.KEY_startTime, new Date());
		context.addAttr(JobConstant.KEY_notifyStatus, JobConstant.Notify_Status_1);
		context.setService("jobProcessService");
		context.setMethod("insert");
		AppResult result =RemoteInvoke.getInstance().call(context);;
		return result;
	}
	/***
	 * 修改任务状态
	 * @param paramMo 参数
	 * @param status 当前状态
	 * @param lastStatus 上次执行状态
	 */
	public static void updateJobStatus(Long jobId,int status,int lastStatus){
		AppParam context = new AppParam();
		context.addAttr(JobConstant.KEY_JOBID, jobId);
		if (status == JobConstant.Execute_Status_6) {//运行完成，直接enable改为0
			context.addAttr(JobConstant.KEY_ENABLE, "0");
		}
		context.addAttr(JobConstant.KEY_JOB_STATUS, status+"");
		if (lastStatus != 0) {
			context.addAttr(JobConstant.KEY_LAST_EXE_STATUS, lastStatus);
			context.addAttr(JobConstant.KEY_LAST_EXE_TIME, new Date());
		}
		context.setService("jobService");
		context.setMethod("update");
		RemoteInvoke.getInstance().call(context);
	}
	
	/***
	 * 修改任务状态
	 * @param paramMo 参数
	 * @param status 当前状态
	 * @param lastStatus 上次执行状态
	 */
	public static boolean recurrentJobIsExecute(Long jobId){
		AppParam context = new AppParam();
		context.addAttr(JobConstant.KEY_JOBID, jobId);
		context.setService("jobService");
		context.setMethod("query");
		AppResult result = RemoteInvoke.getInstance().callNoTx(context);
		if(result.getRows().size()>0){
			Map<String,Object> jobInfo = (Map<String, Object>) result.getRow(0);
			Object endType = jobInfo.get(JobConstant.KEY_END_TYPE);
			if(endType!=null && Integer.valueOf(endType.toString())== JobConstant.JOB_END_TYPE_1 &&
					!StringUtils.isEmpty(jobInfo.get(JobConstant.KEY_END_DATE))){
				Date endDate = DateTimeUtil.toDateByString(jobInfo.get(JobConstant.KEY_END_DATE).toString(),DateTimeUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS);
				if(endDate.before(new Date())){
					return true;
				}
			}
		}
		return false;
	}
	
	
}
