package org.xxjr.summary.base;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;

@Lazy
@Service
public class SumStoreCallService extends BaseService {
	private static final String NAMESPACE = "SUMSTORECALL";

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
	 * 保存门店人员基本统计数据
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult save(AppParam params){
		AppResult result = new AppResult();
		//删除老的数据
		super.getDao().delete(NAMESPACE, "deleteByDay", params.getAttr(), params.getDataBase().toString());
		//插入新的数据
		List<Map<String, Object>> list = (List<Map<String, Object>>) params.getAttr("list");
		int size = super.getDao().batchInsert(NAMESPACE, "batchInsert",
				list, params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Insert_SIZE, size);
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
	 * 每天門店人員的通話統計
	 * queryShow
	 * @param params
	 * @return
	 */
	public AppResult queryShow(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryShow", "queryShowCount");
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
	 * 月度門店人員的通話統計
	 * @param params
	 * @return
	 */
	public AppResult queryMonth(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryMonth", "queryMonthCount");
	}
	/**
	 * queryMonthCount
	 * @param params
	 * @return
	 */
	public AppResult queryMonthCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryMonthCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**
	 * 本月门店的通话统计
	 * queryShow
	 * @param params
	 * @return
	 */
	public AppResult queryStoreCallDay(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryStoreCallDay", "queryStoreCallDayCount");
	}
	/**
	 * queryStoreCallDayCount
	 * @param params
	 * @return
	 */
	public AppResult queryStoreCallDayCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryStoreCallDayCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**
	 * 月度门店的通话统计
	 * @param params
	 * @return
	 */
	public AppResult queryStoreCallMonth(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryStoreCallMonth", "queryStoreCallMonthCount");
	}
	/**
	 * queryStoreCallMonthCount
	 * @param params
	 * @return
	 */
	public AppResult queryStoreCallMonthCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryStoreCallMonthCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
}
