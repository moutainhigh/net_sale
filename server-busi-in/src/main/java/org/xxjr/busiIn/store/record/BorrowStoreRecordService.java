package org.xxjr.busiIn.store.record;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.StoreConstant;
import org.xxjr.busiIn.utils.StoreOptUtil;
import org.xxjr.store.util.StoreApplyUtils;

import lombok.extern.slf4j.Slf4j;

@Lazy
@Service
@Slf4j
public class BorrowStoreRecordService extends BaseService {
	private static final String NAMESPACE = "BORROWSTORERECORD";

	/**
	 * 查询跟单记录
	 * @param params
	 * @return
	 */
	public AppResult queryStoreRecord(AppParam params) {
		AppResult result = new AppResult();
		String curDate = StringUtil.getString(params.getAttr("curDate"));
		String tableName = StoreOptUtil.getTableName(curDate);
		params.addAttr("tableName", tableName);
		String lastMonth = StringUtil.getString(params.getAttr("lastMonth"));
		String lastTwoMonth = StringUtil.getString(params.getAttr("lastTwoMonth"));
		if(!StringUtils.isEmpty(lastMonth)){
			params.addAttr("tableName1", StoreOptUtil.getTableName(lastMonth));
		}
		if(!StringUtils.isEmpty(lastTwoMonth)){
			params.addAttr("tableName2", StoreOptUtil.getTableName(lastTwoMonth));
		}
		
		try{
			result = super.queryByPage(params, NAMESPACE,"queryStoreRecord","queryStoreRecordCount");
		}catch(Exception e){
			// 出现插入异常
			boolean havTable = this.isExsitTable(params);
			if(!havTable){
				createNewTable(params);
				//进行查询操作
				result = super.queryByPage(params, NAMESPACE,"queryStoreRecord","queryStoreRecordCount");
			}
		}
		return result;
	}
	
	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		
		AppResult result = new AppResult();
		String curDate = StringUtil.getString(params.getAttr("curDate"));
		String tableName = StoreOptUtil.getTableName(curDate);
		params.addAttr("tableName", tableName);
		try{
			result = super.query(params, NAMESPACE);
		}catch(Exception e){
			// 出现插入异常
			boolean havTable = this.isExsitTable(params);
			if(!havTable){
				createNewTable(params);
				//进行查询操作
				result = super.query(params, NAMESPACE);
			}
		}
		return result;
	}
	
	/**
	 * queryByPage
	 * @param params
	 * @return
	 */
	public AppResult queryByPage(AppParam params) {
		params.addAttr("tableName", StoreOptUtil.getTableName(null));
		return super.queryByPage(params, NAMESPACE);
	}
	
	/**
	 * queryShowByPage
	 * @param params
	 * @return
	 */
	public AppResult queryShowByPage(AppParam params) {
		params.addAttr("tableName", StoreOptUtil.getTableName(null));
		return super.queryByPage(params, NAMESPACE, "queryShow", "queryShowCount");
	}
	
	/**门店处理相关的查询列表
	 * queryStoreHandleList
	 * @param params
	 * @return
	 */
	public AppResult queryStoreHandleList(AppParam params) {
		params.addAttr("tableName", StoreOptUtil.getTableName(null));
		return super.queryByPage(params, NAMESPACE, "queryStoreHandleList", "queryStoreHandleCount");
	}
	
	/**反馈列表
	 * feedbackList
	 * @param params
	 * @return
	 */
	public AppResult feedbackList(AppParam params) {
		params.addAttr("tableName", StoreOptUtil.getTableName(null));
		return super.queryByPage(params, NAMESPACE, "feedbackList", "feedbackCount");
	}
	
	/**
	 * queryCount
	 * @param params
	 * @return
	 */
	public AppResult queryCount(AppParam params) {
		AppResult result = new AppResult();
		try{
			int size = getDao().count(NAMESPACE, super.COUNT,params.getAttr(),params.getDataBase());
			result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		}catch(Exception e){
			// 出现插入异常
			boolean havTable = this.isExsitTable(params);
			if(!havTable){
				createNewTable(params);
				//进行查询操作
				int size = getDao().count(NAMESPACE, super.COUNT,params.getAttr(),params.getDataBase());
				result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
			}
		}
		
		
		
		return result;
	}
	
	
	/***
	 * 判断是否存在表
	 * @param params
	 * @return
	 */
	public boolean isExsitTable(AppParam params){
		int count = getDao().count(NAMESPACE, "isExsitTable" ,params.getAttr(),params.getDataBase());
		return count > 0 ? true : false;
	}
	
	/***
	 * 创建表
	 * @param params
	 * @return
	 */
	public int createNewTable(AppParam params){
		int size = getDao().update(NAMESPACE, "createNewTable", params.getAttr(), params.getDataBase());
		return size;
	}
	
	/**
	 * insert
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		AppResult result = new AppResult();
		int totalSize = 0; 
		try{
			params.addAttr("createTime", new Date());
			params.addAttr("tableName", StoreOptUtil.getTableName(null));
			totalSize = getDao().insert(NAMESPACE, "insert", params.getAttr(), params.getDataBase());
		}catch(Exception e){
			// 出现插入异常
			boolean havTable = this.isExsitTable(params);
			if(!havTable){
				createNewTable(params);
				//进行插入操作
				totalSize = getDao().insert(NAMESPACE, "insert", params.getAttr(), params.getDataBase());
			}
			log.error("insert error", e);
		}
		
		if(totalSize > 0){
			// 删除当月缓存
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_FOLLOW_RECORD 
					+ params.getAttr("applyId") + "_"+ StoreConstant.QUERY_DATE_FLAG_0
						+ StoreConstant.IS_ADMIN_TRUE);
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_FOLLOW_RECORD
					+ params.getAttr("applyId") + "_"+ StoreConstant.QUERY_DATE_FLAG_0
					+ StoreConstant.IS_ADMIN_FALSE);
		}
		
		result.putAttr(DuoduoConstant.TOTAL_SIZE, totalSize);
		return result;
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		String tableName = StoreOptUtil.getTableName(null);
		params.addAttr("tableName", tableName);
		return super.update(params, NAMESPACE);
	}
	
	/**更新为已读
	 * updateReadFlag
	 * @param params
	 * @return
	 */
	public AppResult updateReadFlag(AppParam params) {
		int size = getDao().update(NAMESPACE, "updateReadFlag", params.getAttr(), params.getDataBase());
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return backContext;
	}
	
	/**查询退款信息
	 * queryBackInfo
	 * @param params
	 * @return
	 */
	public AppResult queryBackInfo(AppParam params) {
		return super.query(params, NAMESPACE,"queryBackInfo");
	}
	
	
	/**
	 * delete
	 * @param params
	 * @return
	 */
	public AppResult delete(AppParam params) {
		String ids = (String) params.getAttr("ids");
		String curDate = (String)params.getAttr("curDate");
		params.addAttr("tableName", StoreOptUtil.getTableName(curDate));
		AppResult  result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.addAttr("recordId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("recordId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	
	/**
	 * deleteByApplyId
	 * @param params
	 * @return
	 */
	public AppResult deleteByApplyId(AppParam params) {
		AppResult result = new AppResult();
		String curDate = (String)params.getAttr("curDate");
		params.addAttr("tableName", StoreOptUtil.getTableName(curDate));
		super.getDao().delete(NAMESPACE, "deleteByApplyId", params.getAttr(),params.getDataBase());
		return result;
	}
	
	/***
	 * 查询门店的待处理，上门量，签单量，成功量等统计
	 * @return
	 */
	public AppResult queryOrderCount(AppParam params){
		params.addAttr("tableName", StoreOptUtil.getTableName(null));
		return super.query(params, NAMESPACE,"queryOrderCount");
	}
	
	/***
	 * 查询门店人员的接单记录
	 * @return
	 */
	public AppResult queryStoreRecRecord(AppParam params){
		String curDate = StringUtil.getString(params.getAttr("startReceviceDate"));
		String tableName = StoreOptUtil.getTableName(curDate);
		params.addAttr("tableName", tableName);
		return super.queryByPage(params, NAMESPACE,"queryStoreRecRecord","queryStoreRecRecordCount");
	}
	
	/***
	 * 查询门店分单量
	 * @param params
	 * @return
	 */
	public AppResult queryOrgAllotCount(AppParam params){
		AppResult result = new AppResult();
		params.addAttr("tableName", StoreOptUtil.getTableName(null));
		try{
			int size = getDao().count(NAMESPACE, "queryOrgAllotCount",params.getAttr(),params.getDataBase());
			result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		}catch(Exception e){
			// 出现插入异常
			boolean havTable = this.isExsitTable(params);
			if(!havTable){
				createNewTable(params);
				//进行查询操作
				int size = getDao().count(NAMESPACE, "queryOrgAllotCount",params.getAttr(),params.getDataBase());
				result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
			}
		}
		
		return result;
	}
	
	/**
	 * 查询用户接单记录（获取最近的一条记录）
	 * queryCustAllotRecord
	 * @param params
	 * @return
	 */
	public AppResult queryCustAllotRecord(AppParam params) {
		AppResult result = new AppResult();
		String curDate = StringUtil.getString(params.getAttr("curDate"));
		String tableName = StoreOptUtil.getTableName(curDate);
		params.addAttr("tableName", tableName);
		try{
			result = super.query(params, NAMESPACE,"queryCustAllotRecord");
		}catch(Exception e){
			// 出现插入异常
			boolean havTable = this.isExsitTable(params);
			if(!havTable){
				createNewTable(params);
				//进行查询操作
				result = super.query(params, NAMESPACE,"queryCustAllotRecord");
			}
		}
		return result;
	}

}
