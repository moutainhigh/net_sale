package org.xxjr.busiIn.store.treat;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.DateUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.store.util.StoreApplyUtils;


@Lazy
@Service
public class TreatBookDetailService extends BaseService {
	private static final String NAMESPACE = "TREATBOOKDETAIL";

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
		params.addAttr("createBy", DuoduoSession.getUserName());
		return super.insert(params, NAMESPACE);
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		//查询是否存在未过期的预约
		AppParam queryParam = new AppParam();
		queryParam.addAttr("bookCustId", params.getAttr("customerId"));
		queryParam.addAttr("applyId", params.getAttr("applyId"));
		String today = DateUtil.toStringByParttern(DateUtil.getNextDay(new Date(),-1),DateUtil.DATE_PATTERN_YYYY_MM_DD);
		queryParam.addAttr("startTime", today);
		AppResult result = this.query(queryParam);
		
		params.addAttr("bookCustId", params.removeAttr("customerId"));
		if(result.isSuccess() && result.getRows().size() >0){
			params.addAttr("detailId", result.getRow(0).get("detailId"));
			return super.update(params,NAMESPACE);
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
	 * 预约记录
	 * @param params
	 * @return
	 */
	public AppResult queryBookRecord(AppParam params) {
		return super.query(params, NAMESPACE,"queryBookRecord");
	}
	
	
	/**
	 * 取消预约
	 * @param params
	 * @return
	 */
	public AppResult updateBookStatus(AppParam params) {
		//查询是否存在未过期的预约
		AppParam queryParam = new AppParam();
		queryParam.addAttr("bookCustId", params.getAttr("customerId"));
		queryParam.addAttr("applyId", params.getAttr("applyId"));
		String today = DateUtil.toStringByParttern(DateUtil.getNextDay(new Date(),0),DateUtil.DATE_PATTERN_YYYY_MM_DD);
		queryParam.addAttr("startTime", today);
		AppResult result = this.query(queryParam);
		
		if(result.isSuccess() && result.getRows().size() >0){
			params.addAttr("detailId", result.getRow(0).get("detailId"));
			int size = getDao().update(NAMESPACE, "updateBookStatus",params.getAttr(),params.getDataBase());
			result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
			// 删除缓存
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_BOOK_RECORD + params.getAttr("applyId"));
		}else{
			result.setMessage("预约已过期或非本人操作");
			result.setSuccess(false);
		}
		return result;
	}
	
	/***
	 * 查询预约记录(上传到CFS)
	 * @param params
	 * @return
	 */
	public AppResult queryBookRecordList(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryBookRecordList","queryBookRecordCount");
	}
	
	/**
	 * 纯净更新
	 * @param params
	 * @return
	 */
	public AppResult updateBook(AppParam params) {
		return super.update(params,NAMESPACE);
	}
}
