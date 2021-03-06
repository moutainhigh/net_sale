package org.xxjr.sys.util;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.RandomUtils;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;

public class SmsConfigUtil {

	private static final String Key_configWebSms = "SMS_Config";

	/***
	 * 刷新web短信服务
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> refreshConfig() {
		AppParam param = new AppParam();
		param.setService("sysSmsService");
		param.setMethod("query");
		
		AppResult config  = null;
		//若没有相应的对象，使用远程调用 
		if (SpringAppContext.getBean("sysSmsService") == null) {
			config = RemoteInvoke.getInstance().call(param);
		}else{
			config = SoaManager.getInstance().invoke(param);
		}
		if (config.getRows().size() > 0) {
			RedisUtils.getRedisService().set(Key_configWebSms, (Serializable) config.getRows(), 60 * 60 * 12);
		}
		return config.getRows();
	}

	/***
	 * 获取web短信服务
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getConfig() {
		List<Map<String, Object>> list = (List<Map<String, Object>>) RedisUtils
				.getRedisService().get(Key_configWebSms);
		if (list == null) {
			list = refreshConfig();
		}
		return list;
	}
	
	/**
	 * http发送短信参数
	 * @param content
	 * @param phone
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getSMSInfo(String content, String phone) throws UnsupportedEncodingException{
		List<Map<String, Object>> list = SmsConfigUtil.getConfig();
		if (list == null) {
			LogerUtil.error(SmsConfigUtil.class,"not have SmsHttpSend Configurate.");
			return null;
		}
		int index = RandomUtils.nextInt(list.size());
		Map<String, Object> map = list.get(index);
		return StringUtil.getString(map.get("smsUrl"));
	}
}
