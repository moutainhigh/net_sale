package org.xxjr.cust.store;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class SysRoleMenuService extends BaseService {
	private static final String NAMESPACE = "SYSROLEMENU";

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
		AppResult updateResult = new AppResult();
		String menuIds = (String) params.getAttr("menuIds");
		String roleId = (String) params.getAttr("roleId");
		
		//先删除权限
		int size = getDao().delete(NAMESPACE, "deleteRoleMenus", params.getAttr(),params.getDataBase());
		if(size >= 0){
			List<Map<String,Object>> mainList = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> subList = new ArrayList<Map<String,Object>>();
			
			//拆分菜单权限 和按钮权限
			for (String menuId : menuIds.split(",")) {
				if(menuId.contains("-")){
					String []subMenus = menuId.split("-");
					
					Map<String,Object> map = new HashMap<String, Object>();
					map.put("menuId",subMenus[0]);
					if("1".equals(subMenus[1])){
						map.put("queryAuth",1);
					}else if("2".equals(subMenus[1])){
						map.put("check",1);
					}else{
						map.put("modify",1);
					}
					subList.add(map);
					
				}else{
					Map<String,Object> map = new HashMap<String, Object>();
					map.put("menuId", menuId);
					map.put("roleId", roleId);
					map.put("queryAuth", 0);
					map.put("check", 0);
					map.put("modify", 0);
					mainList.add(map);
				}
			}
			
			
			for(int i=0;i<mainList.size();i++){
				for(int j=0;j<subList.size();j++){
					
					Map<String,Object> map = mainList.get(i);
					Map<String,Object> map1 = subList.get(j);
					
					if(map.get("menuId").equals(map1.get("menuId"))){
						if(map1.get("queryAuth") != null){
							 map.put("queryAuth",map1.get("queryAuth"));
						}else if(map1.get("check") != null){
							 map.put("check",map1.get("check"));
						}else{
							 map.put("modify",map1.get("modify"));
						}
					}
					
				}
			}
			
			//批量插入权限
			size = super.getDao().batchInsert(NAMESPACE, "batchInsertByRole",
					mainList, params.getDataBase());
			updateResult.putAttr(DuoduoConstant.DAO_Insert_SIZE, size);
		}
		
		return updateResult;
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
				param.addAttr("id", id);
				param.setDataBase(DBConst.Key_cust_DB);
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("id"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
}
