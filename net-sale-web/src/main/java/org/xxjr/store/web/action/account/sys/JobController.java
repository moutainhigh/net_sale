package org.xxjr.store.web.action.account.sys;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xxjr.store.web.action.BaseController;
import org.xxjr.store.web.action.account.user.UserInfoAction;
import org.xxjr.sys.util.ServiceKey;

@RestController
@RequestMapping("/account/config/")
public class JobController extends BaseController{
	
	/**
	 * 任务列表
	 * @param request
	 * @return
	 */
	@RequestMapping("job/queryList")
	public AppResult queryList(){
		AppResult result = new AppResult();
		try {	
			AppParam param = new AppParam("jobService","queryByPage");
			RequestUtil.setAttr(param, request);
			param.setOrderBy("createTime");
			param.setOrderValue("DESC");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(UserInfoAction.class,e, "任务类型列表错误");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 增加job
	 * @param request
	 * @return
	 */
	@RequestMapping("job/insert")
	public AppResult insert(){
		AppResult result = new AppResult();
		try {
			AppParam param = new AppParam("jobService","insert");
			RequestUtil.setAttr(param, request);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(param);
		} catch (Exception e) {
			LogerUtil.error(UserInfoAction.class,e, "增加job错误");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 立即执行
	 * @param request
	 * @return
	 */
	@RequestMapping("job/executeQuick")
	public AppResult executeQuick(){
		AppResult result = new AppResult();
		try {	
			String jobId = request.getParameter("jobId");
			if (StringUtils.isEmpty(jobId)) {
				result.setMessage("缺少必传参数");
				result.setSuccess(false);
				return result;
			}
			AppParam param = new AppParam("executeJobService","executeQuick");
			param.addAttr("jobId", jobId);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(param);
		} catch (Exception e) {
			LogerUtil.error(UserInfoAction.class,e, "任务立即执行错误");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 删除任务
	 * @param request
	 * @return
	 */
	@RequestMapping("job/delete")
	public AppResult delete(){
		AppResult result = new AppResult();
		try {	
			String jobId = request.getParameter("jobId");
			if (StringUtils.isEmpty(jobId)) {
				result.setMessage("缺少必传参数");
				result.setSuccess(false);
				return result;
			}
			AppParam param = new AppParam("jobService","delete");
			param.addAttr("jobId", jobId);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(param);
		} catch (Exception e) {
			LogerUtil.error(UserInfoAction.class,e, "删除任务错误");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 修改任务
	 * @param request
	 * @return
	 */
	@RequestMapping("job/update")
	public AppResult update(){
		AppResult result = new AppResult();
		try {	
			String jobId = request.getParameter("jobId");
			if (StringUtils.isEmpty(jobId)) {
				result.setMessage("缺少必传参数");
				result.setSuccess(false);
				return result;
			}
			AppParam param = new AppParam("executeJobService","resetJob");
			RequestUtil.setAttr(param, request);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(param);
		} catch (Exception e) {
			LogerUtil.error(UserInfoAction.class,e, "修改任务错误");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询运行记录
	 * @param request
	 * @return
	 */
	@RequestMapping("job/queryRecord")
	public AppResult queryRecord(){
		AppResult result = new AppResult();
		try {
			AppParam param = new AppParam("jobProcessService","queryByPage");
			RequestUtil.setAttr(param, request);
			param.setOrderBy("startTime");
			param.setOrderValue("DESC");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(param);
		} catch (Exception e) {
			LogerUtil.error(UserInfoAction.class,e, "查询运行记录错误");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 查询任务锁
	 * @param request
	 * @return
	 */
	@RequestMapping("joblock/queryJobLock")
	public AppResult queryJobLock(){
		AppResult result = new AppResult();
		try {
			AppParam param = new AppParam("jobLockService","queryByPage");
			RequestUtil.setAttr(param, request);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(UserInfoAction.class,e, "查询任务锁错误");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 删除任务锁
	 * @param request
	 * @return
	 */
	@RequestMapping("joblock/deleteJobLock")
	public AppResult deleteJobLock(){
		AppResult result = new AppResult();
		try {	
			String lockId = request.getParameter("lockId");
			if (StringUtils.isEmpty(lockId)) {
				result.setMessage("缺少必传参数");
				result.setSuccess(false);
				return result;
			}
			AppParam param = new AppParam("jobLockService","delete");
			param.addAttr("lockId", lockId);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(param);
		} catch (Exception e) {
			LogerUtil.error(UserInfoAction.class,e, "删除任务锁错误");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
