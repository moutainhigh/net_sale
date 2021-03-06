package org.xxjr.busi.util.kefu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.xxjr.sys.util.ServiceKey;

public class KefuRoleUtil {
	/** 客服平台角色缓存key  **/
	public static final String KEFU_ROLE_KEY="kefu_roleKey_";
	
	/**
	 * 获取角色列表
	 * @return
	 */
	public static List<Map<String,Object>> getAllRoles(){
		AppParam params  = new AppParam();
		params.setService("sysKfRoleService");
		params.setMethod("query");
		params.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult roleResult = RemoteInvoke.getInstance().callNoTx(params);
		
		List<Map<String,Object>> roleList =  new ArrayList<Map<String,Object>>();
		if(roleResult.getRows().size()>0){
			roleList =  (List<Map<String, Object>>) roleResult.getRows();
		}
		return roleList;
	}
}
