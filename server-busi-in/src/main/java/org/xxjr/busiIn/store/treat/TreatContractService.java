package org.xxjr.busiIn.store.treat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.ddq.active.mq.store.StoreTaskSend;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.StoreConstant;
import org.xxjr.store.util.StoreApplyUtils;

@Lazy
@Service
@Slf4j
public class TreatContractService extends BaseService {
	private static final String NAMESPACE = "TREATCONTRACT";

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
		AppResult result = new AppResult();
		params.addAttr("createTime", new Date());
		result = super.insert(params, NAMESPACE);
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_CONTRACT_RECORD + params.getAttr("applyId"));
		return result;
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		AppResult result = new AppResult();
		result = super.update(params, NAMESPACE);
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_CONTRACT_RECORD + params.getAttr("applyId"));
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
				param.addAttr("contractId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("contractId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**
	 * 删除数据
	 * @param params
	 * @return
	 */
	public AppResult deleteBy(AppParam params) {
		int size = getDao().delete(NAMESPACE, "deleteBy",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_CONTRACT_RECORD + params.getAttr("applyId"));
		return result;
	}
	
	/**
	 * 保存数据
	 * @param params
	 * @return
	 * @throws ParseException 
	 */
	@SuppressWarnings("unchecked")
	public AppResult saveData(AppParam params) throws ParseException {
		String applyId = StringUtil.getString(params.getAttr("applyId"));
		AppResult result = new AppResult();
		if(!StringUtils.isEmpty(applyId)){
			AppResult appResuklt = this.deleteBy(params);
			if(appResuklt.isSuccess()){
				List<Map<String,Object>> recordList = (List<Map<String,Object>>)params.getAttr("recordList");
				String reContractId = StringUtil.getString(params.getAttr("reContractId"));
				String contractStatus = StringUtil.getString(params.getAttr("contractStatus"));
				for(Map<String,Object> map : recordList){
					String approvalTime = null;
					if(!StringUtils.isEmpty(map.get("ApprovalTime"))){
						DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						Date date = sdf.parse(StringUtil.getString(map.get("ApprovalTime")));
						SimpleDateFormat simpFormat = new SimpleDateFormat ("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK);
				        Date payDate =  simpFormat.parse(date.toString());
				        approvalTime = DateUtil.toStringByParttern(payDate, DateUtil.DATE_PATTERN_YYYY_MM_DD);
				        
					}
					AppParam updateParams  = new AppParam();
					updateParams.addAttr("applyId",applyId);
					updateParams.addAttr("reContractId",reContractId);
					updateParams.addAttr("contractStatus",contractStatus);
					updateParams.addAttr("loanNo",map.get("LoanNo"));
					updateParams.addAttr("loanDesc",map.get("LoanDecs"));
					updateParams.addAttr("approvalAmount",map.get("ApprovalAmount"));
					updateParams.addAttr("approvalTime",approvalTime);
					updateParams.addAttr("status",map.get("Status"));
					updateParams.addAttr("lendDate",map.get("LendDate"));
					updateParams.addAttr("queryStatus", 2); //查询状态 1-未查询 2-查询成功 3-查询失败
					updateParams.addAttr("errorMessage", "");
					result = this.insert(updateParams);
					if(!StringUtils.isEmpty(map.get("LendRecords"))){
						List<Map<String,Object>> listRecord = (List<Map<String,Object>>) map.get("LendRecords");
						AppParam recordParams  = new AppParam();
						recordParams.addAttr("applyId",applyId);
						recordParams.addAttr("reContractId",reContractId);
						recordParams.addAttr("loanNo",map.get("LoanNo"));
						recordParams.addAttr("listRecord",listRecord);
						recordParams.setService("treatLoanRecordService");
						recordParams.setMethod("saveData");
						SoaManager.getInstance().invoke(recordParams);
					}
				}
				//更新签单状态
				String status = "1";
				AppParam queryParams  = new AppParam("treatInfoService","query");
				queryParams.addAttr("applyId",applyId);
				AppResult queryResult = SoaManager.getInstance().invoke(queryParams);
				if(queryResult.getRows().size() > 0 && !StringUtils.isEmpty(queryResult.getRow(0))){
					AppParam treatParams  = new AppParam("treatInfoService","updateSignInfo");
					AppParam treatHistoryParams  = new AppParam("treatInfoHistoryService","update");
					String oldSignStatus = StringUtil.getString(queryResult.getRow(0).get("status"));
					String treatyNo = StringUtil.getString(queryResult.getRow(0).get("treatyNo"));
					//签单状态为结案的不再更新状态
					if(!"2".equals(oldSignStatus)){
						//只保留提交按揭中、贷款未提交、全部结案
						if("1".equals(contractStatus)){
							status = StoreConstant.STORE_SIGN_5;
						}else if("2".equals(contractStatus)){
							status = StoreConstant.STORE_SIGN_1;
						}else{
							status = StoreConstant.STORE_SIGN_2;
						}
						treatParams.addAttr("status",status);
						treatHistoryParams.addAttr("status",status);
						String customerId = StringUtil.getString(params.getAttr("customerId"));
						treatParams.addAttr("applyId",applyId);
						treatParams.addAttr("customerId",customerId);
						SoaManager.getInstance().invoke(treatParams);
						
						treatHistoryParams.addAttr("treatyNo",treatyNo);
						treatHistoryParams.addAttr("applyId",applyId);
						treatHistoryParams.addAttr("customerId",customerId);
						SoaManager.getInstance().invoke(treatHistoryParams);
						try{
							//创建任务对象 更新签单后调用mq
							StoreTaskSend storeSend = (StoreTaskSend)SpringAppContext.getBean(StoreTaskSend.class);
							Map<String, Object> msgParam = new HashMap<String, Object>();
							msgParam.put("recordDate", DateUtil.getSimpleFmt(new Date()));
							storeSend.sendStoreMessage(customerId,"signDealType" , msgParam);
						}catch (Exception e) {
							log.error("saveData error", e);
							ExceptionUtil.setExceptionMessage(e, result,
									DuoduoSession.getShowLog());
						}
					}
				}
					
			}
			
		}
		return result;
	}
	
	/**
	 * 删除数据
	 * @param params
	 * @return
	 * @throws ParseException 
	 */
	public AppResult deleteData(AppParam params) throws ParseException {
		String applyId = StringUtil.getString(params.getAttr("applyId"));
		AppResult result = new AppResult();
		if(!StringUtils.isEmpty(applyId)){
			AppResult appResult = this.deleteBy(params);
			if(appResult.isSuccess()){
				result = this.insert(params);
			}
		}
		return result;
	}
}
