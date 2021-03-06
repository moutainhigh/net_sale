package org.xxjr.sys.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.llw.model.cache.RedisUtils;

public class FriendlyLinkUtil {
	
	public static final String FRIENDLY_LINK = "friendly_link";
	
	
	/**
	 * 获取友情链接
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getFriendsLink(){
		List<Map<String, Object>> friendlyLinkInfo = (List<Map<String, Object>>) RedisUtils.getRedisService().get(
				FRIENDLY_LINK);
		if (friendlyLinkInfo == null) {
			friendlyLinkInfo = refreshFriendsLink();
		}
		return friendlyLinkInfo;
	}


	public static List<Map<String, Object>> refreshFriendsLink() {
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		AppParam param = new AppParam();
		param.setService("friendlyLinkService");
		param.setMethod("query");
		param.addAttr("enable", "1");
		param.setOrderBy("orderIndex");
		param.setOrderValue("asc");
		AppResult result = null;
		if (SpringAppContext.getBean("friendlyLinkService") == null) {
			result = RemoteInvoke.getInstance().call(param);
		}else{
			result = SoaManager.getInstance().invoke(param);
		}
		if (result.getRows().size() > 0) {
			list =  result.getRows();
			RedisUtils.getRedisService().set(FRIENDLY_LINK, (Serializable)list,60 * 60 * 48);
		}
		return list;
	}
}
