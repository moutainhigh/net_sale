package org.xxjr.busiIn.kf;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.exception.SysException;
import org.ddq.common.security.md5.Md5;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.BorrowConstant;
import org.xxjr.busiIn.utils.AllotCostUtil;
import org.xxjr.busiIn.utils.StoreOptUtil;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.SysParamsUtil;


@Lazy
@Service
public class BorrowApplyService extends BaseService {
	public static final String NAMESPACE = "BORROWAPPLY";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * 查询疑似回款
	 * @param params
	 * @return
	 */
	public AppResult queryIncome(AppParam params) {
		return super.query(params, NAMESPACE, "queryIncome");
	}
	/**
	 * 查询优质单数量
	 * @param params
	 * @return
	 */
	public AppResult querySeniorCount(AppParam params) {
		return super.query(params, NAMESPACE, "applySeniorCount");
	}
	

	/**
	 * pendStoreCount
	 * @param params
	 * @return
	 */
	public AppResult pendStoreCount(AppParam params) {
		return super.query(params, NAMESPACE,"pendStoreCount");
	}


	/**
	 * querySimpleInfo
	 * @param params
	 * @return
	 */
	public AppResult querySimpleInfo(AppParam params) {
		return super.query(params, NAMESPACE, "querySimpleInfo");
	}

	/**
	 * queryPushInfo
	 * @param params
	 * @return
	 */
	public AppResult queryPushInfo(AppParam params) {
		return super.query(params, NAMESPACE, "queryPushInfo");
	}
	
	/**
	 * queryBaseInfo
	 * @param params
	 * @return
	 */
	public AppResult queryBaseInfo(AppParam params) {
		return super.query(params, NAMESPACE, "queryBaseInfo");
	}

	/**简要信息，客服操作时用的
	 * queryPriefInfo
	 * @param params
	 * @return
	 */
	public AppResult queryPriefInfo(AppParam params) {
		return super.query(params, NAMESPACE, "queryPriefInfo");
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
	 * queryShowByPage
	 * @param params
	 * @return
	 */
	public AppResult queryShowByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryShow", "queryShowCount");
	}
	/**
	 * query2Count
	 * @param params
	 * @return
	 */
	public AppResult query2Count(AppParam params) {
		return super.query(params, NAMESPACE, "queryCount");
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
	 * batchUpdateStage
	 * @param params
	 * @return
	 */
	public AppResult batchUpdateStage(AppParam params) {
		int size = getDao().update(NAMESPACE, "batchUpdateStage", params.getAttr(),params.getDataBase());
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
		Date d1 = new Date();
		params.addAttr("createTime", d1);
		if (StringUtils.isEmpty(params.getAttr("applyTime"))) {
			params.addAttr("applyTime", d1);
		}		
		params.addAttr("updateTime", new Date());
		int applyType = NumberUtil.getInt(params.getAttr("applyType"), -1);
		if(applyType == -1){
			params.addAttr("applyType", BorrowConstant.apply_type_2);//普通单
			params.addAttr("haveDetail", "0");
		}
		params.addAttr("unionId", Md5.getInstance().encrypt
				(params.getAttr("telephone").toString()));
		
		AppResult result = super.insert(params, NAMESPACE);
		result.putAttr("applyId", params.getAttr("applyId"));
		return result;
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
	 * applyUpdate
	 * @param params
	 * @return
	 */
	public AppResult applyUpdate(AppParam params) {
		Object applyId = params.getAttr("applyId");
		Map<String, Object> applyMap = this.queryByApplyId(applyId);
		if(applyMap != null && !applyMap.isEmpty()){
			int applyCount = NumberUtil.getInt(applyMap.get("applyCount"), 0);
			if(applyCount >= 1){
				
				AppParam clearParam = new AppParam();
				clearParam.addAttr("applyId",applyId);
				this.clearData(clearParam);//还原数据
				
				AppParam reParams = new AppParam();
				reParams.addAttr("referer", applyMap.get("referer"));
				reParams.addAttr("telephone", applyMap.get("telephone"));
				reParams.addAttr("applyName", applyMap.get("applyName"));
				reParams.addAttr("channelDetail", applyMap.get("channelDetail"));
				reParams.setService("borrowApplyDtlService");
				reParams.setMethod( "insert");
				SoaManager.getInstance().invoke(reParams);

				if(!StringUtils.isEmpty(params.getAttr("isLite"))){//简版过来，需要删除 pageReferer，否则会覆盖
					params.removeAttr("pageReferer");
				}else{
					params.addAttr("applyCount", applyCount+1);
				}
				
				if(StringUtils.isEmpty(params.getAttr("status"))){
					params.addAttr("status", 0);
				}
				
				if (StringUtils.isEmpty(params.getAttr("applyTime"))) {
					params.addAttr("applyTime", new Date());
				}	
				params.addAttr("createTime", new Date());
				return super.update(params, NAMESPACE);
			}
		}

		throw new SysException("申请失败，请尝试重新申请！");
	}

	/**
	 * 客服编辑借款信息
	 * @param params
	 * @return
	 */
	public AppResult editInfo(AppParam params) {
		Object applyId = params.getAttr("applyId");
		Object customerId = params.getAttr("customerId");
		int newApplyType = NumberUtil.getInt(params.getAttr("applyType"));
		if(StringUtils.isEmpty(applyId) || StringUtils.isEmpty(customerId)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}

		// 判断是否为当前处理人
		Map<String,Object> applyInfo = queryByApplyId(applyId);
		int oldApplyType = NumberUtil.getInt(applyInfo.get("applyType"));
		if(!CustomerUtil.isAdmin(customerId.toString())){
			if(!StringUtils.isEmpty(applyInfo.get("lastKf")) 
					&& !customerId.equals(applyInfo.get("lastKf").toString())){
				throw new SysException("您不是当前借款处理人，不能修改借款信息");
			}
		}

		// 更新apply信息
		AppParam updateParam = new AppParam();
		updateParam.addAttr("applyId", applyId);
		updateParam.addAttr("telephone", params.getAttr("telephone"));
		updateParam.addAttr("applyType", params.getAttr("applyType"));
		updateParam.addAttr("cityName", params.getAttr("cityName"));
		updateParam.addAttr("haveDetail", 1);
		updateParam.addAttr("channelDetail", params.getAttr("channelDetail"));
		AppResult result = this.update(updateParam);

		if(result.isSuccess() && !StringUtils.isEmpty(applyId)){
//			double price = NumberUtil.getDouble(params.getAttr("price"));
//			params.addAttr("price", price*100);//价格乘以100
			//保存基本信息
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
			if(!StringUtils.isEmpty(params.getAttr("houseType"))){
				delParam.setService("borrowHouseService");
				SoaManager.getInstance().invoke(delParam);
				
				params.setService("borrowHouseService");
				params.setMethod("insert");
				SoaManager.getInstance().invoke(params);
			}
			
			// 修改车产信息
			if(!StringUtils.isEmpty(params.getAttr("carType"))){
				delParam.setService("borrowCarService");
				SoaManager.getInstance().invoke(delParam);
				
				params.setService("borrowCarService");
				params.setMethod("insert");
				SoaManager.getInstance().invoke(params);
			}
			
			// 修改保险信息
			if(!StringUtils.isEmpty(params.getAttr("insurType"))){
				delParam.setService("borrowInsureService");
				SoaManager.getInstance().invoke(delParam);
				
				params.setService("borrowInsureService");
				params.setMethod("insert");
				SoaManager.getInstance().invoke(params);
			}
			
			// 同步门店申请表
			if(!StringUtils.isEmpty(params.getAttr("cityName"))){
				AppParam storeParam = new AppParam();
				storeParam.setService("borrowStoreApplyService");
				storeParam.addAttr("applyId", applyId);
				storeParam.addAttr("cityName", params.getAttr("cityName"));
				storeParam.setMethod("update");
				SoaManager.getInstance().invoke(storeParam);
			}

			int haveDetial = NumberUtil.getInt(applyInfo.get("haveDetail"), 0);
			String handleDesc = StringUtil.getString(params.getAttr("handleDesc"));
			if(newApplyType != oldApplyType){
				handleDesc = handleDesc+"【单子类型改变(1-优质单，2-普通单)，由"+oldApplyType+"-" + newApplyType +"】";
			}
			// 增加客服修改记录
			AppParam recordParam = new AppParam();
			recordParam.setService("borrowKfRecordService");
			recordParam.setMethod("insert");
			recordParam.addAttr("applyId", applyId);
			recordParam.addAttr("handleDesc", params.getAttr("handleDesc"));
			recordParam.addAttr("kf", customerId);
			recordParam.addAttr("handleType", haveDetial==1 ? BorrowConstant.KEFU_OPER_6 : BorrowConstant.KEFU_OPER_10);
			SoaManager.getInstance().invoke(recordParam);
		}


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
				param.addAttr("applyId", id);

				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("applyId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}

	/**
	 * 
	 * @param applyId
	 * @return
	 */
	private Map<String,Object> queryByApplyId(Object applyId){
		AppParam queryParam = new AppParam();
		queryParam.addAttr("applyId", applyId);
		AppResult queryResult = this.query(queryParam);
		if(queryResult.getRows().size() == 0){
			throw new SysException(DuoduoError.UPDATE_DATA_IS_NOTEXISTS);
		}
		return queryResult.getRow(0);
	}

	/**
	 * kfSelfUnHandle
	 * @param params
	 * @return
	 */
	public AppResult kfSelfUnHandle(AppParam params) {
		return super.query(params, NAMESPACE,"kfSelfUnHandle");
	}

	/**
	 * allStoreNotice
	 * @param params
	 * @return
	 */
	public AppResult allStoreNotice(AppParam params) {
		params.addAttr("seniorData", SysParamsUtil.getStringParamByKey("seniorData", "kefu"));
		return super.query(params, NAMESPACE,"allStoreNotice");
	}

	/**
	 * singleStoreNotice
	 * @param params
	 * @return
	 */
	public AppResult singleStoreNotice(AppParam params) {
		return super.query(params, NAMESPACE,"singleStoreNotice");
	}


	/**
	 * storeSelfUnHandle
	 * @param params
	 * @return
	 */
	public AppResult storeSelfUnHandle(AppParam params) {
		return super.query(params, NAMESPACE,"storeSelfUnHandle");
	}

	/**
	 * 批量更新转化的状态 ( 可转化--转化中)
	 * @param context
	 * @return
	 */
	public AppResult updateTransStatus(AppParam context){
		AppResult result = new AppResult();
		int size = super.getDao().update(NAMESPACE, "updateTransStatus", context.getAttr(), context.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Insert_SIZE, size);
		return result;
	}
	/**
	 * 批量更新转化的状态 ( 可转化--转化失败)
	 * @param context
	 * @return
	 */
	public AppResult updateTransFailStatus(AppParam context){
		AppResult result = new AppResult();
		int size = super.getDao().update(NAMESPACE, "updateTransFailStatus", context.getAttr(), context.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}


	/**
	 * 客服左菜单数量统计(管理员)
	 * @param params
	 * @return
	 */
	public AppResult querySummary1(AppParam params){
		return super.query(params, NAMESPACE,"querySummary1");
	}

	/**
	 *  客服左菜单数量统计(普通用户)
	 * @param params
	 * @return
	 */
	public AppResult querySummary2(AppParam params){
		return super.query(params, NAMESPACE,"querySummary2");
	}

	/**
	 *  客服左菜单数量统计(贷上我类型的用户统计)
	 * @param params
	 * @return
	 */
	public AppResult querySummary3(AppParam params){
		return super.query(params, NAMESPACE,"querySummary3");
	}

	/**
	 * 优质单列表查询
	 * @param params
	 * @return
	 */
	public AppResult seniorList(AppParam params){
		params.addAttr("seniorData", SysParamsUtil.getStringParamByKey("seniorData", "kefu"));
		return super.queryByPage(params, NAMESPACE,"seniorList", "seniorCount");
	}

	/**
	 * 保存所有信息
	 * @param params
	 * @return
	 */
	public AppResult saveAllApllyInfo(AppParam params){
		AppResult result = new AppResult();
		Object applyId = params.getAttr("applyId");
		Object pageReferer = params.getAttr("pageReferer");
		params.addAttr("applyType", "1");
		params.addAttr("haveDetail", 1);
		
		String cityName = StringUtil.getString(params.getAttr("cityName"));
		double loanAmount = NumberUtil.getDouble(params.getAttr("loanAmount"),0);
		int houseType = NumberUtil.getInt(params.getAttr("houseType"),0);
		int haveDetail = NumberUtil.getInt(params.getAttr("haveDetail"),0);
		boolean flag = StoreOptUtil.IntSaleCityJudge(cityName, loanAmount, houseType);
		if(flag){
			params.addAttr("tsFlag", 1);
			params.addAttr("status", BorrowConstant.apply_status_0);
		}else{
			if(loanAmount<=0 || haveDetail == 0){
				params.addAttr("status", BorrowConstant.apply_status_0);
				params.addAttr("haveDetail", 0);
			}
		}
		
		if(StringUtils.isEmpty(applyId)){
			result = this.insert(params);
			applyId = result.getAttr("applyId");	
		}else{
			AppParam clearParam = new AppParam("borrowApplyService","clearData");
			clearParam.addAttr("applyId",applyId);
			this.clearData(clearParam);//还原数据
			
			result = this.applyUpdate(params);
		}
		
		
		String applyStatus = StringUtil.getString(params.getAttr("status"));
		if(result.isSuccess() && BorrowConstant.apply_status_3.equals(applyStatus)){
			
			Map<String,Object> pushMap = new HashMap<String, Object>();
			pushMap.putAll(params.getAttr());
			pushMap.put("applyId", applyId);
		}
		
		
		AppParam baseParams = new AppParam("borrowBaseService", "update");
		baseParams.addAttrs(params.getAttr());
		baseParams.addAttr("pageReferer", pageReferer);
		baseParams.addAttr("score", 10000);
		baseParams.addAttr("price", 10000);
		if(result.isSuccess()){
			result = SoaManager.getInstance().invoke(baseParams);
		}
		if(result.isSuccess()){
			if(StringUtils.isEmpty(baseParams.getAttr("income"))){
				baseParams.addAttr("income", 0);
			}
			baseParams.setService("borrowIncomeService");
			baseParams.setMethod("update");
			SoaManager.getInstance().invoke(baseParams);
		}
		if(result.isSuccess()){
			if(!StringUtils.isEmpty(params.getAttr("houseVal"))){
				baseParams.setService("borrowHouseService");
				baseParams.setMethod("update");
				SoaManager.getInstance().invoke(baseParams);
			}
		}
		if(result.isSuccess()){
			if(!StringUtils.isEmpty(params.getAttr("carPrice"))){
				baseParams.setService("borrowCarService");
				baseParams.setMethod("update");
				SoaManager.getInstance().invoke(baseParams);
			}
		}

		if(result.isSuccess()){
			String insurType = StringUtil.getString(params.getAttr("insurType"));
			if(StringUtils.hasText(insurType) && !"0".equals(insurType)){
				baseParams.setService("borrowInsureService");
				baseParams.setMethod("update");
				SoaManager.getInstance().invoke(baseParams);
			}
		}

		result.putAttr("applyId", applyId);
		return result;
	}
	/**
	 * 保存所有信息
	 * @param params
	 * @return
	 */
	public AppResult saveAllApply(AppParam params){
		AppResult result = new AppResult();
		result = AllotCostUtil.immedTranApply(params.getAttr());
		if (!result.isSuccess()) {
			throw new SysException("插入失败，回滚");
		}
		return result;
	}


	/**
	 *抢优质单
	 * @param params
	 * @return
	 */
	public AppResult robSenior(AppParam params){
		AppResult result = new AppResult();
		Object applyId = params.getAttr("applyId");
		if(StringUtils.isEmpty(applyId)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		String customerId = params.getAttr("customerId").toString();
		Map<String, Object> custMap = CustomerIdentify.getCustIdentify(customerId);
		params.addAttr("lastStore", customerId);
		params.addAttr("orgId",custMap.get("orgId"));
		params.addAttr("lockBy", customerId);
		int size = super.getDao().update(NAMESPACE, "robSenior", params.getAttr(), params.getDataBase());
		if(size == 1){
			AppParam queryStoreParam = new AppParam("borrowStoreApplyService", "query");
			queryStoreParam.addAttr("applyId", applyId);
			AppResult queryResult = SoaManager.getInstance().invoke(queryStoreParam);
			//加入网销申请表
			AppParam applyParam = new AppParam();
			applyParam.addAttr("applyId", applyId);
			applyParam.setService("borrowStoreApplyService");
			if (queryResult.getRows().size() > 0) {
				applyParam.addAttr("lastStore", customerId);
				applyParam.setMethod("update");
			}else{
				applyParam.setMethod("insertByBorrowApply");
			}
			SoaManager.getInstance().invoke(applyParam);
			
		}else{
			throw new SysException("手慢了，该优质单已被其他人抢走");
		}
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		if(result.isSuccess()){
			this.insertStoreRecord(applyId, customerId, BorrowConstant.STORE_OPER_0, "门店人员抢单",
					params.getAttr("robWay"),params.getAttr("price"),params.getAttr("score"),0,1);
		}
		return result;
	}

	/**
	 * 插入门店人员操作记录
	 */
	private AppResult insertStoreRecord(Object applyId,Object storeBy,
			String handleType,Object handleDesc, Object robWay,Object price, Object score,
			int isFeedback,int readFlag){
		String orgId = ""; 
		// 通过customerId获取orgId
		AppParam queryParams = new AppParam("busiCustService","query");
		queryParams.addAttr("customerId", storeBy);
		AppResult custResult = SoaManager.getInstance().invoke(queryParams);
		if(custResult.isSuccess() && custResult.getRows().size() > 0){
			orgId = StringUtil.getString(custResult.getRow(0).get("orgId"));
		}
		AppParam recordParams = new AppParam("borrowStoreRecordService", "insert");
		recordParams.addAttr("applyId", applyId);
		recordParams.addAttr("storeBy", storeBy);
		recordParams.addAttr("handleType",handleType);
		recordParams.addAttr("robWay", robWay);
		recordParams.addAttr("amount", price);
		recordParams.addAttr("score", score);
		recordParams.addAttr("handleDesc", handleDesc);
		recordParams.addAttr("isFeedback", isFeedback);
		recordParams.addAttr("readFlag", readFlag);
		recordParams.addAttr("orgId", orgId);
		return SoaManager.getInstance().invoke(recordParams);
	}
	
	/**
	 * 更新之前还原数据
	 * @param params
	 * @return
	 */
	public AppResult clearData(AppParam params) {
		int size = getDao().update(NAMESPACE, "clearData", params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	
	/**
	 * 所有申请列表
	 * @param params
	 * @return
	 */
	public AppResult queryAllApplayList(AppParam params){
		return super.queryByPage(params, NAMESPACE, "queryAllApplayList", "queryAllApplayCount");
	}
}
