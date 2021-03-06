package org.xxjr.busiIn.store;

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
public class ExclusiveOrderService extends BaseService {
	private static final String NAMESPACE = "EXCLUSIVEORDER";

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
	 * 查询专属订单列表
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryExcOrder(AppParam params) {
		if (params.getCurrentPage() == -1) {
			return super.query(params, NAMESPACE, "queryExcOrder");
		} else {
			return super.queryByPage(params, NAMESPACE, "queryExcOrder",
					"queryExcOrderCount");
		}
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
		result = super.insert(params, NAMESPACE);
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_EXEC_ORDER + params.getAttr("applyId"));
		return result;
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		AppResult updateResult = new AppResult();
		updateResult = super.update(params, NAMESPACE);
		int count = (Integer) updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE);
		if (count <= 0) {
			updateResult = this.insert(params);
		}
		return updateResult;
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
				param.addAttr("applyId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("applyId"))) {
			result = super.delete(params, NAMESPACE);
			//删除缓存
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_EXEC_ORDER + params.getAttr("applyId"));
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**
	 * deleteByCustId 
	 * @param params
	 * @return
	 */
	public AppResult deleteByCustId(AppParam params) {
		int size = getDao().delete(NAMESPACE, "deleteByCustId",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Delete_SIZE, size);
		return result;
	}
	
	/**
	 * 7天未处理的专属单 提醒门店人员处理
	 */
	
	public AppResult queryNotDeal(AppParam params){
		return super.queryByPage(params, NAMESPACE,"queryNotDeal","queryNotDealCount");
	}
}
