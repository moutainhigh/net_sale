package org.xxjr.cust.info;

import java.util.Date;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.FundConstant;
import org.xxjr.cust.util.ShowErrorCode;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.sys.util.DBConst;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.SysParamsUtil;

@Lazy
@Service
public class CustomerService extends BaseService {
	private static final String NAMESPACE = "CUSTOMER";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * 登录方法
	 * @param params
	 * @return
	 */
	public AppResult login(AppParam params) {
		return super.query(params, NAMESPACE, "login");
	}
	
	/**
	 * queryStores
	 * @param params
	 * @return
	 */
	public AppResult queryStores(AppParam params) {
		return super.query(params, NAMESPACE, "queryStores");
	}
	
	/**
	 * queryView
	 * @param params
	 * @return
	 */
	public AppResult queryView(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryView", "queryViewCount");
	}
	
	/** 查询即将过期用户
	 * queryVipOverdue
	 * @param params
	 * @return
	 */
	public AppResult queryVipOverdue(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryVipOverdue", "queryVipOverdueCount");
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
	 * custCount
	 * @param params
	 * @return
	 */
	public AppResult custCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "custCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * 查询客服列表
	 */
	public AppResult queryKefuCfgList(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryKefuCfgList", "queryKefuCfgListCount");
	}
	
	/**
	 * 修改当前用户状态
	 * updateStatus
	 * @param params
	 * @return
	 */
	public AppResult updateStatus(AppParam params) {
		int size = super.getDao().update(NAMESPACE, "updateStatus", params.getAttr(), params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		RedisUtils.getRedisService().del(CustomerIdentify.CacheKey_PASS + params.getAttr("customerId"));
		return result;
	}
	
	/**
	 * 纯净的update
	 * updateStatus
	 * @param params
	 * @return
	 */
	public AppResult newUpdate(AppParam params) {
		AppResult result =  super.update(params, NAMESPACE);
		int isReflushCache = NumberUtil.getInt(params.getAttr("isReflushCache"), 0);
		if((Integer)result.getAttr(DuoduoConstant.DAO_Update_SIZE) >0 && isReflushCache == 1){
			RedisUtils.getRedisService().del(CustomerIdentify.CacheKey_PASS + params.getAttr("customerId"));
		}
		
		return result;
	}
	
	/**
	 * 查询客户学徒信息
	 * @param params
	 * @return
	 */
	public AppResult queryApprentice(AppParam params){
		return super.queryByPage(params, NAMESPACE, "queryApprentice","queryApprenticeCount");
	}
	
	/**
	 * insert
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		params.addAttr("createTime", new Date());
		params.addAttr("gzhId", SysParamsUtil.getStringParamByKey("xxjrGzh", "11"));
		params.addAttr("createBy", DuoduoSession.getUserName());
		AppResult result = super.insert(params, NAMESPACE);
		return result;
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		AppResult result =  super.update(params, NAMESPACE);
		Object userName = params.getAttr("userName");
		Object isAdmin = params.getAttr("isAdmin");
		String customerId = params.getAttr("customerId").toString();
		if(result.isSuccess() && !StringUtils.isEmpty(userName) && !StringUtils.isEmpty(isAdmin)){
			AppParam identifyParam = new AppParam("custIdentifyService","query");
			identifyParam.addAttr("customerId", customerId);
			result = SoaManager.getInstance().invoke(identifyParam);
			if(result.getRows().size() == 1){
				identifyParam.setMethod("update");
				identifyParam.addAttr("realName", userName);
				SoaManager.getInstance().invoke(identifyParam);

			}else{
				identifyParam.setMethod("insert");
				identifyParam.addAttr("realName", userName);
				SoaManager.getInstance().invoke(identifyParam);
			}
		}
		RedisUtils.getRedisService().del(CustomerIdentify.CacheKey_PASS + customerId);
		return result;
	}
	
	/**复制权限用到
	 * copyRoleType
	 * @param params
	 * @return
	 */
	public AppResult copyRoleType(AppParam params) {
		AppResult result = new AppResult();
		Object reCustomerId = params.getAttr("reCustomerId");//参考人ID
		Object customerId = params.getAttr("customerId");// 目标人ID
		if(StringUtils.isEmpty(reCustomerId) || StringUtils.isEmpty(customerId)){
			result.setMessage("必要参数为空!");
			result.setSuccess(false);
			return result;
		}
		AppParam queryParam = new AppParam();
		queryParam.addAttr("customerId", reCustomerId);
		queryParam.setDataBase(DBConst.Key_cust_DB);
		result = this.query(queryParam);
		if(result.getRows().size() > 0){
			Map<String, Object> queryMap = result.getRow(0);
			
			AppParam updateParam = new AppParam();
			updateParam.addAttr("customerId", customerId);
			updateParam.addAttr("roleType", queryMap.get("roleType"));
			updateParam.addAttr("storeNo", queryMap.get("storeNo"));
			updateParam.setDataBase(DBConst.Key_cust_DB);
			result = this.update(updateParam);
		}
		RedisUtils.getRedisService().del(CustomerIdentify.CacheKey_PASS + customerId);
		return result;
	}
	
	
	/***
	 * 查询用户详细信息
	 * @param context
	 * @return
	 */
	public AppResult queryCustInfo(AppParam context) {
		return super.query(context, NAMESPACE, "queryCustInfo");
	}
	
	/***
	 * 登录查寻
	 * @param context
	 * @return
	 */
	public AppResult loginQuery(AppParam context) {
		return super.query(context, NAMESPACE, "loginQuery");
	}
	
	
	/***
	 * 锁定用户
	 * @param context
	 * @return
	 */
	public AppResult lockCust(AppParam context){
		String customerId = context.getAttr("customerId").toString();
		String status =  context.getAttr("status").toString();
		if( status.equals("1") || status.equals("2")){
			AppParam param = new AppParam();
			param.addAttr("customerId", customerId);
			param.addAttr("status", status);
			param.setDataBase(DBConst.Key_cust_DB);
			return super.update(param, NAMESPACE);
		}
		throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
	}
	
	
	/***
	 *	查用户ID 和用户名 CustomerId
	 * 可使用条件： loginStatus,userName,startLoginTime
	 * @param context
	 * @return
	 */
	public AppResult queryCustomerId(AppParam params){
		return super.queryByPage(params, NAMESPACE, "queryCustomerId","queryCustomerIdCount");
	}
	
	/***
	 *	查询用户ID和用户名
	 * 可使用条件： loginStatus,userName,startLoginTime
	 * @param context
	 * @return
	 */
	public AppResult queryCustName(AppParam params){
		return super.query(params, NAMESPACE, "queryCustomerId");
	}
	
	
	/***
	 * 查用户ID 和用户名 CustomerId的计数
	 * 可使用条件： loginStatus,userName,startLoginTime
	 * @param context
	 * @return
	 */
	public AppResult queryCustomerIdCount(AppParam params){
		int size = getDao().count(NAMESPACE, "queryCustomerIdCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/***
	 * cust registe
	 * @param context
	 * @return
	 */
	public AppResult register(AppParam context){
		context.addAttr("registerTime", new Date());
		context.addAttr("status", "1");
	    context.addAttr("loginStatus", "0");
	    
		if(StringUtils.isEmpty(context.getAttr("sourceType"))){
			throw new AppException(ShowErrorCode.CUST_HAVE_NO_SOURCETYPE);
		}
		
		if(StringUtils.isEmpty(context.getAttr("gzhId"))){
			context.addAttr("gzhId", SysParamsUtil.getStringParamByKey("xxjrGzh", "11"));
		}
		AppResult result = insert(context);
		
		Object customerId = context.getAttr("customerId");
		
		//个人信息初始化
		AppParam infoParam = new AppParam();
		infoParam.addAttr("customerId", customerId);
		infoParam.addAttr("sex", context.getAttr("sex"));
		infoParam.addAttr("cityName", context.getAttr("cityName"));
		infoParam.setService("custInfoService");
		infoParam.setMethod("insert");
		SoaManager.getInstance().invoke(infoParam);
		
		//金额初始化
		AppParam amountParam = new AppParam();
		amountParam.addAttr("customerId", customerId);
		amountParam.addAttr("gzhId", SysParamsUtil.getStringParamByKey("xxjrGzh", "11"));
		amountParam.setService("custAmountService");
		amountParam.setMethod("insert");
		SoaManager.getInstance().invoke(amountParam);
		
		result.putAttr("customerId", customerId);
		return result;
	}
	
	/**
	 * 出借人个人信息修改
	 * @param context
	 * @return
	 */
	public AppResult modifyInfo(AppParam context){
		Object customerId = context.getAttr("customerId");
		if(StringUtils.isEmpty(customerId)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		// 修改个人信息
		AppParam customerParam = new AppParam();
		customerParam.setDataBase(DBConst.Key_cust_DB);
		if(!StringUtils.isEmpty(context.getAttr("userName")) ||
				!StringUtils.isEmpty(context.getAttr("email")) ){
			customerParam.addAttr("userName", context.getAttr("userName"));
			customerParam.addAttr("email", context.getAttr("email"));
			customerParam.addAttr("customerId", customerId);
			this.update(customerParam);
		}
		
		AppParam custInfoParam = new AppParam();
		custInfoParam.setService("custInfoService");
		custInfoParam.setMethod("update");
		custInfoParam.addAttr("headStatus", context.getAttr("headStatus"));
		Map<String,Object> custInfo = CustomerIdentify.getCustIdentify(customerId.toString());
		Object userImage = custInfo.get("userImage");
		Object headImgUrl = context.getAttr("headImgUrl");
		if(!StringUtils.isEmpty(headImgUrl)){//如果头像重新上传，需要重新审核
			if(StringUtils.isEmpty(userImage) || !headImgUrl.toString().equals(userImage.toString())){
				custInfoParam.addAttr("headStatus", 0);
				custInfoParam.addAttr("auditBy", "");
				custInfoParam.addAttr("auditTime", "");
			}
		}

		custInfoParam.addAttr("headImgUrl", context.getAttr("headImgUrl"));
		custInfoParam.addAttr("qrcodeImgUrl", context.getAttr("qrcodeImgUrl"));
		custInfoParam.addAttr("provice", context.getAttr("provice"));
		custInfoParam.addAttr("job", context.getAttr("job"));
		custInfoParam.addAttr("cityName", context.getAttr("cityName"));
		custInfoParam.addAttr("sex", context.getAttr("sex"));
		custInfoParam.addAttr("cityArea", context.getAttr("cityArea"));
		custInfoParam.addAttr("address", context.getAttr("address"));
		custInfoParam.addAttr("custDesc", context.getAttr("custDesc"));
		custInfoParam.addAttr("customerId", customerId);
		SoaManager.getInstance().invoke(custInfoParam);

		// 真实姓名
		Object realName = context.getAttr("realName");
		
		AppParam cardParam = new AppParam();
		cardParam.setService("custIdentifyService");
		if(!StringUtils.isEmpty(realName) && (custInfo.get("identifyStatus")==null ||
				!"1".equals(custInfo.get("identifyStatus").toString()))){
			cardParam.addAttr("realName", context.getAttr("realName"));
		}
		if (custInfo.get("identifyStatus")==null ||
				!"1".equals(custInfo.get("identifyStatus").toString())) {
			cardParam.addAttr("cardNo", context.getAttr("cardNo"));
			cardParam.addAttr("idCardLimit", context.getAttr("idCardLimit"));
			cardParam.addAttr("idCardSignOrg", context.getAttr("idCardSignOrg"));
			cardParam.addAttr("photoUrl", context.getAttr("photoUrl"));
			cardParam.addAttr("realImage", context.getAttr("realImage"));
			cardParam.addAttr("idCardImage", context.getAttr("idCardImage"));
		}
		cardParam.addAttr("customerId", customerId);
		cardParam.addAttr("status", custInfo.get("identifyStatus"));
		cardParam.setMethod("update");
		SoaManager.getInstance().invoke(cardParam);
		
		AppParam params = new AppParam();
		params.setService("custIdentifyCardService");
		params.setMethod("update");
		if (!"1".equals(custInfo.get("cardStatus").toString())) {
			params.addAttr("company", context.getAttr("company"));
			params.addAttr("companyDesc", context.getAttr("companyDesc"));
		}
		params.addAttr("compType", context.getAttr("compType"));
		params.addAttr("status", custInfo.get("cardStatus"));
		params.addAttr("customerId", customerId);
		SoaManager.getInstance().invoke(params);
		
		return new AppResult();
	}
	
	
	/**
	 * 更新用户表unionid及 openid绑定关系
	 * @param context
	 * @return
	 */
	public AppResult setUnionidAndOpenid(AppParam context){
		updateUnionid(context);
		updateOpenid(context);
		return new AppResult();
	}
	
	/**
	 * 更新用户表 openid绑定关系
	 * @param context
	 * @return
	 */
	public AppResult updateOpenid(AppParam context){
		AppResult result = new AppResult();
		Object customerId = context.getAttr("customerId");
		Object openid = context.getAttr("openid");
		if(StringUtils.isEmpty(customerId) || StringUtils.isEmpty(openid)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		AppParam queryParam = new AppParam();
		queryParam.addAttr("openid", openid);
		queryParam.setDataBase(DBConst.Key_cust_DB);
		AppResult queryResult = this.query(queryParam);
		if(queryResult.getRows().size() > 0){
			// 存在openid绑定关系则先置空原来的
			Map<String,Object> custInfo = queryResult.getRow(0);
			String queryCustomerId = custInfo.get("customerId").toString();
			if(queryCustomerId.equals(customerId.toString())){
				return result;
			}
			AppParam clearParam = new AppParam();
			clearParam.addAttr("customerId", queryCustomerId);
			clearParam.setDataBase(DBConst.Key_cust_DB);
			super.getDao().update(NAMESPACE, "clearOpenid", clearParam.getAttr(), clearParam.getDataBase());
		}
		AppParam updateParam = new AppParam();
		updateParam.addAttr("customerId", customerId);
		updateParam.addAttr("openid", openid);
		updateParam.setDataBase(DBConst.Key_cust_DB);
		this.update(updateParam);
		
		return result;
	}
	
	/**
	 * 更新用户表 unionid绑定关系
	 * @param context
	 * @return
	 */
	public AppResult updateUnionid(AppParam context){
		AppResult result = new AppResult();
		Object customerId = context.getAttr("customerId");
		Object unionid = context.getAttr("unionid");
		if(StringUtils.isEmpty(customerId) || StringUtils.isEmpty(unionid)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		AppParam queryParam = new AppParam();
		queryParam.addAttr("unionid", unionid);
		queryParam.setDataBase(DBConst.Key_cust_DB);
		AppResult queryResult = this.query(queryParam);
		if(queryResult.getRows().size() > 0){
			Map<String,Object> custInfo = queryResult.getRow(0);
			String queryCustomerId = custInfo.get("customerId").toString();
			if(queryCustomerId.equals(customerId.toString())){
				return result;
			}
			// 存在unionid绑定关系则先置空原来的
			AppParam clearParam = new AppParam();
			clearParam.addAttr("customerId", queryCustomerId);
			clearParam.setDataBase(DBConst.Key_cust_DB);
			super.getDao().update(NAMESPACE, "clearUnionid", clearParam.getAttr(), clearParam.getDataBase());
		}
		AppParam updateParam = new AppParam();
		updateParam.addAttr("customerId", customerId);
		updateParam.addAttr("unionid", unionid);
		updateParam.setDataBase(DBConst.Key_cust_DB);
		this.update(updateParam);
		
		return result;
	}
	
	/**
	 * 清空该用户余额及会员资格
	 * @param context
	 * @return
	 */
	public AppResult clearVip(AppParam context){
		AppResult result = new AppResult();
		Object customerId = context.getAttr("customerId");
		if(StringUtils.isEmpty(customerId)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		int closeVipSize = super.getDao().update(NAMESPACE, "closeVip", context.getAttr(), context.getDataBase());
		if (closeVipSize > 0) {
			context.addAttr("vipGrade", "0");
			//updateBusiVipGrade(context);
		}
		
		// 清空余额
		AppParam updateParam = new AppParam();
		updateParam.setService("custAmountService");
		updateParam.setMethod("clearUsableAmount");
		updateParam.addAttr("customerId", customerId);
		updateParam.addAttr("recordDesc", "会员全额退款");
		updateParam.addAttr("fundType", FundConstant.FundType_VIP_BACK);
		SoaManager.getInstance().invoke(updateParam);
		
		// 清空缓存
		//RedisUtils.getRedisService().del(CustAmountUtil.CacheKey_PASS + customerId);
		RedisUtils.getRedisService().del(CustomerIdentify.CacheKey_PASS + customerId);
		return result;
	}
	
	/**
	 * 用户openid置空
	 * @param context
	 * @return
	 */
	public AppResult clearOpenid(AppParam context){
		AppResult result = new AppResult();
		int size = super.getDao().update(NAMESPACE, "clearOpenid", context.getAttr(), context.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	/**
	 * 查询用户基本信息和权限信息
	 * @param params
	 * @return
	 */
	public AppResult queryWithRight(AppParam params){
		return super.query(params, NAMESPACE, "queryWithRight");
	}
	
	/**
	 * 用户修改个人信息
	 * @param params
	 * @return
	 */
	public AppResult updateInfo(AppParam params) {	
		params.addAttr("isAdmin", "isAdmin");
		AppResult result =  this.update(params);
		//Object customerId = params.getAttr("customerId");
		Object roleType = params.getAttr("roleType");
		if(!StringUtils.isEmpty(roleType) && (CustConstant.CUST_ROLETYPE_1.equals(roleType.toString()) 
				|| CustConstant.CUST_ROLETYPE_2.equals(roleType.toString())
				|| CustConstant.CUST_ROLETYPE_3.equals(roleType.toString()))
				|| CustConstant.CUST_ROLETYPE_10.equals(roleType.toString())){
			//TODO 菜单权限通过客服角色配置
			//菜单
			/*Object menuId = params.getAttr("menuIds");
			if(!StringUtils.isEmpty(menuId)&& menuId.toString().split(",").length>0){
				List<Map<String,Object>> menuIds = new ArrayList<Map<String,Object>>();
				for(String id:menuId.toString().split(",")){
					Map<String,Object> map = new HashMap<String,Object>();
					map.put("customerId", customerId);
					map.put("menuId", id);
					map.put("createTime", new Date());
					menuIds.add(map);
				}
				AppParam menuParam = new AppParam();
				menuParam.addAttr("customerId", customerId);
				super.getDao().delete("CUSTMENU", "delByCustId",menuParam.getAttr(), params.getDataBase());
			    super.getDao().batchInsert("CUSTMENU", "batchInsert", menuIds, params.getDataBase());
			}*/
		    //权限
		    int size = super.getDao().update("CUSTRIGHT", "update", params.getAttr(), params.getDataBase());
		    if(size<1){
		    	 super.getDao().insert("CUSTRIGHT", "insert", params.getAttr(), params.getDataBase());
		    }
		    
		}
		
		return result;
	}
	
	
	/**
	 * 查询用户列表queryView
	 * @param 
	 * @return
	 */
	public AppResult queryCustList(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryCustList", "queryCustCount");
	}

	/**
	 * 查询用户密码
	 * @param context
	 * @return
	 */
	public AppResult queryCustPwd(AppParam context){
		return super.query(context, NAMESPACE, "queryCustPwd");
	}
	
	/***
	 * 变更号码
	 * @param params
	 * @return
	 */
	public AppResult updateTelephone(AppParam params){
		AppResult result = new AppResult();
		String customerId = StringUtil.getString(params.getAttr("customerId"));
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("customerId不能为空");
			return result;
		}
		int size = super.getDao().update(NAMESPACE, "updateTelephone", params.getAttr(), params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
	/**
	 * queryCustMessage
	 * @param params
	 * @return
	 */
	public AppResult queryCustMessage(AppParam params) {
		return super.query(params, NAMESPACE, "queryCustMessage");
	}
	
	/**
	 * 过期的Vip
	 * @param params
	 * @return
	 */
	public AppResult expiredVipCust(AppParam params){
		return super.query(params, NAMESPACE, "expiredVipCust");
	}
	
	/**
	 * 按天查询注册人数
	 * @param 
	 * @return
	 */
	public AppResult queryRegOfDay(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryRegOfDay", "queryRegOfDayCount");
	}
	
}