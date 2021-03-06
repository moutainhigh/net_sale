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
public class CustIdentifyCardService extends BaseService {
	private static final String NAMESPACE = "CUSTIDENTIFYCARD";

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
	 * queryByPage
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE);
	}

	/**
	 * 
	 */
	public AppResult querySendList(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "querySendList", "querySendListCount");
	}
	
	/**
	 * querySendListCount
	 * 
	 * @param params
	 * @return
	 */
	public AppResult querySendListCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "querySendListCount", params.getAttr(),
				params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
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
	 * insert
	 * 
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		params.addAttr("createTime", new Date());
		AppResult result =  super.insert(params, NAMESPACE);
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
	 * 每日认证通过情况
	 * @param params
	 * @return
	 */
	public AppResult queryAuditByDay(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryAuditByDay", "queryAuditByDayCount");
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
			query.addAttr("customerId",id);
			this.manAuditPass(query);
		}
		return result;
	}

	/**
	 * 批量审核不通过的
	 * autoAudit
	 * @param params
	 * @return
	 */
	public AppResult auditAllNoPass(AppParam params) {
		String ids = (String) params.getAttr("ids");
		Object auditDesc=params.getAttr("auditDesc");
		Object auditLabel=params.getAttr("auditLabel");
		AppResult  result = new AppResult();
		if (StringUtils.isEmpty(ids)) {
			throw new SysException("没有选择数据！");
		} 
		for (String id : ids.split(",")) {
			AppParam query = new AppParam();
			query.addAttr("customerId",id);
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
		String oldStatus =  StringUtil.objectToStr(oldRecord.get("status"));
		String realName =  StringUtil.objectToStr(oldRecord.get("realName"));
		String identifyStatus =  StringUtil.objectToStr(oldRecord.get("identifyStatus"));
		//如果身份认证不通过，则工作认证不能审核通过
		if(!CustConstant.identify_status_1.equals(identifyStatus)){
			throw new SysException(oldRecord.get("realName")
					+ "工作认证失败，身份认证没有审核通过！");
		}
		
		// 手动认证，并且当前状态不为审核通过的
		if (CustConstant.identify_status_0.equals(oldStatus)
				|| CustConstant.identify_status_2.equals(oldStatus)) {
			AppParam update = new AppParam();
			
			if (params.getAttr("lenderType") != null) {
				update.addAttr("lenderType", params.getAttr("lenderType"));
			}
			update.addAttr("status", CustConstant.identify_status_1);
			
			update.addAttr("customerId", params.getAttr("customerId"));
			if (StringUtils.isEmpty(params.getAttr("company"))
					&& StringUtils.isEmpty(oldRecord.get("company"))) {
				throw new SysException(oldRecord.get("realName")
						+ "工作认证失败，没有公司名称!");
			}
			update.addAttr("auditDesc", params.getAttr("auditDesc"));
			update.addAttr("auditLabel", params.getAttr("auditLabel"));
			update.addAttr("auditBy", StringUtils.isEmpty(params.getAttr("auditBy")) ?
					DuoduoSession.getUserName() :params.getAttr("auditBy")
				);
			update.addAttr("auditTime", new Date());
			update.addAttr("fromStatus", oldStatus);
			update.setService("custIdentifyCardService");
			update.setMethod("update");
			result = SoaManager.getInstance().callNewTx(update);
			int size = (Integer) result.getAttr(DuoduoConstant.DAO_Update_SIZE);
			if (size == 1) {
				// 更新用户缓存信息
				CustomerIdentify.refreshIdentifyById(oldRecord
						.get("customerId").toString());
			} else {
				result.setSuccess(Boolean.FALSE);
				if(StringUtils.isEmpty(realName)){
					result.setMessage("工作认证失败，请联系相应管理员!");
				}else{
					result.setMessage("["+realName+"]工作认证失败，请联系相应管理员!");
				}
				
			}
			return result;
		}
		result.setSuccess(Boolean.FALSE);
		if(StringUtils.isEmpty(realName)){
			result.setMessage("工作认证失败，当前的状态不允许处理实名处理!");
		}else{
			result.setMessage("["+realName+"]工作认证失败，当前的状态不允许处理实名处理!");
		}
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
		String oldStatus = StringUtil.objectToStr(oldRecord.get("status"));
		String realName =  StringUtil.objectToStr(oldRecord.get("realName"));
		// 手动认证，并且当前状态不为审核通过的
		if (CustConstant.identify_status_0.equals(oldStatus)
				|| CustConstant.identify_status_2.equals(oldStatus)) {
			queryResult.putAttr("status", CustConstant.identify_status_2);
			AppParam update = new AppParam();
			update.addAttr("customerId", oldRecord.get("customerId"));
			update.addAttr("auditBy", StringUtils.isEmpty(params.getAttr("auditBy")) ?
					DuoduoSession.getUserName() :params.getAttr("auditBy")
				);
			update.addAttr("auditDesc", params.getAttr("auditDesc"));
			update.addAttr("fromStatus", oldStatus);
			update.addAttr("auditTime", new Date());
			update.addAttr("auditLabel", params.getAttr("auditLabel"));
			update.addAttr("status", CustConstant.identify_status_2);
			update.setDataBase(DBConst.Key_cust_DB);
			result = super.update(update, NAMESPACE);
			
			//消息推送
			try{
				Map<String, Object> paramsMap = new HashMap<String, Object>();
				
				//消息推送
				String auditTime = DateUtil.toStringByParttern((Date)update.getAttr("auditTime"), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS);
				paramsMap.put("realName", realName);
				paramsMap.put("auditTime", auditTime);
				paramsMap.put("auditDesc", params.getAttr("auditDesc"));
				String customerId = String.valueOf(oldRecord.get("customerId"));
				CustMessageSend messageSend = SpringAppContext.getBean(CustMessageSend.class);
				messageSend.sendCustMessage(customerId,  "workNotifyFailure", paramsMap);
			}catch(Exception e){
				log.error("failureManAuditNoPass", e);
			}
			
			return result;
		}
		result.setSuccess(Boolean.FALSE);
		if(StringUtils.isEmpty(realName)){
			result.setMessage("工作不通过处理失败，当前的状态不允许处理认证处理!");
		}else{
			result.setMessage("["+realName+"]工作不通过处理失败，当前的状态不允许处理认证处理!");
		}
		return result;
	}
	
	/**
	 * 取消认证
	 * @param param
	 * @return
	 */
	public AppResult cancelIdentify(AppParam params){
		Object customerId = params.getAttr("customerId");
		if (StringUtils.isEmpty(customerId)) {
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		AppResult result = new AppResult();
		AppParam query = new AppParam();
		query.addAttr("customerId", customerId);
		query.setDataBase(DBConst.Key_cust_DB);
		AppResult queryResult = this.query(query);
		if (queryResult.getRows().size() == 0) {
			throw new AppException(DuoduoError.UPDATE_DATA_IS_NOTEXISTS);
		}
		Map<String, Object> oldRecord = queryResult.getRow(0);
		String oldStatus = StringUtil.objectToStr(oldRecord.get("status"));
		if (!"1".equals(oldStatus)) {
			throw new SysException("该用户尚未通过工作认证，无需撤销认证！");
		}
		
		params.addAttr("status", "2");
		params.addAttr("fromStatus", "1");
		params.addAttr("auditDesc", "撤销认证");
		params.addAttr("auditBy", DuoduoSession.getUserName());
		params.addAttr("auditTime", new Date());
		params.setDataBase(DBConst.Key_cust_DB);
		result = this.update(params);
		
		int size = (Integer) result.getAttr(DuoduoConstant.DAO_Update_SIZE);
		if (size == 1) {
			// 更新用户缓存信息
			CustomerIdentify.refreshIdentifyById(oldRecord.get("customerId").toString());
		}
		return result;
	}
	
	/**
	 * 编辑工作信息
	 * @param params
	 * @return
	 */
	public AppResult editWordInfo(AppParam params){
		AppResult result = new AppResult();
		Object customerId = params.getAttr("customerId");
		if (StringUtils.isEmpty(customerId)) {
			result.setMessage("缺少必要信息");
			result.setSuccess(false);
			return result;
		}
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId.toString());
		Object cardStatus = custInfo.get("cardStatus");
		if (cardStatus == null || !"1".equals(cardStatus.toString())) {
			result.setSuccess(false);
			result.setMessage("用户未通过工作认证，不能编辑");
			return result;
		}
		params.setDataBase(DBConst.Key_cust_DB);
		this.update(params);
		
		// 更新用户缓存信息
		CustomerIdentify.refreshIdentifyById(customerId.toString());
		return result;
	}
}