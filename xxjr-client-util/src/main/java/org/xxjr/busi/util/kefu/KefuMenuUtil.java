package org.xxjr.busi.util.kefu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.ServiceKey;

/***
 * 菜单工具类
 * @author sty
 *
 */
public class KefuMenuUtil {
	
	/**
	 * 获取角色所有菜单
	 * @return
	 */
	public static List<Map<String,Object>> getAllMenusByRole(int roleId){

		AppParam params = new AppParam();
		params.setService("sysKfMenuService");
		params.setMethod("queryAllMenusByRole");
		params.addAttr("roleId", roleId);
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult menuResult = RemoteInvoke.getInstance().callNoTx(params);

		List<Map<String,Object>> menuList=null;
 		List<Map<String,Object>> mainMenu =  new ArrayList<Map<String,Object>>();
		if (menuResult.getRows().size()>0) {
			menuList = menuResult.getRows();
			//转换成菜单树
			for(Map<String,Object> map:menuList){
				if(StringUtils.isEmpty(map.get("parentId"))){
					mainMenu.add(map);
				}
			}
			for(Map<String,Object> map:mainMenu){
				List<Map<String,Object>> subList = getChild(map.get("menuId").toString(), menuList);
				if(subList != null){
					map.put("subMenus",subList);
				}
			}
		}
	
		return mainMenu;
	}

	/**
	 * 递归查找子菜单
	 * 
	 * @param id
	 *            当前菜单id
	 * @param rootMenu
	 *            要查找的列表
	 * @return
	 */
	private static List<Map<String,Object>> getChild(String id, List<Map<String,Object>> rootMenu) {
		// 子菜单
		List<Map<String,Object>> childList = new ArrayList<>();
		for (Map<String,Object> map : rootMenu) {
			// 遍历所有节点，将父菜单id与传过来的id比较
			if (!StringUtils.isEmpty(map.get("parentId"))) {
				if (map.get("parentId").toString().equals(id)) {
					childList.add(map);
				}
			}
		}
		// 把子菜单的子菜单再循环一遍
		for (Map<String,Object> menu : childList) {
			// 递归
			List<Map<String,Object>> subList = getChild(menu.get("menuId").toString(), rootMenu);
			if(subList != null){
				menu.put("subMenus",subList);
			}
		} // 递归退出条件
		if (childList.size() == 0) {
			return null;
		}
		return childList;
	}
}
