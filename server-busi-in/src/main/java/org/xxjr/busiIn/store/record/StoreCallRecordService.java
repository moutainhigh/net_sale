package org.xxjr.busiIn.store.record;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.store.util.StoreApplyUtils;
import org.xxjr.sys.util.NumberUtil;

@Lazy
@Service
@Slf4j
public class StoreCallRecordService extends BaseService {
	private static final String NAMESPACE = "STORECALLRECORD";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	
	/**
	 * getRecentData
	 * @param params
	 * @return
	 */
	public AppResult getRecentData(AppParam params) {
		return super.query(params, NAMESPACE, "getRecentData");
	}
	
	
	
	/**
	 * recentUpLoadTime
	 * @param params
	 * @return
	 */
	public AppResult recentUpLoadTime(AppParam params) {
		return super.query(params, NAMESPACE, "recentUpLoadTime");
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
	
	@SuppressWarnings("unchecked")
	public AppResult batchUploadCallRecord(AppParam params){
		AppResult result = new AppResult();
		//插入新的数据
		List<Map<String, Object>> list = (List<Map<String, Object>>) params.getAttr("callRecordList");
		for(Map<String,Object> map:list){
			params.addAttrs(map);
			super.insert(params, NAMESPACE);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public AppResult batchRelation(AppParam params){
		AppResult result = new AppResult();
		//插入新的数据
		List<Map<String, Object>> list = (List<Map<String, Object>>) params.getAttr("callRecordList");
		
		if(list != null && list.size() > 0){
			AppParam updateParams = new AppParam();
			updateParams.addAttr("list", list);
			int size = super.getDao().update(NAMESPACE, "updateRelationData",updateParams.getAttr(), updateParams.getDataBase());
			
			if(size > 0){
				AppParam queryParams = new AppParam();
				
				Map<String, String> includeMap = new HashMap<String, String>();
				
				for(Map<String,Object> callRecordMap : list){
					Object borrowTel = callRecordMap.get("borrowTel");
					if(StringUtils.isEmpty(borrowTel)){
						continue;
					}
					
					if(includeMap.containsKey(borrowTel.toString())){
						continue;
					}
					
					includeMap.put(borrowTel.toString(),borrowTel.toString());
					
					queryParams.addAttr("borrowTel", borrowTel);
					AppResult queryResult = this.getRecentData(queryParams);
					Map<String, Object> dataMap = new HashMap<String, Object>();
					if(queryResult.getRows().size() > 0){
						dataMap = queryResult.getRow(0);
					}
					
					
					if(StringUtils.isEmpty(dataMap.get("applyId"))){
						continue;
					}
					
					Object applyId = dataMap.get("applyId");
					int duration = NumberUtil.getInt(callRecordMap.get("duration"),0);
					AppParam recentParams = new AppParam("storeCallRecentService", "saveOrUpdate");
					recentParams.addAttr("recentTime", dataMap.get("startCallTime"));
					recentParams.addAttr("applyId", applyId);
					recentParams.addAttr("recentStatus", duration > 0 ? 1 : 0);
					recentParams.addAttr("callTimes", NumberUtil.getInt(dataMap.get("callTimes"), 0));
					recentParams.addAttr("sucCallTimes",  NumberUtil.getInt(dataMap.get("sucCallTimes"), 0));
					recentParams.addAttr("totalDuration",  NumberUtil.getInt(dataMap.get("totalDuration"), 0));
					recentParams.addAttr("curDuration", duration);
					
					SoaManager.getInstance().invoke(recentParams);
					// 删除缓存
					RedisUtils.getRedisService().del(StoreApplyUtils.STORE_CALL_RECORD + applyId);
					
					recentParams = null;
					dataMap = null;
					queryResult = null;
				}
				
				includeMap = null;
			}
		}
		return result;
	}

	
	@SuppressWarnings("unchecked")
	public AppResult storeCallbatch(AppParam params){
		AppResult result = new AppResult();
		//插入新的数据
		List<Map<String, Object>> list = (List<Map<String, Object>>) params.getAttr("callRecordList");
		if(list != null && list.size() > 0){
			//按开始通话时间升序排序
			this.listSort(list);
			AppParam queryParams = new AppParam();
			for(Map<String,Object> callRecordMap : list){
				String borrowTel = StringUtil.getString(callRecordMap.get("borrowTel"));
				queryParams.addAttr("borrowTel", borrowTel);
				AppResult queryResult = this.getRecentData(queryParams);
				Map<String, Object> dataMap = new HashMap<String, Object>();
				if(queryResult.getRows().size() > 0 && !StringUtils.isEmpty(queryResult.getRow(0))){
					dataMap = queryResult.getRow(0);
					Object applyId = dataMap.get("applyId");
					int duration = NumberUtil.getInt(callRecordMap.get("duration"),0);
					AppParam recentParams = new AppParam("storeCallRecentService", "saveOrUpdate");
					String startCallTime = DateUtil.toStringByParttern((Date)dataMap.get("startCallTime"), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS);
					recentParams.addAttr("recentTime", startCallTime);
					recentParams.addAttr("firstTime", startCallTime);
					recentParams.addAttr("applyId", applyId);
					recentParams.addAttr("recentStatus", duration > 0 ? 1 : 0);
					recentParams.addAttr("callTimes", NumberUtil.getInt(dataMap.get("callTimes"), 0));
					recentParams.addAttr("sucCallTimes",  NumberUtil.getInt(dataMap.get("sucCallTimes"), 0));
					recentParams.addAttr("totalDuration",  NumberUtil.getInt(dataMap.get("totalDuration"), 0));
					recentParams.addAttr("curDuration", duration);
					
					SoaManager.getInstance().invoke(recentParams);
					
					// 删除缓存
					RedisUtils.getRedisService().del(StoreApplyUtils.STORE_CALL_RECORD + applyId);
				}
				
			}
			
		}
		return result;
	}
	
	/**
	 * list排序
	 * @param resultList
	 */
	public void listSort(List<Map<String, Object>> resultList) {
		try {
			Collections.sort(resultList, new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> map1,
						Map<String, Object> map2) {
					// map1，map2是list中的Map，可以在其内取得值，按其排序，此例为升序，mapStartTime1和mapStartTime2是排序字段值
					long mapStartTime1 = ((Date)map1.get("startCallTime")).getTime();
					long mapStartTime2 = ((Date)map2.get("startCallTime")).getTime();
					if (mapStartTime1 > mapStartTime2) {
						return 1;
					} else {
						return -1;
					}
				}
			});
		} catch (Exception e) {
			log.error("listSort error", e);
		}
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
	 * 門店人員通話記錄統計
	 * 
	 * @param params
	 * @return
	 */
	public AppResult sumStoreCall(AppParam params) {
		return super.query(params, NAMESPACE, "sumStoreCall");
	}
	
	/**
	 * 門店人員通話記錄統計
	 * 
	 * @param params
	 * @return
	 */
	public AppResult sumStoreCallByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "sumStoreCall","sumStoreCallCount");
	}
	
	/**
	 * sumStoreCallCount
	 * @param params
	 * @return
	 */
	public AppResult sumStoreCallCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "sumStoreCallCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * 门店今日通话记录
	 * 
	 * @param params
	 * @return
	 */
	public AppResult sumOrgCallByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "sumOrgCall","sumOrgCallCount");
	}
	/**
	 * sumOrgCallCount
	 * @param params
	 * @return
	 */
	public AppResult sumOrgCallCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "sumOrgCallCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**
	 * 查询出当天所需统计的人和日期
	 * 
	 * @param params
	 * @return
	 */
	public AppResult getCountParamsByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "getCountParams","getCountParamsCount");
	}
	
	
	/**
	 * 查询出当天所需统计的人和日期
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryCallRecord(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryCallRecord","queryCallRecordCount");
	}
	
	/**
	 * 查询未关联的通话记录
	 * @param params
	 * @return
	 */
	public AppResult queryNotRelationRecord(AppParam params) {
		return super.query(params, NAMESPACE,"queryNotRelationRecord");
	}
	
}
