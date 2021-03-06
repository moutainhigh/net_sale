package org.xxjr.busi.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.ServiceKey;

/**
 *  能力相关配置
 * @author Administrator
 *
 */
public class AbilityCfgUtil {
	
	/** 能力等级 **/
	private static final String ABILITYGRADECFG = "ability_grade_cfg";
	
	/** 能力项 **/
	private static final String ABILITY_ITEM_CFG = "ability_item_cfg";

	/**
	 * 获取配置信息
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getAbilityGradeCfgInfo(){
		List<Map<String, Object>> abilityGradeCfgInfo = (List<Map<String, Object>>)RedisUtils.getRedisService().get(ABILITYGRADECFG);
		if (abilityGradeCfgInfo == null) {
			abilityGradeCfgInfo = refreshAbilityGradeCfg();
		}
		return abilityGradeCfgInfo;
	}
	 
	/**
	 * 刷新等级配置信息
	 * @return
	 */
	public static List<Map<String, Object>> refreshAbilityGradeCfg() {
		List<Map<String, Object>> abilityGradeCfgInfo = new ArrayList<Map<String,Object>>();
		AppParam param = new AppParam();
		param.setService("abilityGradeCfgService");
		param.setMethod("queryByPage");
		param.setOrderBy("gradeCode");
		param.setOrderValue("DESC");
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult result = new AppResult();
		if (SpringAppContext.getBean("abilityGradeCfgService") == null) {
			result = RemoteInvoke.getInstance().callNoTx(param);
		}else{
			result = SoaManager.getInstance().invoke(param);
		}
		if (result.getRows().size() > 0) {
			abilityGradeCfgInfo = result.getRows();
			RedisUtils.getRedisService().set(ABILITYGRADECFG, (Serializable)abilityGradeCfgInfo, 60 * 60 * 48);
		}
		return abilityGradeCfgInfo;
	}
		
	/**
	 * 根据gradeCode获取配置信息
	 * @return
	 */
	public static Map<String, Object> getAbilityGradeCfg(String gradeCode){
		Map<String,Object> map = new HashMap<String, Object>();
		if(StringUtils.isEmpty(gradeCode)){
			return map;
		}
		List<Map<String, Object>> templates = getAbilityGradeCfgInfo();
		for(Map<String, Object> row : templates){
			if(gradeCode.equals(row.get("gradeCode").toString())){
				return row;
			}
		}
		return map;
	}
	
	/**
	 * 删除缓存
	 * @param appVersion
	 */
	public static void delAbilityGradeCfg(Object gradeCode) {
		RedisUtils.getRedisService().del(ABILITYGRADECFG + gradeCode);
	}
	
	/**
	 * 获取能力值配置
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getAbilityItems(){
		List<Map<String, Object>> cfgInfo = (List<Map<String, Object>>) RedisUtils.getRedisService().get(ABILITY_ITEM_CFG);
		if (cfgInfo == null) {
			cfgInfo = refreshAbilityItems();
		}
		return cfgInfo;
	}
	 
	/**
	 * 刷新能力值配置
	 * @return
	 */
	public static List<Map<String, Object>> refreshAbilityItems() {
		List<Map<String, Object>> cfgInfo = new ArrayList<Map<String,Object>>();
		
		AppParam param = new AppParam("abilityItemService", "query");
		param.addAttr("enable", "1");
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		AppResult result = null;
		if (SpringAppContext.getBean("abilityItemService") == null) {
			result = RemoteInvoke.getInstance().call(param);
		}else{
			result = SoaManager.getInstance().invoke(param);
		}
		if (result.getRows().size() > 0) {
			cfgInfo = result.getRows();
			RedisUtils.getRedisService().set(ABILITY_ITEM_CFG, (Serializable)cfgInfo, 60 * 60 * 48);
		}
		return cfgInfo;
	}
	
	/**
	 * 获取能力项配置
	 * @param handleIndex
	 * @param itemType
	 * @return
	 */
	public static Map<String,Object> getConfigByitemType(String itemType){
		List<Map<String,Object>> configs = getAbilityItems();
		for(Map<String, Object> row : configs){
			if(itemType.equals(row.get("itemType").toString())){
				return row;
			}
		}
		return null;
	}
	
	/**
	 * 获取能力项配置
	 * @param itemType
	 * @param orderType
	 * @return
	 */
	public static Object getValByItemType(String itemType, String orderType){
		Map<String,Object> itemConfig = getConfigByitemType(itemType);
		if(itemConfig == null){
			return 0;
		}
		if(StringUtils.isEmpty(orderType) || "1".equals(orderType)){
			return itemConfig.get("rewardFirstVal");
		}
		return itemConfig.get("rewardNextVal");
	}
}

