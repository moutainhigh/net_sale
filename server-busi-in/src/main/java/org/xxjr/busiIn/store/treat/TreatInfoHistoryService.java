package org.xxjr.busiIn.store.treat;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.store.util.StoreApplyUtils;

@Lazy
@Service
public class TreatInfoHistoryService extends BaseService {
	private static final String NAMESPACE = "TREATINFOHISTORY";

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
		return super.query(params, NAMESPACE,"queryShow");
	}
	
	/**
	 * queryShowCount
	 * @param params
	 * @return
	 */
	public AppResult queryShowCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryShowCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
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
		AppResult result = new AppResult();
		params.addAttr("createTime", new Date());
		params.addAttr("updateTime", new Date());
		result = super.insert(params, NAMESPACE);
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_SIGN_RECORD + params.getAttr("applyId"));
		return result;
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		AppResult result = new AppResult();
		params.addAttr("updateTime", new Date());
		result = super.update(params, NAMESPACE);
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_SIGN_RECORD + params.getAttr("applyId"));
		return result;
	}
	
	/**
	 * updateHistory
	 * @param params
	 * @return
	 */
	public AppResult updateHistory(AppParam params) {
		params.addAttr("updateTime", new Date());
		int size = getDao().update(NAMESPACE, "updateHistory",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_SIGN_RECORD + params.getAttr("applyId"));
		return result;
	}
	/**
	 * updateSign
	 * @param params
	 * @return
	 */
	public AppResult updateSign(AppParam params) {
		int size = getDao().update(NAMESPACE, "updateSign",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_SIGN_RECORD + params.getAttr("applyId"));
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
				param.addAttr("treatyNo", id);
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("treatyNo"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**
	 * deleteBy
	 * @param params
	 * @return
	 */
	public AppResult deleteBy(AppParam params) {
		int size = getDao().delete(NAMESPACE, "deleteBy",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Delete_SIZE, size);
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_SIGN_RECORD + params.getAttr("applyId"));
		return result;
	}
}
