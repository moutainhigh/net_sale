package org.xxjr.store.web.action.account.config;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.llw.model.cache.RedisUtils;
import org.ddq.common.core.service.RemoteInvoke;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.store.StoreRoleUtils;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.sys.util.ServiceKey;

/***
 * 菜单相关
 * @author sty
 *
 */
@Controller
@RequestMapping("/account/config/menu/")
public class StoreMenuAction {

	/***
	 * 查询菜单信息
	 * @param request
	 * @return
	 */
	@RequestMapping("queryMenuInfo")
	@ResponseBody
	public AppResult queryMenuInfo(HttpServletRequest request){
		AppResult result = new AppResult();
		String menuId = request.getParameter("menuId");

		if(StringUtils.isEmpty(menuId)){
			result.setMessage("menuId不能为空");
			result.setSuccess(false);
			return result;
		}

		try{
			AppParam params = new AppParam();
			params.addAttr("menuId", menuId);
			params.setService("sysStoreMenuService");
			params.setMethod("query");
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().callNoTx(params);
		}catch(Exception e){
			LogerUtil.error(StoreMenuAction.class,e, "queryMenuInfo error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}

		return result;
	}

	/***
	 * 查询所有菜单列表
	 * @param request
	 * @return
	 */
	@RequestMapping("queryMenuList")
	@ResponseBody
	public AppResult queryMenuList(HttpServletRequest request){
		AppResult result = new AppResult();
		try{
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sysStoreMenuService");
			params.setMethod("queryAllMenusTree");
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().callNoTx(params);
		}catch(Exception e){
			LogerUtil.error(StoreMenuAction.class,e, "queryMenuList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}


	/***
	 * 添加菜单
	 * @param request
	 * @return
	 */
	@RequestMapping("addMenu")
	@ResponseBody
	public AppResult addMenu(HttpServletRequest request){
		AppResult result = new AppResult();
		return result;
	}


	/***
	 * 更新菜单
	 * @param request
	 * @return
	 */
	@RequestMapping("update")
	@ResponseBody
	public AppResult update(HttpServletRequest request){
		AppResult result = new AppResult();
		String menuId = request.getParameter("menuId");

		if(StringUtils.isEmpty(menuId)){
			result.setMessage("menuId不能为空");
			result.setSuccess(false);
			return result;
		}
		try{
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.addAttr("menuId", menuId);
			params.setService("sysStoreMenuService");
			params.setMethod("update");
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(params);
			
			//删除角色缓存
			if(result.isSuccess()){
				List<Map<String,Object>> roleList =  StoreRoleUtils.getAllRoles();
				for(Map<String,Object> map:roleList){
					RedisUtils.getRedisService().del(StoreUserUtil.USER_TREE_MENUS_KEY + map.get("roleId"));
					RedisUtils.getRedisService().del(StoreUserUtil.USER_BASE_MENUS_KEY + map.get("roleId"));
					RedisUtils.getRedisService().del(StoreUserUtil.USER_CHECK_MENUS_KEY + map.get("roleId"));
					RedisUtils.getRedisService().del(StoreUserUtil.USER_MODIFY_MENUS_KEY + map.get("roleId"));
				}
			}
			
		}catch(Exception e){
			LogerUtil.error(StoreRoleAction.class,e, "update menu error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}

		return result;
	}

	/***
	 * 删除菜单
	 * @param request
	 * @return
	 */
	@RequestMapping("delete")
	@ResponseBody
	public AppResult delete(HttpServletRequest request){
		AppResult result = new AppResult();

		String menuId = request.getParameter("menuId");

		if(StringUtils.isEmpty(menuId)){
			result.setSuccess(false);
			result.setMessage("menuId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			params.addAttr("menuId", menuId);
			params.setService("sysStoreMenuService");
			params.setMethod("delete");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(params);	
			//删除角色缓存
			if(result.isSuccess()){
				List<Map<String,Object>> roleList =  StoreRoleUtils.getAllRoles();
				for(Map<String,Object> map:roleList){
					RedisUtils.getRedisService().del(StoreUserUtil.USER_TREE_MENUS_KEY + map.get("roleId"));
					RedisUtils.getRedisService().del(StoreUserUtil.USER_BASE_MENUS_KEY + map.get("roleId"));
					RedisUtils.getRedisService().del(StoreUserUtil.USER_CHECK_MENUS_KEY + map.get("roleId"));
					RedisUtils.getRedisService().del(StoreUserUtil.USER_MODIFY_MENUS_KEY + map.get("roleId"));
				}
			}

		} catch (Exception e) {
			LogerUtil.error(StoreRoleAction.class, e, " delete menu error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}




}
