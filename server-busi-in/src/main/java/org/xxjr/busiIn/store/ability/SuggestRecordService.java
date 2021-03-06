package org.xxjr.busiIn.store.ability;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.exception.SysException;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Lazy
@Service
public class SuggestRecordService extends BaseService {
	private static final String NAMESPACE = "SUGGESTRECORD";

	/**
	 * 查寻数据
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * 分页查寻数据
	 * @param params
	 * @return
	 */
	public AppResult queryByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE);
	}
	
	/**
	 * 查寻分页统计数据
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
	 * 添加数据处理
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		params.addAttr("createTime", new Date());
		params.addAttr("createBy", DuoduoSession.getUserName());
		return super.insert(params, NAMESPACE);
	}
	
	/**
	 * 修改数据处理
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		params.addAttr("serviceTime", new Date());
		String customerId = (String) params.getAttr("customerId");
		params.addAttr("serviceName", customerId);
		return super.update(params, NAMESPACE);
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
				param.addAttr("recordId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("recordId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**
	 * 保存投诉及建议信息
	 * @param params
	 * @return
	 */
	public AppResult save(AppParam params){
		Object customerId = null;
		Object applyId = null;
		Object type = params.getAttr("type");
		Object custName = params.getAttr("custName");
		Object custTelephone = params.getAttr("custTelephone");
		Object busiTelephone = params.getAttr("busiTelephone");
		//投诉需要校验参数
			if (StringUtils.isEmpty(custName) 
					|| StringUtils.isEmpty(custTelephone)) {
				throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
			}
		
		if ("2".equals(type)) {
			if (StringUtils.isEmpty(busiTelephone)) {
				throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
			}
			AppParam queryCustParams = new AppParam("borrowApplyService","query");
			queryCustParams.addAttr("telephone", custTelephone);
			AppResult queryCustResult = SoaManager.getInstance().invoke(queryCustParams);
			if (queryCustResult.getRows().size() == 0) {
				throw new SysException("您没有贷款记录不能进行投诉");
			}
			if(queryCustResult.getRows().size() > 0){
				applyId = queryCustResult.getRow(0).get("applyId");
				params.addAttr("applyId", applyId);
			}
		
			AppParam queryParams = new AppParam();
			queryParams.addAttr("telephone", busiTelephone);
			queryParams.setService("busiCustService");
			queryParams.setMethod("query");
			AppResult queryResult = SoaManager.getInstance().invoke(queryParams);
			if (queryResult.getRows().size() == 0) {
				throw new SysException("该业务经理不存在");
			}
			if (queryResult.getRows().size() > 0) {
				customerId = queryResult.getRow(0).get("customerId");
				params.addAttr("customerId", customerId);
			}
			
			AppParam param = new AppParam();
			param.addAttr("applyId", applyId);
			param.addAttr("telephone", custTelephone);
			param.addAttr("customerId", customerId);
			param.addAttr("type", type);
			AppResult existResult = super.query(param, NAMESPACE);
			if (existResult.getRows().size() > 0) {
				throw new SysException("您已投诉过该业务经理,投诉未处理完成,不能再次投诉");
			}
		}
		return this.insert(params);
	}
	
	
	/***
	 * 查询投诉建议列表
	 * @param params
	 * @return
	 */
	public AppResult querySuggestList(AppParam params){
		return super.queryByPage(params, NAMESPACE,"querySuggestList","querySuggestCount");
	}
}
