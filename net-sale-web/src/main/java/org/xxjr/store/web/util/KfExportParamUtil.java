package org.xxjr.store.web.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.kf.KfUserUtil;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;
import org.xxjr.sys.util.ValidUtils;

public class KfExportParamUtil {
	public volatile static KfExportParamUtil exportParamUtil = null;
	public static KfExportParamUtil getInstance(){
		if(exportParamUtil == null){
			synchronized (KfExportParamUtil.class) {
				if(exportParamUtil == null){
					exportParamUtil = new KfExportParamUtil();
				}
			}
		}
		return exportParamUtil;
	}

	public static final String P_DAY = "day";
	public static final String P_MONTH = "month";
	public static final String P_RANGE = "range";
	public static final String P_DAY_PATTERN = "%Y-%m-%d";
	public static final String P_MONTH_PATTERN = "%Y-%m";
	public static final String P_COUNT_METHOD = "countMethod";
	public static final List<String> channels = new ArrayList<String>();
	
	static {
		channels.add("baoxin");
		channels.add("ywapi");
		channels.add("ycd");
		channels.add("qianzhang");
		channels.add("yimeiqi");
		channels.add("kouzi");
		channels.add("xindan");
		channels.add("bb");
		channels.add("zdw");
		channels.add("sd");
		channels.add("baojie");
		channels.add("guoke");
		channels.add("kdw8");
		channels.add("kwd");
		channels.add("dongf");
		channels.add("xw");
	}
	/**
	 * 封装参数
	 * @param exportType
	 * @param param
	 * @param result
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static void  packParams(String exportType,AppParam params, 
			AppResult result,HttpServletRequest request) throws Exception {
		Class<KfExportParamUtil> clazz = KfExportParamUtil.class;
        Method method = clazz.getMethod(exportType,AppParam.class,AppResult.class,HttpServletRequest.class);
        method.invoke(getInstance(),params,result,request);
	}
	/**
	 *本月简单实时统计（按时间，渠道）
	 */
	public void channelSimple(AppParam params, AppResult result, HttpServletRequest request){
		String customerId = KfUserUtil.getCustomerId(request); 
		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
		Object fixChannels = custRight.get("channels");
		params.addAttr("fixChannels", fixChannels);//固定渠道
		
		params.setService("borrowChannelRecordService");
		params.setMethod("channelSimpleByMonth");
		params.addAttr(P_COUNT_METHOD, "channelSimpleByMonthCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	/**
	 *总的统计实时日期
	 */
	public void sumaryAll(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		params.setService("sumTotalBaseService");
		if ("day".equals(dateType)) {
			params.setMethod("queryDay");
			params.addAttr(P_COUNT_METHOD,"queryDayCount");
		}else if("month".equals(dateType)) {
			params.setMethod("queryMonth");
			params.addAttr(P_COUNT_METHOD,"queryMonthCount");
		}else {//区间
			params.setMethod("querySection");
			params.addAttr(P_COUNT_METHOD,"querySection");
		}
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	/**
	 *总的统计实时
	 */
	public void totalCountReal(AppParam params, AppResult result, HttpServletRequest request){
		params.setService("sumUtilExtService");
		params.setMethod("totalApplyTimeSum");
		params.addAttr(P_COUNT_METHOD, "totalApplyTimeSumCount");
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate) 
				|| StringUtils.isEmpty(dateType)){
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
			return;
		}
		
		if("day".equals(dateType)){
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");

		}else if ("range".equals(dateType)) {
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			params.setMethod("totalApplyTimeSumRange");
		}else {//按月
				params.addAttr("datePattern", "%Y-%m");
				params.addAttr("startRecordDate", startRecordDate+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordDate+"-01")+" 23:59:59");
		}

		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	/**
	 *城市统计
	 */
	public void citySumary(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		params.setService("channelModifySumService");
		params.setMethod("citySumary");
		params.addAttr(P_COUNT_METHOD, "citySumaryCount");
		if("day".equals(dateType)){
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
		}else if ("range".equals(dateType)) {
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.setService("channelMsSectionSumService");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
		}else{//按月
			    String startRecordMonth = StringUtil.getString(params.getAttr("startRecordMonth"));
			    String endRecordMonth = StringUtil.getString(params.getAttr("endRecordMonth"));
				params.addAttr("datePattern", "%Y-%m");
				params.addAttr("startRecordDate", startRecordMonth+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordMonth+"-01")+" 23:59:59");
		}
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
//	/**
//	 *实时挂卖情况统计(按日期)
//	 */
//	public void saleCountReal(AppParam params, AppResult result, HttpServletRequest request){
//		String startRecordDate = StringUtil.getString(params.getAttr("startDate"));
//		String endRecordDate = StringUtil.getString(params.getAttr("endDate"));
//		String dateType = StringUtil.getString(params.getAttr("dateType"));
//		params.setMethod("saleCountReal");
//		params.setService("daiBorrowReService");
//		params.addAttr(P_COUNT_METHOD, "saleCountRealCount");
//		params.setOrderBy("date");
//		params.setOrderValue("DESC");
//		if("day".equals(dateType)){
//			params.addAttr("datePattern", "%Y-%m-%d");
//			params.addAttr("startRecordDate", startRecordDate);
//			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
//
//		}else if ("range".equals(dateType)) {
//			params.addAttr("startRecordDate", startRecordDate);
//			params.addAttr("startDateStr", startRecordDate);
//			params.addAttr("endDateStr", endRecordDate);
//			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
//			params.addAttr("datePattern", "%Y-%m-%d");
//			params.setMethod("saleCountRealRange");
//		}else {//按月
//				params.addAttr("datePattern", "%Y-%m");
//				params.addAttr("startRecordDate", startRecordDate+"-01");
//				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordDate+"-01")+" 23:59:59");
//		}
//		params.setRmiServiceName(AppProperties
//				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi));
//	}
	/**
	 *实时挂卖情况统计(按城市)
	 */
//	public void citySaleCount(AppParam params, AppResult result, HttpServletRequest request){
//		String dateType = StringUtil.getString(params.getAttr("dateType"));
//		String startDate = StringUtil.getString(params.getAttr("startDate"));
//		String endDate = StringUtil.getString(params.getAttr("endDate"));
//		params.setService("citySaleCountService");
//		params.setMethod("query");
//		params.addAttr(P_COUNT_METHOD, "queryCount");
//		if("day".equals(dateType)){
//			params.addAttr("startDate", startDate);
//			params.addAttr("endDate", endDate+" 23:59:59");
//			params.addAttr("datePattern", "%Y-%m-%d");
//		}else if ("month".equals(dateType)) {//按月
//			params.addAttr("startDate", startDate+"-01");
//			params.addAttr("endDate", PageUtil.getLastDay(endDate+"-01")+" 23:59:59");
//			params.addAttr("datePattern", "%Y-%m");
//		}else{//区间
//			params.addAttr("startDate", startDate);
//			params.addAttr("endDate", endDate+" 23:59:59");
//			params.addAttr("startDateStr", startDate);
//			params.addAttr("endDateStr", endDate);
//			params.setMethod("queryRange");
//			params.addAttr(P_COUNT_METHOD, "queryRangeCount");
//		}
//		params.setRmiServiceName(AppProperties
//				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi));
//	}
	/**
	 * 推送数据情况统计
	 * @param params
	 * @param result
	 * @param request
	 */
	public void sumPushData(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		params.setService("sumBorrowPushService");
		if ("day".equals(dateType)) {
			params.setMethod("queryByPage");
			params.addAttr(P_COUNT_METHOD, "queryCount");
		}else if ("range".equals(dateType)) {
			params.setMethod("querySection");
			params.addAttr(P_COUNT_METHOD, "querySectionCount");
		}else{
			params.setMethod("queryByMonth");
			params.addAttr(P_COUNT_METHOD, "queryMonthCount");
		}
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	/**
	 * 渠道统计（第三方）
	 * @param params
	 * @param result
	 * @param request
	 */
	public void thirdChannel(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		params.addAttr(P_COUNT_METHOD, "thirdChannelCount");
		params.setService("channelDtlModifySumService");
		params.setMethod("thirdChannel");
		params.addAttr("datePattern", P_DAY_PATTERN);
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
			return ;
		}
		params.addAttr("endRecordDate", endRecordDate + " 23:59:59");
		if("month".equals(dateType)){//按月
			params.addAttr("datePattern", P_MONTH_PATTERN);
			startRecordDate = startRecordDate + "-01";
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordDate + "-01") + " 23:59:59");
		}else if ("range".equals(dateType)) {
			params.setService("channelDtlMsSectionSumService");
			params.setMethod("thirdChannelSect");
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
		}
		String customerId = KfUserUtil.getCustomerId(request); 
		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
		Object fixChannels = custRight.get("channels");
		try {
			Date now = DateUtil.toDateByString("2018-04-28", DateUtil.DATE_PATTERN_YYYY_MM_DD);
			Date startDate = DateUtil.toDateByString(startRecordDate, DateUtil.DATE_PATTERN_YYYY_MM_DD);
			if (channels.contains(fixChannels) && startDate.getTime() < now.getTime()) {
				params.addAttr("startDateStr", "2018-04-28");
				params.addAttr("startRecordDate", "2018-04-28");
			}
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, " 时间格式转换失败！");
		}
		params.addAttr("fixChannels", fixChannels);//固定渠道
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	/**
	 * 推送数据情况统计(按渠道)
	 * @param params
	 * @param result
	 * @param request
	 */
	public void sumChannelPushData(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		params.setService("sumChannelPushService");
		if ("day".equals(dateType)) {
			params.setMethod("queryByPage");
			params.addAttr(P_COUNT_METHOD, "queryCount");
		}else if ("range".equals(dateType)) {
			params.setMethod("querySection");
			params.addAttr(P_COUNT_METHOD, "querySectionCount");
		}else {
			params.setMethod("queryMonth");
			params.addAttr(P_COUNT_METHOD, "queryMonthCount");
		}
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	/**
	 * 点击页面统计
	 * @param params
	 * @param result
	 * @param request
	 */
	public void pageCount(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startDate = StringUtil.getString(params.getAttr("startDate"));
		String endDate = StringUtil.getString(params.getAttr("endDate"));
		
		params.setService("pageCountService");
		params.setMethod("queryList");
		params.addAttr(P_COUNT_METHOD, "queryListCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		if("day".equals(dateType)){
			if(StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate) 
					|| StringUtils.isEmpty(dateType)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("endDate", endDate+" 23:59:59");
		}else if ("range".equals(dateType)) {
			if(StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate) 
					|| StringUtils.isEmpty(dateType)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("startDateStr", startDate);
			params.addAttr("endDateStr", endDate);
			params.addAttr("endRecordDate", endDate+" 23:59:59");
		}else{//按月
			String startMonth = StringUtil.getString(params.getAttr("startMonth"));
			String endMonth = StringUtil.getString(params.getAttr("endMonth"));
			params.addAttr("datePattern", "%Y-%m");
			params.addAttr("startDate", startMonth+"-01");
			params.addAttr("endDate", PageUtil.getLastDay(endMonth+"-01")+" 23:59:59");
		}
	}
	/**
	 * 点击按钮统计
	 * @param params
	 * @param result
	 * @param request
	 */
	public void btnCount(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startDate = StringUtil.getString(params.getAttr("startDate"));
		String endDate = StringUtil.getString(params.getAttr("endDate"));
		
		params.setService("btnCountService");
		params.setMethod("queryList");
		params.addAttr(P_COUNT_METHOD, "queryListCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		if("day".equals(dateType)){
			if(StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate) 
					|| StringUtils.isEmpty(dateType)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("endDate", endDate+" 23:59:59");
		}else if ("range".equals(dateType)) {
			if(StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate) 
					|| StringUtils.isEmpty(dateType)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("startDateStr", startDate);
			params.addAttr("endDateStr", endDate);
			params.addAttr("endRecordDate", endDate+" 23:59:59");
		}else{//按月
			String startMonth = StringUtil.getString(params.getAttr("startMonth"));
			String endMonth = StringUtil.getString(params.getAttr("endMonth"));
			params.addAttr("datePattern", "%Y-%m");
			params.addAttr("startDate", startMonth+"-01");
			params.addAttr("endDate", PageUtil.getLastDay(endMonth+"-01")+" 23:59:59");
		}
	}
	/**
	 * 点击短链接统计
	 * @param params
	 * @param result
	 * @param request
	 */
	public void shortUrlCount(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startDate = StringUtil.getString(params.getAttr("startDate"));
		String endDate = StringUtil.getString(params.getAttr("endDate"));
		
		params.setService("shortUrlCountService");
		params.setMethod("queryShortUrlList");
		params.addAttr(P_COUNT_METHOD, "queryShortUrlCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		if("day".equals(dateType)){
			if(StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate) 
					|| StringUtils.isEmpty(dateType)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("endDate", endDate+" 23:59:59");
		}else if ("range".equals(dateType)) {
			if(StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate) 
					|| StringUtils.isEmpty(dateType)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("startDateStr", startDate);
			params.addAttr("endDateStr", endDate);
			params.addAttr("endRecordDate", endDate+" 23:59:59");
		}else{//按月
			String startMonth = StringUtil.getString(params.getAttr("startMonth"));
			String endMonth = StringUtil.getString(params.getAttr("endMonth"));
			params.addAttr("datePattern", "%Y-%m");
			params.addAttr("startDate", startMonth+"-01");
			params.addAttr("endDate", PageUtil.getLastDay(endMonth+"-01")+" 23:59:59");
		}
	}
	/**
	 * 用户注册统计
	 * @param params
	 * @param result
	 * @param request
	 */
	public void userRegisterCvr(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startDate = StringUtil.getString(params.getAttr("startDate"));
		String endDate = StringUtil.getString(params.getAttr("endDate"));
		params.setService("sumUserRegisterService");
		params.setMethod("queryList");
		params.addAttr(P_COUNT_METHOD, "queryListCount");
		params.addAttr("pageId", 1180165);//只查询资产注册统计
		params.addAttr("datePattern", "%Y-%m-%d");
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		if ("1".equals(request.getParameter("group"))) {
			params.addAttr("groupSql", "t1.channelCode");//有两张表，名字不同，需要处理
			params.addAttr("groupSql1", "channelCode");
		}else {
			params.addAttr("groupSql", "t1.channelDetail");
			params.addAttr("groupSql1", "channelDetail");
		}
		if("day".equals(dateType)){
			if(StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate) 
					|| StringUtils.isEmpty(dateType)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.addAttr("endDate", endDate);
		}else if ("range".equals(dateType)) {
			if(StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate) 
					|| StringUtils.isEmpty(dateType)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.addAttr("startDate", startDate);
			params.addAttr("endDate", endDate);
			params.setMethod("queryRangeList");
		}else{//按月
			String startMonth = StringUtil.getString(params.getAttr("startMonth"));
			String endMonth = StringUtil.getString(params.getAttr("endMonth"));
			params.addAttr("datePattern", "%Y-%m");
			params.addAttr("startDate", startMonth+"-01");
			params.addAttr("endDate", PageUtil.getLastDay(endMonth+"-01"));
		}
	}
	/**
	 * 推送统计(去重)
	 * @param params
	 * @param result
	 * @param request
	 */
	public void sumPushDisData(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		params.setService("sumBorrowPushService");
		if ("day".equals(dateType)) {
			params.addAttr(P_COUNT_METHOD, "queryDistinctCount");
			params.setMethod("queryDistinct");
		}else if ("range".equals(dateType)) {
			params.addAttr(P_COUNT_METHOD, "queryDistinctSecCount");
			params.setMethod("queryDistinctSec");
		}else{
			params.addAttr(P_COUNT_METHOD, "queryDistinctMonthCount");
			params.setMethod("queryDistinctByMonth");
		}
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	public void storeSumary(AppParam params, AppResult result, HttpServletRequest request){
		RequestUtil.setAttr(params, request);
		params.setService("sumOrgBaseService");
		params.setMethod("queryByStore");
		params.addAttr(P_COUNT_METHOD, "queryByStoreCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
}
	
	/**
	 * ios推广统计
	 * @param params
	 * @param result
	 * @param request
	 */
	public void queryIosTgMonth(AppParam params, AppResult result, HttpServletRequest request){
		params.setService("thirdIdfaInfoService");
		params.setMethod("queryIosTgMonth");
		params.addAttr(P_COUNT_METHOD, "queryIosTgMonthCount");
		params.setOrderBy("recordDate");
		params.setOrderValue("DESC");
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	/**
	 * 总的统计(按团队)
	 * @param params
	 * @param result
	 * @param request
	 */
	public void teamSumAll(AppParam params, AppResult result, HttpServletRequest request){
		params.setService("sumTotalTeamService");
		params.setMethod("queryDay");
		params.addAttr(P_COUNT_METHOD, "queryDayCount");
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
			return ;
		}
		params.addAttr("endRecordDate", endRecordDate + " 23:59:59");
		if("month".equals(dateType)){//按月
			params.setMethod("queryMonth");
			params.addAttr("startRecordDate", startRecordDate + "-01");
			params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordDate + "-01") + " 23:59:59");
		}else if ("range".equals(dateType)) {
			params.setMethod("querySection");
			params.addAttr(P_COUNT_METHOD, "querySectionCount");
		}
		
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	/**
	 * 
	 * @param params
	 * @param result
	 * @param request
	 */
	public void channelDtl(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		params.setService("channelModifySumService");
		params.setMethod("channelDtl");
		params.addAttr(P_COUNT_METHOD, "channelBaseCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		
		String customerId = KfUserUtil.getCustomerId(request);
		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
		Object fixChannels = custRight.get("channels");
		params.addAttr("fixChannels", fixChannels);//固定渠道	
		
		if("day".equals(dateType)){
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
		}else if ("range".equals(dateType)) {
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.setService("channelMsSectionSumService");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
		}else {//按月
			    String startRecordMonth = StringUtil.getString(params.getAttr("startRecordMonth"));
			    String endRecordMonth = StringUtil.getString(params.getAttr("endRecordMonth"));
				params.addAttr("datePattern", "%Y-%m");
				params.addAttr("startRecordDate", startRecordMonth+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordMonth+"-01")+" 23:59:59");
		}
	}
	/**
	 * 线索流向情况统计
	 * @param params
	 * @param result
	 * @param request
	 */
	public void channelSale(AppParam params, AppResult result, HttpServletRequest request){
		params.setService("channelModifySumService");
		params.setMethod("channelSale");
		params.addAttr(P_COUNT_METHOD, "channelBaseCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		
		String customerId = KfUserUtil.getCustomerId(request);
		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
		Object fixChannels = custRight.get("channels");
		params.addAttr("fixChannels", fixChannels);//固定渠道	
		
		if("day".equals(dateType)){
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
		}else if ("range".equals(dateType)) {
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.setService("channelMsSectionSumService");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
		}else {//按月
			    String startRecordMonth = StringUtil.getString(params.getAttr("startRecordMonth"));
			    String endRecordMonth = StringUtil.getString(params.getAttr("endRecordMonth"));
				params.addAttr("datePattern", "%Y-%m");
				params.addAttr("startRecordDate", startRecordMonth+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordMonth+"-01")+" 23:59:59");
		}
	}
	/**
	 * 网销门店情况统计
	 * @param params
	 * @param result
	 * @param request
	 */
	public void channelNet(AppParam params, AppResult result, HttpServletRequest request){
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		params.setService("channelModifySumService");
		params.setMethod("channelNet");
		params.addAttr(P_COUNT_METHOD, "channelBaseCount");
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		
		String customerId = KfUserUtil.getCustomerId(request);
		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
		Object fixChannels = custRight.get("channels");
		params.addAttr("fixChannels", fixChannels);//固定渠道	
		
		if("day".equals(dateType)){
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
		}else if ("range".equals(dateType)) {
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.setService("channelMsSectionSumService");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
		}else{//按月
			    String startRecordMonth = StringUtil.getString(params.getAttr("startRecordMonth"));
			    String endRecordMonth = StringUtil.getString(params.getAttr("endRecordMonth"));
				params.addAttr("datePattern", "%Y-%m");
				params.addAttr("startRecordDate", startRecordMonth+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordMonth+"-01")+" 23:59:59");
		}
	}
	/**
	 * 网销门店情况统计(实时)
	 * @param params
	 * @param result
	 * @param request
	 */
	public void channelNetReal(AppParam params, AppResult result, HttpServletRequest request){
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		params.setService("channelModifySumService");
		params.setMethod("channelNetReal");
		params.addAttr(P_COUNT_METHOD, "channelNetRealCount");
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		
		String customerId = KfUserUtil.getCustomerId(request);
		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
		Object fixChannels = custRight.get("channels");
		params.addAttr("fixChannels", fixChannels);//固定渠道	
		
		if("day".equals(dateType)){
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
		}else if ("range".equals(dateType)) {
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.setService("channelMsSectionSumService");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
		}else{//按月
			String startRecordMonth = StringUtil.getString(params.getAttr("startRecordMonth"));
			String endRecordMonth = StringUtil.getString(params.getAttr("endRecordMonth"));
			params.addAttr("datePattern", "%Y-%m");
			params.addAttr("startRecordDate", startRecordMonth+"-01");
			params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordMonth+"-01")+" 23:59:59");
		}
	}
	/**
	 * ROI情况统计
	 * @param params
	 * @param result
	 * @param request
	 */
	public void channelROI(AppParam params, AppResult result, HttpServletRequest request){
		params.setService("channelModifySumService");
		params.setMethod("channelROI");
		params.addAttr(P_COUNT_METHOD, "channelBaseCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		
		String customerId = KfUserUtil.getCustomerId(request);
		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
		Object fixChannels = custRight.get("channels");
		params.addAttr("fixChannels", fixChannels);//固定渠道	
		
		if("day".equals(dateType)){
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
		}else if ("range".equals(dateType)) {
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.setService("channelMsSectionSumService");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
		}else {//按月
			    String startRecordMonth = StringUtil.getString(params.getAttr("startRecordMonth"));
			    String endRecordMonth = StringUtil.getString(params.getAttr("endRecordMonth"));
				params.addAttr("datePattern", "%Y-%m");
				params.addAttr("startRecordDate", startRecordMonth+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordMonth+"-01")+" 23:59:59");
		}
	}
	/**
	 * 渠道城市情况统计
	 * @param params
	 * @param result
	 * @param request
	 */
	public void channelCity(AppParam params, AppResult result, HttpServletRequest request){
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
			return ;
		}
		String configCitys = StringUtil.getString(params.getAttr("configCitys"));
		if (!StringUtils.isEmpty(configCitys)) {
			params.addAttr("configCitys", configCitys.split(","));
		}
		
		String customerId = KfUserUtil.getCustomerId(request);
		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
		Object fixChannels = custRight.get("channels");
		params.addAttr("fixChannels", fixChannels);//固定渠道	
		
		params.setService("channelModifySumService");
		params.setMethod("channelCityDate");
		params.addAttr(P_COUNT_METHOD, "channelCityDateCount");
		if("day".equals(dateType)){
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");

		}else if ("range".equals(dateType)) {
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			params.addAttr("endDate", endRecordDate);
			params.addAttr("datePattern", "%Y-%m-%d");
			params.setMethod("channelCity");
			params.addAttr(P_COUNT_METHOD, "channelCityCount");
		}else {//按月
				params.addAttr("datePattern", "%Y-%m");
				params.addAttr("startRecordDate", startRecordDate+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordDate+"-01")+" 23:59:59");
		}
		
		if (1 == NumberUtil.getInt(request.getParameter("isReal"), 0)) {
			params.setService("channelMsSectionSumService");
			if("day".equals(dateType) || "month".equals(dateType)){
				params.setMethod("realChannelCityDate");
				params.addAttr(P_COUNT_METHOD, "realChannelCityDateCount");
			}else {
				params.setMethod("realChannelCitySec");
				params.addAttr(P_COUNT_METHOD, "realChannelCitySecCount");
			}
		}
		
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	/**
	 * 大渠道基本情况统计(原始数据)
	 * @param params
	 * @param result
	 * @param request
	 */
	public void channelOrigBase(AppParam params, AppResult result, HttpServletRequest request){
		params.setService("channelOrigSumService");
		params.setMethod("channelOrigBase");
		params.addAttr(P_COUNT_METHOD, "channelBaseCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		
		String customerId = KfUserUtil.getCustomerId(request);
		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
		Object fixChannels = custRight.get("channels");
		params.addAttr("fixChannels", fixChannels);//固定渠道	
		if("day".equals(dateType)){
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
		}else if ("range".equals(dateType)) {
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.setService("channelMsSectionSumService");
			params.setMethod("channelOrigBase");
			params.addAttr(P_COUNT_METHOD, "channelOrigBaseCount");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
		} else {//按月
			    String startRecordMonth = StringUtil.getString(params.getAttr("startRecordMonth"));
			    String endRecordMonth = StringUtil.getString(params.getAttr("endRecordMonth"));
				if(StringUtils.isEmpty(startRecordMonth) || StringUtils.isEmpty(endRecordMonth)){
					result.setSuccess(false);
					result.setMessage("缺少必传参数!");
					return ;
				}
				params.addAttr("datePattern", "%Y-%m");
				params.addAttr("startRecordDate", startRecordMonth+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordMonth+"-01")+" 23:59:59");
		}
	}
	/**
	 * 金额资质详细情况统计(原始数据)
	 * @param params
	 * @param result
	 * @param request
	 */
	public void channelOrigDtl(AppParam params, AppResult result, HttpServletRequest request){
		params.setService("channelOrigSumService");
		params.setMethod("channelOrigDtl");
		params.addAttr(P_COUNT_METHOD, "channelBaseCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		
		String customerId = KfUserUtil.getCustomerId(request);
		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
		Object fixChannels = custRight.get("channels");
		params.addAttr("fixChannels", fixChannels);//固定渠道	
		
		if("day".equals(dateType)){
			params.addAttr("datePattern", "%Y-%m-%d");
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
		}else if ("range".equals(dateType)) {
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
				return ;
			}
			params.setService("channelMsSectionSumService");
			params.setMethod("channelOrigDtl");
			params.addAttr(P_COUNT_METHOD, "channelOrigBaseCount");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
		} else {//按月
				String startRecordMonth = StringUtil.getString(params.getAttr("startRecordMonth"));
				String endRecordMonth = StringUtil.getString(params.getAttr("endRecordMonth"));
				if(StringUtils.isEmpty(startRecordMonth) || StringUtils.isEmpty(endRecordMonth)){
					result.setSuccess(false);
					result.setMessage("缺少必传参数!");
					return ;
				}
				params.addAttr("datePattern", "%Y-%m");
				params.addAttr("startRecordDate", startRecordMonth+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordMonth+"-01")+" 23:59:59");
		}
	}
	/**
	 * 24小时分析统计
	 * @param params
	 * @param result
	 * @param request
	 */
	public void channel24Analysis(AppParam params, AppResult result, HttpServletRequest request){
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		if(StringUtils.isEmpty(startRecordDate)||params.getAttr("time")==null){
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
			return ;
		}
		
		String customerId = KfUserUtil.getCustomerId(request);
		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
		Object fixChannels = custRight.get("channels");
		params.addAttr("fixChannels", fixChannels);//固定渠道	
		
		params.addAttr("recordDate", StringUtil.getString(params.getAttr("startRecordDate")));
		params.setService("channelOrigSumService");
		params.setMethod("channel24Analysis");
		params.addAttr(P_COUNT_METHOD, "channel24AnalysisCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	
	/**
	 * 查询所有原始的
	 * @param params
	 * @param result
	 * @param request
	 */
	public void origAllList(AppParam params, AppResult result, HttpServletRequest request){
		params.setService("applyService");
		params.setMethod("queryViewByPage");
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		String customerId = KfUserUtil.getCustomerId(request);
		String applyName = request.getParameter("applyName");
		if(ValidUtils.validateTelephone(applyName)){//加快查询效率
			params.addAttr("telephone", applyName);
			params.removeAttr("applyName");
		}
		params.addAttr(P_COUNT_METHOD,"queryViewCount");
		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
		Object fixChannels = custRight.get("channels");
		params.addAttr("fixChannels", fixChannels);//固定渠道
		PageUtil.packageQueryParam(request, params);
	}
	
	/**
	 * 查询所有列表
	 * @param params
	 * @param result
	 * @param request
	 */
	public void allList(AppParam params, AppResult result, HttpServletRequest request){
		String customerId = KfUserUtil.getCustomerId(request);
		String applyName = StringUtil.getString(params.getAttr("applyName"));
		String roleType = CustomerUtil.getRoleType(customerId);
		String searchType = StringUtil.getString(params.getAttr("searchType"));
		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
		Object fixChannels = custRight.get("channels");
		params.addAttr("fixChannels", fixChannels);//固定渠道
		// 是否查客服处理的
		if("1".equals(searchType)){// 我录入的
			params.addAttr("lastKf", customerId);
		}else if ("4".equals(searchType)) {
			params.addAttr("grade", "A");
		}else if ("5".equals(searchType)) {
			params.addAttr("grade", "B");
		}else if ("6".equals(searchType)) {
			params.addAttr("grade", "C");
		}else if ("7".equals(searchType)) {
			params.addAttr("grade", "D");
		}else if ("8".equals(searchType)) {
			params.addAttr("grade", "E");
		}else if ("9".equals(searchType)) {
			params.addAttr("grade", "F");
		}
		
		if(ValidUtils.validateTelephone(applyName)){//加快查询效率
			params.addAttr("telephone", applyName);
			params.removeAttr("applyName");
		}
		
		PageUtil.packageQueryParam(request, params);
		
		params.addAttr("roleType", roleType);
		params.addAttr("loginKf", customerId);
		params.addAttr(P_COUNT_METHOD,"queryShowCount");
		params.setService("borrowApplyService");
		params.setMethod("queryShowByPage");
		params.setOrderBy("applyTime");
		params.setOrderValue("desc");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
	}
	
	/**
	 * 查询所有列表(给推广用的)
	 * @param params
	 * @return
	 */
	public void tgAllList(AppParam params, AppResult result, HttpServletRequest request) {
		String customerId = KfUserUtil.getCustomerId(request);
		String roleType = CustomerUtil.getRoleType(customerId);
		String applyName = request.getParameter("applyName");
		if(ValidUtils.validateTelephone(applyName)){//加快查询效率
			params.addAttr("telephone", applyName);
			params.removeAttr("applyName");
		}

		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
		int spread = NumberUtil.getInt(custRight.get("spread"),0);
		Object fixChannels = custRight.get("channels");
		params.addAttr("fixChannels", fixChannels);//固定渠道
		
		String searchType = request.getParameter("searchType");
		if("1".equals(searchType)){// 我录入的
			params.addAttr("lastKf", customerId);
		}else if ("4".equals(searchType)) {
			params.addAttr("grade", "A");
		}else if ("5".equals(searchType)) {
			params.addAttr("grade", "B");
		}else if ("6".equals(searchType)) {
			params.addAttr("grade", "C");
		}else if ("7".equals(searchType)) {
			params.addAttr("grade", "D");
		}else if ("8".equals(searchType)) {
			params.addAttr("grade", "E");
		}else if ("9".equals(searchType)) {
			params.addAttr("grade", "F");
		}
		//关闭未处理单的查询，系统分单后，不能让客服在所有的里面查到待处理的单子(0-忽略 1-开启),推广的可查询
		if(CustConstant.CUST_ROLETYPE_2.equals(roleType)){
			int autoAllotStatus = SysParamsUtil.getIntParamByKey("autoAllotStatus", 0);
			if(autoAllotStatus == 1 && spread == 0){
				result.setSuccess(false);
				return ;
			}
		}
		
		PageUtil.packageQueryParam(request, params);
		
		params.addAttr("roleType", roleType);
		params.addAttr("loginKf", customerId);
		params.addAttr(P_COUNT_METHOD,"queryShowCount");
		params.setService("borrowApplyService");
		params.setMethod("queryShowByPage");
		params.setOrderBy("applyTime");
		params.setOrderValue("desc");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_busi_in));
	}
	/**
	 * 大渠道统计（已跟进）-基本情况统计
	 * @param params
	 * @return
	 */
	public void channelBase(AppParam params, AppResult result, HttpServletRequest request) {
		params.setService("channelModifySumService");
		params.setMethod("channelBase");
		params.addAttr(P_COUNT_METHOD, "channelBaseCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		
		String customerId = KfUserUtil.getCustomerId(request);
		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
		Object fixChannels = custRight.get("channels");
		params.addAttr("fixChannels", fixChannels);//固定渠道	
		
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(P_DAY.equals(dateType)){
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
			}
			params.addAttr("datePattern", P_DAY_PATTERN);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
		}else if (P_RANGE.equals(dateType)) {
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
			}
			params.setService("channelMsSectionSumService");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
		}else {//按月
			String startRecordMonth = StringUtil.getString(params.getAttr("startRecordMonth"));
			String endRecordMonth = StringUtil.getString(params.getAttr("endRecordMonth"));
			params.addAttr("datePattern", P_MONTH_PATTERN);
			params.addAttr("startRecordDate", startRecordMonth+"-01");
			params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordMonth+"-01")+" 23:59:59");
		}
	}
	
	/**
	 * app相关统计-第三方渠道注册用户统计
	 * @param params
	 * @return
	 */
	public  void thirdAppChannelRegister(AppParam params, AppResult result, HttpServletRequest request) {
		String customerId = KfUserUtil.getCustomerId(request); 
		String roleType = CustomerUtil.getRoleType(customerId);
		if(!CustConstant.CUST_ROLETYPE_1.equals(roleType)){
			Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
			Object fixChannels = custRight.get("channels");
			if(ObjectUtils.isEmpty(fixChannels)){
				result.setSuccess(false);
				result.setMessage("您当前没有配置管理的渠道，请联系管理员!");
			}
			params.addAttr("fixChannels", fixChannels);//固定渠道	
		}
		params.addAttr(P_COUNT_METHOD, "thirdQueryCount");
		params.setService("sumUserRegisterCountService");
		params.setMethod("thirdQuery");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
	}
	
	/**
	 *  微信推广统计
	 * @param params
	 * @return
	 */
//	public  void wxTgSum(AppParam params, AppResult result, HttpServletRequest request) {
//		String dateType = StringUtil.getString(params.getAttr("dateType"));
//		if("day".equals(dateType)){
//			params.setService("sceneQrcodeRecordService");
//			params.setMethod("qrcodeSummary");
//			params.addAttr(P_COUNT_METHOD, "qrcodeSummaryCount");
//			params.addAttr("recordDate", new Date());
//		}else{
//			params.setService("qrcodeSumCountService");
//			params.setMethod("querySummary");
//			params.addAttr(P_COUNT_METHOD, "querySummaryCount");
//		}
//		String name = request.getParameter("name");
//		if(ValidUtils.validateTelephone(name)){
//			params.addAttr("telephone", name);
//			params.removeAttr("name");
//		}
//		params.addAttr("type", 2);//查询参数二维码
//		params.setRmiServiceName(AppProperties
//				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_wx));
//	}
	/**
	 *  小渠道基本情况统计
	 * @param params
	 * @return
	 */
	public  void channelDtlBase(AppParam params, AppResult result, HttpServletRequest request) {
		params.setService("channelDtlModifySumService");
		params.setMethod("channelDtlBase");
		params.addAttr(P_COUNT_METHOD, "channelBaseCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		params.addAttr("datePattern", "%Y-%m-%d");
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
			return ;
		}
		params.addAttr("endRecordDate", endRecordDate + " 23:59:59");
		if("month".equals(dateType)){//按月
			params.addAttr("datePattern", "%Y-%m");
			params.addAttr("startRecordDate", startRecordDate + "-01");
			params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordDate+"-01")+" 23:59:59");
		}else if ("range".equals(dateType)) {
			params.setService("channelDtlMsSectionSumService");
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
		}

	}
	/**
	 * 金额资质详细情况统计
	 * @param params
	 * @return
	 */
	public  void channelDtlAmount(AppParam params, AppResult result, HttpServletRequest request) {
		params.setService("channelDtlModifySumService");
		params.setMethod("channelDtlAmount");
		params.addAttr(P_COUNT_METHOD, "channelBaseCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		params.addAttr("datePattern", "%Y-%m-%d");
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
			return ;
		}
		params.addAttr("endRecordDate", endRecordDate + " 23:59:59");
		if("month".equals(dateType)){//按月
			params.addAttr("datePattern", "%Y-%m");
			params.addAttr("startRecordDate", startRecordDate + "-01");
			params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordDate + "-01") + " 23:59:59");
		}else if ("range".equals(dateType)) {
			params.setService("channelDtlMsSectionSumService");
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
		}
	}
	/**
	 * 线索流向情况统计
	 * @param params
	 * @return
	 */
	public  void channelDtlSale(AppParam params, AppResult result, HttpServletRequest request) {
		params.addAttr(P_COUNT_METHOD, "channelBaseCount");
		params.setService("channelDtlModifySumService");
		params.setMethod("channelDtlSale");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		params.addAttr("datePattern", "%Y-%m-%d");
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
			return ;
		}
		params.addAttr("endRecordDate", endRecordDate + " 23:59:59");
		if("month".equals(dateType)){//按月
			params.addAttr("datePattern", "%Y-%m");
			params.addAttr("startRecordDate", startRecordDate + "-01");
			params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordDate + "-01") + " 23:59:59");
		}else if ("range".equals(dateType)) {
			params.setService("channelDtlMsSectionSumService");
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
		}
	}
	/**
	 * 网销门店情况统计
	 * @param params
	 * @return
	 */
	public  void channelDtlNet(AppParam params, AppResult result, HttpServletRequest request) {
		params.addAttr(P_COUNT_METHOD, "channelBaseCount");
		params.setService("channelDtlModifySumService");
		params.setMethod("channelDtlNet");
		params.addAttr("datePattern", "%Y-%m-%d");
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
			return ;
		}
		params.addAttr("endRecordDate", endRecordDate + " 23:59:59");
		if("month".equals(dateType)){//按月
			params.addAttr("datePattern", "%Y-%m");
			params.addAttr("startRecordDate", startRecordDate + "-01");
			params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordDate + "-01") + " 23:59:59");
		}else if ("range".equals(dateType)) {
			params.setService("channelDtlMsSectionSumService");
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
		}

		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	/**
	 * 第三方挂卖渠道统计
	 * @param request
	 * @return
	 */
//	public void thirdSaleChannelCount(AppParam params, AppResult result, HttpServletRequest request){
//		params.setService("thirdSaleSumService");
//		params.setMethod("queryChannel");
//		params.addAttr(P_COUNT_METHOD, "queryChannelCount");
//		int formatType = NumberUtil.getInt(params.getAttr("formatType"), 1);
//		switch (formatType) {
//		case 1:
//			params.addAttr("datePattern", P_DAY_PATTERN);
//			break;
//		case 2:
//			String monthStart = params.getAttr("monthStart") + "-01";
//			String monthEnd = PageUtil.getLastDay(params.getAttr("monthEnd") + "-01");
//			params.addAttr("datePattern", P_MONTH_PATTERN);
//			params.addAttr("startTime", monthStart);
//			params.addAttr("endTime", monthEnd);
//			break;
//		case 3:
//			params.setMethod("queryChannelRange");
//			params.addAttr(P_COUNT_METHOD, "queryChannelRangeCount");
//			break;
//		}
//		String customerId = StringUtil.getString(params.getAttr("customerId"));
//		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
//		Object fixChannels = custRight.get("channels");
//		params.addAttr("fixChannels", fixChannels);//固定渠道
//		params.setRmiServiceName(AppProperties
//				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi));
//		
//	}
	
	/**
	 * 第三方挂卖汇总统计
	 * @param request
	 * @return
	 */
//	public void thirdSaleSumCount(AppParam params, AppResult result, HttpServletRequest request){
//		params.setService("thirdSaleSumService");
//		params.setMethod("querySum");
//		params.addAttr(P_COUNT_METHOD, "querySumCount");
//		int formatType = NumberUtil.getInt(params.getAttr("formatType"), 1);
//		switch (formatType) {
//		case 1:
//			params.addAttr("datePattern", P_DAY_PATTERN);
//			break;
//		case 2:
//			String monthStart = params.getAttr("monthStart") + "-01";
//			String monthEnd = PageUtil.getLastDay(params.getAttr("monthEnd") + "-01");
//			params.addAttr("datePattern", P_MONTH_PATTERN);
//			params.addAttr("startTime", monthStart);
//			params.addAttr("endTime", monthEnd);
//			break;
//		case 3:
//			params.setMethod("querySumRange");
//			params.addAttr(P_COUNT_METHOD, "querySumRangeCount");
//			break;
//		}
//		String customerId = StringUtil.getString(params.getAttr("customerId")); 
//		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
//		Object fixChannels = custRight.get("channels");
//		params.addAttr("fixChannels", fixChannels);//固定渠道
//		params.setRmiServiceName(AppProperties
//				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi));
//		
//	}
	/**
	 * 等级去向统计
	 * @param params
	 * @param result
	 * @param request
	 */
	public void allotGrade(AppParam params, AppResult result,HttpServletRequest request){
		params.setService("allotGradeSumService");
		params.setMethod("allotGrade");
		params.addAttr(P_COUNT_METHOD, "allotGradeCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(P_DAY.equals(dateType)){
			params.addAttr("datePattern", P_DAY_PATTERN);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
			}
		}else if (P_RANGE.equals(dateType)) {
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
			}
			params.setService("allotGradeSectionSumService");
			params.addAttr(P_COUNT_METHOD, "allotGradeCount");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
		}else {//按月
			    String startRecordMonth = StringUtil.getString(params.getAttr("startRecordMonth"));
			    String endRecordMonth = StringUtil.getString(params.getAttr("endRecordMonth"));
				params.addAttr("datePattern", P_MONTH_PATTERN);
				params.addAttr("startRecordDate", startRecordMonth+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordMonth+"-01")+" 23:59:59");
		}
	}
	
	/**
	 * 城市 等级 去向 统计
	 * @param request
	 * @return
	 */
	public void allotCity(AppParam params, AppResult result,HttpServletRequest request){
		params.setService("allotGradeSumService");
		params.setMethod("allotCity");
		params.addAttr(P_COUNT_METHOD, "allotCityCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(P_DAY.equals(dateType)){
			params.addAttr("datePattern", P_DAY_PATTERN);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
			}
		}else if (P_RANGE.equals(dateType)) {
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
			}
			params.setService("allotGradeSectionSumService");
			params.addAttr(P_COUNT_METHOD, "allotCityCount");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
		}else {//按月
			    String startRecordMonth = StringUtil.getString(params.getAttr("startRecordMonth"));
			    String endRecordMonth = StringUtil.getString(params.getAttr("endRecordMonth"));
				params.addAttr("datePattern", P_MONTH_PATTERN);
				params.addAttr("startRecordDate", startRecordMonth+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordMonth+"-01")+" 23:59:59");
		}
	}
	/**
	 * 渠道 等级 （ABCDEF） 去向统计
	 * @param request
	 * @return
	 */
	public void allotChannelCode(AppParam params, AppResult result,HttpServletRequest request){
		params.setService("allotGradeSumService");
		params.setMethod("allotChannelCode");
		params.addAttr(P_COUNT_METHOD, "allotChannelCodeCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(P_DAY.equals(dateType)){
			params.addAttr("datePattern", P_DAY_PATTERN);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
			}
		}else if (P_RANGE.equals(dateType)) {
			if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
				result.setSuccess(false);
				result.setMessage("缺少必传参数!");
			}
			params.setService("allotGradeSectionSumService");
			params.addAttr(P_COUNT_METHOD, "allotChannelCodeCount");
			params.addAttr("startRecordDate", startRecordDate);
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
		}else {//按月
			    String startRecordMonth = StringUtil.getString(params.getAttr("startRecordMonth"));
			    String endRecordMonth = StringUtil.getString(params.getAttr("endRecordMonth"));
				params.addAttr("datePattern", P_MONTH_PATTERN);
				params.addAttr("startRecordDate", startRecordMonth+"-01");
				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordMonth+"-01")+" 23:59:59");
		}
	}
	
	/**
	 * 原始小渠道统计-24时段分析统计
	 */
	public void channelDtl24Analysis(AppParam params, AppResult result,HttpServletRequest request){
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		if(StringUtils.isEmpty(startRecordDate)||params.getAttr("time")==null){
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
		}
		params.addAttr("recordDate", StringUtil.getString(params.getAttr("startRecordDate")));
		params.addAttr(P_COUNT_METHOD, "channelDtl24AnalysisCount");
		params.setService("channelDtlOrigSumService");
		params.setMethod("channelDtl24Analysis");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
	}
	
	/**
	 *  原始小渠道统计-金额资质详细情况统计
	 */
	public void channelDtlOrigAmount(AppParam params, AppResult result,HttpServletRequest request){
		params.setService("channelDtlOrigSumService");
		params.setMethod("channelDtl");
		params.addAttr(P_COUNT_METHOD, "channelBaseCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		params.addAttr("datePattern", P_DAY_PATTERN);
		
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
		}
		params.addAttr("endRecordDate", endRecordDate + " 23:59:59");
		
		if(P_MONTH.equals(dateType)){//按月
			params.addAttr("datePattern", P_MONTH_PATTERN);
			params.addAttr("startRecordDate", startRecordDate + "-01");
			params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordDate + "-01") + " 23:59:59");
		}else if (P_RANGE.equals(dateType)) {
			params.setService("channelDtlMsSectionSumService");
			params.setMethod("channelOrigDtl");
			params.addAttr(P_COUNT_METHOD, "channelOrigBaseCount");
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
		}
	}
	
	/**
	 * 原始小渠道统计-基本情况统计
	 */
	public void channelDtlOrigBase(AppParam params, AppResult result,HttpServletRequest request){
		params.setService("channelDtlOrigSumService");
		params.setMethod("channelBase");
		params.addAttr(P_COUNT_METHOD, "channelBaseCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		params.addAttr("datePattern", P_DAY_PATTERN);

		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		if(StringUtils.isEmpty(startRecordDate) || StringUtils.isEmpty(endRecordDate)){
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
		}
		params.addAttr("endRecordDate", endRecordDate + " 23:59:59");
		if(P_MONTH.equals(dateType)){//按月
			params.addAttr("datePattern", P_MONTH_PATTERN);
			params.addAttr("startRecordDate", startRecordDate + "-01");
			params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordDate+"-01")+" 23:59:59");
		}else if (P_RANGE.equals(dateType)) {
			params.setService("channelDtlMsSectionSumService");
			params.setMethod("channelOrigBase");
			params.addAttr(P_COUNT_METHOD, "channelOrigBaseCount");
			params.addAttr("startDateStr", startRecordDate);
			params.addAttr("endDateStr", endRecordDate);
		}
	}
	/**
	 * mjb实时挂卖情况统计(按日期)
	 */
//	public void mjbSaleCountReal(AppParam params, AppResult result,HttpServletRequest request){
//		params.addAttr(P_COUNT_METHOD, "saleCountRealCount");
//		String startRecordDate = StringUtil.getString(params.getAttr("startDate"));
//		String endRecordDate = StringUtil.getString(params.getAttr("endDate"));
//		String dateType = request.getParameter("dateType");
//		params.setMethod("saleCountReal");
//		params.setService("daiBorrowReService");
//		params.setOrderBy("date");
//		params.setOrderValue("DESC");
//		if("day".equals(dateType)){
//			params.addAttr("datePattern", "%Y-%m-%d");
//			params.addAttr("startRecordDate", startRecordDate);
//			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
//
//		}else if ("range".equals(dateType)) {
//			params.addAttr("startRecordDate", startRecordDate);
//			params.addAttr("startDateStr", startRecordDate);
//			params.addAttr("endDateStr", endRecordDate);
//			params.addAttr("endRecordDate", endRecordDate+" 23:59:59");
//			params.addAttr("datePattern", "%Y-%m-%d");
//			params.setMethod("saleCountRealRange");
//		}else {//按月
//				params.addAttr("datePattern", "%Y-%m");
//				params.addAttr("startRecordDate", startRecordDate+"-01");
//				params.addAttr("endRecordDate", PageUtil.getLastDay(endRecordDate+"-01")+" 23:59:59");
//		}
//		params.setRmiServiceName(AppProperties
//				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_mjb));
//	}
	
	/**
	 * 回款列表
	 */
	public void refund(AppParam params, AppResult result,HttpServletRequest request){
		params.setService("treatSuccessService");
		params.setMethod("queryShowByPage");
		String customerId = KfUserUtil.getCustomerId(request);
		Map<String,Object> custRight = KfUserUtil.getUserRight(customerId);
		Object fixChannels = custRight.get("channels");
		params.addAttr("fixChannels", fixChannels);//固定渠道		
		if(StringUtils.isEmpty(params.getOrderBy())){
			params.setOrderBy("t.createTime");
			params.setOrderValue("desc");
		}
		
		PageUtil.packageQueryParam(request, params);
		
		params.addAttr(P_COUNT_METHOD, "queryShowCount");
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
	}
}
