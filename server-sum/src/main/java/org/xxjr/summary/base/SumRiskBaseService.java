package org.xxjr.summary.base;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;

@Lazy
@Service
public class SumRiskBaseService extends BaseService {
	private static final String NAMESPACE = "SUMRISKBASE";

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
		return result;
	}
	
	/**
	 * 保存风控基本统计数据
	 * @param params
	 * @return
	 */
	public AppResult save(AppParam params){
		super.getDao().delete(NAMESPACE, "deleteByDay", params.getAttr(), params.getDataBase().toString());
		AppResult riskResult = this.insert(params);
		return riskResult;
	}
	
	/**
	 * 风控数据日统计
	 * queryMonth
	 * @param params
	 * @return
	 */
	public AppResult queryRiskDay(AppParam params) {
		if(params.getCurrentPage() == -1){
			params.setEveryPage(1000);
			params.setCurrentPage(1);
			return super.queryByPage(params, NAMESPACE,"queryRiskDay", "queryRiskDayCount");
		}else{
			return super.queryByPage(params, NAMESPACE,"queryRiskDay", "queryRiskDayCount");
		}
	}
	
	/**
	 * 风控数据月统计
	 * queryMonth
	 * @param params
	 * @return
	 */
	public AppResult queryRiskMonth(AppParam params) {
		if(params.getCurrentPage() == -1){
			params.setEveryPage(1000);
			params.setCurrentPage(1);
			return super.queryByPage(params, NAMESPACE,"queryRiskMonth", "queryRiskMonthCount");
		}else{
			return super.queryByPage(params, NAMESPACE,"queryRiskMonth", "queryRiskMonthCount");
		}
	}
	/**
	 * 风控本月统计总计
	 * @param params
	 * @return
	 */
	public AppResult queryRiskDaySum(AppParam params) {
		return super.query(params, NAMESPACE,"queryRiskDaySum");
	}
	
	/**
	 * 风控月度统计总计
	 * @param params
	 * @return
	 */
	public AppResult queryRiskMonthSum(AppParam params) {
		return super.query(params, NAMESPACE,"queryRiskMonthSum");
	}
}
