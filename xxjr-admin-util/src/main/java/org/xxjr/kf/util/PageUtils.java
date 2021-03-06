package org.xxjr.kf.util;

import java.io.Serializable;
import java.util.ArrayList;
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


public class PageUtils {
	public static final String BTN_CFG = "btn_cfg";
	public static final String PAGE_CFG = "page_cfg"; 
	/**
	 * 获取配置信息
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getBtnCfg(){
		List<Map<String, Object>> btnCfg = (List<Map<String, Object>>) RedisUtils.getRedisService().get(BTN_CFG);
		if (btnCfg == null) {
			btnCfg = refreshBtnCfg();
		}
		return btnCfg;
	}
	 
	/**
	 * 刷新按钮配置信息
	 * @return
	 */
	public static List<Map<String, Object>> refreshBtnCfg() {
		List<Map<String, Object>> btnCfg = new ArrayList<Map<String,Object>>();
		AppResult result = new AppResult();
		AppParam param = new AppParam("btnCfgService", "query");
		param.setOrderBy("btnId");
		param.setOrderValue("DESC");
		param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		result = RemoteInvoke.getInstance().callNoTx(param);
		if (SpringAppContext.getBean("btnCfgService") == null) {
			result = RemoteInvoke.getInstance().callNoTx(param);
		}else{
			result = SoaManager.getInstance().invoke(param);
		}
		if (result.getRows().size() > 0) {
			btnCfg = result.getRows();
			RedisUtils.getRedisService().set(BTN_CFG, (Serializable)btnCfg, 60 * 60 * 48);
		}
		return btnCfg;
	}

	
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getPageCfg(){
		List<Map<String, Object>> pageCfg = (List<Map<String, Object>>) RedisUtils.getRedisService().get(PAGE_CFG);
		if (pageCfg == null) {
			pageCfg = refreshPageCfg();
		}
		return pageCfg;
	}
	public static List<Map<String, Object>> refreshPageCfg() {
		List<Map<String, Object>> pageCfg = new ArrayList<Map<String,Object>>();
		AppResult result = new AppResult();
		AppParam param = new AppParam("pageCfgService", "query");
		param.setOrderBy("pageId");
		param.setOrderValue("DESC");
		param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		result = RemoteInvoke.getInstance().callNoTx(param);
		if (SpringAppContext.getBean("pageCfgService") == null) {
			result = RemoteInvoke.getInstance().callNoTx(param);
		}else{
			result = SoaManager.getInstance().invoke(param);
		}
		if (result.getRows().size() > 0) {
			pageCfg = result.getRows();
			RedisUtils.getRedisService().set(PAGE_CFG, (Serializable)pageCfg, 60 * 60 * 48);
		}
		return pageCfg;
		
	}
}
