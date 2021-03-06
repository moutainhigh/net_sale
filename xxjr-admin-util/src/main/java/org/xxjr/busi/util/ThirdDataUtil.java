package org.xxjr.busi.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

public class ThirdDataUtil {
	
	/**
	 * 获取渠道今天的推送数量
	 * @param channelCode
	 * @return
	 */
	public static int getChannelDataCount (String channelCode) {
		AppParam queryParam = new AppParam("thirdDataService", "queryCount");
		queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		queryParam.addAttr("sourceChannel", channelCode);
		queryParam.addAttr("isRepeat", "0");
		queryParam.addAttr("nowDate", true);
		AppResult result = null;
		if (SpringAppContext.getBean("thirdDataService") == null) {
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
		}else {
			result = SoaManager.getInstance().callNoTx(queryParam);
		}
		int count = NumberUtil.getInt(result.getAttr(DuoduoConstant.TOTAL_SIZE), 0);
		return count;
	}
	
	
	public static List<Integer> getRandoms (int count) {
		int randomNum = SysParamsUtil.getIntParamByKey("random_Number", 10);
		List<Integer> randoms = new ArrayList<Integer>(count);
		Random random = new Random();
		for (int i = 0; i < count; i++) {
			if (i == 0) {
				randoms.add((random.nextInt(randomNum)));
			}else {
				int j = (random.nextInt(randomNum));;
				while (randoms.contains(j)) {
					j = (random.nextInt(randomNum));
				}
				randoms.add(j);
			}
		}
		Collections.sort(randoms);
		return randoms;
	}
	
	public static boolean contains (List<Integer> randoms, int count) {
		if (randoms == null || randoms.isEmpty() || count == 0) {
			return false;
		}
		String countStr = StringUtil.getString(count);
		for (int i = 0; i < randoms.size(); i++) {
			if (countStr.endsWith(StringUtil.getString(randoms.get(i)))) {
				randoms.remove(i);//用完删除
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 根据随机数进行重复
	 * @param count 这个渠道今天的推送数量
	 * @return true:重复 false:非重复
	 */
	public static boolean randomRepeat (int deduction, String channelCode, String randomsStr) {
		List<Integer> randoms = null;
		int channelDataCount = getChannelDataCount(channelCode);
		int randomNum = SysParamsUtil.getIntParamByKey("random_Number", 10);
		if (StringUtils.isEmpty(randomsStr) && (channelDataCount == 0 || channelDataCount % randomNum == 0)) {
			randoms = getRandoms(deduction);
			updateRandoms(channelCode, randoms);
		}else {
			randoms = getStrToArray(randomsStr);
		}
		boolean isRepeat = contains(randoms, (channelDataCount + 1));
		if (isRepeat) {
			//如果返回成功代表随机数列表中有一个值已经被使用，需要重新设置缓存中列表的值
			updateRandoms(channelCode, randoms);
		}
		return isRepeat;
	}
	
	
	/**
	 * 亿科思奇参数转化
	 * @param params
	 */
	public static void transParams(Map<String,Object> params){
		String birthday = StringUtil.getString(params.get("birthday"));//年龄转换
		if (StringUtils.hasText(birthday)) {
			try{
				int year = NumberUtil.getInt(birthday.split("-")[0], 0);
				Date now = new Date();
				String nowYearStr = DateUtil.toStringByParttern(now, "yyyy");
				int age = NumberUtil.getInt(nowYearStr, 0) - year;
				params.put("age", age);
			}catch(Exception e){
				
			}
		}
		params.put("workType", "4");
		if(!StringUtils.isEmpty(params.get("workType"))){
			int workType = Integer.valueOf(params.get("workType").toString());
			// 0职员 1私营业主 2公务员 99 其他
			// 小小金融(1无固定职业 2企业主 3个体户 4上班族  5学生)
			if(workType == 1){
				params.put("workType", "3");
			}
		}
		
		params.put("houseType", "2");
		if(!StringUtils.isEmpty(params.get("houseType"))){
			int houseType = Integer.valueOf(params.get("houseType").toString());
			//  1无房产  2有房产 还贷中  3有房产无房贷
			// 小小金融 1-有房,但不确认房产类型  2-无房产 3-商品住宅 4-商住两用房 5-军产房 6-办公楼8-商铺  7-厂房 9-自建房/小产权房 10-经适/限价房 11-房改/危改房
			if(houseType == 2 || houseType == 3){
				params.put("houseType", "1");
			}
		}
		
		params.put("socialType", "2");
		if(!StringUtils.isEmpty(params.get("socialType"))){
			int socialType = Integer.valueOf(params.get("socialType").toString());
			// 0 无  1 1年以内  2一年以上
			// 小小金融  1有本地社保，2无
			if(socialType == 1 || socialType == 2){
				params.put("socialType", "1");// 有社保
			}
		}
		
		params.put("fundType", "2");
		if(!StringUtils.isEmpty(params.get("fundType"))){
			int fundType = Integer.valueOf(params.get("fundType").toString());
			// 0 无  1 1年以内  2一年以上
			// 小小金融  1有本地公积金，2无
			if(fundType == 1 || fundType == 2){
				params.put("fundType", "1");// 有公积金
			}
		}
		
		params.put("carType", "0");
		if(!StringUtils.isEmpty(params.get("carType"))){
			int carType = Integer.valueOf(params.get("carType").toString());
			// 1无  2有还贷中 3有且无车贷
			// 小小金融  0 无车 1 名下有车 2 准备买车
			if(carType == 2 || carType == 3){
				params.put("carType", "1");
			}
		}
		
		params.put("creditType", "1");
		if(!StringUtils.isEmpty(params.get("creditType"))){
			int creditType = Integer.valueOf(params.get("creditType").toString());
			//       1无 2良好 3有逾期
			// 小小金融 1-信息良好 2-无信用卡，贷款 3有过逾期)
			if(creditType == 1){
				params.put("creditType", "2");// 无信用卡
			}else if(creditType == 3){
				params.put("creditType", "3");// 有逾期
			}
		}
		
		if(StringUtils.isEmpty(params.get("wagesType"))){
			params.put("wagesType", "1");
		}

		if(StringUtils.isEmpty(params.get("insurType"))){
			params.put("insurType", "0");
		}
	}
	
	
	public static AppResult isCheckChannel (String channelCode) {
		int deduction = 0;
		AppResult result = null;
		AppParam param = new AppParam("thirdDataCfgService", "getThirdDataCfg");
		param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		param.addAttr("channelCode", channelCode);
		param.addAttr("recordDate", DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD));
		result = RemoteInvoke.getInstance().callNoTx(param);
		if (result.getRows().size() > 0) {
			int canUse = NumberUtil.getInt(result.getRow(0).get("canUse"), 1);
			switch (canUse) {
			case -1:
				result.setMessage("渠道暂未开启!");
				result.setErrorCode("0010");
				result.setSuccess(false);
				break;
			case 0:
				result.setMessage("数据已达上限!");
				result.setErrorCode("009");
				result.setSuccess(false);
				break;
			}
			deduction = NumberUtil.getInt(result.getRow(0).get("deduction"), 0);
			result.putAttr("randoms", result.getRow(0).get("randoms"));
		}
		result.putAttr("deduction", deduction);
		return result;
	}
	
	public static void getAge (String birthday, Map<String, Object> params) {
		Integer age = 0;
		if (StringUtils.hasText(birthday) && birthdayPattern.matcher(birthday).matches()) {
			try{
				int year = NumberUtil.getInt(birthday.split("-")[0], 0);
				Date now = new Date();
				String nowYearStr = DateUtil.toStringByParttern(now, "yyyy");
				age = NumberUtil.getInt(nowYearStr, 0) - year;
			}catch(Exception e){
				LogerUtil.error(ThirdDataUtil.class, e, "getAge error");
			}
		}
		if (age > 0 && age < 100) {
			params.put("age", age);
		}
	}
	
	public static void updateRandoms(String channelCode, List<Integer> randoms){
		AppParam updateParam = new AppParam("thirdDataCfgService", "update");
		updateParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		updateParam.addAttr("channelCode", channelCode);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < randoms.size(); i++) {
			builder.append(randoms.get(i) + ",");
		}
		String randomsStr = builder.toString();
		if (!StringUtils.isEmpty(randomsStr)) {
			randomsStr = randomsStr.substring(0, randomsStr.lastIndexOf(","));
		}
		updateParam.addAttr("randoms", randomsStr);
		RemoteInvoke.getInstance().call(updateParam);
	}
	
	private static List<Integer> getStrToArray(String randomsStr) {
		if (StringUtils.isEmpty(randomsStr)) {
			return null;
		}
		int size = 0;
		String[] split = null;
		split = randomsStr.split(",");
		size = split.length;
		List<Integer> randoms = new ArrayList<Integer>(size);
		for (int i = 0; i < split.length; i++) {
			randoms.add(Integer.valueOf(split[i]));
		}
		return randoms;
	}
	
	public static final Pattern birthdayPattern = Pattern.compile("^\\d{4}\\-\\d{1,2}\\-\\d{1,2}$");
}
