package org.xxjr.summary.base;

import java.util.Date;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;
import org.xxjr.sys.util.NumberUtil;
import org.ddq.common.exception.DuoduoError;
import org.llw.common.core.service.BaseService;

@Lazy
@Service
public class SumTotalTeamService extends BaseService {
	private static final String NAMESPACE = "SUMTOTALTEAM";

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
				param.addAttr("teamNo", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("teamNo"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**
	 * 更新花费
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult updateTeamCost(AppParam params){
		List<Map<String,Object>> dataList = (List<Map<String, Object>>) params.getAttr("dataList");
		IntSummaryStatistics collect = dataList.stream().collect(Collectors.summarizingInt(((e) ->{
				int size = super.getDao().update(NAMESPACE, "update", (Map<String,Object>)e, params.getDataBase());
				return size;
			}
		)));
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Update_SIZE, collect.getSum());
		return backContext;
	}
	
	
	/**
	 * 更新挂卖失败金额
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult updateTeamFailSale(AppParam params){
		List<Map<String,Object>> dataList = (List<Map<String, Object>>) params.getAttr("dataList");
		IntSummaryStatistics collect = dataList.stream().collect(Collectors.summarizingInt(((e) ->{
				int size = super.getDao().update(NAMESPACE, "updateTeamFailSale", (Map<String,Object>)e, params.getDataBase());
				return size;
			}
		)));
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Update_SIZE, collect.getSum());
		return backContext;
	}
	
	@SuppressWarnings("unchecked")
	public AppResult batchSave (AppParam params) {
		AppResult result = new AppResult();
		List<Map<String, Object>> dataList = (List<Map<String, Object>>) params.getAttr("dataList");
		IntSummaryStatistics collect = dataList.stream().collect(Collectors.summarizingInt((e) -> {
			AppParam saveParam = new AppParam();
			saveParam.addAttrs((Map<String, Object>)e);
			saveParam.setDataBase(DBConst.Key_sum_DB);
			AppResult saveResult = this.update(saveParam);
			if (NumberUtil.getInt(saveResult.getAttr(DuoduoConstant.DAO_Update_SIZE), 0) > 0) {
				return NumberUtil.getInt(saveResult.getAttr(DuoduoConstant.DAO_Update_SIZE), 0);
			}
			saveResult = this.insert(saveParam);
			return NumberUtil.getInt(saveResult.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
		}));
		result.putAttr("count", collect.getSum());
		return result;
	}
	
	
	/**
	 * 总的数据日统计
	 * queryMonth
	 * @param params
	 * @return
	 */
	public AppResult queryDay(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryDay", "queryDayCount");
	}
	/**
	 * 总的数据统计
	 * @param params
	 * @return
	 */
	public AppResult queryDayCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryDayCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * 总的数据日统计
	 * queryMonth
	 * @param params
	 * @return
	 */
	public AppResult queryMonth(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryMonth", "queryMonthCount");
	}
	/**
	 * 总的数据统计
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
	 * querySection区间
	 * @param params
	 * @return
	 */
	public AppResult querySection(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"querySection", "querySectionCount");
	}
	
	/**
	 * 总的数据统计
	 * @param params
	 * @return
	 */
	public AppResult querySectionCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "querySectionCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
}
