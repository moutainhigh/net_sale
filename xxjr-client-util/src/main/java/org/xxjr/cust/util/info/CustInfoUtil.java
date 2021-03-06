package org.xxjr.cust.util.info;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;
import org.xxjr.tools.util.QcloudUploader;

public class CustInfoUtil {

	/***
	 * 用户类型 信贷员出借人
	 */
	public final static String UserType_1 = "1";
	/*
	 * 用户类型 借款人
	 */
	public final static String UserType_2 = "2";

	/**
	 * 修改个人信息
	 * @param params
	 * @return
	 */
	public static AppResult update(AppParam params) {
		params = new AppParam("custInfoService", "update");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = RemoteInvoke.getInstance().call(params);
		if (result.isSuccess()) {
			CustomerIdentify.refreshIdentifyById(params.getAttr("customerId")
					.toString());
		}
		return result;
	}

	/**
	 * 查看个人会员等级
	 * @param customerId
	 * @return
	 */
	public static Map<String, Object> queryGradeInfo(String customerId) {
		AppParam params = new AppParam();
		params.setService("custInfoService");
		params.setMethod("queryGradeInfo");
		params.addAttr("customerId", customerId);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		Map<String, Object> map = new HashMap<String, Object>();
		if (result.getRows().size() > 0) {
			map = result.getRow(0);
		}
		return map;
	}
	
	/**
	 * 更新用户头像
	 * @param customerId
	 * @param headImgUrl
	 * @return
	 */
	public static AppResult updateHeadImage(String customerId, Object headImgUrl) {
		if (StringUtils.isEmpty(headImgUrl)) {
			return null;
		}
		AppParam params = new AppParam("custInfoService", "update");
		params.addAttr("customerId", customerId);
		params.addAttr("headImgUrl", headImgUrl);
		
		Map<String,Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
		Object userImage = custInfo.get("userImage");
		boolean headImgChange = false;
		if(!StringUtils.isEmpty(headImgUrl)){//如果头像重新上传，需要重新审核
			if(StringUtils.isEmpty(userImage) || !headImgUrl.toString().equals(userImage.toString())){
				params.addAttr("headStatus", 0);
				params.addAttr("auditBy", "");
				params.addAttr("auditTime", "");
				headImgChange = true;
			}
		}

		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = RemoteInvoke.getInstance().call(params);
		if (result.isSuccess()) {
			DuoduoSession.getUser().getSessionData()
					.put("userImage", headImgUrl);
			CustomerIdentify.refreshIdentifyBySession(customerId);
			
			if(headImgChange && !StringUtils.isEmpty(userImage) &&
					userImage.toString().lastIndexOf("/") >= 0){//删除旧头像
				String headName = userImage.toString().substring(userImage.toString().lastIndexOf("/"));
				if(!StringUtils.isEmpty(headName)){
					QcloudUploader.deleteFile("/upfile/head/"+headName);
				}
			}
		}
		custInfo = null;
		return result;
	}

	/**
	 * 查询用户简单信息
	 * @param customerId
	 * @return
	 */
	public static Map<String, Object> queryPreCustInfo(String customerId) {
		AppParam param = new AppParam();
		param.addAttr("customerId", customerId);
		param.setService("custInfoService");
		param.setMethod("queryPriefCustInfo");
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = RemoteInvoke.getInstance().callNoTx(param);
		Map<String, Object> map = new HashMap<String, Object>();
		if (result.getRows().size() > 0) {
			map = result.getRow(0);
		}
		return map;
	}

	/**
	 * 查询用户身份实名信息
	 * 
	 * @param params
	 * @return
	 */
	public static Map<String, Object> queryIdentifyInfo(AppParam params) {
		params.setService("custIdentifyService");
		params.setMethod("query");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		Map<String, Object> map = new HashMap<String, Object>();
		if (result.getRows().size() > 0)
			map = result.getRow(0);

		result = null;
		return map;
	}

	/**
	 * 查询用户身份个数
	 * 
	 * @param params
	 * @return
	 */
	public static Integer queryIdentifyCount(AppParam params) {
		params.setService("custIdentifyService");
		params.setMethod("queryCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		return (Integer) result.getAttr(DuoduoConstant.TOTAL_SIZE);

	}

	/**
	 * 编辑用户身份信息
	 * 
	 * @param params
	 * @return
	 */
	public static AppResult editIdentifyInfo(AppParam params) {
		params.setService("custIdentifyService");
		params.setMethod("update");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		return RemoteInvoke.getInstance().call(params);

	}

	/**
	 * 查询用户工作认证信息
	 * 
	 * @param params
	 * @return
	 */
	public static Map<String, Object> queryWorkCardInfo(AppParam params) {
		params.setService("custIdentifyCardService");
		params.setMethod("query");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		Map<String, Object> map = new HashMap<String, Object>();
		if (result.getRows().size() > 0)
			map = result.getRow(0);

		result = null;
		return map;
	}

	/**
	 * 查询用户工作认证个数
	 * 
	 * @param params
	 * @return
	 */
	public static Integer queryWorkCardCount(AppParam params) {
		params.setService("custIdentifyCardService");
		params.setMethod("queryCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		return (Integer) result.getAttr(DuoduoConstant.TOTAL_SIZE);

	}

	/**
	 * 编辑用户工作认证信息
	 * 
	 * @param params
	 * @return
	 */
	public static AppResult editWorkCardInfo(AppParam params) {
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		params.setService("custIdentifyCardService");
		params.setMethod("update");
		return RemoteInvoke.getInstance().call(params);

	}

	/**
	 * 查询提现参数信息
	 * 
	 * @param custId
	 * @return
	 */
	public static Map<String, Object> queryCustPayInfo(String custId) {
		AppParam param = new AppParam();
		param.addAttr("customerId", custId);
		param.setService("custInfoService");
		param.setMethod("queryPayInfo");
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		AppResult result = RemoteInvoke.getInstance().callNoTx(param);
		if (result.getRows().size() > 0) {
			return result.getRow(0);
		}
		return null;
	}

	/**
	 * 判断用户是否是VIP
	 * 
	 * @param customerId
	 * @return
	 */
	public static boolean isVip(String customerId) {
		boolean vipOpenStatus = SysParamsUtil.getBoleanByKey("vipOpenStatus",
				false);
		if (!vipOpenStatus) {
			return false;
		}
		Map<String, Object> custInfo = CustomerIdentify
				.getCustIdentify(customerId);
		Object vipEndDate = custInfo.get("vipEndDate");
		Object vipGrade = custInfo.get("vipGrade");
		if (!StringUtils.isEmpty(vipGrade) && "1".equals(vipGrade.toString())
				&& !StringUtils.isEmpty(vipEndDate)) {
			Date now = new Date();
			try {
				Date endTime = DateUtils.parseDate(vipEndDate.toString()
						+ " 23:59:59", new String[]{DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS});
				if (endTime.compareTo(now) > 0) {
					return true;
				}
			} catch (ParseException e) {
				LogerUtil.error(CustInfoUtil.class, e, "parseDate error");
			}
		}
		return false;
	}
	
	/**
	 * 查询公司列表(分页)
	 * 
	 * @param params
	 * @return
	 */
	public static AppResult queryCompanysByPage(AppParam params) {
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		params.setService("companyService");
		params.setMethod("queryByPage");
		return RemoteInvoke.getInstance().call(params);

	}

	/**
	 * 查询公司列表
	 * 
	 * @param params
	 * @return
	 */
	public static AppResult queryCompanys(AppParam params) {
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		params.setService("companyService");
		params.setMethod("query");
		return RemoteInvoke.getInstance().call(params);

	}



	/**
	 * 在登录时将将以前的signid删除并插入最新的
	 * 
	 * @param params
	 * @return
	 */
	public static AppResult loginOperatingSignId(AppParam params) {
		AppResult result = new AppResult();
		if (StringUtils.isEmpty(params.getAttr("custId"))) {
			result.setMessage("缺少参数");
			return result;
		}
		if (StringUtils.isEmpty(params.getAttr("signId"))) {
			result.setMessage("缺少参数");
			return result;
		}
		if (StringUtils.isEmpty(params.getAttr("signType"))) {
			result.setMessage("缺少参数");
			return result;
		}
		if (StringUtils.isEmpty(params.getAttr("UUID"))) {
			result.setMessage("缺少参数");
			return result;
		}
		// 插入signid
		AppParam addSigeParam = new AppParam();
		addSigeParam.addAttr("customerId", params.getAttr("custId"));
		addSigeParam.addAttr("signId", params.getAttr("signId"));
		String signType = params.getAttr().get("signType").toString();
		addSigeParam.addAttr("signType", signType);
		addSigeParam.addAttr("uuid", params.getAttr("UUID"));
		addSigeParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		addSigeParam.setService("custSignidService");
		addSigeParam.setMethod("insert");
		AppResult addSignrResult = RemoteInvoke.getInstance()
				.call(addSigeParam);
		
		if (addSignrResult.isSuccess()) {
			//查询signid并删除redis的数据
			AppParam queryParam=new AppParam();
			queryParam.addAttrs(addSigeParam.getAttr());
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
			queryParam.setService("custSignidService");
			queryParam.setMethod("queryBeforeSignId");
			AppResult queryResult = RemoteInvoke.getInstance()
					.call(queryParam);
			List<Map<String, Object>> rows = queryResult.getRows();
			for (Map<String, Object> map : rows) {
				if (!StringUtils.isEmpty(map.get("signId"))) {
					RedisUtils.getRedisService().del(map.get("signId").toString());
				}
			}
			//删除数据库中的signid
			AppParam delSignParam = new AppParam();
			delSignParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
			delSignParam.setService("custSignidService");
			delSignParam.setMethod("conditionDelete");
			delSignParam.addAttr("customerId", params.getAttr("custId"));
			delSignParam.addAttr("signId", params.getAttr("signId"));
			delSignParam.addAttr("signType", params.getAttr("signType"));
			delSignParam.addAttr("createTime",
					new Date(System.currentTimeMillis()));
			RemoteInvoke.getInstance().call(delSignParam);
		}
		
		return result;
	}

	/**
	 * 删除当前用户的其他的signId
	 * @param custId
	 * @param signId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static AppResult clearSignId(String custId,String signId) {
		AppResult result = new AppResult();
		if (StringUtils.isEmpty(signId)) {
			result.setMessage("缺少参数");
			return result;
		}
		if (StringUtils.isEmpty(custId)) {
			result.setMessage("缺少参数");
			return result;
		}
		Map<String, Object> tokenInfo =(Map<String, Object>) RedisUtils.getRedisService().get(signId);
		System.out.println(tokenInfo);
		String signType = tokenInfo.get("loginType").toString();
		String xxjr = "appxxjr";
		String xdjl = "appxdjl";
		AppParam params = new AppParam();
		params.addAttr("customerId", custId);
		params.addAttr("xxjr", xxjr);
		params.addAttr("xdjl", xdjl);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		params.setService("custSignidService");
		params.setMethod("querySignIds");
		AppResult signIds = RemoteInvoke.getInstance().call(params);
		String xxjrsignId = signIds.getAttr("xxjrsignId").toString();
		String xdjlsignId = signIds.getAttr("xdjlsignId").toString();
		AppParam delParams = new AppParam();
		switch (signType) {
		case "appxxjr":
			RedisUtils.getRedisService().del(xdjlsignId);
			delParams.addAttr("signId", xdjlsignId);
			break;
		case "appxdjl":
			RedisUtils.getRedisService().del(xxjrsignId);
			delParams.addAttr("signId", xxjrsignId);
			break;
		
		}
		delParams.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		delParams.setService("custSignidService");
		delParams.setMethod("deleteSignIds");
		result=RemoteInvoke.getInstance().call(delParams);
		return result;
	}

	/**
	 * 查询信贷用户信息
	 * @param param
	 * @return
	 */
	public static Map<String, Object> queryLoanInfo(AppParam param){
		Map<String, Object> loanInfo = new HashMap<String, Object>();
		param.setService("custLoanInfoService");
		param.setMethod("query");
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult result = RemoteInvoke.getInstance().callNoTx(param);
		if (result.getRows().size() > 0) {
			loanInfo = result.getRow(0);
		}
		return loanInfo;
	}
	
	/**
	 * 查询信贷用户联系人信息
	 * @param param
	 * @return
	 */
	public static AppResult queryLoanContact(AppParam param){
		param.setService("custLoanContactService");
		param.setMethod("query");
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		return RemoteInvoke.getInstance().callNoTx(param);
	}
	
	//获取表单填写完成情况
	public static HashMap<String, Object> getCompletionInfo(Map<String, Object> custMap) {
		HashMap<String, Object> map = new HashMap<String,Object>();
		//个人信息
		if (StringUtils.isEmpty(custMap.get("realName"))
				||StringUtils.isEmpty(custMap.get("cardNo"))
				||StringUtils.isEmpty(custMap.get("workType"))
				||StringUtils.isEmpty(custMap.get("creditType"))
				||StringUtils.isEmpty(custMap.get("haveWeiLi"))
				||StringUtils.isEmpty(custMap.get("zhimaScore"))
				||StringUtils.isEmpty(custMap.get("education"))
				|| !getCustInfo(custMap)
				|| !getFundInfo(custMap)
				|| !getSocialInfo(custMap)) {
			map.put("custInfo", 0);
		}else {
			map.put("custInfo", 1);
		}
		
		//实名认证信息
		map.put("identifyInfo", StringUtils.isEmpty(custMap.get("status"))? -1:custMap.get("status"));
		
		//家庭情况
		if (StringUtils.isEmpty(custMap.get("marry"))
				||StringUtils.isEmpty(custMap.get("childrenAmount"))
				||StringUtils.isEmpty(custMap.get("monthlyExpenses"))) {
			map.put("familyInfo", 0);
		}else {
			map.put("familyInfo", 1);
		}
		// 家庭情况-居住地
		if (!StringUtils.isEmpty(custMap.get("address"))
				&& getAddrInfo(custMap)) {
			map.put("addrInfo", 1);
		}else {
			map.put("addrInfo", 0);
		}
		
		//其他联系人
		if (!"2".equals(custMap.get("contactSize").toString())) {
			map.put("contactInfo", 0);
		}else {
			map.put("contactInfo", 1);
		}
		
		//房产
		if (!StringUtils.isEmpty(custMap.get("houseType"))
			&& ("4".equals(custMap.get("houseType").toString()) 
					||"5".equals(custMap.get("houseType").toString())
					||"6".equals(custMap.get("houseType").toString())
					||"7".equals(custMap.get("houseType").toString()))
			&& !StringUtils.isEmpty(custMap.get("houseVal"))){
			map.put("houseInfo", 1);
		}else if (!StringUtils.isEmpty(custMap.get("houseType"))
				&& !StringUtils.isEmpty(custMap.get("houseVal"))
				&& "3".equals(custMap.get("houseType").toString())
				&& !StringUtils.isEmpty(custMap.get("houseMonthPay"))) {
			map.put("houseInfo", 1);
		}else if (!StringUtils.isEmpty(custMap.get("houseType"))) {
			map.put("houseInfo", 1);
		}else{
			map.put("houseInfo", 0);
		}
		
		//车产
		if (!StringUtils.isEmpty(custMap.get("carType"))) {
			if ("2".equals(custMap.get("carType").toString())
					||("3".equals(custMap.get("carType").toString())
							&& !StringUtils.isEmpty(custMap.get("carPrice"))
							&& !StringUtils.isEmpty(custMap.get("carMonthPay")))
					|| ("4".equals(custMap.get("carType").toString())
							&& !StringUtils.isEmpty(custMap.get("carPrice")))) {
				map.put("carInfo", 1);
			}else {
				map.put("carInfo", 0);
			}
		}else {
			map.put("carInfo", 0);
		}
		
		//保险
		if (!StringUtils.isEmpty(custMap.get("insurType")) 
				&& "0".equals(custMap.get("insurType").toString())) {
			map.put("insurInfo", 1);
		}else if (!StringUtils.isEmpty(custMap.get("insurType"))
				&& !StringUtils.isEmpty(custMap.get("insurPayType"))
				&& !StringUtils.isEmpty(custMap.get("insurMonthAmt"))
				&& !StringUtils.isEmpty(custMap.get("insurMonth"))) {
			map.put("insurInfo", 1);
		}else {
			map.put("insurInfo", 0);
		}
		return map;
	}
	/** 居住地 **/
	protected static boolean getAddrInfo(Map<String, Object> custMap) {
		if (StringUtils.isEmpty(custMap.get("cityArea"))) {
			return false;
		}
		return true;
	}
	/** 社保 **/
	protected static boolean getSocialInfo(Map<String, Object> custMap) {
		Object socialType = custMap.get("socialType");
		Object socialMonth = custMap.get("socialMonth");
		if (!StringUtils.isEmpty(socialType)) {
			if ("1".equals(socialType.toString()) 
					&& !StringUtils.isEmpty(socialMonth)) {
				return true;
			}else if ("2".equals(socialType.toString())) {
				return true;
			}
		}
		return false;
	}
	/** 公积金**/
	protected static boolean getFundInfo(Map<String, Object> custMap) {
		Object fundType = custMap.get("fundType");
		Object fundMonth = custMap.get("fundMonth");
		if (!StringUtils.isEmpty(fundType)) {
			if ("1".equals(fundType.toString()) 
					&& !StringUtils.isEmpty(fundMonth)) {
				return true;
			}else if ("2".equals(fundType.toString())) {
				return true;
			}
		}
		return false;
	}
	
	protected static boolean getCustInfo(Map<String, Object> custMap) {
		if (StringUtils.isEmpty(custMap.get("workType"))) {
			return false;
		}
		String workType = custMap.get("workType").toString();
		//职业身份（1无固定职业 2企业主 3个体户 4上班族  5-学生）
		if ("1".equals(workType)) {
			if (!StringUtils.isEmpty(custMap.get("income"))) {
				return true;
			}
		}else if ("2".equals(workType)) {
			if (!StringUtils.isEmpty(custMap.get("manageYear"))
					&& !StringUtils.isEmpty(custMap.get("totalAmount"))
					&& !StringUtils.isEmpty(custMap.get("pubAmount"))
					&& !StringUtils.isEmpty(custMap.get("hasLicense"))) {
				return true;
			}
		}else if ("3".equals(workType)) {
			if (!StringUtils.isEmpty(custMap.get("manageYear"))
					&& !StringUtils.isEmpty(custMap.get("totalAmount"))
					&& !StringUtils.isEmpty(custMap.get("caseAmount"))
					&& !StringUtils.isEmpty(custMap.get("hasLicense"))) {
				return true;
			}
		}else if ("4".equals(workType) && !StringUtils.isEmpty(custMap.get("wagesType"))) {
			if ("1".equals(custMap.get("wagesType").toString())
					&& !StringUtils.isEmpty(custMap.get("income"))) {
				return true;
			}else if ("2".equals(custMap.get("wagesType").toString())
					&& !StringUtils.isEmpty(custMap.get("caseAmount"))) {
				return true;
			}
		}else if ("5".equals(workType)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 用户登录日志记录
	 * @param params
	 * @return
	 */
	public static AppResult custLoginLog(AppParam params) {
		AppResult result = new AppResult();
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "cust"));
		params.setService("custLoginLogService");
		params.setMethod("loginLog");
		result = RemoteInvoke.getInstance().call(params);
		return result;
	}
	
	/**
	 * 用户免单券活动配置
	 * @param customerId 用户id
	 * @param status	  用户配置状态  0-已撤销 1-有效
	 * @return
	 */
	public static Map<String,Object> getCustTicketConfig(Object customerId,Object status){
		AppParam params = new AppParam();
		params.setService("custRobConfigService");
		params.setMethod("queryConfig");
		params.addAttr("customerId", customerId);
		params.addAttr("status", status);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		if (result.getRows().size() > 0) {
			return result.getRow(0);
		}
		return null;
	}
	
	/**
	 * 查询是否电销员工
	 * @param telephone
	 * @return
	 */
	public static boolean isTeleEmployee(Object telephone) {
		AppParam params = new AppParam();
		params.setService("teleEmployeeService");
		params.setMethod("query");
		params.addAttr("telephone", telephone);
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		if (result.getRows().size() > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 用户是否拥有可查看分销系统的权限
	 * @param custInfo 用户信息
	 * @param roleType 指定有权限的角色
	 * @return
	 */
	public static boolean isFxFullInfoRole(Map<String, Object> custInfo, String roleType) {
		String authType = StringUtil.getString(custInfo.get("authType"));
		if(StringUtils.isEmpty(authType) || StringUtils.isEmpty(roleType)){
			return false;
		}
		String[] roleIds = org.apache.commons.lang.StringUtils.stripAll(roleType.split(","));
		List<String> roleList = Arrays.asList(roleIds);
		if(roleList.contains(authType)){
			return true;
		}
		return false;
	}
}
