package org.xxjr.store.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.StoreConstant;
import org.xxjr.sys.util.NumberUtil;

/***
 * 集奥风控查询公共类
 * @author ZQH
 *
 */
public class GeoDealUtils {
	private static final String GEO_TOKEN_INFO="geo_token_info";
	private static final String USERNAME = "xfjr" ; // 用户名
	private static final String PASSWORD = "Xxjr@geo123"; //密码
	private static final String UNO = "200140"; //密码
	private static final String lOGIN_SERVER_URL = "http://yz.geotmtai.com/civp/getview/api/o/login";// 登录服务器地址
	private static final String MULTI_SERVER_URL = "http://yz.geotmtai.com/civp/getview/api/u/queryUnify";// 多头借贷服务器地址
	private static final String CD_NO = "450721198202286060"; //固定身份证
	private static final String QUERY_TYPE = "T40301";//查询接口的类型
	private static final int ENCRYPTED = 1; //是否加密 1 加密 0 不加密	
	private static final String CRYPTED_KEY = "xxjr123456@#xx#8";//解密密钥
	private static final int httpConnectTimeout = 10000;
	private static final int httpReadTimeout = 10000;
	
	/***
	 * 获取集奥风控数据
	 * @param params
	 */
	@SuppressWarnings("unchecked")
	public static AppResult getGeoData(AppParam params){
		AppResult result = new AppResult();
		String reqencoding = "UTF-8";
		String respencoding = "UTF-8";
		String requestMethod = "POST";
		String dealParam = getDealParam(params);
		if(!StringUtils.isEmpty(dealParam)){
			try{
				String returnStr = GeoHttpClient.getRs(MULTI_SERVER_URL,dealParam, reqencoding, respencoding, requestMethod, httpConnectTimeout, httpReadTimeout, null);
				if(!StringUtils.isEmpty(returnStr)){
					if(returnStr.startsWith("{")){
						LogerUtil.log("未加密:"+returnStr);
					}else{
						returnStr = decrypt(returnStr);
					}
					LogerUtil.log("return data:" + returnStr);
					Map<String,Object> returnMap = JsonUtil.getInstance().json2Object(returnStr,Map.class);
					String code = StringUtil.getString(returnMap.get("code"));
					if("-100".equals(code)||"-200".equals(code)||"-300".equals(code)||"-301".equals(code)){
						result.setSuccess(false);
						result.setErrorCode(code);
						result.putAttr("jsonText", returnStr);
						result.setMessage("tokenId is invalid");
						//清除tokenId缓存
						RedisUtils.getRedisService().del(GEO_TOKEN_INFO);
						LogerUtil.log("tokenId is invalid");
						return result;
					}else if("200".equals(code)){
						LogerUtil.log("获取成功");
						String  data = StringUtil.getString(returnMap.get("data"));
						//存在数据
						if(!StringUtils.isEmpty(data)){
							Map<String,Object> dataMap = JsonUtil.getInstance().json2Object(data, Map.class);
							if(dataMap != null){
								String rslStr = StringUtil.getString(dataMap.get("RSL"));
								if(!StringUtils.isEmpty(rslStr)){
									List<Map<String,Object>> rslList = JsonUtil.getInstance().json2Object(rslStr, List.class);
									if(rslList != null){
										Map<String,Object> rslMap = rslList.get(0);
										String rsStr = StringUtil.getString(rslMap.get("RS"));
										//获取是否命中
										if(!StringUtils.isEmpty(rsStr)){
											Map<String,Object> rsMap = JsonUtil.getInstance().json2Object(rsStr, Map.class);
											if(rsMap != null){
												String rsCode = StringUtil.getString(rsMap.get("code"));
												//未命中
												if(StoreConstant.GEO_RESPONSE_CODE_NO.equals(rsCode)){
													result.setMessage("未命中");
													result.putAttr("respcode", rsCode);
													result.putAttr("jsonText", returnStr);
													LogerUtil.log("未命中");
													return result;
												}else if(StoreConstant.GEO_RESPONSE_CODE_YES.equals(rsCode)){
													LogerUtil.log("命中多头借贷");
													result.putAttr("respcode", rsCode);
													result.setMessage("命中多头借贷");
													result.putAttr("jsonText", returnStr);
													String desc = StringUtil.getString(rsMap.get("desc"));
													LogerUtil.log("desc info:[" + desc+":end]");
													if(!StringUtils.isEmpty(desc)){
														Map<String,Object> descMap = JsonUtil.getInstance().json2Object(desc, Map.class);
														if(descMap != null){
															String tjxx180 = StringUtil.getString(descMap.get("TJXX_180d"));
															if(!StringUtils.isEmpty(tjxx180)){
																Map<String,Object> tjxx180Map = JsonUtil.getInstance().json2Object(tjxx180, Map.class);
																if(tjxx180Map != null){
																	int apptimes = NumberUtil.getInt(tjxx180Map.get("apptimes"),0);
																	result.putAttr("day180appTimes", apptimes);
																	LogerUtil.log("180天命中多头借贷次数:" + apptimes + "次");
																}
															}
															String tjxx90 = StringUtil.getString(descMap.get("TJXX_90d"));
															if(!StringUtils.isEmpty(tjxx90)){
																Map<String,Object> tjxx90Map = JsonUtil.getInstance().json2Object(tjxx90, Map.class);
																if(tjxx90Map != null){
																	int apptimes = NumberUtil.getInt(tjxx90Map.get("apptimes"),0);
																	result.putAttr("day90apptimes", apptimes);
																	LogerUtil.log("90天命中多头借贷次数:" + apptimes + "次");
																}
															}
															String tjxx60 = StringUtil.getString(descMap.get("TJXX_60d"));
															if(!StringUtils.isEmpty(tjxx60)){
																Map<String,Object> tjxx60Map = JsonUtil.getInstance().json2Object(tjxx60, Map.class);
																if(tjxx60Map != null){
																	int apptimes = NumberUtil.getInt(tjxx60Map.get("apptimes"),0);
																	result.putAttr("day60apptimes", apptimes);
																	LogerUtil.log("60天命中多头借贷次数:" + apptimes + "次");
																}
															}
														}
													}
													return result;
												}else{
													result.setSuccess(false);
													result.setErrorCode(rsCode);
													result.setMessage("未知状态" + returnMap.get("msg"));
													result.putAttr("jsonText", returnStr);
													LogerUtil.log("未知状态" + returnMap.get("msg"));
													return result;
												}
											}
										}
									}
								}
							}
						}
					}else{
						LogerUtil.log("获取信息失败 获取结果:" + returnMap.get("msg"));
						result.setErrorCode(StringUtil.getString(returnMap.get("code")));
						result.setMessage(StringUtil.getString(returnMap.get("msg")));
						result.setSuccess(false);
						result.putAttr("jsonText", returnStr);
						return result;
					}
				}else{
					result.setMessage("返回结果为空");
					result.setSuccess(false);
					LogerUtil.log("返回结果为空");
				}
			}catch(Exception e){
				LogerUtil.log("转换数据异常：" + e);
			}
		}else{
			result.setMessage("tokenId为空");
			result.setSuccess(false);
			return result;
		}
		return result;
	}
		
	/***
	 * 登录获取token信息
	 * @return
	 */
	private static String getToken(){
		String tokenId = (String) RedisUtils.getRedisService().get(GEO_TOKEN_INFO);
		if(StringUtils.isEmpty(tokenId)){
			String username = encrypt(USERNAME);
			String password = encrypt(PASSWORD);
			String params = "username="+username+"&password="+password+"&uno="+UNO+"&encrypted="+ENCRYPTED;
			String reqencoding = "UTF-8";
			String respencoding = "UTF-8";
			String requestMethod = "POST";
			Map<String,String> headerMap = null;
			try{
				String returnStr = GeoHttpClient.getRs(lOGIN_SERVER_URL, params, reqencoding, respencoding, requestMethod, httpConnectTimeout, httpReadTimeout, headerMap);
				if(returnStr!=null && !"".equals(returnStr)){
					if(returnStr.startsWith("{")){
						LogerUtil.log("未加密:"+returnStr);
					}else{
						returnStr = decrypt(returnStr);
					}
					LogerUtil.log("解密:"+returnStr);
	
					@SuppressWarnings("unchecked")
					Map<String,Object> map = JsonUtil.getInstance().json2Object(returnStr, Map.class);
					if("200".equals(StringUtil.getString(map.get("code")))){
						String geoTokenId = StringUtil.getString(map.get("tokenId")) ;
						if(!StringUtils.isEmpty(geoTokenId)){
							RedisUtils.getRedisService().set(GEO_TOKEN_INFO, geoTokenId, 24 * 60 * 60);
						}else{
							LogerUtil.log("获取tokenId失败!tokenId为空");
						}
						return geoTokenId;
					}else{
						LogerUtil.log("登录失败!code="+map.get("code"));
						return "";
					}
				}else{
					return "";
				}
			}catch(Exception e){
				LogerUtil.log("获取信息失败 获取结果:" + e);
			}
		}
		return tokenId;
	}
	
	/***
	 * 处理待发送的参数
	 * @param params
	 * @return
	 */
	private static String getDealParam(AppParam params){
		String applyId = StringUtil.getString(params.getAttr("applyId"));
		String telephone = StringUtil.getString(params.getAttr("telephone"));
		String applyName = StringUtil.getString(params.getAttr("applyName"));
		
		String tokenId = getToken();
		if(StringUtils.isEmpty(tokenId)){
			LogerUtil.log("tokenId is null.........");
			return "";
		}
		if(StringUtils.isEmpty(applyId)){
			LogerUtil.log("applyId is null.........");
			return "";
		}
		if(StringUtils.isEmpty(applyName) || StringUtils.isEmpty(telephone)){
			LogerUtil.log("applyName or telephone is null.........");
			return "";
		}
		// 进行数据查询
		StringBuilder strBuilder = new StringBuilder();
		Map<String,Object> sendParams = new HashMap<String, Object>();
		sendParams.put("innerIfType", QUERY_TYPE);
		sendParams.put("cid", telephone);
		sendParams.put("idNumber",CD_NO);
		sendParams.put("realName", applyName);
		sendParams.put("authCode", getAuthCode(applyId));
		sendParams.put("cycle", "360");
		
		Set<String> set = sendParams.keySet();
		for (String k : set) {
			String value = (String) sendParams.get(k);
			value = encrypt(value) ; // 加密
			strBuilder.append(k).append("=").append(value).append("&");
		}
		String sbStr = strBuilder.toString() + "tokenId="+tokenId;
		return sbStr;
	}
	
	/***
	 * 长度50位
	 * 组成 时间戳10位+用户信息32位（唯一）这里使用applyId拼接 + 连接用户编号8位 + 
	 * @param applyId
	 * @return
	 */
	private static String getAuthCode(String applyId){
		long nowTime = System.currentTimeMillis()/1000;
		String uuid = lpad(applyId,32,'0'); //生产应该使用applyId拼接成完整的32位
		return nowTime + "00" + UNO +uuid;
	}
	
	/***
	 * 补充长度
	 * @param str
	 * @param strLength
	 * @param chr
	 * @return
	 */
	private static String lpad(String str, int strLength, char chr) {
		int strLen = str.length();
		if (strLen < strLength) {
			while (strLen < strLength) {
				str = chr + str; // 左补0
				strLen = str.length();
			}
		}
		return str;
	}

	
	/***
	 * 加密
	 * @param text
	 * @param sys
	 * @return
	 */
	private static String encrypt(String text){
		text = AES2.encrypt(text, CRYPTED_KEY) ;  
		return text ;
	
	}
	
	/**
	 * 解密
	 * @param rs
	 * @return
	 */
	private static String decrypt(String rs){
		return AES2.decrypt(rs, CRYPTED_KEY) ;
	}
	
	public static void main2(String[] args) {
		//String tokenId = getToken();
		String applyId = "1111232323";
		applyId = getAuthCode(applyId);
		System.out.println(applyId);
		System.out.println(applyId.length());
	}
}
