package org.xxjr.busi.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.security.md5.Md5;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.push.PushPlatformUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShanDaiApplyUtil {
	
	private static Map<String, ThirdSaleUtil> pushList;
	
	static{
		pushList = new HashMap<String, ThirdSaleUtil>();
		pushList.put("saleShandai", (orderId,applyName,tlephone, row) -> {
			if (!StringUtils.isEmpty(orderId)) {
				String url = SysParamsUtil.getStringParamByKey("shandai_sale_third_url", "http://sd.bjgit.com/home/memberApi/GetPhone");
				JSONObject context = new JSONObject();
				context.put("token", SysParamsUtil.getStringParamByKey("shandai_sale_third_token", "d3e79bfb6f8f17d4e0176904160fdb27"));
				context.put("media", SysParamsUtil.getStringParamByKey("shandai_sale_third_media", "小小金融脱敏"));
				JSONObject data = new JSONObject();
				data.put("orderid", orderId.split("_")[1]);
				context.put("data", data);
				Map<String, Object> resMap = null;
				try {
					resMap = PushPlatformUtils.httpPost(url, null, context.toJSONString(), true, null);
				} catch (Exception e) {
					log.error("shanDai error", e);
				}
				if ("1".equals(StringUtil.objectToStr(resMap.get("code")))) {
					return StringUtil.objectToStr(resMap.get("phone"));
				}
			}
			return null;
		});
		
		pushList.put("saleLieBao", (orderId,applyName,tlephone, row) -> {
			if (!StringUtils.isEmpty(orderId)) {
				String url = SysParamsUtil.getStringParamByKey("liebao_sale_third_url", "https://obapi.mianbaojr.cn/callback/xiaoxiaojinrong/phone");
				Map<String, String> context = new HashMap<String, String>();
				String source = SysParamsUtil.getStringParamByKey("liebao_sale_third_source", "xiaoxiaojinrong");
				String timestamp = StringUtil.getString(System.currentTimeMillis());
				timestamp = timestamp.substring(0, (timestamp.length()-3));
				context.put("timestamp", timestamp);
				context.put("token", Md5.getInstance().encrypt(source + "_" + orderId + "_" + timestamp));
				context.put("source", source);
				context.put("orderId", orderId);
				Map<String, Object> resMap = null;
				try {
					resMap = PushPlatformUtils.httpPost(url, context, true);
				} catch (Exception e) {
					log.error("liebao error", e);
				}
				if (resMap != null && "0".equals(StringUtil.objectToStr(resMap.get("errCode")))) {
					@SuppressWarnings("unchecked")
					Map<String, Object> data = (Map<String, Object>) resMap.get("data");
					if (data != null && !data.isEmpty()) {
						return StringUtil.objectToStr(data.get("mobile"));
					}
				}
			}
			return null;
			}
		);
		
		
		pushList.put("salesy", (orderId,applyName,tlephone, row) -> {
			if (!StringUtils.isEmpty(orderId)) {
				String url = SysParamsUtil.getStringParamByKey("sy_sale_third_url", "https://d.yoojum.com/api/xiaoxiaojinrong/showphone.aspx");
				String key = SysParamsUtil.getStringParamByKey("sy_sale_third_key", "yH6tZhT3DsFG7Lj1");
				String uid = SysParamsUtil.getStringParamByKey("sy_sale_third_uid", "sy_xxjr.com");

				Map<String, String> context = new HashMap<String, String>();
				String timestamp = StringUtil.getString(System.currentTimeMillis());
				timestamp = timestamp.substring(0, (timestamp.length()-3));
				
				context.put("timestamp", timestamp);
				context.put("sign", Md5.getInstance().encrypt(uid  + orderId + timestamp + key));
				context.put("orderId", orderId);
				context.put("uid", uid);
				Map<String, Object> resMap = null;
				try {
					resMap = PushPlatformUtils.httpPost(url, context, true);
					if (resMap != null && "1".equals(StringUtil.getString(resMap.get("code")))) {
						@SuppressWarnings("unchecked")
						Map<String, Object> data = (Map<String, Object>) resMap.get("data");
						if (data != null && !data.isEmpty()) {
							return StringUtil.objectToStr(data.get("phone"));
						}
					}
				} catch (Exception e) {
					log.error("sy error", e);
				}
			}
			return null;
			}
		);
		
		pushList.put("sudaizjSale", (orderId,applyName,hideTel, row) -> {
			if (!StringUtils.isEmpty(orderId)) {
				String url = SysParamsUtil.getStringParamByKey("sudaizj_sale_third_url", "http://api.1sudai.com/api/v1/partner/callback/mobile");
				String time = DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERNYYYYMMDDHHMMSSSSS);
				String sign = hideTel + "_" + time + "_" + applyName + "_" + orderId;
				
				Map<String, String> content = new HashMap<String, String>();
				content.put("time", time);
				content.put("sign", Md5.getInstance().encrypt(sign));
				content.put("orderId", orderId);
				content.put("telephone", hideTel);
				content.put("applyName", applyName);
				Map<String, Object> resMap = null;
				try {
					resMap = PushPlatformUtils.httpPost(url, content, true);
					if (resMap != null && NumberUtil.getInt(resMap.get("error_code"),-1) == 0) {
						@SuppressWarnings("unchecked")
						Map<String, Object> data = (Map<String, Object>) resMap.get("data");
						if (data != null && !data.isEmpty()) {
							return StringUtil.objectToStr(data.get("telephone"));
						}
					}
				} catch (Exception e) {
					log.error("sudaizj error", e);
				}
			}
			return null;
			}
		);
		
		pushList.put("salemysq", (orderId,applyName,hideTel, row) -> {
			if (!StringUtils.isEmpty(orderId)) {
				String url = SysParamsUtil.getStringParamByKey("mysq_sale_third_url", "http://loannew.xiangrongdai.com/api/get-info");
				JSONObject context = new JSONObject();
				String key = SysParamsUtil.getStringParamByKey("mysq_sale_third_key", "^&*YIUHKJHL");
				String code = SysParamsUtil.getStringParamByKey("mysq_sale_third_code", "xxjrxd");
				int timestamp = (int) System.currentTimeMillis();
				context.put("code", code);
				context.put("ts", timestamp);
				context.put("orderid", orderId);
				context.put("sn", Md5.getInstance().encrypt(key + "_" + orderId + "_" + timestamp));
				
				if (RedisUtils.getRedisService().get("third_sale_start_price") != null) {
					context.put("sucPrice", row.get("sucPrice"));
				}
				
				Map<String, Object> resMap = null;
				try {
					resMap = PushPlatformUtils.httpPost(url, null, context.toJSONString(), true, null);
				} catch (Exception e) {
					log.error("mysq error", e);
				}
				if (resMap != null && "1".equals(StringUtil.objectToStr(resMap.get("code")))) {
					@SuppressWarnings("unchecked")
					Map<String, Object> data = (Map<String, Object>) resMap.get("data");
					if (data != null && !data.isEmpty()) {
						return StringUtil.objectToStr(data.get("mobile"));
					}
				}
			}
			return null;
			}
		);
	}
	
	
	
	public static Map<String, Object> queryApplyId(String telephone,String cityName){
		if (!StringUtils.isEmpty(telephone) && !StringUtils.isEmpty(cityName)) {
			telephone = telephone.replace('*', '_');
			AppParam queryParam = new AppParam("thirdDataService", "queryApplyId");
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			queryParam.addAttr("telephone", telephone);
			queryParam.addAttr("cityName", cityName);
			AppResult result = null;
			if (SpringAppContext.getBean("thirdDataService") == null) {
				result = RemoteInvoke.getInstance().callNoTx(queryParam); 
			}else {
				result = SoaManager.getInstance().callNoTx(queryParam);
			}
			if (result != null && result.getRows().size() > 0) {
				return result.getRow(0);
			}
		}
		return new HashMap<String, Object>();
	}
	

	public static String getTel (String orderId,String channelCode, Map<String, Object> info) {
		String telephone = null;
		ThirdSaleUtil exec = pushList.get(channelCode);
		if (exec != null) {
			try {
				telephone = exec.getTelByOrderId(orderId, StringUtil.objectToStr(info.get("applyName")), StringUtil.objectToStr(info.get("telephone")), info);
			} catch (Exception e) {
				log.error("getTelByOrderId error" , e);
			}
		}
		return telephone;
	}
	
	public static void main(String[] args) {
		System.err.println(getTel("salemysq_877616403808", "salemysq", new HashMap<String, Object>()));	
	}
	
}
