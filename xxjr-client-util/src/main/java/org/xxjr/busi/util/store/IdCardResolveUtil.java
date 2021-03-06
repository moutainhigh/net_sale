package org.xxjr.busi.util.store;

import java.util.Date;

import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.springframework.util.StringUtils;

/**
 * 身份证解析
 *
 */
public class IdCardResolveUtil {
	
	/**
	 * 根据身份证返回出生日期
	 * @param idCard
	 * @return
	 * 如:2017-11-13
	 * null 为失败
	 */
	public static String getBirthday (String idCard) {
		try {
			if (!StringUtils.isEmpty(idCard)&&idCard.length() == 18) {
				String year = null;
				String month = null;
				String date = null;
				year = idCard.substring(6, 10);
 	    		month = idCard.substring(10, 12);
 	    		date = idCard.substring(12, 14);
				return year + "-" + month + "-" + date;
			}
		} catch (Exception e) {
			LogerUtil.error(IdCardResolveUtil.class, e, "getBirthday error");
		}
		return null;
	}
	
	/**
	 * 根据身份证获取年龄
	 * @param idCard
	 * @return
	 * -1 为失败
	 */
	public static Integer getAge (String idCard) {
		try {
			if (!StringUtils.isEmpty(idCard)&&idCard.length() == 18) {
				String year = null;
				year = idCard.substring(6, 10);
				Date now = new Date();
				String nowStr = DateUtil.toStringByParttern(now, DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMM);
				String[] split = nowStr.split("-");
				Integer age = Integer.valueOf(split[0]) - Integer.valueOf(year);
				return age;
			}
		} catch (Exception e) {
			LogerUtil.error(IdCardResolveUtil.class, e, "getAge error");
		}
		return -1;
	}
	
	/**
	 * 根据身份证获取性别
	 * @param idCard
	 * @return
	 * -1 失败
	 * 1 男
	 * 2 女
	 */
	public static Integer getSex (String idCard) {
		try {
			if (!StringUtils.isEmpty(idCard)&&idCard.length() == 18) {
				return Integer.valueOf(idCard.substring(16, 17)) % 2 == 0 ? 2 : 1;
			}
		} catch (Exception e) {
			LogerUtil.error(IdCardResolveUtil.class, e, "getSex error");
		}
		return -1;
	}
}
