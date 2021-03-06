package org.xxjr.store.web.action.account.config;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.llw.model.cache.RedisUtils;
import org.ddq.common.core.service.RemoteInvoke;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.busi.util.store.StoreMenuUtils;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.sys.util.ServiceKey;

/***
 * 角色相关
 * @author sty
 *
 */
@Controller
@RequestMapping("/account/config/role/")
public class StoreRoleAction {

	/***
	 * 查询角色列表
	 * @param request
	 * @return
	 */
	@RequestMapping("queryRoleList")
	@ResponseBody
	public AppResult queryRoleList(HttpServletRequest request){
		AppResult result = new AppResult();
		try{
			//获取所有角色
			AppParam params  = new AppParam();
			RequestUtil.setAttr(params, request);
			params.setService("sysRoleService");
			params.setMethod("queryByPage");
			params.setOrderBy("createTime");
			params.setOrderValue("desc");
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().callNoTx(params);
		}catch(Exception e){
			LogerUtil.error(StoreRoleAction.class, e, "queryRoleList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}	
		return result;
	}

	/***
	 * 查询角色信息
	 * @param request
	 * @return
	 */
	@RequestMapping("queryRoleInfo")
	@ResponseBody
	public AppResult queryRoleInfo(HttpServletRequest request){
		AppResult result = new AppResult();
		String roleId = request.getParameter("roleId");

		if(StringUtils.isEmpty(roleId)){
			result.setMessage("roleId不能为空");
			result.setSuccess(false);
			return result;
		}

		try{
			AppParam params = new AppParam();
			params.addAttr("roleId", roleId);
			params.setService("sysRoleService");
			params.setMethod("query");
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().callNoTx(params);
		}catch(Exception e){
			LogerUtil.error(StoreRoleAction.class,e, "queryRoleInfo error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}

		return result;
	}
	
	
	/***
	 * 添加角色信息
	 * @param request
	 * @return
	 */
	@RequestMapping("addRole")
	@ResponseBody
	public AppResult addRole(HttpServletRequest request){
		AppResult result = new AppResult();
		String roleName = request.getParameter("roleName");

		if(StringUtils.isEmpty(roleName)){
			result.setMessage("角色名称不能为空");
			result.setSuccess(false);
			return result;
		}
		try{
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.addAttr("roleName", roleName);
			params.setService("sysRoleService");
			params.setMethod("insert");
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(params);
			
		}catch(Exception e){
			LogerUtil.error(StoreRoleAction.class,e, "updateRole error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}

		return result;
	}

	/***
	 * 更新角色信息
	 * @param request
	 * @return
	 */
	@RequestMapping("updateRole")
	@ResponseBody
	public AppResult update(HttpServletRequest request){
		AppResult result = new AppResult();
		String roleId = request.getParameter("roleId");

		if(StringUtils.isEmpty(roleId)){
			result.setMessage("roleId不能为空");
			result.setSuccess(false);
			return result;
		}

		try{
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			params.addAttr("roleId", roleId);
			params.setService("sysRoleService");
			params.setMethod("update");
			params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(params);
			
		}catch(Exception e){
			LogerUtil.error(StoreRoleAction.class,e, "updateRole error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}

		return result;
	}

	/***
	 * 删除角色信息
	 * @param request
	 * @return
	 */
	@RequestMapping("delRole")
	@ResponseBody
	public AppResult delete(HttpServletRequest request){
		AppResult result = new AppResult();

		String roleId = request.getParameter("roleId");

		if(StringUtils.isEmpty(roleId)){
			result.setSuccess(false);
			result.setMessage("roleId不能为空");
			return result;
		}
		try {
			AppParam params = new AppParam();
			params.addAttr("roleId", roleId);
			params.setService("sysRoleService");
			params.setMethod("delete");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(params);	

		} catch (Exception e) {
			LogerUtil.error(StoreRoleAction.class, e, " delRole error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}



	/***
	 * 获取角色菜单列表
	 * @param request
	 * @return
	 */
	@RequestMapping("queryMemuList")
	@ResponseBody
	public AppResult queryMemuList(HttpServletRequest request){
		AppResult result = new AppResult();
		String roleId = request.getParameter("roleId");
		if(StringUtils.isEmpty(roleId)){
			result.setSuccess(false);
			result.setMessage("roleId不能为空");
			return result;
		}
		try {
			result.addRows(StoreMenuUtils.getAllMenusByRole(Integer.parseInt(roleId)));
		} catch (Exception e) {
			LogerUtil.error(StoreRoleAction.class, e, " queryMemuList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}

		return result;

	}


	/***
	 * 角色赋菜单权限
	 * @param request
	 * @return
	 */
	@RequestMapping("authRoleMemus")
	@ResponseBody
	public AppResult authRoleMemus(HttpServletRequest request){
		AppResult result = new AppResult();
		String menuIds = request.getParameter("menuIds");
		String roleId = request.getParameter("roleId");
		
		if(StringUtils.isEmpty(roleId) || StringUtils.isEmpty(menuIds)){
			result.setSuccess(false);
			result.setMessage("menuIds或roleId不能为空");
			return result;
		}
		try{
			AppParam params = new AppParam();
			params.addAttr("menuIds", menuIds);
			params.addAttr("roleId", roleId);
			params.setService("sysRoleMenuService");
			params.setMethod("update");
			params.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().callNoTx(params);
			
			if(result.isSuccess()){
				RedisUtils.getRedisService().del(StoreUserUtil.USER_TREE_MENUS_KEY+roleId);
				RedisUtils.getRedisService().del(StoreUserUtil.USER_BASE_MENUS_KEY+roleId);
				RedisUtils.getRedisService().del(StoreUserUtil.USER_CHECK_MENUS_KEY+roleId);
				RedisUtils.getRedisService().del(StoreUserUtil.USER_MODIFY_MENUS_KEY+roleId);
				//重新设置用户菜单
				String customerId = StoreUserUtil.getCustomerId(request);
				Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
				if(custInfo != null){
					roleId = StringUtil.getString(custInfo.get("roleType"));
					result.putAttr("menus", StoreUserUtil.getUserMenusTree(roleId));
					//用户审核权限
					result.putAttr("checkMenus", StoreUserUtil.getUserCheckUrls(roleId));
				}
				
			
			}
			
		}catch(Exception e){
			LogerUtil.error(StoreRoleAction.class, e, "authRoleMemus error");
		}
		return result;
	}
}
