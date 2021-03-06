package org.xxjr.summary.base;

import java.util.Date;

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
import org.xxjr.sys.util.NumberUtil;

@Lazy
@Service
public class SumStorePauseAllotService extends BaseService {
	private static final String NAMESPACE = "SUMSTOREPAUSEALLOT";

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
		params.addAttr("updateTime", new Date());
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
				param.addAttr("customerId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("customerId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.setDataBase(DBConst.Key_sum_DB);
				param.addAttr("pauseDate", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("pauseDate"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**
	 * saveOrUpdate
	 * @param params
	 * @return
	 */
	public AppResult saveOrUpdate(AppParam params) {
		AppResult updateResult = this.update(params);
		int count = NumberUtil.getInt(updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
		if(count <= 0){
			AppParam insertParams = new AppParam();
			insertParams.addAttr("customerId", params.getAttr("customerId"));
			insertParams.addAttr("orgId", params.getAttr("orgId"));
			insertParams.addAttr("pauseDate", params.getAttr("pauseDate"));
			insertParams.setDataBase(DBConst.Key_sum_DB);
			updateResult = this.insert(insertParams);
		}
		return updateResult;
	}
	
	/**
	 * 查询门店人员暂停情况列表
	 * @param params
	 * @return
	 */
	public AppResult queryStorePauseAllotList(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryStorePauseAllotList","queryStorePauseAllotCount");
	}
	/**
	 * queryStorePauseAllotCount
	 * @param params
	 * @return
	 */
	public AppResult queryStorePauseAllotCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryStorePauseAllotCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**
	 * 查询门店暂停情况列表
	 * @param params
	 * @return
	 */
	public AppResult queryOrgPauseAllotList(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryOrgPauseAllotList","queryOrgPauseAllotCount");
	}
	
	/**
	 * queryOrgPauseAllotCount
	 * @param params
	 * @return
	 */
	public AppResult queryOrgPauseAllotCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryOrgPauseAllotCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * 查询门店人员暂停情况总计
	 * @param params
	 * @return
	 */
	public AppResult queryStorePauseAllotSum(AppParam params) {
		return super.query(params, NAMESPACE,"queryStorePauseAllotSum");
	}
	
	/**
	 * 查询门店暂停情况总计
	 * @param params
	 * @return
	 */
	public AppResult queryOrgPauseAllotSum(AppParam params) {
		return super.query(params, NAMESPACE,"queryOrgPauseAllotSum");
	}
	
	/**
	 * 删除暂停分单统计信息(通过用户ID删除)
	 * @param params
	 * @return
	 */
	public AppResult deleteByCustId(AppParam params) {
		int size = getDao().delete(NAMESPACE, "deleteByCustId",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Delete_SIZE, size);
		return result;
	}
	
}
