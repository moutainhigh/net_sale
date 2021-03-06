package org.xxjr.cust.info;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;

@Lazy
@Service
public class CustRightService extends BaseService {
	private static final String NAMESPACE = "CUSTRIGHT";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**查询需要统计的客服
	 * querySummary
	 * @param params
	 * @return
	 */
	public AppResult querySummary(AppParam params) {
		return super.query(params, NAMESPACE, "querySummary");
	}
	
	/**查客服，分单时用
	 * querySummary
	 * @param params
	 * @return
	 */
	public AppResult queryRealKf(AppParam params) {
		return super.query(params, NAMESPACE, "queryRealKf");
	}
	
	
	/**查分信贷经理的kf
	 * querySummary
	 * @param params
	 * @return
	 */
	public AppResult queryLendKf(AppParam params) {
		return super.query(params, NAMESPACE, "queryLendKf");
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
		params.addAttr("createBy", DuoduoSession.getUserName());
		return super.insert(params, NAMESPACE);
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
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
				param.setDataBase(DBConst.Key_cust_DB);
				param.addAttr("customerId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("customerId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	public AppResult authUser(AppParam params){
		AppResult result = new AppResult();
		
		//先删除被授权的信息
		result = this.delete(params);
		
		if(result.isSuccess()){
			
			//插入权限
			int aSize = getDao().insert(NAMESPACE, "insertCopyAuth", params.getAttr(), params.getDataBase());
			
			if(aSize == 1){
				
				params.setService("custMenuService");
				params.setMethod("deleteForOne");
				
				//删除menu权限
				result = SoaManager.getInstance().invoke(params);
				if(result.isSuccess()){
					params.setService("custMenuService");
					params.setMethod("insertCopyAuth");
					
					result = SoaManager.getInstance().invoke(params);
				}
				// copy 角色
				if(result.isSuccess()){
					params.setService("customerService");
					params.setMethod("copyRoleType");
					
					result = SoaManager.getInstance().invoke(params);
				}
				
			}else{
				result.setSuccess(false);
				result.setMessage("操作失败");
			}
			
		}else{
			result.setSuccess(false);
			result.setMessage("操作失败");
		}
		return result;
		
	}
	
	/**
	 * 变更指定渠道
	 * @param params
	 * @return
	 */
	public AppResult updateChannel(AppParam params) {
		AppResult result = new AppResult();
		int size = super.getDao().update(NAMESPACE, "updateChannel", params.getAttr(), params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
	
}
