package org.xxjr.store.web.action.account.work;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.ThreadLogUtil;
import org.xxjr.sys.util.ValidUtils;

/**
 * 无效客户列表
 * @author chencx
 *
 */
@Controller()
@RequestMapping("/account/work/invalidCustomer/")
public class InvalidCustomerAction {
	/**
	 *  查询无效客户列表信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("queryInvalidCustList")
	@ResponseBody
	public AppResult queryInvalidCustList(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(false);
				result.setMessage("用户ID不能为空");
				return result;
			}
			AppParam param = new AppParam();
			param.setService("storeHandleExtService");
			param.setMethod("queryInvalidCustList");
			RequestUtil.setAttr(param, request);
			StoreUserUtil.dealUserAuthParam(param, customerId, "lastStore");
			String searchKey = request.getParameter("searchKey");
			if(!StringUtils.isEmpty(searchKey)){
				if(ValidUtils.validateTelephone(searchKey)){//加快查询效率
					param.addAttr("telephone", searchKey);
					param.removeAttr("searchKey");
				}else{
					param.addAttr("applyName", searchKey);
					param.removeAttr("searchKey");
				}
			}
			String storeSearchKey = request.getParameter("storeSearchKey");
			if(!StringUtils.isEmpty(storeSearchKey)){
				if(ValidUtils.validateTelephone(storeSearchKey)){//加快查询效率
					param.addAttr("mobile", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}else{
					param.addAttr("realName", storeSearchKey);
					param.removeAttr("storeSearchKey");
				}
			}
			param.setOrderBy("lastUpdateTime");
			param.addAttr("orderStatusIn", "7,8");
			param.setOrderValue("desc");
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(param);
		} catch (Exception e) {
			LogerUtil.error(InvalidCustomerAction.class, e, "queryInvalidCustList error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	/**
	 *  自动把无效客户和空号错号的订单加入无效订单池
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("autoInsertInvalidPool")
	@ResponseBody
	public AppResult autoInsertInvalidPool(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(false);
				result.setMessage("用户ID不能为空");
				return result;
			}
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			if (custInfo != null) {
				String roleType = StringUtil.getString(custInfo.get("roleType"));
				if(!"1".equals(roleType)){
					result.setSuccess(false);
					result.setMessage("您没有权限操作此功能！");
					return result;
				}
			}else{
				result.setSuccess(false);
				result.setMessage("用户信息不能为空");
				return result;
			}
			//查询无效订单
			AppParam queryparam = new AppParam("invalidStorePoolService","queryInvalidOrder");
			queryparam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult queryResult = RemoteInvoke.getInstance().call(queryparam);
			if(queryResult.getRows().size() > 0){
				AppParam param = new AppParam("invalidStorePoolService","batchDealInvalidPool");
				param.addAttr("orderList", queryResult.getRows());
				param.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				ThreadLogUtil.sendMessageNewThread(param);
			}else{
				result.setMessage("暂时没有符合条件的无效订单");
			}
			
		} catch (Exception e) {
			LogerUtil.error(InvalidCustomerAction.class, e, "autoInsertInvalidPool error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 *  批量加入无效订单池
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("addInvalidOrderPool")
	@ResponseBody
	public AppResult addInvalidOrderPool(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			
			Map<String, Object> orderInfo = JsonUtil.getInstance().json2Object(
					request.getParameter("str"), Map.class);
			if (StringUtils.isEmpty(orderInfo)) {
				return CustomerUtil.retErrorMsg("订单的基本信息不能为空");
			}
			List<Map<String, Object>> orders = (List<Map<String, Object>>) orderInfo
					.get("orders");
			if (StringUtils.isEmpty(orders)) {
				return CustomerUtil.retErrorMsg("请传入订单的基本信息");
			}
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(false);
				result.setMessage("用户ID不能为空");
				return result;
			}
			List<String> applyIdList = new ArrayList<String>();
			for(Map<String, Object> map : orders){
				applyIdList.add(StringUtil.getString(map.get("applyId")));
			}
			//查询选择的无效订单信息
			AppParam queryparam = new AppParam("storeHandleExtService","queryInvalidOrderInfo");
			queryparam.addAttr("list", applyIdList);
			queryparam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			AppResult queryResult = RemoteInvoke.getInstance().call(queryparam);
			if(queryResult.getRows().size() > 0){
				AppParam param = new AppParam("invalidStorePoolService","batchInsertInvalidPool");
				param.addAttr("orderList", queryResult.getRows());
				param.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START
								+ ServiceKey.Key_busi_in));
				result = RemoteInvoke.getInstance().call(param);
			}
		} catch (Exception e) {
			LogerUtil.error(InvalidCustomerAction.class, e, "addInvalidOrderPool error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 转其他信贷经理
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/checkTransOtherXDJL")
	@ResponseBody
	public AppResult checkTransOtherXDJL(HttpServletRequest request) {
		AppResult result = new AppResult();
		try {
			Map<String, Object> orderInfo = JsonUtil.getInstance().json2Object(
					request.getParameter("str"), Map.class);
			if (StringUtils.isEmpty(orderInfo)) {
				return CustomerUtil.retErrorMsg("订单的基本信息不能为空");
			}
			List<Map<String, Object>> orders = (List<Map<String, Object>>) orderInfo
					.get("orders");
			if (StringUtils.isEmpty(orders)) {
				return CustomerUtil.retErrorMsg("请传入订单的基本信息");
			}
			String custId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(custId)) {
				return CustomerUtil.retErrorMsg("用户ID不能为空");
			}
			String orgId = request.getParameter("orgId");
			if (StringUtils.isEmpty(orgId)) {
				return CustomerUtil.retErrorMsg("门店不能为空");
			}
			String customerId = request.getParameter("customerId");
			if (StringUtils.isEmpty(customerId)) {
				return CustomerUtil.retErrorMsg("门店人员不能为空");
			}
			AppParam applyParam = new AppParam();
			applyParam.addAttr("orders", orders);
			applyParam.addAttr("orgId", orgId);
			applyParam.addAttr("custId", custId);
			applyParam.addAttr("customerId", customerId);
			applyParam.setService("storeOptExtService");
			applyParam.setMethod("transOtherXDJL");
			applyParam.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(applyParam);
			if(result.isSuccess()){
				StringBuffer strBuffer = new StringBuffer();
				strBuffer.append("总共:").append(orders.size()).append("笔,成功:");
				strBuffer.append(result.getAttr("sucSize")).append("笔,失败");
				strBuffer.append(result.getAttr("failSize")).append("笔");
				if(!StringUtils.isEmpty(result.getAttr("failDesc"))){
					strBuffer.append(",").append(result.getAttr("failDesc"));
				}
				result.setMessage(strBuffer.toString());	
			}
		} catch (Exception e) {
			LogerUtil.error(InvalidCustomerAction.class, e, "transOtherXDJL error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		}
		return result;
	}
}
