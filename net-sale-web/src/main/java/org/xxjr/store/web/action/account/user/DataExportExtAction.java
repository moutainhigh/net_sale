package org.xxjr.store.web.action.account.user;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.kf.ExportUtil;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.sys.util.ServiceKey;

import com.alibaba.fastjson.JSONArray;

@Controller()
@RequestMapping("/account/user/exportext/")
public class DataExportExtAction {

	/*** 
	 * 导出excel
	 * @param request
	 * @param response
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("exportExcel")
	@ResponseBody
	public void  exportExcel(HttpServletRequest request,HttpServletResponse response){
		AppResult result = new AppResult();
		PrintWriter printWriter = null;
		OutputStream os = null;
		try{
			printWriter = response.getWriter();
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);

			if(StringUtils.isEmpty(params.getAttr("exportTitles"))){
				printWriter.print("not found params:exportTitles");
				printWriter.flush();
				return;
			}
			if(StringUtils.isEmpty(params.getAttr("exportParams"))){
				printWriter.print("not found params:exportParams");
				printWriter.flush();
				return;
			}
			String exportType = request.getParameter("exportType");
			String exportTitles = params.removeAttr("exportTitles").toString();
			String exportParams = params.removeAttr("exportParams").toString();//查询参数
			Map<String,Object> queryParams = JsonUtil.getInstance().json2Object(exportParams, Map.class);
			params.addAttrs(queryParams);

			LinkedHashMap<String, String> exchangeTitle = new LinkedHashMap<String, String>();
			JSONArray titleJson = JSONArray.parseArray(exportTitles);
			for (int i = 0; i < titleJson.size(); i++) {
				Map<String,String> titleMap=(Map<String,String>)titleJson.get(i);
				if(titleMap.containsKey("title")&&titleMap.containsKey("name")){
					exchangeTitle.put(titleMap.get("title"),titleMap.get("name"));
				}
			}

			String fileName = "";
			if("dealOrderTypeToday".equals(exportType)){
				result = this.queryDealOrderTypeToday(params,request);
				fileName = "订单类型今日统计列表";
			}else if("dealOrderTypeDay".equals(exportType)){
				result = this.queryDealOrderTypeDay(params,request);
				fileName = "订单类型本月统计列表";
			}else if("dealOrderTypeMonth".equals(exportType)){
				result = this.queryDealOrderTypeMonth(params,request);
				fileName = "订单类型月度统计列表";
			}else if("orderRateToday".equals(exportType)){
				result = this.queryRateToday(params,request);
				fileName = "订单评分今日统计列表";
			}else if("orderRateDay".equals(exportType)){
				result = this.queryRateDay(params,request);
				fileName = "订单评分本月统计列表";
			}else if("orderRateMonth".equals(exportType)){
				result = this.queryRateMonth(params,request);
				fileName = "订单评分月度统计列表";
			}else if("signFailToday".equals(exportType)){
				result = this.querySignFailToday(params,request);
				fileName = "签单失败渠道今日统计列表";
			}else if("signFailDay".equals(exportType)){
				result = this.querySignFailDay(params,request);
				fileName = "签单失败渠道本月统计列表";
			}else if("signFailMonth".equals(exportType)){
				result = this.querySignFailMonth(params,request);
				fileName = "签单失败渠道月度统计列表";
			}else if("orderChannelToday".equals(exportType)){
				result = this.queryOrderChannelToday(params,request);
				fileName = "订单状态渠道今日统计列表";
			}else if("orderChannelDay".equals(exportType)){
				result = this.queryOrderChannelDay(params,request);
				fileName = "订单状态渠道本月统计列表";
			}else if("orderChannelMonth".equals(exportType)){
				result = this.queryOrderChannelMonth(params,request);
				fileName = "订单状态渠道月度统计列表";
			}else if("orgDealOrderToday".equals(exportType)){
				result = this.queryOrgDealOrderToday(params,request);
				fileName = "订单状态分组今日统计（门店）列表";
			}else if("orgDealOrderDay".equals(exportType)){
				result = this.queryOrgDealOrderDay(params,request);
				fileName = "订单状态分组本月统计（门店）列表";
			}else if("orgDealOrderMonth".equals(exportType)){
				result = this.queryOrgDealOrderMonth(params,request);
				fileName = "订单状态分组月度统计（门店）列表";
			}else{
				printWriter.print("exportType not find exportType" + exportType);
				printWriter.flush();
				return;
			}
			
			fileName = fileName + "_" + DateUtil.toStringByParttern(new Date(),
					DateUtil.DATE_PATTERNYYYYMMDDHHMMSSSSS) + ".xls";
			response.reset();// 清空输出流
			response.setHeader("Content-disposition", "attachment; filename=" + new String(fileName.getBytes(),"iso-8859-1"));
			// 设定输出文件头
			response.setContentType("application/msexcel");// 定义输出类型
			response.setCharacterEncoding("UTF-8");
			os = response.getOutputStream();
			ExportUtil.writeExcel(os, exchangeTitle, result.getRows());

		}catch(Exception e){
			LogerUtil.error(this.getClass(), e,"DataExportAction exportExcel error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
			printWriter.print("error exportExcel:" + e.getMessage());
			printWriter.flush();
		}finally{
			IOUtils.closeQuietly(printWriter);
			IOUtils.closeQuietly(os);
		}
	}


	/**
	 * 签单失败渠道今日统计列表
	 * @param params
	 * @param request
	 * @return
	 */
	public AppResult querySignFailToday(AppParam params, HttpServletRequest request) {
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("storeApplyExtService");
		params.setMethod("querySignFailToday");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	/**
	 * 签单失败渠道本月统计列表
	 * @param params
	 * @param request
	 * @return
	 */
	public AppResult querySignFailDay(AppParam params, HttpServletRequest request) {
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumSignFailChannelService");
		params.setMethod("querySignFailDay");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	/**
	 * 签单失败渠道月度统计列表
	 * @param params
	 * @param request
	 * @return
	 */
	public AppResult querySignFailMonth(AppParam params, HttpServletRequest request) {
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumSignFailChannelService");
		params.setMethod("querySignFailMonth");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}

	/***
	 * 订单类型今日统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryDealOrderTypeToday(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
		params.setService("storeListOptExtService");
		params.setMethod("queryDealOrderTypeToday");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}


	/***
	 * 订单类型按日统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryDealOrderTypeDay(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumDealOrderTypeService");
		params.setMethod("queryDealOrderTypeDay");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}

	/***
	 * 订单类型月度统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryDealOrderTypeMonth(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumDealOrderTypeService");
		params.setMethod("queryDealOrderTypeMonth");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}

	/***
	 * 订单评分今日统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryRateToday(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
		params.setService("storeApplyExtService");
		params.setMethod("queryRateToday");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		return RemoteInvoke.getInstance().callNoTx(params);
	}


	/***
	 * 订单评分本月统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryRateDay(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumOrderRateService");
		params.setMethod("queryRateDay");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}

	/***
	 * 订单评分月度统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryRateMonth(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumOrderRateService");
		params.setMethod("queryRateMonth");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	/***
	 * 订单状态渠道统计今日统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryOrderChannelToday(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("storeApplyExtService");
		params.setMethod("queryOrderChannelToday");
		params.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		return result;
	}
	/***
	 * 订单状态渠道统计本月统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryOrderChannelDay(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumChannelDealordertypeService");
		params.setMethod("queryChannelDealordertypeDay");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		return result;
	}
	/***
	 * 订单状态渠道统计月度统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryOrderChannelMonth(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumChannelDealordertypeService");
		params.setMethod("queryChannelDealordertypeMonth");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	/***
	 * 微名片统计本月统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryWzCardDay(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumWzCardService");
		params.setMethod("queryWzCardDay");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		return result;
	}
	/***
	 * 微名片统计月度统计列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryWzCardMonth(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumWzCardService");
		params.setMethod("queryWzCardMonth");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	/***
	 * 订单状态分组今日统计（门店）列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryOrgDealOrderToday(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("storeListOptExtService");
		params.setMethod("queryOrgDealOrderToday");
		params.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		return result;
	}
	/***
	 * 订单状态分组本月统计（门店）列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryOrgDealOrderDay(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumOrgDealOrderService");
		params.setMethod("queryOrgDealOrderDay");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		return result;
	}
	/***
	 * 订单状态分组月度统计（门店）列表导出
	 * @param params
	 * @return
	 */
	public AppResult queryOrgDealOrderMonth(AppParam params ,HttpServletRequest request){
		//处理条件
		String customerId = StoreUserUtil.getCustomerId(request);
		RequestUtil.setAttr(params, request);
		StoreUserUtil.dealUserAuthParam(params, customerId, "customerId");
		params.setCurrentPage(-1);
		params.setService("sumOrgDealOrderService");
		params.setMethod("queryOrgDealOrderMonth");
		params.setOrderBy(StringUtil.getString(params.getAttr("orderBy")));
		params.setOrderValue(StringUtil.getString(params.getAttr("orderValue")));
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		return RemoteInvoke.getInstance().callNoTx(params);
	}
	
	
}
