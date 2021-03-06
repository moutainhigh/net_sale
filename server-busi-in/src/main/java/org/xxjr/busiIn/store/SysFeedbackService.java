package org.xxjr.busiIn.store;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Lazy
@Service
public class SysFeedbackService extends BaseService {
	private static final String NAMESPACE = "SYSFEEDBACK";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * queryShow
	 * @param params
	 * @return
	 */
	public AppResult queryShow(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryShow","queryShowCount");
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
		params.addAttr("createTime", new Date());
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
	public AppResult delete(AppParam params) {
		String ids = (String) params.getAttr("ids");
		AppResult  result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.addAttr("feedId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("feedId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/***
	 * 处理反馈
	 * @param params
	 * @return
	 */
	public AppResult dealFeedBack(AppParam params){
		String isDelCost = StringUtil.getString(params.getAttr("isDelCost"));
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> orders = (List<Map<String, Object>>)params.removeAttr("orders");
		AppResult result = new AppResult();
		for(Map<String, Object> map : orders){
			params.addAttr("feedId", map.get("feedId"));
			result = this.update(params);
			
			String dealCustomerId = StringUtil.getString(map.get("customerId"));
			String telephone = StringUtil.getString(map.get("telephone"));
			//删除成本，需要删除成本且号码不为空
			if(result.isSuccess() && "1".equals(isDelCost) &&
					!StringUtils.isEmpty(telephone) && !StringUtils.isEmpty(dealCustomerId)){
				
				//查询该笔订单是否存在
				AppParam queryParam = new AppParam();
				queryParam.addAttr("telephone", telephone);
				queryParam.setService("borrowStoreApplyService");
				queryParam.setMethod("query");
				result = SoaManager.getInstance().invoke(queryParam);
				
				if(result.isSuccess() && result.getRows().size() > 0){
					Map<String,Object> resultMap = result.getRow(0);
					String applyId = StringUtil.getString(resultMap.get("applyId"));
					String orgId = StringUtil.getString(resultMap.get("orgId"));
					AppParam updateparams = new AppParam();
					updateparams.setService("orgCostRecordService");
					updateparams.setMethod("delete");
					updateparams.addAttr("orgId", orgId);
					updateparams.addAttr("applyId", applyId);
					result = SoaManager.getInstance().invoke(updateparams);
				}
			}
		}
		return result;
	}
}
