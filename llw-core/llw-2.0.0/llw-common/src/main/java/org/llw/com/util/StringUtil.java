package org.llw.com.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.UUID;

import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StringUtil {
	
	
	/***
	 * 判断str是否为空，为空或 'null'返回 空串
	 *  否则返回数据并trim
	 * @param str
	 * @return
	 */
	public static String getString(String str)  {
		if (str == null || "null".equals(str)) {
			return "";
		}
		return str.trim();
	}
	
	/***
	 * 判断str是否为空，为空或 'null'返回 空串
	 * 否则返回数据并trim
	 * @param str
	 * @return
	 */
	public static String getString(Object str)  {
		if (str == null || "null".equals(str.toString())) {
			return "";
		}
		return str.toString().trim();
	}
	
	
	
	/****
	 * 用户名号码隐藏
	 * @param userName
	 * @return
	 */
	public static String getHideUserName(String userName) {
		if (StringUtils.isEmpty(userName)) {
			return "";
		}
		return userName.substring(0, 1) + "***";

	}

	/****
	 * 姓名隐藏
	 * @param userName
	 * @return
	 */
	public static String getHideRealname(String realname) {
		if (StringUtils.isEmpty(realname)) {
			return "";
		}
		return "*" + realname.substring(1,realname.length()) ;

	}
	
	/****
	 * 获取姓氏
	 * @param realname
	 * @return
	 */
	public static String getFirstName(String realname) {
		if (StringUtils.isEmpty(realname)) {
			return "";
		}
		return realname.substring(0, 1) ;

	}
	
	/****
	 * 手机号码隐藏
	 * 
	 * @param phone
	 * @return
	 */
	public static String getHideTelphone(String phone) {
		if (StringUtils.isEmpty(phone) || phone.trim().length() != 11) {
			return "";
		}
		return phone.substring(0, 4) + "*****" + phone.substring(9);
	}
	/****
	 * 手机号码隐藏后六位
	 * 
	 * @param phone
	 * @return
	 */
	public static String getHideSixTel(String phone) {
		if (StringUtils.isEmpty(phone) || phone.trim().length() != 11) {
			return "";
		}
		return phone.substring(0, 5) + "******";
	}

	/****
	 * 银行号码隐藏
	 * 
	 * @param cardNo
	 * @return
	 */
	public static String getHideBankCard(String cardNo) {
		if (StringUtils.isEmpty(cardNo) || cardNo.length() < 6) {
			return cardNo;
		}
		return cardNo.substring(cardNo.length() - 4);
	}
	
	/****
	 * 银行号码隐藏
	 * 
	 * @param cardNo
	 * @return
	 */
	public static String getHideBankCard4(String cardNo) {
		if (StringUtils.isEmpty(cardNo) || cardNo.length() < 8) {
			return cardNo;
		}
		return "尾号"
				+ cardNo.substring(cardNo.length() - 4);
	}
	
	/**
	 * 邮箱地址隐藏
	 * @param email
	 * @return
	 */
	public static String getHideEmail(String email){
		if(StringUtils.isEmpty(email)){
			return "";
		}
		int index = email.indexOf("@");
		if(index<1){
			return email;
		}
		if(email.substring(0, index).length() <= 6){
			return email.substring(0, 1) + "***" + email.substring(index);
		}else{
			return email.substring(0, 3) + "***" + email.substring(index);
		}
	}
	
		
	/****
	 * 身份证号码隐藏
	 * 
	 * @param cardNo
	 * @return
	 */
	public static String getHideIdentify(String cardNo) {
		if (StringUtils.isEmpty(cardNo) || cardNo.length() != 18) {
			return "";
		}
		return cardNo.substring(0, 3) + "***" + cardNo.substring(8, 14)
				+ "****";
	}
	
	/**
	 * 处理空对象
	 * @param obj
	 * @return
	 */
	public static String objectToStr(Object obj){
		return null == obj ? "" : obj.toString(); 
	}
	
	/**
	 * decode 字符串
	 * @param str
	 * @return
	 */
	public static String decodeStr(Object str){
		if(!org.springframework.util.StringUtils.isEmpty(str)){
			 try {
				return URLDecoder.decode(str.toString().replaceAll("%(?![0-9a-fA-F]{2})", "%25"), "utf-8");
			} catch (UnsupportedEncodingException e) {
				log.error( "decodeStr",e);
			}
		}
		return null;
	}
	
	/**
	 * encode 字符串
	 * @param str
	 * @return
	 */
	public static String encodeStr(Object str){
		if(!org.springframework.util.StringUtils.isEmpty(str)){
			 try {
				return URLEncoder.encode(str.toString(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				log.error("encodeStr",e);
			}
		}
		return null;
	}
	
    /**
     * html转义
     * @param htmlStr
     * @return
     */
    public static String escapeHtml(Object htmlStr){
    	if(org.springframework.util.StringUtils.isEmpty(htmlStr)){
    		return null;
    	}
    	return htmlStr.toString().replaceAll("\"", "&quot;")
    				.replaceAll("'", "&apos;")
    				.replaceAll("<", "&lt;")
    				.replaceAll(">", "&gt;");
    }

	/**
	 * 处理数字存在,的处理
	 * @param obj
	 * @return
	 */
	public static BigDecimal getFormatAmount(Object obj){
		return null == obj ? null: new BigDecimal(obj.toString().replace(",", "").toString()); 
	}
	
	/***
	 * Base64 加密码
	 * @param str
	 * @return
	 */
    public static String encodeBase64(String str) {  
        byte[] b = null;  
        String s = null;  
        try {  
            b = str.getBytes("utf-8");  
        } catch (Exception e) {
        	log.error("encodeBase64",e);
        }  
        if (b != null) {  
            s = new String(Base64.getEncoder().encode(b));
        }  
        return s;  
    }  
  
    /***
	 * Base64 解密
	 * @param str
	 * @return
	 */
    public static String decoderBase64(String s) {  
        byte[] b = null;  
        String result = null;  
        if (s != null) {    
            try {  
                b = Base64.getDecoder().decode(s.getBytes());  
                result = new String(b, "utf-8");  
            } catch (Exception e) {  
            	log.error("decoderBase64",e);
            }  
        }  
        return result;  
    }  

	/***
	 * encode数据
	 * 
	 * @param s
	 * @return
	 */
	public static String URLEncoder(Object s) {
		String news = "";
		if (s != null) {
			try {
				news = java.net.URLEncoder.encode(s.toString(), "utf-8");
			} catch (Exception e) {
				log.error("URLEncoder",e);
			}
		}
		return news;
	}  
       
	/***
	 * decode数据
	 * 
	 * @param s
	 * @return
	 */
	public static String URLDecoder(Object s) {
		String news = "";
		if (s != null) {
			try {
				news = java.net.URLDecoder.decode(s.toString(), "utf-8");
			} catch (Exception e) {
				log.error("URLDecoder",e);
			}
		}
		return news;
	}

	/***
	 * 判断str是否为空，为空或 'null'返回 空串
	 * 否则返回数据并trim
	 * @param str
	 * @return
	 */
	public static String deCodee(Object str)  {
		if (str == null || "null".equals(str.toString())) {
			return "";
		}
		return str.toString().trim();
	}
	
	/***
	 * from ISO-8859-1 to UTF-8
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String converCodeToUTF8(String str)  {
		if(str == null){
			return null;
		}
	
		try {
			return new String(str.getBytes("iso-8859-1"),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("converCodeToUTF8",e);
		}
		return str;
	}
	
	/***
	 * 
	 * @param str
	 * @return
	 */
	public static String getBytes(byte[] strs)  {
		if (strs == null) {
			return null;
		}
		String str="";
		for(byte bb:strs){
			str+= bb +":";
		}
		return str;
	}
	/***
	 * from UTF-8 to GBK
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String converCodeToGBK(String str)  {
		if(str == null){
			return null;
		}
		try {
			return new String(str.getBytes("iso-8859-1"),"GBK");
		} catch (UnsupportedEncodingException e) {
			log.error("converCodeToGBK",e);
		}
		return str;
	}
	private static java.text.DecimalFormat myFormatter = new java.text.DecimalFormat("###,###,###,###,###,###,###,###,###,###.##");
	
	/***
	 * from UTF-8 to GBK
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String formatValue(String str)  {
		try{
			return myFormatter.format(new BigDecimal(str));
		}catch(Exception e){
			log.error("formatValue",e);
		}
		return "";
	}
	
	/** *//** 
     * 由于String.subString对汉字处理存在问题（把一个汉字视为一个字节)，因此在 
     * 包含汉字的字符串时存在隐患，现调整如下: 
     * @param src 要截取的字符串 
     * @param start_idx 开始坐标（包括该坐标) 
     * @param end_idx   截止坐标（包括该坐标） 
     * @return 
     */  
    public static String substring(String src, int start_idx, int end_idx){  
        byte[] b = src.getBytes();  
        String tgt = "";  
        for(int i=start_idx; i<=end_idx; i++){  
            tgt +=(char)b[i];  
        }  
        return tgt;  
    }  
    
    /**获取UUID随机字符串**/
	public static String getUUID(){
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
	
	private static java.text.DecimalFormat myFormatter4String = new java.text.DecimalFormat("0000");
	/***
	 * from UTF-8 to GBK
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String format4String(Integer i)  {
		try{
			return myFormatter4String.format(new BigDecimal(i));
		}catch(Exception e){
			return "";
		}
	}
	/**G B*/
	public static Long G_value = 1024L*1024*1024;
	/**M B*/
	public static Long M_value = 1024L*1024;
	/**K B*/
	public static Long K_value = 1024L;
	
	/***
	 * 将43.5 G 变为bytes
	 * @param values
	 * @return
	 */
	public static String formatKbSize(String value)  {
		if(value==null){
			return "";
		}
		BigDecimal newbig = new BigDecimal(value);
		if (newbig.longValue() >= G_value) {
			return newbig.divide(new BigDecimal(G_value)).setScale(2,BigDecimal.ROUND_HALF_UP).toString()+ " TB";
		} else if (newbig.longValue() >= M_value) {
			return newbig.divide(new BigDecimal(M_value)).setScale(2,BigDecimal.ROUND_HALF_UP).toString()+ " GB";
		} else if (newbig.longValue() >= K_value) {
			return newbig.divide(new BigDecimal(K_value)).setScale(2,BigDecimal.ROUND_HALF_UP).toString() + " MB";
		}
		return newbig.setScale(2,BigDecimal.ROUND_HALF_UP).toBigInteger()+" KB";
	}
	/***
	 * 将43.5 G 变为bytes
	 * @param values
	 * @return
	 */
	public static Long formatLinuxSize(String sizeString)  {
		Long size = 1L;
		sizeString = sizeString.toUpperCase();
		int idx_G = sizeString.indexOf("G");
		int idx_M = sizeString.indexOf("M");
		int idx_K = sizeString.indexOf("K");
		if (idx_G > 0) {
			sizeString = sizeString.substring(0, idx_G).trim();
			size = new Double(new Double(sizeString) * G_value).longValue(); 
		} else if (idx_M > 0) {
			sizeString = sizeString.substring(0, idx_M).trim();
			size = new Double(new Double(sizeString) * M_value).longValue(); 
		} else if (idx_K > 0) {
			sizeString = sizeString.substring(0, idx_K).trim();
			size = new Double(new Double(sizeString) * K_value).longValue(); 
		} else {
			int idx_B = sizeString.indexOf("B");
			if(idx_B>0){
				sizeString = sizeString.substring(0, idx_K).trim();
			}
			sizeString = sizeString.trim();
			size = new Double(sizeString).longValue(); 
		}
		return size;
	}
	/***
	 * 格式化数据变为GB,MB,KB 或bytes
	 * @param values
	 * @return
	 */
	public static String formatSpace(Double values)  {
		Double size = values;
		String app = " Bytes";
		
		if (values > G_value) {
			size = values/G_value;
			app = " GB";
		} else if (values>M_value){
			size = values/M_value;
			app = " MB";
		} else if(values>K_value){
			size = values/K_value;
			app = " KB";
		}
		BigDecimal oo = new BigDecimal(size);
		return oo.setScale(2, RoundingMode.HALF_UP).toString() + app;
	}
	
	/**
	 *String char set change * 
	 *@param str need change Strings　　
	 *@param oldCharset from char set
	 *@param newCharset to char set
	 * @return
	 * @throwsUnsupportedEncodingException 　　
	 */
	public static String changeCharset(String str, String oldCharset, String newCharset)
			throws UnsupportedEncodingException {
		if (str != null) {
			// 用旧的字符编码解码字符串。解码可能会出现异常。
			new String(str.getBytes(oldCharset), newCharset);
		}
		return null;
	}
	
	
	
	/***
	 * get seconds by  duration of 01:03:14
	 * @param duration
	 * @return
	 */
	public static Integer getSecondByString(String duration){
		if (duration == null) {
			return null;
		}
		if(duration.length()==8){
			String[] values = duration.split(":");
			if (values.length == 3) {
				int hour = Integer.valueOf(values[0]);
				int min = Integer.valueOf(values[1]);
				int second = Integer.valueOf(values[2]);
				return second + min*60 + hour*60*60;
			}
		}
		return null;
	}
	
	/**
	 * 全角转半角:
	 * @param fullStr
	 * @return
	 */
	public static final String full2Half(String fullStr) {
		if(fullStr == null || fullStr.isEmpty()){
			return fullStr;
		}
		char[] c = fullStr.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] >= 65281 && c[i] <= 65374) {
				c[i] = (char) (c[i] - 65248);
			} else if (c[i] == 12288) { // 空格
				c[i] = (char) 32;
			}
		}
		return new String(c);
	}

	/**
	 * 半角转全角
	 * @param halfStr
	 * @return
	 */
	public static final String half2Full(String halfStr) {
		if(halfStr == null || halfStr.isEmpty()){
			return halfStr;
		}
		char[] c = halfStr.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 32) {
				c[i] = (char) 12288;
			} else if (c[i] < 127) {
				c[i] = (char) (c[i] + 65248);
			}
		}
		return new String(c);
	}
	/***
	 * 获取order by 数据
	 * @param order
	 * @return
	 */
	public static String getOderBy(String order){
		char startC = 'A';
		char startZ = 'Z';
		StringBuffer newOrder = new StringBuffer("");
		for (int j = 0; j < order.length(); j++) {
			char c = order.charAt(j);
			if (c >= startC && c <= startZ) {
				newOrder.append("_" + String.valueOf(c).toLowerCase());
			} else {
				newOrder.append(String.valueOf(c));
			}
		}
		return newOrder.toString();
	}
	
	/***
	 * 字符串替换，只替换 指定长度的
	 * @param str
	 * @param replacement
	 * @param beginIndex
	 * @param endIndex
	 * @return
	 */
	public static final String replaceSubstring(String str, String replacement, int beginIndex, int endIndex) {
		int length = str.length();
		if (beginIndex < 0) {
			beginIndex += length;
		}
		if (endIndex < 0) {
			endIndex += length;
		}
		if (beginIndex == length || beginIndex >= endIndex)
			return str;

		StringBuilder sb = new StringBuilder(replacement.length() + length - endIndex + beginIndex);
		if (beginIndex > 0) {
			sb.append(str, 0, beginIndex);
		}
		sb.append(replacement);
		if (endIndex < length) {
			sb.append(str, endIndex, length);
		}
		return sb.toString();
	}


	public static void main(String[] args) {
		System.out.println(StringUtil.getHideTelphone("13817862400"));
	}
}
