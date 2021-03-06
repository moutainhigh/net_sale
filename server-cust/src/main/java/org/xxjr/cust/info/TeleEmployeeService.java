package org.xxjr.cust.info;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.ddq.active.mq.message.CustMessageSend;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;
import org.xxjr.sys.util.ValidUtils;

@Lazy
@Service
@Slf4j
public class TeleEmployeeService extends BaseService {
	private static final String NAMESPACE = "TELEEMPLOYEE";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
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
	 * insert
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		params.addAttr("createTime", new Date());
		return super.insert(params, NAMESPACE);
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
	 * 选择电销员工批量发送短信
	 * @param params
	 * @return
	 */
	public AppResult batchSend(AppParam params) {
		String empIds = (String) params.getAttr("empIds");
		if (!StringUtils.isEmpty(empIds)) {
			List<String> empIdList = new ArrayList<>();
			for (String empId : empIds.split(",")) {
				AppParam queryParam = new AppParam();
				queryParam.addAttr("empId", empId);
				queryParam.setDataBase(DBConst.Key_cust_DB);
				AppResult queryResult = this.query(queryParam);
				if(queryResult.getRows().size() > 0){
					Map<String, Object> teleEmpInfo = queryResult.getRow(0);
					String telephone = StringUtil.getString(teleEmpInfo.get("telephone"));
					// 通知电销员工
					sendTeleEmpSms(telephone, "teleEmpMsgNotice");
					// 电销员工id
					String empIdStr = StringUtil.getString(teleEmpInfo.get("empId"));
					empIdList.add(empIdStr);
				}
			}
			// 更新日期
			if (empIdList.size() > 0) {
				AppParam updateParam = new AppParam();
				updateParam.addAttr("empIdList", empIdList);
				updateParam.setDataBase(DBConst.Key_cust_DB);
				this.updateSendTime(updateParam);
			}
		}
		return new AppResult();
	}
	
	/**
	 * 给所有电销员工发送短信
	 * @param params
	 * @return
	 */
	public AppResult sendAll(AppParam params) {
		AppParam queryParam = new AppParam();
		queryParam.setDataBase(DBConst.Key_cust_DB);
		AppResult queryResult = this.query(queryParam);
		List<String> empIdList = new ArrayList<>();
		for (Map<String,Object> row : queryResult.getRows()) {
			String telephone = StringUtil.getString(row.get("telephone"));
			// 通知电销员工
			sendTeleEmpSms(telephone, "teleEmpMsgNotice");
			String empId = StringUtil.getString(row.get("empId"));
			empIdList.add(empId);
		}
		if (empIdList.size() > 0) {
			AppParam updateParam = new AppParam();
			updateParam.addAttr("empIdList", empIdList);
			updateParam.setDataBase(DBConst.Key_cust_DB);
			this.updateSendTime(updateParam);
		}
		return new AppResult();
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
				param.addAttr("empId", id);
				param.setDataBase(DBConst.Key_cust_DB);
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("empId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**
	 * 更新或插入
	 */
	public AppResult saveOrUpdate(AppParam params) {
		AppResult result = new AppResult();
		Object empName = params.getAttr("empName");
		Object telephone = params.getAttr("telephone");
		if (StringUtils.isEmpty(empName) || StringUtils.isEmpty(telephone)) {
			result.setMessage("姓名或手机号为空！");
			result.setSuccess(false);
			return result;
		}
		// 验证手机号
		if (!ValidUtils.validateTelephone(telephone.toString())) {
			result.setSuccess(false);
			result.setMessage("请输入正确的手机号码");
			return result;
		}
		result = this.update(params);
		Integer size = (Integer)result.getAttr(DuoduoConstant.DAO_Update_SIZE);
		if(size == null || size == 0){
			AppParam queryParam = new AppParam();
			queryParam.addAttr("telephone", telephone);
			queryParam.setDataBase(DBConst.Key_cust_DB);
			AppResult queryResult = this.query(queryParam);
			if (queryResult.getRows().size() > 0) {
				result.setMessage("手机号已存在！");
				result.setSuccess(false);
				return result;
			}
			result = this.insert(params);
		}
		return result;
	}
	
	
	/**
	 * 发送消息
	 * @param customerId
	 * @param telephone
	 * @param messageType
	 */
	private void sendTeleEmpSms(Object telephone, String messageType){
		// 发送消息
		Map<String,Object> msgParam = new HashMap<String, Object>();
		msgParam.put("telephone", telephone);
		try {
			CustMessageSend messageSend = SpringAppContext.getBean(CustMessageSend.class);
			messageSend.sendCustMessage(null, messageType, msgParam);
		} catch (Exception e) {
			log.error("send message error", e);
		}
	}
	
	/**
	 * 批量更新发送时间
	 * @param params
	 * @return
	 */
	public AppResult updateSendTime(AppParam params) {
		AppResult result = new AppResult();
		int size = this.getDao().update(NAMESPACE, "updateSendTime", params.getAttr(), params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Delete_SIZE, size);
		return result;
	}
}
