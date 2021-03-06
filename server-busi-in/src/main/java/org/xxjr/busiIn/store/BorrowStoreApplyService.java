package org.xxjr.busiIn.store;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.BorrowConstant;
import org.xxjr.store.util.StoreApplyUtils;
import org.xxjr.sys.util.NumberUtil;

@Lazy
@Service
public class BorrowStoreApplyService extends BaseService {
	private static final String NAMESPACE = "BORROWSTOREAPPLY";

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
	 * queryShowByPage
	 * @param params
	 * @return
	 */
	public AppResult queryShowByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryShow", "queryShowCount");
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
	 * 查询主要信息和基本信息
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryMainBaseInfo(AppParam params) {
		return super.query(params, NAMESPACE,"queryMainBaseInfo");
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
		params.addAttr("updateTime", new Date());
		AppResult result = super.update(params, NAMESPACE);
		if(result.isSuccess()){
			int size = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
			if(size > 0){
				String applyId = StringUtil.getString(params.getAttr("applyId"));
				// 删除订单缓存
				RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_INFO + applyId);
				RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_MAININFO + applyId);
				if(params.getAttr("lastStore") != null){
					RedisUtils.getRedisService().del(StoreApplyUtils.BORROW_APPLY_INFO + applyId);
				}
			}
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
				param.addAttr("applyId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("applyId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	
	/**
	 * 查询插入
	 * @param params
	 * @return
	 */
	public AppResult insertByBorrowApply(AppParam params) {
		AppResult result = new AppResult();
		int size = super.getDao().insert(NAMESPACE, "insertByBorrowApply", params.getAttr(), params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Insert_SIZE, size);
		return result;
	}
	
	/**
	 * 查询更新
	 * @param params
	 * @return
	 */
	public AppResult updateByBorrowApply(AppParam params) {
		AppResult result = new AppResult();
		int count = super.getDao().update(NAMESPACE, "updateByBorrowApply", params.getAttr(), params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, count);
		
		// 删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_INFO + params.getAttr("applyId"));
		return result;
	}
	
	
	
	/**
	 * insert
	 * @param params
	 * @return
	 */
	public AppResult insertStoreApply(AppParam params) {
		Date d1 = new Date();
		params.addAttr("createTime", d1);
		if (StringUtils.isEmpty(params.getAttr("applyTime"))) {
			params.addAttr("applyTime", d1);
		}		
		params.addAttr("updateTime", new Date());
		int applyType = NumberUtil.getInt(params.getAttr("applyType"), -1);
		if(applyType == -1){
			params.addAttr("applyType", BorrowConstant.apply_type_2);//普通单
			params.addAttr("haveDetail", "0");
		}
		AppResult result = super.insert(params, NAMESPACE);
		result.putAttr("applyId", params.getAttr("applyId"));
		return result;
	}
	/**
	 * 我抢的优质单列表(微信端)
	 * @param params
	 * @return
	 */
	public AppResult seniorRobList(AppParam params){
		return super.queryByPage(params, NAMESPACE,"seniorRobList", "seniorRobCount");
	}
	
	/**
	 * 已抢优质单列表订单详情
	 * @param params
	 * @return
	 */
	public AppResult querySimpleInfo(AppParam params) {
		return super.query(params, NAMESPACE, "querySimpleInfo");
	}
	
	/**
	 * 查询客户星级与处理状态不一致
	 * @param params
	 * @return
	 */
	public AppResult queryViolateOrder(AppParam params){
		return super.query(params, NAMESPACE,"queryViolateOrder");
	}
	
	/**
	 * 查询优质单统计
	 * @param params
	 * @return
	 */
	public AppResult querySeniorCount(AppParam params){
		return super.query(params, NAMESPACE,"querySeniorCount");
	}
	
	/***
	 * 查询发送短信相关信息
	 * @param params
	 * @return
	 */
	public AppResult queryMessageInfo(AppParam params){
		return super.query(params, NAMESPACE,"queryMessageInfo");
	}
	
	/***
	 * 查询门店人员基本信息（分销专用）
	 * @param params
	 * @return
	 */
	public AppResult queryStoreCustBaseInfo(AppParam params){
		return super.query(params, NAMESPACE,"queryStoreCustBaseInfo");
	}
	
}
