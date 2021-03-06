package org.xxjr.busi.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.llw.model.cache.RedisUtils;
import org.xxjr.sys.util.ServiceKey;

public class TeamConfigUtil {
	private final static String KEY_BORROW_TEAM = "key_borrow_team";
	
	/** 缓存7天*/
	public static final int base_cache_time = 60*60*24*7;
	
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getTeamAll () {
		
		List<Map<String, Object>> result = (List<Map<String, Object>>) RedisUtils.getRedisService().get(KEY_BORROW_TEAM);
		if (result == null ) {
			result = refreshBorrowTeam();
		}
		return result;
	}
	
	public static List<Map<String, Object>> refreshBorrowTeam () {
		AppParam param = new AppParam("borrowTeamService", "query");
		param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
		AppResult result = new AppResult();
		if (SpringAppContext.getBean("borrowTeamService") == null) {
			result = RemoteInvoke.getInstance().callNoTx(param);
		}else {
			result = SoaManager.getInstance().invoke(param);
		}
		if (result.getRows().size() > 0) {
			List<Map<String, Object>> rows = result.getRows();
			RedisUtils.getRedisService().set(KEY_BORROW_TEAM, (Serializable) rows,base_cache_time);
			return rows;
		}
		return null;
	}
}
