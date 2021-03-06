package org.xxjr.busi.util;

import java.util.Date;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.IDCardValidate;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

public class ApplyUnionUtil {
	
	/**
	 *二次申请判断
	 * @param telephone
	 * @return applyStatus 申请状态 1 未申请  2申请过，可以修改信息  3 不可操作，只能查看进度  4 可重新申请
	 */
	public static String isCanApply(String telephone) {
	
		String applyStatus = "1";
		if (isTestUser(telephone)) {//测试号码直接过
			return applyStatus;
		}
		AppParam params = new AppParam();
		params.addAttr("telephone", telephone);
		Map<String, Object> queryMap = queryApplyInfo(null, telephone);//查询申请状态和时间
		if (queryMap == null || queryMap.isEmpty()) {// 没有过申请
			return applyStatus;
		}
		Integer status = NumberUtil.getInt(queryMap.get("status"), 0);

		String updateTime = StringUtil.getString(queryMap.get("updateTime"));
		boolean checkTime = false;
		if (!StringUtils.isEmpty(updateTime)) {//修改时间可能不存在
			// 获取申请时间之后的一个月
			int month = SysParamsUtil.getIntParamByKey("applyCheckMonth", 1);
			
			Date date = DateUtil.plus(DateUtil.toLocalDateTime(updateTime, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS),
					month, DateUtil.ChronoUnit_MONTHS);
			checkTime = (new Date().getTime() > date.getTime());// 判断当前时间是否是上一次申请结束一个月以后
		}
		if (0 == status || status == 1) {// 待处理的可以修改信息、客服处理中可以修改信息
			applyStatus = "2";
		}else if ((status == 2 || status == 8 || status == 9 || status == 12 || status == 13) && checkTime) {
			//申请流程完成并且时间大于一个月
			applyStatus = "4";
		}else if (StringUtils.isEmpty(queryMap.get("lastStore")) && (status != 2 && status != 8 && status != 9 && status != 12)) {
			//没有处理人并且还在处理中(将成功的取反就可以获得处理中的)
			if (checkTime) {//判断时间是否大于3个月(月为可配置)，大于的话可重新申请否则只能修改数据或查看信息
				applyStatus = "4";
			}else {
				applyStatus = "2";
			}
		}else {
			//其他情况就去查看信息
			applyStatus = "3";	
		}
		
		return applyStatus;
	}
	
	/**
	 * 查询申请基本信息
	 * @param applyId
	 * @param telephone
	 * @return
	 */
	public static Map<String, Object> queryApplyInfo (Object applyId,String telephone) {
		AppParam queryParam = new AppParam("borrowApplyService", "query");
		queryParam.addAttr("applyId", applyId);
		queryParam.addAttr("telephone", telephone);
		queryParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
		AppResult qeuryResult = null;
		if (SpringAppContext.getBean("borrowApplyService") == null) {
			qeuryResult = RemoteInvoke.getInstance().call(queryParam);
		}else {
			qeuryResult = SoaManager.getInstance().callNoTx(queryParam);
		}
		if (qeuryResult.getRows().size() > 0) {
			return qeuryResult.getRow(0);
		}
		return null;
	}
	
	/**旧表和新表类型和名字不一样需要转换**/
	public static void conversionType (Map<String, Object> now) {
		int carType = NumberUtil.getInt(now.get("carType"), 0);
		int houseType = NumberUtil.getInt(now.get("houseType"), 0);
		String identifyNo = StringUtil.getString(now.get("identifyNo"));
		
		String applyIp = StringUtil.getString(now.get("applyIp"));
		if (!StringUtils.isEmpty(applyIp)) {
			int index = applyIp.indexOf(",");//多次反向代理后会有多个ip值，第一个ip才是真实ip
	        if(index != -1){
	        	now.put("applyIp", applyIp.substring(0,index));
	        }
		}
		
		if(houseType == 3){
			now.put("houseMortgage", 1);
		}
		if(houseType == 4){
			now.put("houseMortgage", 2);
		}
		
		if(carType == 3){
			now.put("carMortgage", 2);
		}
		if(carType == 4){
			now.put("carMortgage", 4);
		}

		if (StringUtils.isEmpty(now.get("loanDeadline"))) {
			now.put("loanDeadline", "12");//贷款期限默认一年
		}
		
		double loanAmount = NumberUtil.getDouble(now.remove("applyAmount"),0);
		if(loanAmount > 0 ){
			now.put("loanAmount",loanAmount);
		}
		int wagesType = NumberUtil.getInt(now.get("wagesType"), 0);
		Double income = NumberUtil.getDouble(now.get("income"), 0);
		if (wagesType == 2 && income > 0) {
			now.put("cashMonth", income);
		}
		
		if (StringUtils.isEmpty(now.get("age"))) {//有几个页面有性别和年龄，如果存在不需要
			now.put("age", IDCardValidate.getCardAge(identifyNo));//根据身份证获取年龄
		}
		if (StringUtils.isEmpty(now.get("sex"))) {
			now.put("sex", IDCardValidate.getCardSex(identifyNo));//根据身份证获取性别
		}
		
		now.put("incomeMonth", now.get("totalAmount"));//总经营流水/月
		now.put("cashMonth", now.get("caseAmount"));//现金收入
		now.put("workCmp", now.get("cmpType"));//企业类型
		now.put("pubManageLine", now.get("pubAmount"));//对公账户流水/月
		now.put("havePinan", now.get("haveWeiLi"));
	}
	
	/**
	 * 判断是否是测试号码
	 * @param telephone
	 * @return
	 */
	public static boolean isTestUser (String telephone) {
		String telephones = SysParamsUtil.getParamByKey("tgTestTelephone");
		if (!StringUtils.isEmpty(telephones)) {
			if (telephones.indexOf(telephone) > -1) {
				return true;
			}
		}
		return false;
	}
	
	public static AppResult applyError () {
		AppResult result = new AppResult();
		result .setSuccess(false);
		result.setErrorCode("100");
		result.setMessage("抱歉，您之前已经有过贷款申请，无需再次申请,小小金融将为您推荐最适合您的贷款产品。");
		return result;
	}
	
	/**
	 * 将门店或信贷经理跟进状态转换为union表状态，如果
	 * @param statusType
	 * @param status
	 * @return
	 */
	public static String changeUnionStatus (String statusType, String status) {
		String unionStatus = new String(status);
		if ("2".equals(statusType)) {//门店
			//3-待门店经理联系 4-待上门 5-已上门待签约 6-签约成功 7-放款中 8-已放款 9-放款失败
			if (StoreConstant.STORE_ORDER_0.equals(status)) {
				unionStatus = "3";
			}else if(StoreConstant.STORE_ORDER_1.equals(status)){
				unionStatus = "4";
			}else if(StoreConstant.STORE_ORDER_2.equals(status)){
				unionStatus = "5";
			}else if(StoreConstant.STORE_ORDER_3.equals(status)){
				unionStatus = "6";
			}else if(StoreConstant.STORE_ORDER_4.equals(status)){
				unionStatus = "7";
			}else if(StoreConstant.STORE_ORDER_5.equals(status)){
				unionStatus = "8";
			}else if(StoreConstant.STORE_ORDER_6.equals(status)){
				unionStatus = "9";
			}else if(StoreConstant.STORE_ORDER_7.equals(status) || StoreConstant.STORE_ORDER_8.equals(status)){
				unionStatus = "2";
			}
		}
		if ("3".equals(statusType)) {//信贷经理
			if ("0".equals(status) || "1".equals(status)) {
				unionStatus = "10";//待经理联系
			}else if ("7".equals(status)) {
				unionStatus = "11";//审批中
			}else if ("2".equals(status)) {
				unionStatus = "12";//审批通过
			}else if ("3".equals(status) || "4".equals(status) || "5".equals(status) || "6".equals(status)) {
				unionStatus = "2";//申请失败
			}
		}
		return unionStatus;
	}

}
