package org.xxjr.cust.fund;

import java.util.Date;

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
import org.xxjr.cust.util.FundConstant;

@Lazy
@Service
public class FundRecordService extends BaseService {
	private static final String NAMESPACE = "FUNDRECORD";

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
	 * queryView
	 * @param params
	 * @return
	 */
	public AppResult queryView(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryView", "queryViewCount");
	}
	
	/**
	 * queryViewCount
	 * @param params
	 * @return
	 */
	public AppResult queryViewCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryViewCount",params.getAttr(),params.getDataBase());
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
		if(StringUtils.isEmpty(params.getAttr("fundType"))||
				StringUtils.isEmpty(params.getAttr("amount"))||
				StringUtils.isEmpty(params.getAttr("customerId"))||
				StringUtils.isEmpty(params.getAttr("usableAmount"))){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		params.addAttr("createTime", new Date());
		params.addAttr("createBy", DuoduoSession.getUserName());
		return super.insert(params, NAMESPACE);
	}
	
	
	/***
	 * 提现申请
	 * @param context
	 * @return
	 */
	public AppResult withdrawApply(AppParam params) {
		params.addAttr(FundConstant.key_fundType, FundConstant.FundType_withdrawApply);
		int size = super.getDao().update(NAMESPACE, "withdrawApply", params.getAttr(), 
				params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	/***
	 * 提现取消
	 * @param context
	 * @return
	 */
	public AppResult withdrawCancel(AppParam params) {
		params.addAttr(FundConstant.key_fundType, FundConstant.FundType_withdrawCancel);
		int size = super.getDao().update(NAMESPACE, "withdrawCancel", params.getAttr(), 
				params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	/***
	 * 提现成功
	 * @param context
	 * @return
	 */
	public AppResult withdrawCheck(AppParam params) {
		params.addAttr(FundConstant.key_fundType, FundConstant.FundType_withdraw);
		int size = super.getDao().update(NAMESPACE, "withdrawCheck", params.getAttr(), 
				params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	/***
	 * 提成处理
	 * @param context
	 * @return
	 */
	public AppResult rewardOrder(AppParam params) {
		params.addAttr(FundConstant.key_fundType, FundConstant.FundType_rewardOrder);
		int size = super.getDao().update(NAMESPACE, "rewardOrder", params.getAttr(), 
				params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	/***
	 * 师傅提成处理
	 * @param context
	 * @return
	 */
	public AppResult rewardOrderParent(AppParam params) {
		params.addAttr(FundConstant.key_fundType, FundConstant.FundType_rewardOrderParent);
		int size = super.getDao().update(NAMESPACE, "rewardOrder", params.getAttr(), 
				params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	/**
	 * funcRecord
	 * @param params
	 * @return
	 */
	public AppResult funcRecord(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "funcRecord", "funcRecordCount");
	}
}
