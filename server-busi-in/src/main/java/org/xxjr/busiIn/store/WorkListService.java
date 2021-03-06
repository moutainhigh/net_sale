package org.xxjr.busiIn.store;

import java.util.Date;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busiIn.utils.StoreOptUtil;

@Lazy
@Service
public class WorkListService extends BaseService {
	private static final String NAMESPACE = "WORKLIST";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * queryMsgNotice
	 * @param params
	 * @return
	 */
	public AppResult queryMsgNotice(AppParam params) {
		return super.query(params, NAMESPACE, "queryMsgNotice");
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
		params.addAttr("allotTime", new Date());
		params.addAttr("createBy", DuoduoSession.getUserName());
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
	 * 处理未完成的任务
	 * @param params
	 * @return
	 */
	public AppResult dealAllotOrderWork(AppParam params){
		AppResult result = new AppResult();
		Object applyId = params.getAttr("applyId");
		Object customerId = params.getAttr("customerId");
		if(!StringUtils.isEmpty(applyId) && !StringUtils.isEmpty(customerId)){
			AppParam queryParams = new AppParam();
			queryParams.addAttr("extraId", applyId);
			if(StoreOptUtil.isDealAuth(StringUtil.objectToStr(customerId))){
				queryParams.addAttr("customerId", "");
			}else{
				queryParams.addAttr("customerId", customerId);
			}
			
			queryParams.addAttr("workType", 1);
			result = this.query(queryParams);
			if(result.getRows().size() > 0){
				Map<String, Object> paramMap = result.getRow(0);
				Object workId = paramMap.get("workId");
				Object custTel = paramMap.get("custTel");
				Object custName = paramMap.get("custName");
				Object allotTime = paramMap.get("allotTime");
				Object allotBy = paramMap.get("allotBy");
				Object allotDesc = paramMap.get("allotDesc");
				Object remark = paramMap.get("remark");
				//插入完成的任务
				queryParams = new AppParam("workListFishService","insert");
				queryParams.addAttr("extraId", applyId);
				queryParams.addAttr("customerId", customerId);
				queryParams.addAttr("workType", 1);
				queryParams.addAttr("custTel", custTel);
				queryParams.addAttr("custName", custName);
				queryParams.addAttr("allotTime", allotTime);
				queryParams.addAttr("allotBy", allotBy);
				queryParams.addAttr("allotDesc", allotDesc);
				queryParams.addAttr("remark", remark);
				queryParams.addAttr("finishTime", new Date());
				result = SoaManager.getInstance().invoke(queryParams);
				
				//删除任务
			    queryParams = new AppParam();
				queryParams.addAttr("workId", workId);
				result = this.delete(queryParams);
			}
			
		}else{
			result.setSuccess(false);
			result.setMessage("处理业务员待处理单缺少必要参数");
		}
		
		return result;
	}
	/**
	 * delete
	 * @param params
	 * @return
	 */
	public AppResult delete(AppParam params) {
		String ids = (String) params.getAttr("ids");
		AppResult  result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.addAttr("workId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("workId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**
	 * deleteByExtraId
	 * @param params
	 * @return
	 */
	public AppResult deleteByExtraId(AppParam params) {
		int size = getDao().delete(NAMESPACE, "deleteByExtraId", params.getAttr(), params.getDataBase());
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Delete_SIZE, size);
		return backContext;
	}
	
	/**
	 * 批量插入待处理的任务
	 * @param params
	 * @return
	 */
	public AppResult batchInsertWork(AppParam params) {
		int size = getDao().insert(NAMESPACE, "batchInsertWork", params.getAttr(), params.getDataBase());
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Insert_SIZE, size);
		return backContext;
	}
	
	/**
	 * 删除并插入
	 * @param params
	 * @return
	 */
	public AppResult deleteAndInsert(AppParam params) {
		AppResult result = new AppResult();
		String extraId = StringUtil.getString(params.getAttr("extraId"));
		String allotBy = StringUtil.getString(params.getAttr("allotBy"));
		if(!StringUtils.isEmpty(extraId)){
			AppParam deleteParam = new AppParam();
			deleteParam.addAttr("extraId", extraId);
			if(!StoreOptUtil.isDealAuth(allotBy)){
				deleteParam.addAttr("customerId", allotBy);
			}
			getDao().delete(NAMESPACE, "deleteInfo",deleteParam.getAttr(),deleteParam.getDataBase());
			return super.insert(params, NAMESPACE);
		}
		return result;
	}
}
