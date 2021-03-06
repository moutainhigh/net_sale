package org.xxjr.sys.job;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;

@Lazy
@Service
public class JobService extends BaseService {
	private static final String NAMESPACE = "JOB";

	public AppResult query(AppParam context) {
		return super.query(context, NAMESPACE);
	}
	
	public AppResult queryByPage(AppParam context) {
		return super.queryByPage(context, NAMESPACE);
	}
	
	public AppResult queryCount(AppParam context) {
		int size = getDao().count(NAMESPACE, super.COUNT,context.getAttr(),context.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	public AppResult insert(AppParam context) {
		context.addAttr("jobStatus", "0");
		context.addAttr("enable", "1");
		context.addAttr("createTime", new Date());
		context.addAttr("createBy", DuoduoSession.getUserName());
		context.addAttr("updateTime", new Date());
		context.addAttr("updateBy", DuoduoSession.getUserName());
		AppResult result =  super.insert(context, NAMESPACE);
		result.putAttr("jobId", context.getAttr("jobId"));
		return result;
	}
	
	public AppResult update(AppParam context) {
		context.addAttr("updateTime", new Date());
		context.addAttr("updateBy", DuoduoSession.getUserName());
		AppResult result =   super.update(context, NAMESPACE);
		return result;
	}
	
	public AppResult delete(AppParam context)  {
		AppResult  result = null;
		if (!StringUtils.isEmpty(context.getAttr("jobId"))) {
			AppParam param = new AppParam();
			param.setDataBase(DBConst.Key_sys_DB);
			param.addAttr("jobId", context.getAttr("jobId"));
			AppResult existJob = super.query(param, NAMESPACE);
			if(existJob.getRows().size() ==0){
				throw new AppException("Job 不存在了:" + context.getAttr("jobId"));
			}
			result = super.delete(param, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
}
