package org.xxjr.sys.job;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ddq.active.mq.store.StoreJobSend;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.xxjr.sys.util.DBConst;
import org.xxjr.sys.util.JobConstant;
import org.xxjr.sys.util.NumberUtil;

import lombok.extern.slf4j.Slf4j;

@Lazy
@Service
@Slf4j
public class ExecuteJobService extends BaseService {
	
	/***
	 * 立即执行job
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public AppResult executeQuick(AppParam context) throws Exception {
		AppParam param = new AppParam();
		param.setService("jobService");
		param.setMethod("query");
		param.addAttr("jobId", context.getAttr("jobId"));
		param.setDataBase(DBConst.Key_sys_DB);
		AppResult jobList  = SoaManager.getInstance().invoke(param);
		Map<String,Object> jobInfo = jobList.getRow(0);
		
		jobInfo.put(JobConstant.KEY_UPDATE_BY, DuoduoSession.getUserName());
		
		return new AppResult();
	}
	
	/***
	 * 取消job
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public AppResult disableJob(AppParam context) {
		AppParam param = new AppParam();
		param.setService("jobService");
		param.setMethod("query");
		param.addAttr("jobId", context.getAttr("jobId"));
		AppResult jobList  = SoaManager.getInstance().invoke(param);
		Map<String,Object> jobInfo = jobList.getRow(0);
		//若是false
		if(Integer.valueOf(jobInfo.get("enable").toString()) ==JobConstant.JOB_ENABLE_FALSE){
			throw new AppException("该任务已经是取消状态");
		}
		param.setMethod("update");
		param.addAttr("enable", JobConstant.JOB_ENABLE_FALSE+"");
		AppResult result =  SoaManager.getInstance().invoke(param);
		return result;
	}
	
	
	/***
	 * 启用job
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public AppResult enableJob(AppParam context) {
		AppParam param = new AppParam();
		param.setService("jobService");
		param.setMethod("query");
		param.addAttr("jobId", context.getAttr("jobId"));
		AppResult jobList  = SoaManager.getInstance().invoke(param);
		Map<String,Object> jobInfo = jobList.getRow(0);
		//若是false
		if(Integer.valueOf(jobInfo.get("enable").toString()) ==JobConstant.JOB_ENABLE_TRUE){
			throw new AppException("当前任务是执行状态");
		}
		
		param.setMethod("update");
		param.addAttr("enable", JobConstant.JOB_ENABLE_TRUE+"");
		AppResult result =  SoaManager.getInstance().invoke(param);
		return result;
	}
	
	/***
	 * 重置JOb处理
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public AppResult resetJob(AppParam context)  {
		AppParam param = new AppParam();
		param.setService("jobService");
		param.setMethod("update");
		param.addAttrs(context.getAttr());
		AppResult result =  SoaManager.getInstance().invoke(param);
		int updateSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
		String jobId = StringUtil.getString(param.getAttr("jobId"));
		if(updateSize > 0 && !StringUtils.isEmpty(jobId)){
			try{
				StoreJobSend  storeSend = SpringAppContext.getBean(StoreJobSend.class);
				Map<String, Object> sendParam = new HashMap<String, Object>();	
				storeSend.sendJobMessage(jobId, "jobMqType", sendParam);
			}catch(Exception e){
				log.error("ExecuteJobService 发送job消息通知 error",e);
			}
		}
		return result;
	}
}
