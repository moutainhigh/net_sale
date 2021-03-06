package org.xxjr.cust.identify;

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
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.sys.util.DBConst;

import lombok.extern.slf4j.Slf4j;

@Lazy
@Service
@Slf4j
public class CustIdentifyService extends BaseService {
	private static final String NAMESPACE = "CUSTIDENTIFY";

	/**
	 * querys
	 * 
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * 根据手机号查询实名认证信息(信用卡分销同步实名信息)
	 * @param params
	 * @return
	 */
	public AppResult queryByTel(AppParam params) {
		return super.query(params, NAMESPACE, "queryByTel");
	}

	/**
	 * queryByPage
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE);
	}

	/**
	 * queryCount
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryCount(AppParam params) {
		int size = getDao().count(NAMESPACE, super.COUNT, params.getAttr(),
				params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}

	/**
	 * queryView
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryView(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryView",
				"queryViewCount");
	}

	/**
	 * queryViewCount
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryViewCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryViewCount",
				params.getAttr(), params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}

	/**
	 * personalAuth
	 * 
	 * @param params
	 * @return
	 */
	public AppResult personalAuth(AppParam params) {

		AppParam identifyParams = new AppParam();
		AppResult result = new AppResult();
		Object customerId = params.getAttr("customerId");
		if (StringUtils.isEmpty(customerId)) {
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		Map<String, Object> custInfo = CustomerIdentify
				.getCustIdentify(customerId.toString());
		Object identifyStatus = custInfo.get("identifyStatus");
		Object cardStatus = custInfo.get("cardStatus");
		if (identifyStatus == null || !"1".equals(identifyStatus.toString())) {
			identifyParams.addAttr("customerId", customerId);
			identifyParams.addAttr("realName", params.getAttr("realName"));
			identifyParams.addAttr("realImage", params.getAttr("realImage"));
			identifyParams.addAttr("status", StringUtils.isEmpty(params
					.getAttr("status")) ? 0 : params.getAttr("status"));// 0-待审核
			identifyParams.addAttr("autoAuditCount",
					params.getAttr("autoAuditCount"));
			identifyParams.addAttr("cardNo", params.getAttr("cardNo"));
			identifyParams.setDataBase(DBConst.Key_cust_DB);
			result = this.update(identifyParams);
		}

		if (cardStatus == null || !"1".equals(cardStatus.toString())) {
			if (result.isSuccess()) {
				AppParam cardParams = new AppParam("custIdentifyCardService",
						"update");
				cardParams.addAttr("customerId", customerId);
				cardParams.addAttr("compId", params.getAttr("compId"));
				cardParams.addAttr("company", params.getAttr("company"));
				cardParams
						.addAttr("companyDesc", params.getAttr("companyDesc"));
				cardParams.addAttr("cardImage", params.getAttr("cardImage"));
				cardParams.addAttr("status", 0);// 0-待审核
				cardParams.addAttr("workName", params.getAttr("workName"));
				result = SoaManager.getInstance().invoke(cardParams);
			}
		}
		if (!StringUtils.isEmpty(params.getAttr("cityName"))
				&& !StringUtils.isEmpty(params.getAttr("address"))) {
			// 工作城市信息
			AppParam custInfoParam = new AppParam();
			custInfoParam.setService("custInfoService");
			custInfoParam.setMethod("update");
			custInfoParam.addAttr("provice", params.getAttr("provice"));
			custInfoParam.addAttr("cityName", params.getAttr("cityName"));
			custInfoParam.addAttr("cityArea", params.getAttr("cityArea"));
			custInfoParam.addAttr("address", params.getAttr("address"));
			custInfoParam.addAttr("customerId", customerId);
			SoaManager.getInstance().invoke(custInfoParam);
		}
		return result;
	}

	/**
	 * 保存身份和姓名信息
	 * 
	 * @param params
	 * @return
	 */
	public AppResult saveIdentityInfo(AppParam params) {
		AppParam identifyParams = new AppParam();
		AppResult result = new AppResult();
		String customerId = StringUtil.getString(params.getAttr("customerId"));
		if (StringUtils.isEmpty(customerId)) {
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
		String identifyStatus = StringUtil.getString(custInfo.get("identifyStatus"));
		if (identifyStatus == null || !"1".equals(identifyStatus)) {
			String status = StringUtil.getString(params.getAttr("status"));
			identifyParams.addAttr("customerId", customerId);
			identifyParams.addAttr("realName", params.getAttr("realName"));
			identifyParams.addAttr("cardNo", params.getAttr("cardNo"));
			identifyParams.addAttr("status", StringUtils.isEmpty(status) ? 0 : status);// 0-待审核
			identifyParams.setDataBase(DBConst.Key_cust_DB);
			result = this.update(identifyParams);
		}
		return result;
	}

	/**
	 * 添加工作信息
	 * 
	 * @param params
	 * @return
	 */
	public AppResult addWorkInfo(AppParam params) {

		AppResult result = new AppResult();
		Object customerId = params.getAttr("customerId");
		if (StringUtils.isEmpty(customerId)) {
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		Map<String, Object> custInfo = CustomerIdentify
				.getCustIdentify(customerId.toString());
		Object cardStatus = custInfo.get("cardStatus");
		if (cardStatus == null || !"1".equals(cardStatus.toString())) {
			if (result.isSuccess()) {
				AppParam cardParams = new AppParam("custIdentifyCardService",
						"update");
				cardParams.addAttr("customerId", customerId);
				cardParams.addAttr("compId", params.getAttr("compId"));
				cardParams.addAttr("company", params.getAttr("company"));
				cardParams.addAttr("companyDesc", params.getAttr("companyDesc"));
				cardParams.addAttr("status", 0);// 0-待审核
				cardParams.addAttr("workName", params.getAttr("workName"));
				cardParams.addAttr("compType", params.getAttr("compType"));
				result = SoaManager.getInstance().invoke(cardParams);
			}
		}else if ("1".equals(cardStatus.toString())) {
			AppParam cardParams = new AppParam("custIdentifyCardService",
					"update");
			cardParams.addAttr("customerId", customerId);
			cardParams.addAttr("compId", params.getAttr("compId"));
			cardParams.addAttr("workName", params.getAttr("workName"));
			cardParams.addAttr("compType", params.getAttr("compType"));
			result = SoaManager.getInstance().invoke(cardParams);
		}
		if (!StringUtils.isEmpty(params.getAttr("cityName"))
				&& !StringUtils.isEmpty(params.getAttr("address"))) {
			// 工作城市信息
			AppParam custInfoParam = new AppParam();
			custInfoParam.setService("custInfoService");
			custInfoParam.setMethod("update");
			custInfoParam.addAttr("provice", params.getAttr("provice"));
			custInfoParam.addAttr("cityName", params.getAttr("cityName"));
			custInfoParam.addAttr("cityArea", params.getAttr("cityArea"));
			custInfoParam.addAttr("address", params.getAttr("address"));
			custInfoParam.addAttr("customerId", customerId);
			SoaManager.getInstance().invoke(custInfoParam);
		}
		
		return result;
	}

	/**
	 * 个人认证
	 * 
	 * @param params
	 * @return
	 */
	public AppResult personPhotoAuth(AppParam params) {

		AppParam identifyParams = new AppParam();
		AppResult result = new AppResult();
		Object customerId = params.getAttr("customerId");
		if (StringUtils.isEmpty(customerId)) {
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		Map<String, Object> custInfo = CustomerIdentify
				.getCustIdentify(customerId.toString());
		Object identifyStatus = custInfo.get("identifyStatus");
		Object cardStatus = custInfo.get("cardStatus");
		if (identifyStatus == null || !"1".equals(identifyStatus.toString())) {
			identifyParams.addAttr("customerId", customerId);
			identifyParams.addAttr("realName", params.getAttr("realName"));
			identifyParams.addAttr("realImage", params.getAttr("realImage"));
			identifyParams.addAttr("status", StringUtils.isEmpty(params
					.getAttr("status")) ? 0 : params.getAttr("status"));// 0-待审核
			identifyParams.addAttr("cardNo", params.getAttr("cardNo"));
			identifyParams.setDataBase(DBConst.Key_cust_DB);
			result = this.update(identifyParams);
		}

		if (cardStatus == null || !"1".equals(cardStatus.toString())) {
			if (result.isSuccess()) {
				AppParam cardParams = new AppParam("custIdentifyCardService",
						"update");
				cardParams.addAttr("customerId", customerId);
				cardParams.addAttr("compId", params.getAttr("compId"));
				cardParams.addAttr("company", params.getAttr("company"));
				cardParams
						.addAttr("companyDesc", params.getAttr("companyDesc"));
				cardParams.addAttr("cardImage", params.getAttr("cardImage"));
				cardParams.addAttr("status", 0);// 0-待审核
				cardParams.addAttr("workName", params.getAttr("workName"));
				result = SoaManager.getInstance().invoke(cardParams);
			}
		}
		if (!StringUtils.isEmpty(params.getAttr("cityName"))
				&& !StringUtils.isEmpty(params.getAttr("address"))) {
			// 工作城市信息
			AppParam custInfoParam = new AppParam();
			custInfoParam.setService("custInfoService");
			custInfoParam.setMethod("update");
			custInfoParam.addAttr("provice", params.getAttr("provice"));
			custInfoParam.addAttr("cityName", params.getAttr("cityName"));
			custInfoParam.addAttr("cityArea", params.getAttr("cityArea"));
			custInfoParam.addAttr("address", params.getAttr("address"));
			custInfoParam.addAttr("customerId", customerId);
			SoaManager.getInstance().invoke(custInfoParam);
		}
		
		return result;
	}

	/**
	 * 身份证识别
	 * 
	 * @param params
	 * @return
	 */
	public AppResult idCardIdentify(AppParam params) {

		AppParam identifyParams = new AppParam();
		AppResult result = new AppResult();
		Object customerId = params.getAttr("customerId");
		if (StringUtils.isEmpty(customerId)) {
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		Map<String, Object> custInfo = CustomerIdentify
				.getCustIdentify(customerId.toString());
		Object identifyStatus = custInfo.get("identifyStatus");
		if (identifyStatus == null || !"1".equals(identifyStatus.toString())) {
			identifyParams.addAttr("customerId", customerId);
			identifyParams.addAttr("realName", params.getAttr("realName"));
			identifyParams.addAttr("cardNo", params.getAttr("cardNo"));
			identifyParams.addAttr("realImage", params.getAttr("realImage"));
			identifyParams.addAttr("status", StringUtils.isEmpty(params
					.getAttr("status")) ? 0 : params.getAttr("status"));// 0-待审核
			identifyParams.addAttr("autoStatus", 0);
			identifyParams.addAttr("auditDesc", params.getAttr("auditDesc"));
			identifyParams.addAttr("auditLabel", params.getAttr("auditLabel"));
			identifyParams.addAttr("identAuthCount",
					params.getAttr("identAuthCount"));
			identifyParams.setDataBase(DBConst.Key_cust_DB);
			result = this.update(identifyParams);
		}

		return result;
	}

	/**
	 * 人脸实名认证
	 * 
	 * @param params
	 * @return
	 */
	public AppResult personFaceAuth(AppParam params) {

		AppParam identifyParams = new AppParam();
		AppResult result = new AppResult();
		Object customerId = params.getAttr("customerId");
		if (StringUtils.isEmpty(customerId)) {
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		Map<String, Object> custInfo = CustomerIdentify
				.getCustIdentify(customerId.toString());
		Object identifyStatus = custInfo.get("identifyStatus");
		if (identifyStatus == null || !"1".equals(identifyStatus.toString())) {
			identifyParams.addAttr("customerId", customerId);
			identifyParams.addAttr("realName", params.getAttr("realName"));
			identifyParams.addAttr("cardNo", params.getAttr("cardNo"));
			identifyParams.addAttr("photoUrl", params.getAttr("photoUrl"));
			identifyParams.addAttr("realImage", params.getAttr("realImage"));
			identifyParams.addAttr("status", StringUtils.isEmpty(params
					.getAttr("status")) ? 0 : params.getAttr("status"));// 0-待审核
			identifyParams.addAttr("autoAuditCount",
					StringUtils.isEmpty(params.getAttr("autoAuditCount")) ? 0
							: params.getAttr("autoAuditCount"));
			identifyParams.addAttr("autoStatus", params.getAttr("autoStatus"));
			identifyParams.addAttr("auditDesc", params.getAttr("auditDesc"));
			identifyParams.addAttr("auditLabel", params.getAttr("auditLabel"));
			identifyParams.setDataBase(DBConst.Key_cust_DB);
			result = this.update(identifyParams);
		}

		return result;
	}

	/**
	 * 审核通过的 autoAudit
	 * 
	 * @param params
	 * @return
	 */
	public AppResult autoAudit(AppParam params) {
		params.addAttr("createTime", new Date());
		params.addAttr("createBy", DuoduoSession.getUserName());
		AppResult result = super.insert(params, NAMESPACE);
		// 个性用户个人信息
		setCustRealName(params);
		return result;
	}

	private void setCustRealName(AppParam params) {
		// 修改个人信息
		AppParam updateCust = new AppParam();
		updateCust.addAttr("customerId", params.getAttr("customerId"));
		updateCust.addAttr("realName", params.getAttr("realName"));
		updateCust.setService("custInfoService");
		updateCust.setMethod("update");
		SoaManager.getInstance().invoke(updateCust);
	}

	/**
	 * 批量审核通过的 autoAudit
	 * 
	 * @param params
	 * @return
	 */
	public AppResult auditAll(AppParam params) {
		String ids = (String) params.getAttr("ids");
		AppResult result = new AppResult();
		if (StringUtils.isEmpty(ids)) {
			throw new SysException("没有选择数据！");
		}
		for (String id : ids.split(",")) {
			AppParam query = new AppParam();
			query.addAttr("customerId", id);
			query.setDataBase(DBConst.Key_cust_DB);
			this.manAuditPass(query);
		}
		return result;
	}

	/**
	 * 批量审核不通过的 autoAudit
	 * 
	 * @param params
	 * @return
	 */
	public AppResult auditAllNoPass(AppParam params) {
		String ids = (String) params.getAttr("ids");
		Object auditDesc = params.getAttr("auditDesc");
		Object auditLabel = params.getAttr("auditLabel");
		AppResult result = new AppResult();
		if (StringUtils.isEmpty(ids)) {
			throw new SysException("没有选择数据！");
		}
		for (String id : ids.split(",")) {
			AppParam query = new AppParam();
			query.addAttr("customerId", id);
			query.addAttr("auditDesc", auditDesc);
			query.addAttr("auditLabel", auditLabel);
			query.setDataBase(DBConst.Key_cust_DB);
			this.manAuditNoPass(query);
		}
		return result;
	}

	/**
	 * 审核通过的 autoAudit
	 * 
	 * @param params
	 * @return
	 */
	public AppResult manAuditPass(AppParam params) {
		if (StringUtils.isEmpty(params.getAttr("customerId"))) {
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		AppResult result = new AppResult();
		// 修改个人信息
		AppParam query = new AppParam();
		query.addAttr("customerId", params.getAttr("customerId"));
		query.setDataBase(DBConst.Key_cust_DB);
		AppResult queryResult = this.query(query);
		if (queryResult.getRows().size() == 0) {
			throw new AppException(DuoduoError.UPDATE_DATA_IS_NOTEXISTS);
		}
		Map<String, Object> oldRecord = queryResult.getRow(0);
		String oldStatus = oldRecord.get("status").toString();

		// 手动认证，并且当前状态不为审核通过的
		if (CustConstant.identify_status_0.equals(oldStatus)
				|| CustConstant.identify_status_2.equals(oldStatus)) {
			AppParam update = new AppParam();
	
			if (params.getAttr("cardNo") != null) {
				update.addAttr("cardNo", params.getAttr("cardNo"));
			}
			if (StringUtils.isEmpty(oldRecord.get("cardNo"))
					&& StringUtils.isEmpty(update.getAttr("cardNo"))) {
				throw new SysException(oldRecord.get("realName")
						+ "实名认证失败，没有身份证号!");
			}

			update.addAttr("status", CustConstant.identify_status_1);
			update.addAttr("fromStatus", oldStatus);
			update.addAttr("customerId", params.getAttr("customerId"));
			update.addAttr(
					"auditBy",
					StringUtils.isEmpty(params.getAttr("auditBy")) ? DuoduoSession
							.getUserName() : params.getAttr("auditBy"));
			update.addAttr("auditDesc", params.getAttr("auditDesc"));
			update.addAttr("auditLabel", params.getAttr("auditLabel"));
			update.addAttr("auditTime", new Date());
			// update.addAttr("autoStatus", CustConstant.auth_status_0);
			update.setService("custIdentifyService");
			update.setMethod("update");
			result = SoaManager.getInstance().callNewTx(update);
			int size = (Integer) result.getAttr(DuoduoConstant.DAO_Update_SIZE);
			if (size == 1) {
				// 更新用户缓存信息
				CustomerIdentify.refreshIdentifyById(oldRecord.get("customerId").toString());
			} else {
				result.setSuccess(Boolean.FALSE);
				result.setMessage(oldRecord.get("realName")
						+ "实名认证失败，请联系相应管理员!");
			}
			return result;
		}
		result.setSuccess(Boolean.FALSE);
		result.setMessage("实名通过处理失败，当前的状态不允许处理实名认证!");
		return result;
	}

	/**
	 * 审核不通过的 autoAudit
	 * 
	 * @param params
	 * @return
	 */
	public AppResult manAuditNoPass(AppParam params) {
		if (StringUtils.isEmpty(params.getAttr("customerId"))) {
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		String customerId = params.getAttr("customerId").toString();
		AppResult result = new AppResult();
		// 修改个人信息
		AppParam query = new AppParam();
		query.addAttr("customerId", customerId);
		query.setDataBase(DBConst.Key_cust_DB);
		AppResult queryResult = this.query(query);
		if (queryResult.getRows().size() == 0) {
			throw new AppException(DuoduoError.UPDATE_DATA_IS_NOTEXISTS);
		}
		Map<String, Object> oldRecord = queryResult.getRow(0);
		String oldStatus = oldRecord.get("status").toString();
		// 手动认证
		AppParam update = new AppParam();
		update.addAttr("status", CustConstant.identify_status_2);
		update.addAttr("fromStatus", oldStatus);
		update.addAttr("customerId", customerId);
		update.addAttr(
				"auditBy",
				StringUtils.isEmpty(params.getAttr("auditBy")) ? DuoduoSession
						.getUserName() : params.getAttr("auditBy"));
		update.addAttr("auditDesc", params.getAttr("auditDesc"));
		update.addAttr("auditLabel", params.getAttr("auditLabel"));
		update.addAttr("auditTime", new Date());
		 update.addAttr("autoStatus", CustConstant.identify_status_2);
		// update.addAttr("photoUrl", "");
		 update.setDataBase(DBConst.Key_cust_DB);
		result = this.update(update);

		// 消息推送
		try {
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			String realName = (String) oldRecord.get("realName");
			// 消息推送
			String auditTime = DateUtil.toStringByParttern(
					(Date) update.getAttr("auditTime"),
					DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS);
			paramsMap.put("realName", realName);
			paramsMap.put("auditTime", auditTime);
			paramsMap.put("auditDesc", params.getAttr("auditDesc"));
			CustMessageSend messageSend = SpringAppContext
					.getBean(CustMessageSend.class);
			messageSend.sendCustMessage(customerId, "idNotifyFailure",
					paramsMap);

		} catch (Exception e) {
			log.error("failureManAuditNoPass", e);
		}
		// 更新用户缓存信息
		CustomerIdentify.refreshIdentifyById(customerId);

		return result;
	}

	/**
	 * insert
	 * 
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		params.addAttr("createTime", new Date());
		AppResult result = super.insert(params, NAMESPACE);
		return result;
	}

	/**
	 * update
	 * 
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
         if(!StringUtils.isEmpty(params.getAttr("status"))){
        	 params.addAttr("status", StringUtil.getString(params.getAttr("status")));
         }else{
        	 params.removeAttr("status");
         }
		AppResult result = super.update(params, NAMESPACE);
		if (Integer.valueOf(result.getAttr(DuoduoConstant.DAO_Update_SIZE)
				.toString()) == 0) {
			this.insert(params);
		}

		return result;
	}

	/**
	 * delete
	 * 
	 * @param params
	 * @return
	 */
	public AppResult delete(AppParam params) {
		String ids = (String) params.getAttr("ids");
		AppResult result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.setDataBase(DBConst.Key_cust_DB);
				param.addAttr("identifyId", id);

				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("identifyId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**
	 * 修改身份证号
	 * @param param
	 * @return
	 */
	public AppResult updateCardNo(AppParam param){
		AppResult result = new AppResult();
		String customerId = StringUtil.getString(param.getAttr("customerId"));
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
		if (custInfo.isEmpty()) {
			 throw new SysException("查询用户信息失败");
		}
		Object cardNo = custInfo.get("cardNo");
		if (StringUtils.isEmpty(cardNo)) {
			throw new SysException("用户身份证号为空，不能修改");
		}
		AppParam identifyParams = new AppParam();
		identifyParams.addAttr("status", 2);//不通过
		identifyParams.addAttr("auditDesc", "更换手机号");
		identifyParams.addAttr("customerId", customerId);
		identifyParams.addAttr("cardNo", cardNo+"_");//更换手机号时，避免身份证号已被使用问题
		identifyParams.addAttr("auditBy", DuoduoSession.getUserName());
		identifyParams.setDataBase(DBConst.Key_cust_DB);
		result = this.update(identifyParams);
		
		AppParam cardParams = new AppParam();
		cardParams.setService("custIdentifyCardService");
		cardParams.setMethod("update");
		cardParams.addAttr("customerId", customerId);
		cardParams.addAttr("status", 2);//不通过
		cardParams.addAttr("auditDesc", "更换手机号");
		cardParams.addAttr("auditBy", DuoduoSession.getUserName());
		SoaManager.getInstance().invoke(cardParams);
		
		// 更新用户缓存信息
		CustomerIdentify.refreshIdentifyById(customerId);
		return result;
	}
	
	/**
	 * 每日认证通过统计
	 * @param params
	 * @return
	 */
	public AppResult queryAuditByDay(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryAuditByDay", "queryAuditByDayCount");
	}
}
