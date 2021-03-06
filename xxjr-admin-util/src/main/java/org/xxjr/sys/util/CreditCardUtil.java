package org.xxjr.sys.util;

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
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;

@SuppressWarnings("unchecked")
public class CreditCardUtil {

	/** 贷款产品标签 **/
	public static final String KEY_PROD_LABEL = "key_prod_label";
	
	public static final String Cache_Key = "CreditCard_Redis_Key";
	public static final String Count_Cache_Key = "CreditCard_Count_Redis_Key_";
	
	/** 信用卡所在银行缓存 **/
	public static final String NEW_Card_Bank_Cache_Key = "New_CardBank_Redis_Key";
	/** 筛选条件缓存 **/
	public static final String NEW_Card_Term_Cache_Key = "New_Card_Term_Redis_Key";
	
	public static final int Cache_times = 60*60*24*30;
	public static final int Count_Cache_times = 60*60*24;
	//合作信用卡银行
	public static final int cCard_type = 1;
	//信用贷款
	public static final int plat_type = 2;

	//保险机构
	public static final int insurance_type = 3;

	//抵押贷款
	public static final int pledge_type = 4;

	
	/***
	 * 分页查询卡列表
	 * @return
	 */
	public static AppResult getCardListByPage(AppParam params){
		AppResult result = new AppResult();
		
		params.setService("creditCardService");
		params.setMethod("queryByPage");
		params.addAttr("status", "1");
		params.setOrderBy("`index`");
		params.setOrderValue("asc");
		params.setRmiServiceName(AppProperties.
				getProperties(DuoduoConstant.RMI_SERVICE_START+"sys"));
		
		result = RemoteInvoke.getInstance().call(params);
		
		return result;
	}
	
	
	/***
	 * 申请总次数缓存
	 * @param params
	 * @return
	 */
	public static void setCardApplyCache(AppParam params){
		String recordId = StringUtil.objectToStr(params.getAttr("recordId"));
		if (StringUtils.isEmpty(recordId)) {
			recordId = StringUtil.objectToStr(params.getAttr("btnId"));
		}
		
		int oCount = NumberUtil.getInt(RedisUtils.getRedisService().get(Count_Cache_Key+recordId),0);
		int count = 1;
		if(oCount !=0){
			count = count +oCount;
		}
		
		RedisUtils.getRedisService().set(Count_Cache_Key+recordId,count, Count_Cache_times);
	}
	
	/**
	 * 获取贷款产品标签缓存
	 * @return
	 */
	public static List<Map<String, Object>> getProdLabels(){
		List<Map<String, Object>> prodLabelList = (List<Map<String, Object>>) RedisUtils.getRedisService().get(
				KEY_PROD_LABEL);
		if (prodLabelList == null) {
			prodLabelList = refreshProdLabels();
		}
		return prodLabelList;
	}
	
	/***
	 * 刷新贷款产品标签缓存
	 * @return
	 */
	public static List<Map<String,Object>> refreshProdLabels(){
		AppParam param = new AppParam();
		param.setService("prodLabelService");
		param.setMethod("query");
		param.addAttr("enable", "1");
		param.setOrderBy("indexNum");
		param.setOrderValue("asc");
		param.setRmiServiceName(AppProperties.
				getProperties(DuoduoConstant.RMI_SERVICE_START+"sys"));
		AppResult result = null;
		if(SpringAppContext.getBean("prodLabelService") == null){
			result = RemoteInvoke.getInstance().call(param);
		}else{
			result = SoaManager.getInstance().invoke(param);
		}
		List<Map<String,Object>> list = result.getRows();
		if(list.size() > 0 ){
			RedisUtils.getRedisService().set(KEY_PROD_LABEL, (Serializable)list, Cache_times);
		}
		return list;
	}

}
