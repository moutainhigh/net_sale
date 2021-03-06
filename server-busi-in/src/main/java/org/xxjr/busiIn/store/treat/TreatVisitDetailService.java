package org.xxjr.busiIn.store.treat;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.StoreConstant;


@Lazy
@Service
public class TreatVisitDetailService extends BaseService {
	private static final String NAMESPACE = "TREATVISITDETAIL";

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
		params.addAttr("createTime", new Date());
		return super.insert(params, NAMESPACE);
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		AppResult result = new AppResult();
		String visitType = StringUtil.getString(params.getAttr("visitType"));
		//查询是否存在上门记录
		AppParam queryParam = new AppParam();
		queryParam.addAttr("recCustId", params.getAttr("recCustId"));
		queryParam.addAttr("applyId", params.getAttr("applyId"));
		queryParam.addAttr("visitTime", params.getAttr("visitTime"));
		result = this.query(queryParam);
		if(result.isSuccess() && result.getRows().size() >0 
				&& StoreConstant.STORE_VISIT_TYPE_1.equals(visitType)){
			result.setSuccess(false);
			result.setMessage("当天上门记录已存在");
			return result;
		}else if(result.isSuccess() && result.getRows().size() >0 
				&& StoreConstant.STORE_VISIT_TYPE_2.equals(visitType)){//客户上门登记覆盖手工添加
			params.addAttr("updateTime", new Date());
			return super.update(params, NAMESPACE);
		}else{
			return this.insert(params);
		}
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
				param.addAttr("detailId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("detailId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/***
	 * 上门记录
	 * @param params
	 * @return
	 */
	public AppResult queryVisitRecord(AppParam params) {
		return super.query(params, NAMESPACE,"queryVisitRecord");
	}
	
	/**
	 * updateByDetailId
	 * @param params
	 * @return
	 */
	public AppResult updateByDetailId(AppParam params) {
		params.addAttr("updateTime", new Date());
		int size = getDao().update(NAMESPACE, "updateByDetailId",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	/**
	 * 分页查询手工添加的上门记录
	 * @param params
	 * @return
	 */
	public AppResult queryHandleVisitByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryHandleVisit","queryHandleVisitCount");
	}
	
	/**
	 * 查询手工添加的上门记录(不分页)
	 * @param params
	 * @return
	 */
	public AppResult queryHandleVisit(AppParam params) {
		return super.query(params, NAMESPACE,"queryHandleVisit");
	}
}
