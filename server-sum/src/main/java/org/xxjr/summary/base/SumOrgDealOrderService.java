package org.xxjr.summary.base;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;
import org.ddq.common.exception.DuoduoError;
import org.llw.common.core.service.BaseService;

@Lazy
@Service
public class SumOrgDealOrderService extends BaseService {
	private static final String NAMESPACE = "SUMORGDEALORDER";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	/**订单状态分组统计（门店） 本月
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult queryOrgDealOrderDay(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryOrgDealOrderDay","queryOrgDealOrderDayCount");
	}
	
	/**
	 * queryOrgDealOrderDayCount
	 * @param params
	 * @return
	 */
	public AppResult queryOrgDealOrderDayCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryOrgDealOrderDayCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
		
	/**订单状态分组统计（门店） 月度
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult queryOrgDealOrderMonth(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryOrgDealOrderMonth","queryOrgDealOrderMonthCount");
	}

	/**
	 * queryOrgDealOrderDayCount
	 * @param params
	 * @return
	 */
	public AppResult queryOrgDealOrderMonthCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryOrgDealOrderMonthCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**订单状态分组统计合计（门店） 本月
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult queryOrgDealOrderDaySum(AppParam params) {
		return super.query(params, NAMESPACE,"queryOrgDealOrderDaySum");
	}
	/**订单状态分组合计（门店） 月度
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult queryOrgDealOrderMonthSum(AppParam params) {
		return super.query(params, NAMESPACE,"queryOrgDealOrderMonthSum");
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
				param.addAttr("orgId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("orgId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.setDataBase(DBConst.Key_sum_DB);
				param.addAttr("groupName", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("groupName"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	/**
	 * 订单状态渠道统计
	 * @param params
	 * @return
	 */
	public AppResult save(AppParam params){
		AppResult result = new AppResult();
		// 删除旧数据
		super.getDao().delete(NAMESPACE, "deleteByDay", params.getAttr(), params.getDataBase());
		//插入新的数据
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) params.getAttr("list");
		int size = super.getDao().batchInsert(NAMESPACE, "batchInsert",
				list, params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Insert_SIZE, size);
		return result;
	}
}
