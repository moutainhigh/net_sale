
package org.xxjr.busiIn.store.ext;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ddq.active.mq.store.StoreTaskSend;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.BorrowConstant;
import org.xxjr.busi.util.StoreConstant;
import org.xxjr.busi.util.StoreSeparateUtils;
import org.xxjr.busiIn.utils.AllotCostUtil;
import org.xxjr.busiIn.utils.StoreAlllotUtils;
import org.xxjr.busiIn.utils.StoreOptUtil;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sms.SendSmsByUrl;
import org.xxjr.store.util.StoreApplyUtils;
import org.xxjr.sys.util.NumberUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 信贷经理业务操作的逻辑
 * 
 * @author
 *
 */

@Lazy
@Service
@Slf4j
public class StoreOptExtService extends BaseService {
	public static final String NAMESPACE = "STOREOPTEXT";

	/**
	 * 保存录单信息
	 * 
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult saveAllInfo(AppParam params) {
		AppResult result = new AppResult();
		AppParam mapParams = new AppParam();
		String applyId = StringUtil.getString(params.getAttr("applyId"));
		mapParams.addAttrs((Map<String,Object>)params.getAttr("mainInfo"));
		mapParams.addAttr("applyId", applyId);
		mapParams.addAttr("lastStore", params.getAttr("lastStore"));
		mapParams.addAttr("customerId", params.getAttr("customerId"));
		result = saveMainInfo(mapParams);
		if(result.isSuccess()){
			AppParam baseParams = new AppParam();
			baseParams.addAttrs((Map<String,Object>)params.getAttr("baseInfo"));
			if(!StringUtils.isEmpty(result.getAttr("applyId"))){
				baseParams.addAttr("applyId", result.getAttr("applyId"));
			}else{
				baseParams.addAttr("applyId", params.getAttr("applyId"));
			}
			result = saveBaseAllInfo(baseParams);
			if(result.isSuccess()){
				AppParam otherParams = new AppParam();
				otherParams.addAttrs((Map<String,Object>)params.getAttr("otherInfo"));
				otherParams.addAttr("applyId", baseParams.getAttr("applyId"));
				result = saveOtherInfo(otherParams);
			}
			
			// 删除缓存
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_INFO + applyId);
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_MAININFO + applyId);
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_BASEINFO + applyId);
		}
		return result;
	}
	/**
	 * 保存主要信息
	 * 
	 * @param params
	 * @return
	 */
	public AppResult saveMainInfo(AppParam params) {
		Object telephone = params.getAttr("telephone");
		Object recordCustId = params.getAttr("customerId");
		Object applyName = params.getAttr("applyName");
		Object loanAmount = params.getAttr("loanAmount");
		Object loanDeadline = params.getAttr("loanDeadline");
		Object lastStore = params.getAttr("lastStore");
		Object desc = params.getAttr("desc");
		Object applyType = params.getAttr("applyType");
		Object cityName = params.getAttr("cityName");
		Object channelCode = params.getAttr("channelCode");
		if (StringUtils.isEmpty(telephone)) {
			return CustomerUtil.retErrorMsg("手机号码不能为空");
		} else if(StringUtils.isEmpty(applyName)) {
			return CustomerUtil.retErrorMsg("姓名不能为空");
		} else if(StringUtils.isEmpty(loanAmount)) {
			return CustomerUtil.retErrorMsg("贷款额度不能为空");
		} else if(StringUtils.isEmpty(lastStore)) {
			return CustomerUtil.retErrorMsg("当前处理人不能为空");
		} else if(StringUtils.isEmpty(cityName)) {
			return CustomerUtil.retErrorMsg("申请城市不能为空");
		}
		Object applyId = params.getAttr("applyId");
		if (StringUtils.isEmpty(applyId)) {
			// 效验手机号 存在借款记录给出提示
			AppParam queryParam = new AppParam();
			queryParam.addAttr("telephone", telephone);
			queryParam.setService("borrowApplyService");
			queryParam.setMethod("querySimpleInfo");
			AppResult queryResult = SoaManager.getInstance().invoke(queryParam);
			if (queryResult.getRows().size() > 0) {
				return CustomerUtil.retErrorMsg("该手机号已申请过贷款，请核对后重试！");
			}
		}

		AppParam applyParam = new AppParam();
		applyParam.setService("borrowApplyService");
		applyParam.addAttr("applyName", applyName);
		
		AppParam storeApplyParam = new AppParam();
		storeApplyParam.setService("borrowStoreApplyService");
		storeApplyParam.addAttr("applyName", applyName);
		AppResult result = null;
		if (StringUtils.isEmpty(applyId)) {
			Map<String, Object> userMap = CustomerIdentify.getCustIdentify(recordCustId.toString());
			String orgId = "";
			if (!StringUtils.isEmpty(userMap)) {
				orgId = StringUtil.objectToStr(userMap.get("orgId"));
				applyParam.addAttr("orgId", orgId);
			}
			applyParam.setMethod("insert");
			applyParam.addAttr("recorder", recordCustId);
			applyParam.addAttr("telephone", telephone);
			applyParam.addAttr("lastStore", lastStore);
			applyParam.addAttr("applyType", applyType);
			applyParam.addAttr("status", 2);
			applyParam.addAttr("haveDetail", 1);
			applyParam.addAttr("orderType", 1);
			applyParam.addAttr("cityName", cityName);
			applyParam.addAttr("channelDetail", channelCode);
			applyParam.addAttr("channelCode", channelCode);
			applyParam.addAttr("grade", "A");
			result = SoaManager.getInstance().invoke(applyParam);
			applyId = result.getAttr("applyId");
			result.putAttr("applyId", applyId);
			// 插入门店申请表
			storeApplyParam.addAttrs(applyParam.getAttr());
			storeApplyParam.addAttr("applyId", applyId);
			storeApplyParam.setMethod("insert");
			SoaManager.getInstance().invoke(storeApplyParam);
			
		} else {
			applyParam.addAttr("applyId", applyId);
			applyParam.addAttr("cityName", cityName);
			applyParam.setMethod("update");
			result = SoaManager.getInstance().invoke(applyParam);
			//更新门店申请表
			storeApplyParam.addAttr("applyId", applyId);
			storeApplyParam.addAttr("cityName", cityName);
			storeApplyParam.setMethod("update");
			SoaManager.getInstance().invoke(storeApplyParam);
		}

		AppParam reParams = new AppParam();
		// 增加贷款基本信息
		if (result.isSuccess()) {
			reParams.addAttr("applyId", applyId);
			reParams.addAttr("loanAmount", loanAmount);
			reParams.addAttr("loanDeadline", loanDeadline);
			reParams.addAttr("loanPurpose", params.getAttr("loanPurpose"));
			reParams.addAttr("cityName", cityName);
			reParams.addAttr("desc", desc);
			reParams.setService("borrowBaseService");
			reParams.setMethod("update");
			SoaManager.getInstance().invoke(reParams);
		}
		return result;
	}
	
	/**
	 * 保存基本信息（包含车产、房产、收入、保险）
	 * 
	 * @param params
	 * @return
	 */
	public AppResult saveBaseAllInfo(AppParam params) {
		AppResult result = new AppResult();
		//更新基本信息
		String applyId = StringUtil.objectToStr(params.getAttr("applyId"));
		String age = StringUtil.objectToStr(params.getAttr("age"));
		String sex = StringUtil.objectToStr(params.getAttr("sex"));
		String workType = StringUtil.objectToStr(params.getAttr("workType"));
		String fundType = StringUtil.objectToStr(params.getAttr("fundType"));
		String fundAmount = StringUtil.objectToStr(params.getAttr("fundAmount"));
		String havePinan = StringUtil.objectToStr(params.getAttr("havePinan"));
		String isLocal = StringUtil.objectToStr(params.getAttr("isLocal"));
		String socialType = StringUtil.objectToStr(params.getAttr("socialType"));
		String socialAmount = StringUtil.objectToStr(params.getAttr("socialAmount"));
		String socialMonth = StringUtil.objectToStr(params.getAttr("socialMonth"));
		String liabiAmount = StringUtil.objectToStr(params.getAttr("liabiAmount"));
		String zimaScore = StringUtil.objectToStr(params.getAttr("zimaScore"));
		String education = StringUtil.objectToStr(params.getAttr("education"));
		String identifyNo = StringUtil.objectToStr(params.getAttr("identifyNo"));
		String fundNum = StringUtil.objectToStr(params.getAttr("fundNum"));
		AppParam reParams = new AppParam();
		reParams.addAttr("applyId", applyId);
		reParams.addAttr("age", age);
		reParams.addAttr("sex", sex);
		reParams.addAttr("workType", workType);
		reParams.addAttr("fundType", fundType);
		reParams.addAttr("fundAmount", fundAmount);
		reParams.addAttr("havePinan", havePinan);
		reParams.addAttr("isLocal", isLocal);
		reParams.addAttr("socialAmount", socialAmount);
		reParams.addAttr("socialMonth", socialMonth);
		reParams.addAttr("socialType", socialType);
		reParams.addAttr("liabiAmount", liabiAmount);
		reParams.addAttr("zimaScore", zimaScore);
		reParams.addAttr("education", education);
		reParams.addAttr("identifyNo", identifyNo);
		reParams.addAttr("fundNum", fundNum);
		reParams.setService("borrowBaseService");
		reParams.setMethod("update");
		result = SoaManager.getInstance().invoke(reParams);
		if(result.isSuccess()){	
			//更新收入信息
			String workCmp = StringUtil.objectToStr(params.getAttr("workCmp"));
			String wagesType = StringUtil.objectToStr(params.getAttr("wagesType"));
			String income = StringUtil.objectToStr(params.getAttr("income"));
			String jobMonth = StringUtil.objectToStr(params.getAttr("jobMonth"));
			String myLicense = StringUtil.objectToStr(params.getAttr("myLicense"));
			String hasLicense = StringUtil.objectToStr(params.getAttr("hasLicense"));
			String licenceLimit = StringUtil.objectToStr(params.getAttr("licenceLimit"));
			String cashMonth = StringUtil.objectToStr(params.getAttr("cashMonth"));
			String incomeMonth = StringUtil.objectToStr(params.getAttr("incomeMonth"));
			AppParam incomeParams = new AppParam();
			incomeParams.addAttr("applyId", applyId);
			incomeParams.addAttr("workCmp", workCmp);
			incomeParams.addAttr("wagesType", wagesType);
			incomeParams.addAttr("income", income);
			incomeParams.addAttr("jobMonth", jobMonth);
			incomeParams.addAttr("myLicense", myLicense);
			incomeParams.addAttr("hasLicense", hasLicense);
			incomeParams.addAttr("licenceLimit", licenceLimit);
			incomeParams.addAttr("cashMonth", cashMonth);
			incomeParams.addAttr("incomeMonth", incomeMonth);
			incomeParams.setService("borrowIncomeService");
			incomeParams.setMethod("update");
			SoaManager.getInstance().invoke(incomeParams);				
		}
		
		return result;
	}
	
	
	/**
	 * 保存其它基本信息（包含车产、房产，保险，征信等）
	 * 
	 * @param params
	 * @return
	 */
	public AppResult saveOtherInfo(AppParam params) {
		AppResult result = new AppResult();
		//更新基本信息
		String applyId = StringUtil.objectToStr(params.getAttr("applyId"));
		String creditType = StringUtil.objectToStr(params.getAttr("creditType"));
		String carType = StringUtil.objectToStr(params.getAttr("carType"));
		String houseType = StringUtil.objectToStr(params.getAttr("houseType"));

		AppParam reParams = new AppParam();
		reParams.addAttr("applyId", applyId);
		reParams.addAttr("creditType", creditType);
		reParams.addAttr("carType", carType);
		reParams.addAttr("houseType", houseType);
		reParams.setService("borrowBaseService");
		reParams.setMethod("update");
		result = SoaManager.getInstance().invoke(reParams);
		if(result.isSuccess()){
			//更新房产信息
			String houseVal = StringUtil.objectToStr(params.getAttr("houseVal"));
			String houseYears = StringUtil.objectToStr(params.getAttr("houseYears"));
			String housePlace = StringUtil.objectToStr(params.getAttr("housePlace"));
			String houseMonthPay = StringUtil.objectToStr(params.getAttr("houseMonthPay"));
			String myLoanHouse = StringUtil.objectToStr(params.getAttr("myLoanHouse"));
			String houseMonth = StringUtil.objectToStr(params.getAttr("houseMonth"));
			AppParam houseParams = new AppParam();
			houseParams.addAttr("applyId", applyId);
			houseParams.addAttr("houseVal", houseVal);
			houseParams.addAttr("housePlace", housePlace);
			houseParams.addAttr("houseYears", houseYears);
			houseParams.addAttr("houseMonthPay", houseMonthPay);
			houseParams.addAttr("myLoanHouse", myLoanHouse);
			houseParams.addAttr("houseMonth", houseMonth);
			houseParams.setService("borrowHouseService");
			houseParams.setMethod("update");
			SoaManager.getInstance().invoke(houseParams);
			
			//更新车产信息
			String carYears = StringUtil.objectToStr(params.getAttr("carYears"));
			String carPrice = StringUtil.objectToStr(params.getAttr("carPrice"));
			String carMonth = StringUtil.objectToStr(params.getAttr("carMonth"));
			String carMonthPay = StringUtil.objectToStr(params.getAttr("carMonthPay"));
			String carLocal = StringUtil.objectToStr(params.getAttr("carLocal"));
			AppParam carParams = new AppParam();
			carParams.addAttr("applyId", applyId);
			carParams.addAttr("carYears", carYears);
			carParams.addAttr("carPrice", carPrice);
			carParams.addAttr("carMonth", carMonth);
			carParams.addAttr("carMonthPay", carMonthPay);
			carParams.addAttr("carLocal", carLocal);
			carParams.setService("borrowCarService");
			carParams.setMethod("update");
			SoaManager.getInstance().invoke(carParams);
		
			//更新保单信息
			String insurType = StringUtil.objectToStr(params.getAttr("insurType"));
			String insurMonth = StringUtil.objectToStr(params.getAttr("insurMonth"));
			String insurYearAmt = StringUtil.objectToStr(params.getAttr("insurYearAmt"));
			String insurSelf = StringUtil.objectToStr(params.getAttr("insurSelf"));
			String companyName = StringUtil.objectToStr(params.getAttr("companyName"));
			AppParam insureParams = new AppParam();
			insureParams.addAttr("applyId", applyId);
			insureParams.addAttr("insurType", insurType);
			insureParams.addAttr("insurMonth", insurMonth);
			insureParams.addAttr("insurYearAmt", insurYearAmt);
			insureParams.addAttr("insurSelf", insurSelf);
			insureParams.addAttr("companyName", companyName);
			insureParams.setService("borrowInsureService");
			insureParams.setMethod("update");
			SoaManager.getInstance().invoke(insureParams);
			
			//更新征信信息
			String sumAmount = StringUtil.objectToStr(params.getAttr("sumAmount"));
			String useAmount = StringUtil.objectToStr(params.getAttr("useAmount"));
			String overdueCount = StringUtil.objectToStr(params.getAttr("overdueCount"));
			String haveCurOver = StringUtil.objectToStr(params.getAttr("curOverType"));
			AppParam creditParams = new AppParam();
			creditParams.addAttr("applyId", applyId);
			creditParams.addAttr("sumAmount", sumAmount);
			creditParams.addAttr("useAmount", useAmount);
			creditParams.addAttr("overdueCount", overdueCount);
			creditParams.addAttr("haveCurOver", haveCurOver);
			creditParams.setService("borrowCreditService");
			creditParams.setMethod("update");
			SoaManager.getInstance().invoke(creditParams);
		}
		
		return result;
	}
	
	
	/**
	 * 专属单设置(新)
	 * 
	 * @param params
	 * @return
	 */
	public AppResult newOrderSet(AppParam params) {
		AppResult result = new AppResult();
		String customerId = StringUtil.objectToStr(params.getAttr("customerId"));
		AppParam queryParams = new AppParam("custLevelService","query");
		queryParams.addAttr("customerId", customerId);
		AppResult queryReslut = SoaManager.getInstance().invoke(queryParams);
		int gradeCode = 1; //能力等级
		if(queryReslut.getRows().size() > 0 && !StringUtils.isEmpty(queryReslut.getRow(0))){
			gradeCode = NumberUtil.getInt(queryReslut.getRow(0).get("gradeCode"),1);
		}
		Map<String,Object> rankMap = StoreSeparateUtils.getRankConfigByGrade(gradeCode);
		int lockCount = 0; //可锁定单量
		if(rankMap != null && !StringUtils.isEmpty(rankMap)){
			lockCount = NumberUtil.getInt(rankMap.get("lockCount"),30);
		}
		//已存在的专属单
		int myOrders = StoreSeparateUtils.queryOrderCount(customerId);
		AppParam queryOrder = new AppParam("exclusiveOrderService","insert");
		Map<String, Object> userMap = CustomerIdentify.getCustIdentify(customerId);
		String orgId = "";
		if (!StringUtils.isEmpty(userMap)) {
			orgId = StringUtil.objectToStr(userMap.get("orgId"));
			queryOrder.addAttr("orgId", orgId);
		}
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> orders = (List<Map<String, Object>>) params
				.getAttr("orders");
		for (Map<String, Object> orderMap : orders) {
			String applyId = StringUtil.objectToStr(orderMap.get("applyId"));
			//查询是否是专属单
			int count = StoreSeparateUtils.queryExeOrderCount(applyId);
			if(count == 1){
				StringBuilder str = new StringBuilder();
				str.append("申请ID为");
				str.append(applyId);
				str.append("的订单已经是专属单了不能再设置成专属单");
				return CustomerUtil.retErrorMsg(str.toString());
			}
			//判断是否是当前处理人
			Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
			String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
			if(!customerId.equals(lastStore)){
				return CustomerUtil.retErrorMsg("你不是当前处理人，不能设置成专属单");
			}
			
			String orderType = StringUtil.objectToStr(orderMap.get("orderType"));
			queryOrder.addAttr("orderType", orderType);
			queryOrder.addAttr("customerId", customerId);
			queryOrder.addAttr("applyId", applyId);
			if(myOrders >= lockCount){
				return CustomerUtil.retErrorMsg("你的专属单量已达到最高值，请在专属单列表中删除订单再设置!");
			}
			result = SoaManager.getInstance().callNewTx(queryOrder);
			int insertSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Insert_SIZE),0);
			if(insertSize == 1){
				myOrders ++;
				// 插入操作记录
				StoreOptUtil.insertStoreRecord(applyId,customerId, BorrowConstant.STORE_OPER_15,
						"设置专属单", 0, orderType,0,1);
			}
		}
		return result;
	}
	
	
	/**
	 * 取消专属单
	 * 
	 * @param params
	 * @return
	 */
	public AppResult cancleOrder(AppParam params) {
		AppResult result = new AppResult();
		AppParam queryOrder = new AppParam("exclusiveOrderService", "delete");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> orders = (List<Map<String, Object>>) params
				.getAttr("orders");
		String customerId = StringUtil.objectToStr(params.getAttr("customerId"));
		for (Map<String, Object> orderMap : orders) {
			String applyId = StringUtil.objectToStr(orderMap.get("applyId"));
			//判断是否是当前处理人
			Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
			String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
			if(!customerId.equals(lastStore) && !StoreOptUtil.isDealAuth(customerId)){
				return CustomerUtil.retErrorMsg("你不是当前处理人，不能取消专属单");
			}
			queryOrder.addAttr("applyId", applyId);
			result = SoaManager.getInstance().invoke(queryOrder);
			if(result.isSuccess()){
				// 插入操作记录
				StoreOptUtil.insertStoreRecord(applyId,customerId, BorrowConstant.STORE_OPER_16,
						"取消专属单", 0,StringUtil.objectToStr(orderMap.get("orderType")),0,1);
			}
		}
		return result;
	}

	
	
	/**
	 * 优质单处理
	 * @param params
	 */
	public AppResult seniorHandle(AppParam params){
		Object applyId = params.getAttr("applyId");
		Object storeStatus = StringUtil.getString(params.getAttr("storeStatus"));
		if(StringUtils.isEmpty(applyId) || StringUtils.isEmpty(storeStatus)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}

		if(BorrowConstant.STORE_OPER_1.equals(storeStatus)){//继续跟进
			return this.followUpDeal(params);
		}else if(BorrowConstant.STORE_OPER_2.equals(storeStatus)){//预约
			params.addAttr("status", params.getAttr("bookStatus"));
			return this.bookDeal(params);
		}else if(BorrowConstant.STORE_OPER_3.equals(storeStatus)){//签单
			return this.signDeal(params);
		}else if(BorrowConstant.STORE_OPER_5.equals(storeStatus)){//不能做
			return this.failDeal(params);
		}else if(BorrowConstant.STORE_OPER_14.equals(storeStatus)){//不需要
			return this.notNeedDeal(params);
		}

		throw new SysException("处理类型非法！");
	}
	
	
	/**
	 * 优质单处理-新
	 * @param params
	 */
	public AppResult newSeniorHandle(AppParam params){
		Object applyId = params.getAttr("applyId");
		Object storeStatus = StringUtil.getString(params.getAttr("storeStatus"));
		Object handleType = StringUtil.getString(params.getAttr("handleType"));
		if(StringUtils.isEmpty(applyId) || StringUtils.isEmpty(storeStatus)
				 || StringUtils.isEmpty(handleType)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}

		if(StoreConstant.STORE_OPER_1.equals(handleType)){//继续跟进
			return this.followUpDeal(params);
		}else if(StoreConstant.STORE_OPER_2.equals(handleType)){//预约
			return this.bookHandle(params);
		}else if(StoreConstant.STORE_OPER_9.equals(handleType)){//已上门
			return this.visitDeal(params);
		}else if(StoreConstant.STORE_OPER_3.equals(handleType)){//签单
			return this.signDeal(params);
		}else if(StoreConstant.STORE_OPER_5.equals(handleType)){//不能做
			return this.failDeal(params);
		}else if(StoreConstant.STORE_OPER_14.equals(handleType)){//不需要
			return this.notNeedDeal(params);
		}

		throw new SysException("处理类型非法！");
	}
	/**
	 * 信贷经理继续跟进处理
	 * 
	 * @param params
	 * @return
	 */
	public AppResult followUpDeal(AppParam params) {
		AppResult result = new AppResult();
		Object applyId = params.getAttr("applyId");
		Object notifyTime = params.getAttr("bookTime");
		Object bookStatus = params.getAttr("bookStatus");
		Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
		String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
		String customerId = StringUtil.getString(params.getAttr("customerId"));
		//管理员的用户ID
		String adminCustomerId = StringUtil.getString(params.getAttr("adminCustomerId"));
		//管理员可以执行继续跟进
		if((StringUtils.hasText(lastStore) && customerId.equals(lastStore)) 
						|| !StringUtils.isEmpty(adminCustomerId)){
			//门店跟进状态
			String storeStatus = StringUtil.getString(applyInfo.get("storeStatus"));
			AppParam updateParams = new AppParam();
			//如果为接单中状态则变更状态，其它只插入跟进记录
			if(StoreConstant.STORE_STATUS_0.equals(storeStatus)){
				// 修改申请状态
				Map<String, Object> userMap = CustomerIdentify.getCustIdentify(customerId);
				String orgId = "";
				if (!StringUtils.isEmpty(userMap)) {
					orgId = StringUtil.objectToStr(userMap.get("orgId"));
					updateParams.addAttr("orgId", orgId);
				}
				updateParams.addAttr("storeStatus", StoreConstant.STORE_STATUS_1);
				updateParams.addAttr("applyId", applyId);
				updateParams.setService("borrowApplyService");
				updateParams.setMethod("update");
				result = SoaManager.getInstance().invoke(updateParams);
			}
			if(result.isSuccess()){
				String handleType = StoreConstant.STORE_OPER_1;
				if(StoreConstant.STORE_STATUS_1.equals(storeStatus)){
					handleType = StoreConstant.STORE_OPER_17;
				}else if(StoreConstant.STORE_STATUS_2.equals(storeStatus) && StoreConstant.STORE_BOOK_1.equals(bookStatus) ){
					handleType = StoreConstant.STORE_OPER_18;
				}else if(StoreConstant.STORE_STATUS_2.equals(storeStatus) && StoreConstant.STORE_BOOK_3.equals(bookStatus) ){
					handleType = StoreConstant.STORE_OPER_19;
				}else if(StoreConstant.STORE_STATUS_3.equals(storeStatus)){
					handleType = StoreConstant.STORE_OPER_20;
				}
				// 插入操作记录
				StoreOptUtil.insertStoreRecord(params.getAttr("applyId"),
						StringUtils.isEmpty(adminCustomerId)?customerId:adminCustomerId, handleType,
						params.getAttr("handleDesc"), 0, applyInfo.get("orderType"),1,0);
				
				
				//处理workList记录
				updateParams.addAttr("customerId", customerId);
				updateParams.addAttr("applyId", applyId);
				updateParams.setService("workListService");
				updateParams.setMethod("dealAllotOrderWork");
				SoaManager.getInstance().invoke(updateParams);
				
				//添加通知信息
				if(!StringUtils.isEmpty(notifyTime)){
					AppParam notifyParams = new AppParam("infoNotifyService","update");
					notifyParams.addAttr("customerId", customerId);
					notifyParams.addAttr("applyId", applyId);
					notifyParams.addAttr("applyName", applyInfo.get("applyName"));
					notifyParams.addAttr("notifyTime", params.getAttr("bookTime"));
					notifyParams.addAttr("type", 1);//预约跟进类型
					
					SoaManager.getInstance().invoke(notifyParams);
				}
			}

		}else{
			throw new SysException("你不是当前处理人，不能进行跟进处理");
		}
		
		return result;
	}

	/**
	 * 信贷经理预约处理
	 * 
	 * @param params
	 * @return
	 */
	public AppResult bookDeal(AppParam params) {
		Object applyId = params.getAttr("applyId");
		Object notifyTime = params.getAttr("bookTime");
		Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
		String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
		String customerId = StringUtil.getString(params.getAttr("customerId"));
		//管理员的用户ID
		String adminCustomerId = StringUtil.getString(params.getAttr("adminCustomerId"));		
		//管理员可以执行预约处理
		if((StringUtils.hasText(lastStore) && customerId.equals(lastStore)) 
						|| !StringUtils.isEmpty(adminCustomerId)){
			// 增加预约记录
			AppParam updateParams = new AppParam("treatBookService", "update");
			Map<String, Object> userMap = CustomerIdentify.getCustIdentify(customerId);
			String orgId = "";
			if (!StringUtils.isEmpty(userMap)) {
				orgId = StringUtil.objectToStr(userMap.get("orgId"));
				params.addAttr("orgId", orgId);
			}
			updateParams.addAttrs(params.getAttr());
			AppResult result = SoaManager.getInstance().invoke(updateParams);
	
			if (result.isSuccess()) {
			
				// 修改申请状态
				int bookStatus  = NumberUtil.getInt(params.getAttr("status"), 0);
				String handleType  = BorrowConstant.STORE_OPER_2;
				updateParams = new AppParam();
				if (bookStatus == 3) {// 已上门
					handleType = BorrowConstant.STORE_OPER_9;
				} else if (bookStatus == 2) {// 未上门
					handleType = BorrowConstant.STORE_OPER_10;
				} 

				if (result.isSuccess()) {
					Object bookDesc = params.getAttr("bookDesc");
					if(StringUtils.isEmpty(bookDesc)){
						bookDesc = "预约处理";
					}
					
					//添加通知信息
					if(!StringUtils.isEmpty(notifyTime) && BorrowConstant.STORE_BOOK_1.equals(params.getAttr("status"))){
						AppParam notifyParams = new AppParam("infoNotifyService","update");
						notifyParams.addAttr("customerId", customerId);
						notifyParams.addAttr("applyId", params.getAttr("applyId"));
						notifyParams.addAttr("applyName", applyInfo.get("applyName"));
						notifyParams.addAttr("notifyTime", params.getAttr("bookTime"));
						notifyParams.addAttr("type", 2);//预约上门类型
						
						SoaManager.getInstance().invoke(notifyParams);
					}
					
					// 插入操作记录
					StoreOptUtil.insertStoreRecord(applyId,
							StringUtils.isEmpty(adminCustomerId)?customerId:adminCustomerId,
							handleType,bookDesc, 0, applyInfo.get("orderType"),1,0);
	
					String isSmsNotice = StringUtil.getString(params
							.getAttr("isSmsNotice"));
					if ("1".equals(isSmsNotice)) {// 发短信通知
						try {
							String telephone = applyInfo.get("telephone")
									.toString();
							String smsContent = StringUtil.getString(params
									.getAttr("smsContent"));
							SendSmsByUrl.sendSMSInfo("http://123.206.19.78:9001/sendXSms.do?username=qddtz&password=124588&productid=621215&dstime=&xh=11", smsContent, telephone, telephone);
						} catch (Exception e) {
							// 忽略
							log.error("发送预约短信失败", e);
						}
					}
				}
			}
			
			return result;
		}else{
			return CustomerUtil.retErrorMsg("你不是当前处理人，不能进行预约处理");
		}
		
	}
	
	
	/**
	 * 信贷经理预约上门处理
	 * 
	 * @param params
	 * @return
	 */
	public AppResult bookHandle(AppParam params) {
		Object applyId = params.getAttr("applyId");
		Object notifyTime = params.getAttr("bookTime");
		Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
		String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
		String customerId = StringUtil.getString(params.getAttr("customerId"));
		//管理员的用户ID
		String adminCustomerId = StringUtil.getString(params.getAttr("adminCustomerId"));		
		//管理员可以执行预约处理
		if((StringUtils.hasText(lastStore) && customerId.equals(lastStore)) 
						|| !StringUtils.isEmpty(adminCustomerId)){
			//增加预约明细
			AppParam bookParams = new AppParam("treatBookDetailService", "update");
			String bookType = StringUtil.getString(params.getAttr("bookType"));
			if("2".equals(bookType)){
				bookParams.addAttr("bookStatus", "2"); // 2-预约回访
			}else{
				bookParams.addAttr("bookStatus", "0"); // 0-预约上门
			}
			bookParams.addAttrs(params.getAttr());
			SoaManager.getInstance().invoke(bookParams);
			
			// 删除预约缓存
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_BOOK_RECORD + applyId);
			
			// 增加预约记录
			AppParam updateParams = new AppParam("treatBookService", "update");
			Map<String, Object> userMap = CustomerIdentify.getCustIdentify(customerId);
			String orgId = "";
			if (!StringUtils.isEmpty(userMap)) {
				orgId = StringUtil.objectToStr(userMap.get("orgId"));
				params.addAttr("orgId", orgId);
			}
			updateParams.addAttrs(params.getAttr());
			updateParams.addAttr("status", StoreConstant.STORE_BOOK_1);//预约中
			AppResult result = SoaManager.getInstance().invoke(updateParams);
			if (result.isSuccess()) {
				String handleType  = StoreConstant.STORE_OPER_2;
					Object bookDesc = params.getAttr("bookDesc");
					if(StringUtils.isEmpty(bookDesc)){
						bookDesc = "预约处理";
					}
					//添加通知信息
					if(!StringUtils.isEmpty(notifyTime)){
						AppParam notifyParams = new AppParam("infoNotifyService","update");
						notifyParams.addAttr("customerId", customerId);
						notifyParams.addAttr("applyId", params.getAttr("applyId"));
						notifyParams.addAttr("applyName", applyInfo.get("applyName"));
						notifyParams.addAttr("notifyTime", params.getAttr("bookTime"));
						if("1".equals(bookType)){
							notifyParams.addAttr("type", 2);//预约上门
						}else{
							notifyParams.addAttr("type", 3);//预约回访
						}
						SoaManager.getInstance().invoke(notifyParams);
					}
					// 插入操作记录
					StoreOptUtil.insertStoreRecord(applyId,
							StringUtils.isEmpty(adminCustomerId)?customerId:adminCustomerId,
							handleType,bookDesc, 0, applyInfo.get("orderType"),1,0);
	
					String isSmsNotice = StringUtil.getString(params
							.getAttr("isSmsNotice"));
					if ("1".equals(isSmsNotice)) {// 发短信通知
						try {
						/*	String telephone = applyInfo.get("telephone")
									.toString();
							String smsContent = StringUtil.getString(params
									.getAttr("smsContent"));
							SmsHttpSendUtil.sendSMS(smsContent, telephone);*/
						} catch (Exception e) {
							// 忽略
							log.error("发送预约短信失败", e);
						}
					}
			}
			return result;
		}else{
			return CustomerUtil.retErrorMsg("你不是当前处理人，不能进行预约处理");
		}
		
	}
	/**
	 * 添加已上门处理
	 * 
	 * @param params
	 * @return
	 */
	public AppResult visitDeal(AppParam params) {
		Object applyId = params.getAttr("applyId");
		Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
		String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
		String customerId = StringUtil.getString(params.getAttr("customerId"));
		String recCustId = StringUtil.getString(params.getAttr("recCustId")); //接待人
		//管理员的用户ID
		String adminCustomerId = StringUtil.getString(params.getAttr("adminCustomerId"));		
		//管理员可以执行预约处理
		if((StringUtils.hasText(lastStore) && customerId.equals(lastStore)) 
						|| !StringUtils.isEmpty(adminCustomerId)){
					
			//增加上门明细
			AppParam visitParams = new AppParam("treatVisitDetailService", "update");
			visitParams.addAttrs(params.getAttr());
			visitParams.addAttr("visitType", StoreConstant.STORE_VISIT_TYPE_1);//手工添加
			SoaManager.getInstance().invoke(visitParams);
			
			// 删除详情页缓存
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_VISIT_RECORD + applyId);
			
			// 增加上门记录
			AppParam updateParams = new AppParam("treatBookService", "update");
			Map<String, Object> userMap = CustomerIdentify.getCustIdentify(customerId);
			String orgId = "";
			if (!StringUtils.isEmpty(userMap)) {
				orgId = StringUtil.objectToStr(userMap.get("orgId"));
				params.addAttr("orgId", orgId);
			}
			updateParams.addAttrs(params.getAttr());
			updateParams.addAttr("status", StoreConstant.STORE_BOOK_3);//已上门
			AppResult result = SoaManager.getInstance().invoke(updateParams);
	
			if (result.isSuccess()) {
		
				String handleType  = StoreConstant.STORE_OPER_9;//已上门
				if (result.isSuccess()) {				
					// 插入操作记录
					StoreOptUtil.insertStoreRecord(applyId,
							StringUtils.isEmpty(adminCustomerId)?customerId:adminCustomerId,
							handleType,"添加上门处理", 0, applyInfo.get("orderType"),1,0);
					try{
						//创建任务对象
						StoreTaskSend storeSend = (StoreTaskSend)SpringAppContext.getBean(StoreTaskSend.class);
						
						Map<String, Object> msgParam = new HashMap<String, Object>();
						msgParam.put("visitCount", "1");
						msgParam.put("recordDate", DateUtil.getSimpleFmt(new Date()));
						storeSend.sendStoreMessage(recCustId,"countDealType" , msgParam);
					}catch (Exception e) {
						log.error("visitDeal error", e);
						ExceptionUtil.setExceptionMessage(e, result,
								DuoduoSession.getShowLog());
					}
				}
			}
			
			return result;
		}else{
			return CustomerUtil.retErrorMsg("你不是当前处理人，不能进行上门处理");
		}
		
	}

	/**
	 * 失败处理不能做
	 * 
	 * @param params
	 */
	public AppResult failDeal(AppParam params) {
		AppResult result = new AppResult();
		String applyId = StringUtil.getString(params.getAttr("applyId"));
		Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
		//管理员的用户ID
		String adminCustomerId = StringUtil.getString(params.getAttr("adminCustomerId"));
		String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
		String customerId = StringUtil.getString(params.getAttr("customerId"));
		//管理员可以执行不能做
		if((StringUtils.hasText(lastStore) && customerId.equals(lastStore)) 
										|| !StringUtils.isEmpty(adminCustomerId)){
			//门店跟进状态
			String storeStatus = StringUtil.getString(applyInfo.get("storeStatus"));
			if(!StoreConstant.STORE_STATUS_0.equals(storeStatus) && !StoreConstant.STORE_STATUS_1.equals(storeStatus) 
					&&!StoreConstant.STORE_STATUS_2.equals(storeStatus)){
				throw new SysException("待处理或预约状态才能执行不能做操作");
			}

			AppParam updateParams = new AppParam();
			Map<String, Object> userMap = CustomerIdentify.getCustIdentify(customerId);
			String orgId = "";
			if (!StringUtils.isEmpty(userMap)) {
				orgId = StringUtil.objectToStr(userMap.get("orgId"));
				updateParams.addAttr("orgId", orgId);
			}
			updateParams.addAttr("status", BorrowConstant.apply_status_7);
			updateParams.addAttr("storeStatus", BorrowConstant.STORE_OPER_5);
			updateParams.addAttr("applyId", params.getAttr("applyId"));
			updateParams.setService("borrowApplyService");
			updateParams.setMethod("update");
			result = SoaManager.getInstance().invoke(updateParams);
			
			if (result.isSuccess()) {
				// 插入操作记录
				StoreOptUtil.insertStoreRecord(applyId,
						StringUtils.isEmpty(adminCustomerId) ? customerId : adminCustomerId, 
						StoreConstant.STORE_OPER_5,
						params.getAttr("handleDesc"), 0,applyInfo.get("orderType"),1,1);
	
			}
			
			return result;
			
		}else{
			throw new SysException("你不是当前处理人，不能进行不能做处理");
		}
		
	}
	
	/**
	 * 不需要处理
	 * 
	 * @param params
	 */
	public AppResult notNeedDeal(AppParam params) {
		AppResult result = new AppResult();
		// 修改申请状态
		String applyId = StringUtil.getString(params.getAttr("applyId"));
		String customerId = StringUtil.getString(params.getAttr("customerId"));
		//管理员的用户ID
		String adminCustomerId = StringUtil.getString(params.getAttr("adminCustomerId"));
		Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
		String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
		//管理员可以执行不需要
		if((StringUtils.hasText(lastStore) && customerId.equals(lastStore)) 
										|| !StringUtils.isEmpty(adminCustomerId)){
			//门店跟进状态
			String storeStatus = StringUtil.getString(applyInfo.get("storeStatus"));
			if(!StoreConstant.STORE_STATUS_0.equals(storeStatus) && !StoreConstant.STORE_STATUS_1.equals(storeStatus) 
					&&!StoreConstant.STORE_STATUS_2.equals(storeStatus)){
				throw new SysException("待处理或预约状态才能执行不需要操作");
			}
	
			AppParam updateParams = new AppParam();
			Map<String, Object> userMap = CustomerIdentify.getCustIdentify(customerId);
			String roleType = "";
			String orgId = "";
			if (!StringUtils.isEmpty(userMap)) {
				orgId = StringUtil.objectToStr(userMap.get("orgId"));
				updateParams.addAttr("orgId", orgId);
				roleType = StringUtil.objectToStr(userMap.get("roleType"));
				updateParams.addAttr("roleType",roleType);
			}
			updateParams.addAttr("applyId", applyId);
			updateParams.addAttr("customerId", customerId);
			int size = getDao().update(NAMESPACE, "notNeedDeal", updateParams.getAttr(), updateParams.getDataBase());
			result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
			
			if (result.isSuccess()) {
				// 插入操作记录
				StoreOptUtil.insertStoreRecord(applyId,
						StringUtils.isEmpty(adminCustomerId) ? customerId : adminCustomerId, 
						StoreConstant.STORE_OPER_14,
						params.getAttr("handleDesc"), 0,applyInfo.get("orderType"),1,1);
	
			}
			
			return result;
		}else{
			throw new SysException("你不是当前处理人，不能进行不需要处理");
		}
	}
	

	/**
	 * 信贷经理签单处理
	 * 
	 * @param params
	 */
	public AppResult signDeal(AppParam params) {
		return StoreOptUtil.signDeal(params);
	}

	/**
	 * 信贷员回款处理
	 * 
	 * @param params
	 * @return
	 */
	public AppResult retLoanDeal(AppParam params) {
		AppResult result = new AppResult();
		Object recordId = params.getAttr("recordId");
		String customerId = StringUtil.objectToStr(params.getAttr("customerId"));
		String applyId = StringUtil.objectToStr(params.getAttr("applyId"));
		Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
		//管理员用户Id
		String adminCustomerId = StringUtil.getString(params.getAttr("adminCustomerId"));
		String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
		//管理员可以执行回款处理
		if((StringUtils.hasText(lastStore) && customerId.equals(lastStore)) 
										|| !StringUtils.isEmpty(adminCustomerId)){
			//查询是否已经签单
			AppParam queryParam = new AppParam("treatInfoService", "query");
			queryParam.addAttr("customerId", customerId);
			queryParam.addAttr("applyId", applyId);
			AppResult signResult = SoaManager.getInstance().callNoTx(queryParam);
			if(!signResult.isSuccess() || signResult.getRows().size() <= 0){//未签单
				result.setSuccess(false);
				result.setMessage("没有签单信息,不允许添加回款");
				return result;
			}
			params.setService("treatSuccessService");
			params.setMethod("insert");
			Map<String, Object> userMap = CustomerIdentify.getCustIdentify(customerId);
			String orgId = "";
			if (userMap != null && !StringUtils.isEmpty(userMap)) {
				orgId = StringUtil.objectToStr(userMap.get("orgId"));
				params.addAttr("orgId", orgId);
			}
			if (StringUtils.isEmpty(recordId)) {
				result = SoaManager.getInstance().invoke(params);
				if (result.isSuccess()) {
					//同步回款历史表
					recordId = StringUtil.getString(result.getAttr("recordId"));
					AppParam historyParams = new AppParam("treatSuccessHistoryService","updateOrInsert");
					historyParams.addAttr("repayId", recordId);
					historyParams.addAttrs(params.getAttr());
					SoaManager.getInstance().invoke(historyParams);
					
					// 增加回款记录
					String loanOrg = StringUtil
							.getString(params.getAttr("loanOrg"));
					String loanAmount = StringUtil.getString(params.getAttr("loanAmount"));
					String handleDesc = "贷款机构【" + loanOrg + "】,放款:" + loanAmount+ "万";
					// 插入操作记录
					StoreOptUtil.insertStoreRecord(applyId, StringUtils
							.isEmpty(adminCustomerId) ? customerId : adminCustomerId,
									StoreConstant.STORE_OPER_4, handleDesc, 0,applyInfo.get("orderType"),1,0);
				}
			} else {
				params.setMethod("update");
				result = SoaManager.getInstance().invoke(params);
				if(result.isSuccess()){
					//同步回款历史表
					AppParam historyParams = new AppParam("treatSuccessHistoryService","updateOrInsert");
					historyParams.addAttrs(params.getAttr());
					historyParams.addAttr("repayId", recordId);
					SoaManager.getInstance().invoke(historyParams);
				}
			}
			return result;
		}else{
			return CustomerUtil.retErrorMsg("你不是当前处理人，不能进行回款处理");
		}
	}
	
	/**
	 * 查询一手，二手，三手，四手的分单数
	 */
	public AppResult queryAllotCount(AppParam params){

		return super.query(params, NAMESPACE, "queryAllotCount");
	}
	
	/**
	 * 信贷员转单
	 * @param params
	 * @return
	 */
	public AppResult storeTranOrder(AppParam params){
		int size = getDao().update(NAMESPACE, "storeTranOrder", params.getAttr(), params.getDataBase());
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return backContext;
	}

	
	/**
	 * 回收4手单
	 * @param params
	 * @return
	 */
	public AppResult callBackOrder4(AppParam params){
		int size = getDao().update(NAMESPACE, "callBackOrder4", params.getAttr(), params.getDataBase());
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return backContext;
	}
	
	/**
	 * 执行处理操作
	 * @param params
	 * @return
	 */
	public AppResult execDeal(AppParam params){
		Object workId = StringUtil.objectToStr(params.getAttr("workId"));
		String customerId = StringUtil.objectToStr(params.getAttr("customerId"));
		String extraId = StringUtil.objectToStr(params.getAttr("extraId"));
		String workType = StringUtil.objectToStr(params.getAttr("workType"));
		String fishDesc = StringUtil.objectToStr(params.getAttr("fishDesc"));
		if (StringUtils.isEmpty(workId) || StringUtils.isEmpty(customerId)
				|| StringUtils.isEmpty(extraId)
				|| StringUtils.isEmpty(workType)
				|| StringUtils.isEmpty(fishDesc)) {
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		params.setService("workListService");
		params.setMethod("delete");
		params.addAttr("workId", workId);
		AppResult workResult = SoaManager.getInstance().invoke(params);
		
		if(workResult.isSuccess()){
			//判断是否是信贷经理分单
			if("1".equals(workType)){
				AppParam applyParams = new AppParam("borrowApplyService",
						"update");
				applyParams.addAttr("applyId", extraId);
				applyParams.addAttr("storeStatus", 1);
				applyParams.addAttr("lastStore", customerId);
				SoaManager.getInstance().invoke(applyParams);
			}
			
			AppParam fishParams = new AppParam("workListFishService",
					"insert");
			fishParams.addAttr("extraId", extraId);
			fishParams.addAttr("customerId", customerId);
			fishParams.addAttr("workType", workType);
			fishParams.addAttr("fishDesc", fishDesc);
			workResult = SoaManager.getInstance().invoke(fishParams);
		}
		
		return workResult;
	}
	
	/**
	 * 转其他信贷经理
	 * 
	 * @param params
	 * @return
	 */
	public AppResult transOtherXDJL(AppParam params) {
		AppResult result = new AppResult();
		//转信贷经理的用户ID
		String customerId = StringUtil.objectToStr(params.getAttr("customerId"));
		//门店
		String orgId = StringUtil.objectToStr(params.getAttr("orgId"));
		//当前登录人ID
		String custId = StringUtil.objectToStr(params.getAttr("custId"));
		// 获取用户信息
		String userOrgs = "";
		String roleType = "";
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(custId);
		if (custInfo != null) {
			userOrgs = StringUtil.getString(custInfo.get("userOrgs"));
			roleType = StringUtil.getString(custInfo.get("roleType"));
		}
		String excOrderFlag = StringUtil.objectToStr(params.getAttr("excOrderFlag"));
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> orders = (List<Map<String, Object>>) params.getAttr("orders");
		int againSucSize = 0;
		StringBuffer failBuffer = new StringBuffer("失败原因:");
		int failSize = 0; // 失败笔数
		int sucSize = 0; // 成功笔数
		for (Map<String, Object> orderMap : orders) {
			String applyId = StringUtil.objectToStr(orderMap.get("applyId"));
			Map<String, Object> applyInfo = StoreApplyUtils.getStoreApplyInfo(applyId);
			if(applyInfo == null || applyInfo.size() == 0){
				StringBuilder str = new StringBuilder();
				str.append("此订单已加入无效池，请先订单找回再操作！");
				return CustomerUtil.retErrorMsg(str.toString());
			}
			//判断是否是当前处理人
			String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
			//获取转信贷经理的用户门店
			Map<String, Object> trasCustInfo = CustomerIdentify.getCustIdentify(customerId);
			String trasOrgId = "";
			if(trasCustInfo != null){
				trasOrgId = StringUtil.getString(trasCustInfo.get("orgId"));
			}
			//不能转给自己
			if(customerId.equals(lastStore) && !trasOrgId.equals(orgId)){
				if(orders.size() == 1){
					return CustomerUtil.retErrorMsg("此订单当前处理人为你本人，不能再转给自己！");
				}
				//批量转单过滤提示
				continue;
			}
			//判断是否为已退单的客户
			String backStatus = StringUtil.getString(applyInfo.get("backStatus"));
			//是否是异地单标识
			boolean isPlaceFlag = StoreAlllotUtils.isNotPlaceFlag(orgId,applyId);
			if(StoreConstant.STORE_BACK_STATUS_3.equals(backStatus) && !isPlaceFlag){
				failBuffer.append(applyId +"已退单成功    ");
				failSize ++;
				continue;
			}else if(StoreConstant.STORE_BACK_STATUS_4.equals(backStatus)){
				failBuffer.append(applyId +"已退单失败    ");
				failSize ++;
				continue;
			}
			// 没有管理门店权限的不允许转非本门店的单子
			boolean isFlag = StoreAlllotUtils.isOrderTransOther(orgId,userOrgs);
			if(!isFlag){
				result.setSuccess(false);
				result.setMessage("非本门店单不允许再分配");
				return result;
			}
			
			//判断是否是专属单
			int count = StoreSeparateUtils.queryExeOrderCount(applyId);
			if(count == 1){
				if(StringUtils.isEmpty(excOrderFlag)){
					return CustomerUtil.retErrorMsg("专属单只能在专属单列表转给其他信贷经理");
				}
				if (CustConstant.CUST_ROLETYPE_1.equals(roleType) 
						|| CustConstant.CUST_ROLETYPE_7.equals(roleType)){
					//去除专属订单表中数据
					AppParam deleAppParam = new AppParam("exclusiveOrderService","delete");
					deleAppParam.addAttr("applyId", applyId);
					result = SoaManager.getInstance().invoke(deleAppParam);
				}else{
					return CustomerUtil.retErrorMsg(new String("您没有权限处理该订单"));
				}
			}
			// 判断是否已签单,非管理员没有权限转已签单的订单
			boolean signFlag = StoreSeparateUtils.isSignOrderFlag(applyId,roleType);
			if(!signFlag){
				if(orders.size() == 1){
					return CustomerUtil.retErrorMsg("此订单已经签单，您没有权限转单，请联系客服人员！");
				}
				failBuffer.append(applyId +"已签单   ");
				failSize ++;
				continue;
			}
			AppParam updateOrder = new AppParam("borrowStoreApplyService","update");
			updateOrder.addAttr("applyId", applyId);
			updateOrder.addAttr("status", 2);
			updateOrder.addAttr("lastStore", customerId);
			updateOrder.addAttr("orderType", 2); //2是再分配
			updateOrder.addAttr("orgId", orgId);
			updateOrder.addAttr("allotTime", new Date());
			updateOrder.addAttr("allotBy", custId);
			updateOrder.addAttr("allotDesc", "转信贷经理");
			result = SoaManager.getInstance().invoke(updateOrder);
			
			//同步
			if(result.isSuccess()){
				Map<String,Object> dealMap = new HashMap<String, Object>();
				//同步orderStatus
				dealMap.put("applyId", applyId);
				dealMap.put("lastStore", customerId);
				StoreOptUtil.dealStoreOrderByMq(null,"handelOrderType", dealMap);
			}
			
			//去掉分配池中数据
			int delSize = 0;
			AppParam tmpParam = new AppParam("netStorePoolService","delete"); 
			tmpParam.addAttr("applyId", applyId);
			result = SoaManager.getInstance().invoke(tmpParam);
			if(result.isSuccess()){
				delSize = NumberUtil.getInt(result.getAttr("deleteSize"));
				againSucSize ++;
			}
			sucSize ++;
			// 插入操作记录
			StoreOptUtil.insertStoreRecord(applyId,customerId, BorrowConstant.STORE_OPER_0,
					delSize==0?"转信贷经理[CUSTID=]" + custId :"手工分单[CUSTID=]" + custId, 0,2,0,1);
		}
		result.putAttr("againSucSize", againSucSize);
		result.putAttr("sucSize", sucSize);
		result.putAttr("failSize", failSize);
		if(failSize >0){
			result.putAttr("failDesc", failBuffer);
		}
		return result;
	}
	
	/***
	 * 转单处理
	 * @param params
	 * @return
	 */
	public AppResult transDeal(AppParam params){
		AppResult result = new AppResult();
		int size = getDao().update(NAMESPACE, "transDeal",params.getAttr(),params.getDataBase());
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * 新增跟进记录
	 * 
	 * @param params
	 * @return
	 */
	public AppResult addfollowRecord(AppParam params) {
		AppResult result = new AppResult();
		Object applyId = params.getAttr("applyId");
		Object notifyTime = params.getAttr("bookTime");
		Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
		String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
		String customerId = StringUtil.getString(params.getAttr("customerId"));
		//管理员的用户ID
		String adminCustomerId = StringUtil.getString(params.getAttr("adminCustomerId"));
		//管理员可以执行继续跟进
		if((StringUtils.hasText(lastStore) && customerId.equals(lastStore)) 
						|| !StringUtils.isEmpty(adminCustomerId)){
				String handleType = StoreConstant.STORE_OPER_1;
				// 插入操作记录
				StoreOptUtil.insertStoreRecord(params.getAttr("applyId"),
						StringUtils.isEmpty(adminCustomerId)?customerId:adminCustomerId, handleType,
						params.getAttr("handleDesc"), 0, applyInfo.get("orderType"),1,0);
				
				//添加通知信息
				if(!StringUtils.isEmpty(notifyTime)){
					AppParam notifyParams = new AppParam("infoNotifyService","update");
					notifyParams.addAttr("customerId", customerId);
					notifyParams.addAttr("applyId", applyId);
					notifyParams.addAttr("applyName", applyInfo.get("applyName"));
					notifyParams.addAttr("notifyTime", params.getAttr("bookTime"));
					notifyParams.addAttr("type", 1);//预约跟进类型
					
					SoaManager.getInstance().invoke(notifyParams);
				}

		}else{
			return CustomerUtil.retErrorMsg("你不是当前处理人，不能新增跟进记录");
		}
		
		return result;
	}
	
	/***
	 * 新单手工分配
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult newOrderAllot(AppParam params){
		AppResult result = new AppResult();
		//转信贷经理的用户ID
		String customerId = StringUtil.objectToStr(params.getAttr("customerId"));
		//门店
		String orgId = StringUtil.objectToStr(params.getAttr("orgId"));
		//当前登录人ID
		String custId = StringUtil.objectToStr(params.getAttr("custId"));
		String recordDate = DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD);
		List<Map<String, Object>> orders = (List<Map<String, Object>>) params.getAttr("orders");
		int sucSize = 0;
		AppParam updateApplyParam = new AppParam();
		
		for (Map<String, Object> orderMap : orders) {
			String applyId = StringUtil.objectToStr(orderMap.get("applyId"));
			Map<String, Object> applyMap = StoreOptUtil.queryByApplyId(applyId);
			String applyTime = StringUtil.getString(applyMap.get("applyTime"));
			boolean costFlag = false;
			if(!StringUtils.isEmpty(applyTime)) {
				costFlag = AllotCostUtil.saveOrgAllotOrderCost(orgId,applyId,customerId,true);
				
				if(!costFlag) {
					throw new SysException("门店余额不足，请尽量选少量单转");
				}
				
				//去掉分配池中数据
				AppParam tmpParam = new AppParam("netStorePoolService","delete"); 
				tmpParam.addAttr("applyId", applyId);
				tmpParam.addAttr("orderType", "1");
				result = SoaManager.getInstance().invoke(tmpParam);
			}
			
			//判断存在处理人
			String lastStore = StringUtil.getString(applyMap.get("lastStore"));
			if(StringUtils.isEmpty(lastStore)){
				//判断是否是二次申请，是则隐藏相关记录
				int applyCount = NumberUtil.getInt(applyMap.get("applyCount"),1);
				if(applyCount > 1){
					updateApplyParam.addAttr("isHideFlag", "1");
					updateApplyParam.addAttr("backStatus","1");//1 未退单
					updateApplyParam.addAttr("backDesc","");
					updateApplyParam.addAttr("backReDesc","");
					updateApplyParam.addAttr("custLabel","0");
				}
				updateApplyParam.addAttr("applyId", applyId);
				updateApplyParam.addAttr("allotBy", custId);//操作人
				updateApplyParam.addAttr("customerId", customerId);//分配人
				updateApplyParam.addAttr("orgId", orgId);//门店
				int updateSize = getDao().update(NAMESPACE, "storeAllotOrderByHand", updateApplyParam.getAttr(), updateApplyParam.getDataBase());
				if(updateSize > 0){
					
					// 手工分单删除订单缓存
					RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_INFO + applyId);
					RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_MAININFO + applyId);
					RedisUtils.getRedisService().del(StoreApplyUtils.BORROW_APPLY_INFO + applyId);
					
					int isNew = 0; // 是否为新单
					//插入门店人员操作记录
					StoreOptUtil.insertStoreRecord(applyId, customerId, BorrowConstant.STORE_OPER_0, 
							"新单手工分单[CUSTID=]" + custId, 0, 1, 0, 1);

					Map<String, Object> sendParam = new HashMap<String, Object>();
					sendParam.put("recordDate", recordDate);//记录日期
					StoreOptUtil.dealStoreOrderByMq(customerId,"countDealType", sendParam);
					//同步orderStatus
					sendParam.put("applyId", applyId);
					sendParam.put("orderStatus", "-1");
					sendParam.put("lastStore", customerId);
					sendParam.put("orgId", orgId);
					if(1 == isNew){
						sendParam.put("isNew", 1);
					}
					StoreOptUtil.dealStoreOrderByMq(customerId,"handelOrderType", sendParam);

					//发送分单消息通知
					AppParam allotParam = new AppParam();
					String applyName = StringUtil.getString(applyMap.get("applyName"));
					allotParam.addAttr("customerId", customerId);
					allotParam.addAttr("applyId", applyId);
					allotParam.addAttr("orderType", 1);
					allotParam.addAttr("orgId", orgId);
					allotParam.addAttr("applyName", applyName);
					StoreOptUtil.sendAllotMeaasge(allotParam);
					if(costFlag){
						AppParam countParam = new AppParam();
						countParam.addAttr("customerId", customerId);
						countParam.addAttr("recordDate", recordDate);
						countParam.addAttr("totalSize", 1);
						countParam.addAttr("orderType", 1);
						//更新分单数量
						StoreAlllotUtils.updateAllotCount(countParam);
					}
					//二次申请同步custLabel
					if(applyCount > 1){
						Map<String,Object> dealMap = new HashMap<String, Object>();
						dealMap.put("applyId", applyId);
						dealMap.put("custLabel", "0");
						StoreOptUtil.dealStoreOrderByMq(null,"custLabelType", dealMap);
					}
				}
				sucSize ++ ;
			}
		}
		result.putAttr("sucSize", sucSize);
		return result;
	}
	
	/***
	 * 新单转门店
	 * @param params
	 * @return
	 */
	public AppResult newOrderTransOrg(AppParam params){
		AppResult result = new AppResult();
		//门店
		String orgId = StringUtil.objectToStr(params.getAttr("orgId"));
		//当前登录人ID
		String custId = StringUtil.objectToStr(params.getAttr("custId"));
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> orders = (List<Map<String, Object>>) params.getAttr("orders");
		int sucSize = 0;
		AppParam updateParam = new AppParam("netStorePoolService","update");
		for (Map<String, Object> orderMap : orders) {
			String applyId = StringUtil.getString(orderMap.get("applyId"));
			// 计算成本
			boolean costFlag = AllotCostUtil.saveOrgAllotOrderCost(orgId, applyId,null,true);
			
			if(!costFlag) throw new SysException("转门店时，余额不足，请尽量选少量单转");
			
			updateParam.addAttr("applyId", applyId);
			updateParam.addAttr("orgId", orgId);//门店
			AppResult updateResult = SoaManager.getInstance().invoke(updateParam);
			int updateSize = NumberUtil.getInt(updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE),0);
			if(updateSize > 0){
				//插入门店人员操作记录
				StoreOptUtil.insertStoreRecord(applyId, custId, StoreConstant.STORE_OPER_34, 
						"新单转门店[CUSTID=]" + custId, 0, 1, 0, 1);
				sucSize ++ ;
			}
		}
		result.putAttr("sucSize", sucSize);
		return result;
	}
	
}
