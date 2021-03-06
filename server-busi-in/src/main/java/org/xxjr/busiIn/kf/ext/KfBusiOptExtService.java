package org.xxjr.busiIn.kf.ext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ddq.active.mq.message.CustMessageSend;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.BorrowConstant;
import org.xxjr.busi.util.CountGradeUtil;
import org.xxjr.busiIn.kf.BorrowApplyService;
import org.xxjr.busiIn.utils.StoreOptUtil;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.NumberUtil;

import lombok.extern.slf4j.Slf4j;

@Lazy
@Service
@Slf4j
public class KfBusiOptExtService extends BaseService {
	public static final String NAMESPACE = "KFBUSIOPTEXT";

	/**
	 * 客服转门店处理 只有客服可以操作
	 * 转门店，状态为待处理
	 * 转门店人员，状态为门店锁定中
	 * @param params
	 * @return
	 */
	public AppResult transferStore(AppParam params) {
		Object applyId = params.getAttr("applyId");
		Object customerId = params.getAttr("customerId");
		Object orgId = params.getAttr("orgId");
		int netFlag = NumberUtil.getInt(params.getAttr("netFlag"), 1) ;//1-网销，2-电销
		if(StringUtils.isEmpty(applyId)|| StringUtils.isEmpty(customerId)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		
		// 判断是否为当前处理人
		Map<String,Object> applyInfo = queryBaseByApplyId(applyId);
		
		if(!CustomerUtil.isAdmin(customerId.toString())){
			if(!StringUtils.isEmpty(applyInfo.get("lastKf")) 
					&& !customerId.equals(applyInfo.get("lastKf").toString())){
				throw new SysException("您不是当前借款处理人，不能处理该借款");
			}
		}
		//退回审核(0-待审 1-审核通过 2-审核失败)',
		int backStatus = NumberUtil.getInt(applyInfo.get("backStatus"), 0);
		String exsitLastStore = StringUtil.getString(applyInfo.get("lastStore"));
		String applyStatus = StringUtil.getString(applyInfo.get("status"));
		
		// 更新
		AppParam updateParam = new AppParam();
		updateParam.addAttr("orgId", orgId);
		updateParam.addAttr("applyId", applyId);
		updateParam.addAttr("netFlag", netFlag);
		updateParam.addAttr("lastKf", customerId);
		updateParam.addAttr("roleType", CustomerUtil.getRoleType(customerId.toString()));
		updateParam.addAttr("lastKfDesc", params.getAttr("handleDesc"));
		Object lastStore = params.getAttr("lastStore");
		updateParam.addAttr("lastStore", lastStore);	
		//管理员可以不加作任何判断
		if(!CustomerUtil.isAdmin(customerId.toString())){
			if(StringUtils.hasText(exsitLastStore) && backStatus == 0){
				throw new SysException("此单已有门店人员处理，不能再转门店！");
			}
			if(BorrowConstant.apply_status_5.equals(applyStatus)){
				throw new SysException("此单已被成功转化了，不能再转门店！");
			}
		}
		
		int size = 0;
		boolean isEnterPool = false;
		if(!StringUtils.isEmpty(lastStore)){
			//查询该笔单网销池是否存在
			AppParam queryNetParam = new AppParam("netStorePoolService", "query");
			queryNetParam.addAttr("applyId", applyId);
			AppResult queryNetResult = SoaManager.getInstance().invoke(queryNetParam);
			if(queryNetResult.getRows().size() > 0){
				return CustomerUtil.retErrorMsg("该笔订单已在网销池中存在！");
			}
			size = this.getDao().update(NAMESPACE, "transferStoreEmployee", updateParam.getAttr(), updateParam.getDataBase());
			if(size == 1){
				//插入门店跟单记录
				this.insertStoreRecord(applyId,lastStore,BorrowConstant.STORE_OPER_0,"客服分单",BorrowConstant.RobWay_1,applyInfo.get("orderType"),0,1);
			}
		}else{
			size = this.getDao().update(NAMESPACE, "transferStore", updateParam.getAttr(), updateParam.getDataBase());
			if(size == 1){
				//插入转门店记录
				this.insertStoreRecord(applyId,null,BorrowConstant.STORE_OPER_f1,"客服转门店",0,applyInfo.get("orderType"),0,1);
			}
			isEnterPool = true;
		}
		if(1 == size){
			// 插入客服跟单记录
			AppParam recordParam = new AppParam();
			recordParam.setService("borrowKfRecordService");
			recordParam.setMethod("insert");
			recordParam.addAttr("applyId", applyId);
			recordParam.addAttr("handleDesc", params.getAttr("handleDesc"));
			recordParam.addAttr("kf", customerId);
			recordParam.addAttr("handleType", BorrowConstant.KEFU_OPER_2);
			SoaManager.getInstance().invoke(recordParam);
			
			
			//查询该笔单是否存在
			AppParam queryStoreParam = new AppParam("borrowStoreApplyService", "query");
			queryStoreParam.addAttr("applyId", applyId);
			AppResult queryResult = SoaManager.getInstance().invoke(queryStoreParam);
			//加入网销申请表
			AppParam applyParam = new AppParam();
			applyParam.addAttr("applyId", applyId);
			applyParam.setService("borrowStoreApplyService");
			if (queryResult.getRows().size() > 0) {
				applyParam.setMethod("updateByBorrowApply");
			}else{
				applyParam.setMethod("insertByBorrowApply");
			}
			SoaManager.getInstance().invoke(applyParam);	
			
			if(!StringUtils.isEmpty(lastStore)){
				//更新订单处理人
				AppParam updateApplyParam = new AppParam("borrowStoreApplyService", "update");
				updateApplyParam.addAttr("applyId", applyId);
				updateApplyParam.addAttr("lastStore", lastStore);
				SoaManager.getInstance().invoke(updateApplyParam);
			}
			
			//转移到网销池或电销池
			if(isEnterPool){
				
				Date todayTime = new Date();
				String todayDate =  DateUtil.toStringByParttern(todayTime, 
						DateUtil.DATE_PATTERN_YYYY_MM_DD);
				
				AppParam netParam = new AppParam();
				netParam.addAttr("applyId", applyId);
				netParam.addAttr("cityName", applyInfo.get("cityName"));
				netParam.addAttr("gradeType", applyInfo.get("grade"));
				netParam.addAttr("orderType", applyInfo.get("orderType"));
				netParam.addAttr("createTime",todayTime);
				netParam.addAttr("recordDate", todayDate);
				netParam.addAttr("applyTime", applyInfo.get("applyTime"));
				netParam.addAttr("orgId", applyInfo.get("orgId"));
				netParam.addAttr("lastStore", applyInfo.get("lastStore"));
				netParam.addAttr("nextRecordDate",todayDate);
				
				if(netFlag == 1){//网销
					netParam.setService("netStorePoolService");
					netParam.setMethod("delete");
					SoaManager.getInstance().invoke(netParam);
					
					netParam.setMethod("insert");
					SoaManager.getInstance().invoke(netParam);
				}
				
				if(netFlag == 3){//电销
					netParam.setService("eleStorePoolService");
					netParam.setMethod("delete");
					SoaManager.getInstance().invoke(netParam);
					
					netParam.setMethod("insert");
					SoaManager.getInstance().invoke(netParam);
				}
			}
		}else{
			throw new SysException("操作不成功，请重试！");
		}
		return new AppResult();
	}
	
	/**
	 * 转普通单 只有客服可以操作
	 * @param params
	 * @return
	 */
	public AppResult transferCommon(AppParam params) {
		Object applyId = params.getAttr("applyId");
		Object customerId = params.getAttr("customerId");
		if(StringUtils.isEmpty(applyId) || StringUtils.isEmpty(customerId)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		
		// 判断是否为当前处理人
		Map<String,Object> applyInfo = queryBaseByApplyId(applyId);
		if(!CustomerUtil.isAdmin(customerId.toString())){
			if(!StringUtils.isEmpty(applyInfo.get("lastKf")) 
					&& !customerId.equals(applyInfo.get("lastKf").toString())){
				throw new SysException("您不是当前借款处理人，不能处理该借款");
			}
		}

		double loanAmount = NumberUtil.getDouble(applyInfo.get("loanAmount"),0);
		
		if(loanAmount <=0){
			throw new SysException("金额不能为空或0");
		}
		
		// 更新
		AppParam updateParam = new AppParam();
		updateParam.addAttr("applyId", applyId);
		updateParam.addAttr("lastKf", customerId);
		updateParam.addAttr("roleType", CustomerUtil.getRoleType(customerId.toString()));
		updateParam.addAttr("lastKfDesc", params.getAttr("handleDesc"));
		int size = this.getDao().update(NAMESPACE, "transferCommon", updateParam.getAttr(), updateParam.getDataBase());
		if(1 == size){
			// 插入客服跟进记录
			AppParam recordParam = new AppParam();
			recordParam.setService("borrowKfRecordService");
			recordParam.setMethod("insert");
			recordParam.addAttr("applyId", applyId);
			recordParam.addAttr("handleDesc", params.getAttr("handleDesc"));
			recordParam.addAttr("kf", customerId);
			recordParam.addAttr("handleType", BorrowConstant.KEFU_OPER_4);
			SoaManager.getInstance().invoke(recordParam);
		}
		return new AppResult();
	}
	
	/**
	 * 转垃圾单 只有客服可以操作
	 * @param params
	 * @return
	 */
	public AppResult transferRubbish(AppParam params) {
		AppResult result = new AppResult();
		Object applyId = params.getAttr("applyId");
		Object customerId = params.getAttr("customerId");
		if(StringUtils.isEmpty(applyId) || StringUtils.isEmpty(customerId)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		Map<String,Object> applyInfo = queryBaseByApplyId(applyId);
		String applyStatus = StringUtil.getString(applyInfo.get("status"));
		
		if("3".equals(applyInfo.get("applyType").toString())){
			throw new SysException("当前借款类型已经为垃圾单！");
		}
		
		if(BorrowConstant.apply_status_5.equals(applyStatus)){
			throw new SysException("转化成功的单子不能转垃圾单！");
		}
		
		if(BorrowConstant.apply_status_2.equals(applyStatus)){
			throw new SysException("转化成功的单子不能转垃圾单！");
		}
		
		if(!CustomerUtil.isAdmin(customerId.toString())){
			if(!StringUtils.isEmpty(applyInfo.get("lastKf")) 
					&& !customerId.equals(applyInfo.get("lastKf").toString())){
				throw new SysException("您不是当前借款处理人，不能处理该借款");
			}
		}
		
		// 更新
		AppParam updateParam = new AppParam();
		updateParam.addAttr("applyId", applyId);
		updateParam.addAttr("lastKf", customerId);
		updateParam.addAttr("roleType", CustomerUtil.getRoleType(customerId.toString()));
		updateParam.addAttr("lastKfDesc", params.getAttr("handleDesc"));
		int size = this.getDao().update(NAMESPACE, "transferRubbish", updateParam.getAttr(), updateParam.getDataBase());
		if(1 == size){
			// 插入客服跟进记录
			AppParam recordParam = new AppParam();
			recordParam.setService("borrowKfRecordService");
			recordParam.setMethod("insert");
			recordParam.addAttr("applyId", applyId);
			recordParam.addAttr("handleDesc", params.getAttr("handleDesc"));
			recordParam.addAttr("kf", customerId);
			recordParam.addAttr("handleType", BorrowConstant.KEFU_OPER_5);
			SoaManager.getInstance().invoke(recordParam);

			try{
				Object applyCustId = applyInfo.get("applyCustId");
				if(!StringUtils.isEmpty(applyCustId)){
					CustMessageSend messageSend = (CustMessageSend) SpringAppContext.getBean(CustMessageSend.class);
					// 推送消息
					Map<String, Object> paramsMap = new HashMap<String,Object>();
				
					paramsMap.put("applyName", applyInfo.get("applyName"));
					paramsMap.put("customerId", applyCustId);
					messageSend.sendCustMessage(applyCustId.toString(),"invalidApplyNotice", paramsMap);
					
				}
				
			}catch(Exception e){
				log.error("无效申请发送推送消息失败", e);
			}
		}
		return result;
	}
	
	/**
	 * 录单
	 * @param params
	 * @return
	 */
	public AppResult recordBorrow(AppParam params) {
		Object telephone = params.getAttr("telephone");
		String applyType = StringUtil.getString(params.getAttr("applyType"));
		Object recordCustId = params.getAttr("customerId");
		if(StringUtils.isEmpty(telephone) || StringUtils.isEmpty(applyType) 
				|| StringUtils.isEmpty(params.getAttr("loanAmount"))
				|| StringUtils.isEmpty(params.getAttr("applyName"))){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		
		// 效验手机号 存在借款记录给出提示
		AppParam queryParam = new AppParam();
		queryParam.addAttr("telephone", telephone);
		queryParam.setService("borrowApplyService");
		queryParam.setMethod("querySimpleInfo");
		AppResult queryResult = SoaManager.getInstance().invoke(queryParam);
		if(queryResult.getRows().size() > 0){
			Map<String,Object> borrowInfo = queryResult.getRow(0);
			throw new SysException("该手机号已申请过贷款，当前贷款状态为"+borrowInfo.get("statusDesc")
					+",客服状态为"+borrowInfo.get("kfStatusDesc"));
		}
		
		// 增加贷款记录
		AppParam applyParam = new AppParam();
		
		applyParam.addAttr("telephone", params.getAttr("telephone"));
		applyParam.addAttr("applyName", params.getAttr("applyName"));
		applyParam.addAttr("channelDetail", params.getAttr("channelDetail"));
		applyParam.addAttr("applyType", applyType);
		applyParam.addAttr("recorder", recordCustId);
		applyParam.addAttr("haveDetail", 1);
		
		String grade = CountGradeUtil.getGrade(params.getAttr()) ;

		applyParam.addAttr("grade", grade);
		
		applyParam.setService("borrowApplyService");
		applyParam.setMethod("insert");
		//默认有编辑权限
		int haveEdit = NumberUtil.getInt(params.getAttr("haveEdit"),1);
		
		if(haveEdit == 0){
			if(BorrowConstant.apply_type_2 == Integer.parseInt(applyType)){
				applyParam.addAttr("status", "3");
			}
			
		}else{
			applyParam.addAttr("lockBy", recordCustId);
			applyParam.addAttr("lastKf", recordCustId);
			applyParam.addAttr("status", "1");
			applyParam.addAttr("kfStatus", "1");
			applyParam.addAttr("lockTime", new Date());
		}
	
		AppResult result = SoaManager.getInstance().invoke(applyParam);
		Object applyId = result.getAttr("applyId");
		params.addAttr("applyId", applyId);
		
		if(result.isSuccess() && !StringUtils.isEmpty(applyId)){
			//保存基本信息
			params.addAttr("score", 10000);
			params.addAttr("price", 10000);
			
			AppParam delParam = new AppParam("borrowBaseService", "delete");
			delParam.addAttr("applyId", applyId);
			SoaManager.getInstance().invoke(delParam);
			
			params.setService("borrowBaseService");
			params.setMethod("insert");
			SoaManager.getInstance().invoke(params);
			
			//保存收入信息
			delParam.setService("borrowIncomeService");
			SoaManager.getInstance().invoke(delParam);
			
			params.setService("borrowIncomeService");
			params.setMethod("insert");
			SoaManager.getInstance().invoke(params);
			
			// 修改房产信息
			if(!StringUtils.isEmpty(params.getAttr("houseType")) && !"2".equals(StringUtil.objectToStr(params.getAttr("houseType")))){
				delParam.setService("borrowHouseService");
				SoaManager.getInstance().invoke(delParam);
				
				params.setService("borrowHouseService");
				params.setMethod("insert");
				SoaManager.getInstance().invoke(params);
			}
			
			// 修改车产信息
			if(!StringUtils.isEmpty(params.getAttr("carType")) && !"2".equals(StringUtil.objectToStr(params.getAttr("carType")))){
				delParam.setService("borrowCarService");
				SoaManager.getInstance().invoke(delParam);
				
				params.setService("borrowCarService");
				params.setMethod("insert");
				SoaManager.getInstance().invoke(params);
			}
			
			// 修改保险信息
			if(!StringUtils.isEmpty(params.getAttr("insurType")) 
					&& !"0".equals(params.getAttr("insurType"))){
				delParam.setService("borrowInsureService");
				SoaManager.getInstance().invoke(delParam);
				
				params.setService("borrowInsureService");
				params.setMethod("insert");
				SoaManager.getInstance().invoke(params);
			}
		}
		
		return result;
	}
	
	
	/**
	 * 
	 * @param applyId
	 * @return
	 */
	private Map<String,Object> queryBaseByApplyId(Object applyId){
		AppParam queryParam = new AppParam();
		queryParam.addAttr("applyId", applyId);
		AppResult queryResult = super.query(queryParam, BorrowApplyService.NAMESPACE, "queryPriefInfo");
		if(queryResult.getRows().size() == 0){
			throw new SysException(DuoduoError.UPDATE_DATA_IS_NOTEXISTS);
		}
		return queryResult.getRow(0);
	}
	
	/**
	 * 插入门店人员操作记录
	 */
	private AppResult insertStoreRecord(Object applyId,Object storeBy,
			String handleType,Object handleDesc, Object robWay,Object orderType,int isFeedback,int readFlag){
		AppResult result = StoreOptUtil.insertStoreRecord(applyId, storeBy, handleType, handleDesc, robWay, orderType, isFeedback, readFlag);
		return result;
	}
	
	/**
	 *客服数据自动挂卖处理
	 * @param params
	 * @return
	 */
	public AppResult kfDataAutoSale(AppParam params) {
		AppResult result = new AppResult();
		int size = this.getDao().update(NAMESPACE, "kfDataAutoSale", params.getAttr(), params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	/**
	 * 查询客服待处理的未填信息和优质单量
	 * @param
	 * @return
	 */
	public AppResult queryKFDealCount(AppParam params){
		return super.query(params,NAMESPACE, "queryKFDealCount");
	}
	
	
	/**
	 * pendCheckList
	 * @param params
	 * @return
	 */
	public AppResult pendCheckList(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "pendCheckList", "pendCheckCount");
	}
	
	/**
	 * 统计客服每日数据
	 * @param params
	 * @return
	 */
	public AppResult kfAllotOrderStart(AppParam params){
		int size = getDao().update(NAMESPACE, "kfAllotOrderStart", params.getAttr(), params.getDataBase());
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return backContext;
	}
	
	/**
	 * 查询我的借款
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryMyList(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryMyList",
				"queryMyListCount");
	}
	
	/***
	 * 客服离职处理
	 * @param params
	 * @return
	 */
	public AppResult leaveDeal(AppParam params){
		AppResult result = new AppResult();
		// 更新
		AppParam updateParam = new AppParam();
		updateParam.addAttr("lastKf", params.getAttr("lastKf"));
		updateParam.addAttr("customerId", params.getAttr("customerId"));
		int size = this.getDao().update(NAMESPACE, "leaveDeal", updateParam.getAttr(), updateParam.getDataBase());
		result.putAttr("updateSize", size);
		return result;
	}
	
	
	/**
	 * 查询客服订单相关信息
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryOrderInfo(AppParam params) {
		return super.query(params, NAMESPACE,"queryOrderInfo");
	}
	
	public AppResult queryWaitPushData(AppParam params) {
		return super.query(params, NAMESPACE,"queryWaitPushData");
	}
	
	public AppResult queryLoanCityCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryLoanCityCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
}
