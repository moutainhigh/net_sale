package org.xxjr.summary.base;

import java.io.Serializable;
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
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.StoreSeparateUtils;
import org.xxjr.sys.util.DBConst;


@Lazy
@Service
public class SumStoreBaseMonthService extends BaseService {
	private static final String NAMESPACE = "SUMSTOREBASEMONTH";

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
				param.setDataBase(DBConst.Key_sum_DB);
				param.addAttr("recordDate", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("recordDate"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.setDataBase(DBConst.Key_sum_DB);
				param.addAttr("customerId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("customerId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	
	/**
	 * 保存门店人员基本统计数据
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult save(AppParam params){
		AppResult result = new AppResult();
		//删除老的数据
		super.getDao().delete(NAMESPACE, "deleteByMonth", params.getAttr(), params.getDataBase().toString());
		//插入新的数据
		List<Map<String, Object>> list = (List<Map<String, Object>>) params.getAttr("list");
		int size = super.getDao().batchInsert(NAMESPACE, "batchInsert",
				list, params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Insert_SIZE, size);
		return result;
	}
	
	/**
	 * 门店人员月统计
	 * @param params
	 * @return
	 */
	public AppResult queryStoreBaseMonth(AppParam params){
		return super.queryByPage(params, NAMESPACE, "queryStoreBaseMonth", "queryStoreBaseMonthCount");
	}
	/**
	 * queryStoreBaseMonthCount
	 * @param params
	 * @return
	 */
	public AppResult queryStoreBaseMonthCount(AppParam params) {
		int size = getDao().count(NAMESPACE,"queryStoreBaseMonthCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**
	 * 查询本月工作信息
	 * @param params
	 * @return
	 */
	public AppResult queryToMonthWork(AppParam params) {
		return super.query(params, NAMESPACE, "queryToMonthWork");
	}
	/**
	 * 门店人员月度统计总计
	 * @param params
	 * @return
	 */
	public AppResult queryStoreMonthTotal(AppParam params) {
		return super.query(params, NAMESPACE, "queryStoreMonthTotal");
	}
	
	/**
	 * insertStoreBase
	 * @param params
	 * @return
	 */
	public AppResult insertStoreBaseMonth(AppParam params) {
		//查询是否存在记录，如果不存在则插入数据
		AppParam queryParams = new AppParam();
		queryParams.addAttr("recordDate", params.getAttr("recordDate"));
		queryParams.addAttr("customerId", params.getAttr("customerId"));
		queryParams.setDataBase(DBConst.Key_sum_DB);
		AppResult result = this.query(queryParams);
		String costCount = StringUtil.getString(params.getAttr("costCount"));
		if(result.getRows().size() == 0){
			result = super.insert(params, NAMESPACE);
		}else if(!StringUtils.isEmpty(costCount)){
			AppParam updateParams = new AppParam();
			updateParams.setDataBase(DBConst.Key_sum_DB);
			updateParams.addAttr("recordDate", params.getAttr("recordDate"));
			updateParams.addAttr("customerId", params.getAttr("customerId"));
			updateParams.addAttr("costCount", costCount);
			result = this.update(updateParams);
		}
		return result;
	}
	
	/**
	 * 更新门店人员成本统计
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult updateStoreCost(AppParam params) {
		String orgId = StringUtil.getString(params.getAttr("orgId"));
		AppResult result = new AppResult();
		List<Map<String, Object>> costList = (List<Map<String, Object>>) params.getAttr("costList");
		int totalSize = 0;
		for (Map<String, Object> map : costList) {
			params.removeAttr("costList");
			params.setAttr(map);
			int updateSize = getDao().update(NAMESPACE,"updateStoreCost",params.getAttr(),params.getDataBase());
			if(updateSize > 0){
				AppParam updateParams = new AppParam("storeCostMonthRecordService","save");
				updateParams.addAttrs(params.getAttr());
				SoaManager.getInstance().invoke(updateParams);
			}
			totalSize += updateSize;
		}
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, totalSize);
		if(totalSize > 0){
			RedisUtils.getRedisService().set(StoreSeparateUtils.STORE_ORG_COSTDEAL_COUNT_KEY_ 
					+ orgId,(Serializable) 1,StoreSeparateUtils.base_cache_time);
		}
		return result;
	}
	
	/**
	 * 查询门店人员月度成本
	 * @param params
	 * @return
	 */
	public AppResult queryStoreMonthCost(AppParam params) {
		return super.query(params, NAMESPACE, "queryStoreMonthCost");
	}
}
