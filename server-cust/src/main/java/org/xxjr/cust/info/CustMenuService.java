package org.xxjr.cust.info;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
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
public class CustMenuService extends BaseService {
	private static final String NAMESPACE = "CUSTMENU";

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
	 * 查询插入</br>
	 * 查询用户权限复制到其他用户权限
	 * @param params
	 * @return
	 */
	public AppResult insertCopyAuth(AppParam params) {
		
		int size = getDao().insert(NAMESPACE, "insertCopyAuth", params.getAttr(), params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return  result;
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
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.setDataBase(DBConst.Key_cust_DB);
				param.addAttr("menuId", id);
				
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
	 * 单笔删除
	 * @param params
	 * @return
	 */
	public AppResult deleteForOne(AppParam params){
		
		AppResult  result = null;
		
		if (!StringUtils.isEmpty(params.getAttr("customerId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		
		return result;
	}
	
	/***
	 * 获取用户菜单树数据
	 * @param params
	 * @return
	 */
	public AppResult queryTreeMenus(AppParam params){
		return super.query(params, NAMESPACE,"queryTreeMenus");
	}
	
	/***
	 * 获取用户系统菜单
	 * @param params
	 * @return
	 */
	public AppResult queryUserMenus(AppParam params){
		return super.query(params, NAMESPACE,"queryUserMenus");
	}
	
	/**
	 * 获取所有菜单列表
	 * @param params
	 * @return
	 */
	public AppResult queryMenus(AppParam params){
		return super.query(params, NAMESPACE,"queryMenus");
	}
	
	public AppResult editSysMenu (AppParam params) {
		int update = this.getDao().update(NAMESPACE, "editSysMenu", params.getAttr(), params.getDataBase());
		AppResult  result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, update);
		return result;
	}
	
}
