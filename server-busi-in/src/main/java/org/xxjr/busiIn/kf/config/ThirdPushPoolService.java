package org.xxjr.busiIn.kf.config;

import java.util.Date;
import java.util.List;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.NumberUtil;

@Lazy
@Service
public class ThirdPushPoolService extends BaseService {
	private static final String NAMESPACE = "THIRDPUSHPOOL";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * queryByPage
	 * @param params
	 * @return
	 */
	public AppResult queryByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE);
	}
	
	/**
	 * queryCount
	 * @param params
	 * @return
	 */
	public AppResult queryCount(AppParam params) {
		int size = getDao().count(NAMESPACE, super.COUNT,params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	
	/**
	 * insert
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		if (StringUtils.isEmpty(params.getAttr("createTime"))) {
			params.addAttr("createTime", new Date());
		}
		return super.insert(params, NAMESPACE);
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		return super.update(params, NAMESPACE);
	}
	
	/**
	 * delete
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult delete(AppParam params) {
		List<String> ids = (List<String>) params.getAttr("ids");
		AppResult  result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids) {
				AppParam param = new AppParam();
				param.addAttr("pushId", id);
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("pushId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**
	 * 保存结果并修改单子为已推送
	 * @param param
	 * @return
	 */
	public AppResult save (AppParam param) {
		AppResult result = new AppResult();
		
		Object applyId = param.getAttr("applyId");
		if (StringUtils.isEmpty(applyId)) {
			return CustomerUtil.retErrorMsg("插入推送数据，缺少applyId!");
		}
		
		if(StringUtils.isEmpty(param.getAttr("createTime"))){
			param.addAttr("createTime", new Date());
		}
		param.addAttr("notImmediate", 3);//排除为某些特殊渠道推送的数据
		result = this.updateByApplyId(param);//使用applyId作为条件同一时间只能有一条存在
		int count = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Update_SIZE), 0);
		if(count == 0){
			result = this.insert(param);
			count = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
		}
		
		if (count > 0) {
			AppParam updateParam = new AppParam("borrowApplyService", "update");
			updateParam.addAttr("applyId", param.getAttr("applyId"));
			updateParam.addAttr("pushStatus", "1");
			SoaManager.getInstance().invoke(updateParam);
		}
		return result;
	}
	
	/**
	 * 查询待推送列表
	 * @param params
	 * @return
	 */
	public AppResult queryPushData(AppParam params) {
		AppResult result = new AppResult();
		if (StringUtils.isEmpty(params.getAttr("size"))) {
			result.setMessage("请传入查询行数");
			result.setSuccess(false);
			return result;
		}
		return this.query(params, NAMESPACE, "queryPushData");
	}
	
	/**
	 * 记录推送结果并从池中删除记录
	 * @param param
	 * @return
	 */
	public AppResult updateStatus (AppParam param) {
		AppResult result = new AppResult();
		AppParam insertParam = new AppParam("borrowApplyPushService", "insert");//记录这次推送的情况
		int status = NumberUtil.getInt(param.getAttr("status"), 2);
		//int pushType = NumberUtil.getInt(param.getAttr("pushType"));
		
		int applyStatus = 6;
		if (status == 1) {
			applyStatus = 5;
		}
		AppParam updateParam = new AppParam();
		updateParam.addAttr("applyId", param.getAttr("applyId"));
		updateParam.addAttr("status", applyStatus);
		this.updateApplyStatus(updateParam);
		
		insertParam.addAttrs(param.getAttr());
		insertParam.addAttr("updateTime", new Date());
		SoaManager.getInstance().invoke(insertParam);
		//成功就删除，失败是在任务里面做操作
		if (/*(status == 2 && PushPlatformUtils.isDelPushData(pushType)) || */status == 1) {
			this.delete(param);
		}
		return result;
	}
	
	/**
	 * 如果borrow_apply表中status为0，则修改
	 * @param param
	 * @return
	 */
	public AppResult updateApplyStatus (AppParam param) {
		AppResult result = new AppResult();
		int count = super.getDao().update(NAMESPACE, "updateApplyStatus", param.getAttr(), param.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, count);
		return result;
	}
	
	/**
	 * 超过规定时间的单子将 immediate 改成 1，去推送
	 * @param param
	 * @return
	 */
	public AppResult updateImmedStatus (AppParam param) {
		AppResult result = new AppResult();
		int count = super.getDao().update(NAMESPACE, "updateImmedStatus", param.getAttr(), param.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, count);
		return result;
	}
	
	
	/**
	 * 通过applyId 修改参数
	 * @param param
	 * @return
	 */
	public AppResult updateByApplyId (AppParam param) {
		AppResult result = new AppResult();
		int count = super.getDao().update(NAMESPACE, "updateByApplyId", param.getAttr(), param.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, count);
		return result;
	}
	
	public AppResult failDataRestore (AppParam param) {
		AppResult result = new AppResult();
		int size = this.getDao().insert(NAMESPACE, "failDataRestore", param.getAttr(), param.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Insert_SIZE, size);
		return result;
	}
}
