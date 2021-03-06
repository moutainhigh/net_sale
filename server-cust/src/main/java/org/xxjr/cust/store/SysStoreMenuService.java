package org.xxjr.cust.store;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.springframework.context.annotation.Lazy;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;

@Lazy
@Service
public class SysStoreMenuService extends BaseService {
	private static final String NAMESPACE = "SYSSTOREMENU";

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
				param.addAttr("menuId", id);
				param.setDataBase(DBConst.Key_cust_DB);
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("menuId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/***
	 * 查询角色所有菜单
	 * @param params
	 * @return
	 */
	public AppResult queryAllMenusByRole(AppParam params){
		AppResult result = new AppResult();
		result = this.query(params, NAMESPACE,"queryAllMenusByRole");
		return result;
		
	}
	
	/***
	 * 查询所有菜单
	 * @param params
	 * @return
	 */
	public AppResult queryAllMenusTree(AppParam params){
		AppResult result = new AppResult();
		result = super.queryByPage(params, NAMESPACE,"queryAllMenusTree","queryAllMenusCount");
		return result;
		
	}
	
	/***
	 * 查询按钮URL权限
	 * @param params
	 * @return
	 */
	public AppResult queryAllButtonMenus(AppParam params){
		AppResult result = new AppResult();
		result = this.query(params, NAMESPACE,"queryAllButtonMenus");
		return result;
		
	}
	
	/***
	 * 查询用户菜单
	 * @param params
	 * @return
	 */
	public AppResult queryUserMenusTree(AppParam params){
		AppResult result = new AppResult();
		result = this.query(params, NAMESPACE,"queryUserMenusTree");
		return result;
		
	}
	
	/***
	 * 查询用户URL权限
	 * @param params
	 * @return
	 */
	public AppResult queryUserMenuUrls(AppParam params){
		AppResult result = new AppResult();
		result = this.query(params, NAMESPACE,"queryUserMenuUrls");
		return result;
		
	}
	
	/***
	 * 查询用户审核URL权限
	 * @param params
	 * @return
	 */
	public AppResult queryUserCheckUrls(AppParam params){
		AppResult result = new AppResult();
		result = this.query(params, NAMESPACE,"queryUserCheckUrls");
		return result;
		
	}
	
	/***
	 * 查询用户修改URL权限
	 * @param params
	 * @return
	 */
	public AppResult queryUserModifyUrls(AppParam params){
		AppResult result = new AppResult();
		result = this.query(params, NAMESPACE,"queryUserModifyUrls");
		return result;
		
	}
}
