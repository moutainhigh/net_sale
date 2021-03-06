package org.llw.job.core;


import java.util.Date;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.LogerUtil;
import org.llw.job.util.JobConstant;
import org.llw.job.util.JobParamMo;
import org.llw.job.util.JobUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
/***
 * 基本任务类型
 * @author Administrator
 *
 */
public class BaseJob implements Runnable {
	private static final String MALL_JOB_LOCK = "MALL_JOB_LOCK_";
	/**job table信息**/
	protected JobParamMo jobInfo;
	
	public BaseJob(){
		
	}
	
	public BaseJob(JobParamMo jobInfo) {
		super();
		this.jobInfo = jobInfo;
	}
	
	
	public JobParamMo getJobInfo() {
		return jobInfo;
	}

	public void setJobInfo(JobParamMo jobInfo) {
		this.jobInfo = jobInfo;
	}


	public void run(){
		String dealBean = jobInfo.getDealBean();
		BaseExecteJob executeJob = (BaseExecteJob) 
				SpringAppContext.getBean(dealBean);
		if(executeJob == null){
			LogerUtil.log("not find job bean:" + dealBean);
			return;
		}
		
		//添加锁定JOB对象的缓存
		Object isLock = RedisUtils.getRedisService().get(MALL_JOB_LOCK + jobInfo.getJobId());
		if(StringUtils.isEmpty(isLock)){
			RedisUtils.getRedisService().set(MALL_JOB_LOCK + jobInfo.getJobId(), "true",10);//10秒
		}else{
			return;
		}
		AppResult lockResult = JobUtil.insertLock(jobInfo);
		
		if(lockResult.getAttr("lockId")==null) {
			LogerUtil.error(BaseJob.class,"Lock error:" + jobInfo.toString() + " is error!");
			return ;
		}
		
		AppResult result = new AppResult();
		result.setSuccess(Boolean.FALSE);
		AppParam param = new AppParam();
		try{
			//添加任务运行信息
			AppResult processInfo = JobUtil.inertProcessInfo(jobInfo, JobConstant.Execute_Status_1);
			param.addAttr(JobConstant.KEY_processId, processInfo.getAttr(JobConstant.KEY_processId)); 
			param.addAttr(JobConstant.KEY_jobParams, jobInfo.getJobParams());
			//修改任务为运行中
			JobUtil.updateJobStatus(jobInfo.getJobId(), JobConstant.Execute_Status_1, 0);
			//运行任务
			result = executeJob.executeJob(param);
			//判断 任务运行方式
			int executeType = jobInfo.getExecuteType();
			switch(executeType){
				case JobConstant.JOB_EXECUTE_TYPE_0:
				case JobConstant.JOB_EXECUTE_TYPE_1: //运行完成
					JobUtil.updateJobStatus(jobInfo.getJobId(), JobConstant.Execute_Status_6,
							result.isSuccess()? JobConstant.Execute_Status_2 :JobConstant.Execute_Status_3);
					ScheduleManager.getInstance().removeJob(jobInfo.getJobId().toString());
					break;
				case JobConstant.JOB_EXECUTE_TYPE_2:
					
					if(JobUtil.recurrentJobIsExecute(jobInfo.getJobId())){
						//执行完成的 状态为 完成
						JobUtil.updateJobStatus(jobInfo.getJobId(), JobConstant.Execute_Status_6 ,
								result.isSuccess()? JobConstant.Execute_Status_2: JobConstant.Execute_Status_3);
						ScheduleManager.getInstance().removeJob(jobInfo.getJobId().toString());
					}else{
						//还需要再次执行的
						JobUtil.updateJobStatus(jobInfo.getJobId(), JobConstant.Execute_Status_0,
								result.isSuccess()? JobConstant.Execute_Status_2:JobConstant.Execute_Status_3);
					}
					break;
			}
		}catch(Exception e){
			LogerUtil.error(BaseJob.class,e,"execute job:" + jobInfo.toString() + " is error!");
		}
		finally{
			AppParam processInfo = new AppParam();
			processInfo.setService("jobProcessService");
			processInfo.setMethod("update");
			processInfo.addAttr(JobConstant.KEY_processId, param.getAttr(JobConstant.KEY_processId));
			processInfo.addAttr(JobConstant.KEY_endTime, new Date());
			processInfo.addAttr(JobConstant.KEY_executeStatus, 
					result.isSuccess()?JobConstant.Execute_Status_2:JobConstant.Execute_Status_3);
			processInfo.addAttr(JobConstant.KEY_executeDesc, result.getMessage());
			processInfo.addAttr(JobConstant.KEY_notifyStatus, JobConstant.Notify_Status_2);
			try{
				RemoteInvoke.getInstance().call(processInfo);
			}catch(Exception e){
				LogerUtil.error(BaseJob.class,e,"Update processId:" + param.getAttr(JobConstant.KEY_processId));
			}
			//删除任务锁
			AppParam lockInfo = new AppParam();
			lockInfo.setService("jobLockService");
			lockInfo.setMethod("delete");
			lockInfo.addAttr(JobConstant.KEY_lockId, lockResult.getAttr("lockId"));
			try{
				RemoteInvoke.getInstance().call(lockInfo);
				RedisUtils.getRedisService().del(MALL_JOB_LOCK + jobInfo.getJobId());//删除锁定JOB对象的缓存
			}catch(Throwable e){
				LogerUtil.error(BaseJob.class,e," delete joblock Error:" + lockResult.getAttr("lockId"));
			}
		}
	}

}
