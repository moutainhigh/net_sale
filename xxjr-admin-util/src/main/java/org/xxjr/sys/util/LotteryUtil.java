package org.xxjr.sys.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.math.RandomUtils;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;


/***
 * 摇奖处理
 * @author Administrator
 *
 */
public class LotteryUtil {
	private final static  String KEY_CONFIG = "LotteryCfg_";
	
	private final static  String KEY_ALL_CONFIG = "LotteryAllCfg_";
	
    public final static String KEY_DAY_LOTTERY_COUNT="cust_day_lottery_count";

	/**签到福利**/
	public final static  String LotterySingin = "1";
	
	/** 分销拆红包活动  **/
	public final static  String LOTTERY_FX_HB = "2";
	
	/**奖品类型  积分**/
	public final static  int KEY_TYPE_1 = 1;
	/**奖品类型  抢单券**/
	public final static  int KEY_TYPE_2 = 2;
	/**奖品类型  未中奖**/
	public final static  int KEY_TYPE_3 = 3;
	/**转盘配置ID缓存key**/
	public final static String KEY_LP_CFG_ID ="LotteryLpCfgId";
	
	
	/**
	 * 抽取奖品
	 * @param lotteryId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getWinLottery(String configId){
		Map<String,Object> thisLottery = null;
		Map<String,Object> lotteryCfg = getLotteryConfig(configId);
		List<Map<String,Object>> lotteryList = (List<Map<String, Object>>) lotteryCfg.get("lotteryList");
		//随机数的最大值
		int endNum = Integer.valueOf(lotteryCfg.get("endNum").toString());
		int randomInt =  RandomUtils.nextInt(new Random(),endNum);
		for(Map<String,Object> lotter:lotteryList){
			int lotteryStart = Integer.valueOf(lotter.get("lotteryStart").toString());
			int lotteryEnd = Integer.valueOf(lotter.get("lotteryEnd").toString());
			//随机数 为开始和结束之间
			if (lotteryStart <= randomInt && lotteryEnd >= randomInt) {
				thisLottery = lotter;
				break;
			}
		}
		// 没找到奖品，若存在合适奖励，则使用合适的奖品
		int dayCount = NumberUtil.getInt(thisLottery.get("dayCount"),0);
		int leftCount = NumberUtil.getInt(thisLottery.get("leftCount"),0);
		if (thisLottery == null || dayCount == 0 || leftCount == 0) {
			thisLottery = getDefaultLottery(lotteryList);
		}
		if (thisLottery == null) {
			throw new SysException("当前摇奖配置不正确,请联系客服。");
		}
       return thisLottery;
	}
	
	/**
	 * 查询用户可抽奖次数
	 */
	public static int getLotteryCount(String customerId,boolean isVip){
		AppResult result = new AppResult();
		AppParam params = new AppParam();
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		params.setService("custFreeService");
		params.setMethod("queryLotteryCount");
		params.addAttr("isVip", isVip);
		params.addAttr("customerId", customerId);
		if (SpringAppContext.getBean("custFreeService") == null) {
			result = RemoteInvoke.getInstance().call(params);
		}else{
			result = SoaManager.getInstance().invoke(params);
		}
		return (int)result.getAttr("count");
	}
	
	/**
	 * 修改用户可抽奖次数
	 */
	public static AppResult updateCustLotteryCount(String customerId,boolean isVip,int count){
		AppResult result = new AppResult();
		AppParam params = new AppParam();
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		params.setService("custFreeService");
		params.setMethod("updateLotteryCount");
		params.addAttr("isVip", isVip);
		params.addAttr("customerId", customerId);
		params.addAttr("count", count);
		if (SpringAppContext.getBean("custFreeService") == null) {
			result = RemoteInvoke.getInstance().call(params);
		}else{
			result = SoaManager.getInstance().invoke(params);
		}
		return result;
	}
	
	/**
	 * 扣减奖品当日库存数量
	 */
	public static AppResult subLotteryTodayCount(String configId,String lotteryId){
		AppResult result = new AppResult();
		AppParam updateParam = new AppParam("lotteryService", "subDayCount");
		updateParam.addAttr("lotteryId", lotteryId);
		if (SpringAppContext.getBean("lotteryService") == null) {
			result = RemoteInvoke.getInstance().call(updateParam);
		} else {
			result = SoaManager.getInstance().invoke(updateParam);
		}
		refershConfig(configId);
		return result;
	}
	
	/**
	 * 扣减奖品总数量
	 * @param configId
	 * @param lotteryId
	 * @return
	 */
	private static AppResult subLotteryTotalCount(String configId,String lotteryId){
		AppResult result = new AppResult();
		AppParam updateParam = new AppParam("lotteryService", "subTotalLeftCount");
		updateParam.addAttr("lotteryId", lotteryId);
		if (SpringAppContext.getBean("lotteryService") == null) {
			result = RemoteInvoke.getInstance().call(updateParam);
		} else {
			result = SoaManager.getInstance().invoke(updateParam);
		}
		refershConfig(configId);
		return result;
	}
	
	/***
	 * 获取默认的奖品
	 * @param lotteryList
	 * @return
	 */
	public static Map<String,Object> getDefaultLottery(List<Map<String,Object>> lotteryList){
		for(Map<String,Object> lotter:lotteryList){
			int isDefault = Integer.valueOf(lotter.get("isDefault").toString());
			if (isDefault == 1) {
				return  lotter;
			}
		}
		return null;
	}
	
	
	/***
	 * 根据位置获取对应奖品
	 * @param lotteryList
	 * @return
	 */
	public static Map<String,Object> getLotteryBySpace(List<Map<String,Object>> lotteryList, String space){
		for(Map<String,Object> lotter:lotteryList){
			if (lotter.get("space").toString().equals(space)) {
				return  lotter;
			}
		}
		return null;
	}
	
	/**
	 * 摇奖信息缓存
	 * @return
	 */
	public static List<Map<String,Object>> getLotteryList(Object configId){
		AppParam param = new AppParam();
		param.setService("lotteryService");
		param.setMethod("query");
		param.addAttr("configId", configId);
		param.setOrderBy("lotteryEnd");
		AppResult queryResult  = null;
		if (SpringAppContext.getBean("lotteryService") == null) {
			queryResult = RemoteInvoke.getInstance().call(param);
		}else{
			queryResult = SoaManager.getInstance().invoke(param);
		}
		List<Map<String,Object>> rows =  queryResult.getRows();
		return rows;
	}
	
	/**
	 * 摇奖配置
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getLotteryConfig(String configId){
		Map<String,Object> map = (Map<String, Object>) RedisUtils.getRedisService().get(KEY_CONFIG + configId);
		if(map==null || !map.containsKey("configId")){
			map = refershConfig(configId);
		}
		if(map==null || !map.containsKey("configId")){
			throw new SysException("当前没有摇奖配置,请联系客服。");
		}
		//判断是否需要初始化每日可抽奖数
		List<Map<String, Object>> lotteryList = (List<Map<String, Object>>) map
				.get("lotteryList");
		Map<String, Object> initMap = lotteryList.get(0);
		Object initDate = initMap.get("initDate");
		String today = DateUtil.toStringByParttern(new Date(),
				DateUtil.DATE_PATTERN_YYYY_MM_DD);
		if (StringUtils.isEmpty(initDate) || !today.equals(initDate.toString())) {
			AppParam updateParam = new AppParam("lotteryService", "initDate");
			updateParam.addAttr("configId", configId);
			if (SpringAppContext.getBean("lotteryService") == null) {
				RemoteInvoke.getInstance().call(updateParam);
			} else {
				SoaManager.getInstance().invoke(updateParam);
			}
			map = refershConfig(configId);
		}
		return map;
	}
	
	
	/**
	 * 摇奖配置
	 * @return
	 */
	public static Map<String,Object> refershConfig(Object configId){
		AppParam param = new AppParam();
		param.setService("lotteryCfgService");
		param.setMethod("query");
		param.addAttr("status", "1");
		param.addAttr("configId", configId);
		AppResult queryResult  = null;
		//若没有相应的对象，使用远程调用 
		if (SpringAppContext.getBean("lotteryCfgService") == null) {
			queryResult = RemoteInvoke.getInstance().call(param);
		}else{
			queryResult = SoaManager.getInstance().invoke(param);
		}
		List<Map<String,Object>> rows =  queryResult.getRows();
		Map<String,Object> row = new HashMap<String,Object>();
		if(rows.size()>0){
			row = rows.get(0);
			row.put("lotteryList", getLotteryList(row.get("configId")));
			//刷新摇奖配置
			RedisUtils.getRedisService().set(KEY_CONFIG + configId, (Serializable)row);
		}
		return row;
	}
	
	
	/**
	 * 摇奖配置
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String,Object>> getLotteryConfig(){
		List<Map<String,Object>> list = (List<Map<String, Object>>) RedisUtils.getRedisService().get(KEY_ALL_CONFIG);
		if(list==null ||list.isEmpty()){
			refershConfig();
			list = (List<Map<String,Object>>) RedisUtils.getRedisService().get(KEY_ALL_CONFIG);
		}
		return list;
	}

	/**
	 * 摇奖配置
	 * @return
	 */
	public static List<Map<String,Object>> refershConfig(){
		AppParam param = new AppParam();
		param.setService("lotteryCfgService");
		param.setMethod("query");
		param.addAttr("status", "1");
		AppResult queryResult  = null;
		//若没有相应的对象，使用远程调用 
		if (SpringAppContext.getBean("lotteryCfgService") == null) {
			queryResult = RemoteInvoke.getInstance().call(param);
		}else{
			queryResult = SoaManager.getInstance().invoke(param);
		}
		List<Map<String,Object>> rows =  queryResult.getRows();
		if(rows.size()>0){
			RedisUtils.getRedisService().set(KEY_ALL_CONFIG , (Serializable)rows);
		}
		return rows;
	}

	
	/**
	 * 判断用户是否可以抽奖
	 * 
	 */
	public static boolean haslottery(String customerId){
		AppParam param = new AppParam();
		param.setService("lotteryRecordService");
		param.setMethod("hasLotteryRecord");
		param.addAttr("customerId", customerId);
		String createTime=DateUtil.toStringByParttern(new Date(),DateUtil.DATE_PATTERN_YYYY_MM_DD);
		param.addAttr("createTime", createTime);
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_cust));
		AppResult result = RemoteInvoke.getInstance().call(param);
		Object size=result.getRows().get(0);
		return Integer.parseInt(size.toString()) >= SysParamsUtil.getIntParamByKey(KEY_DAY_LOTTERY_COUNT, 3)?false:true;
	}
	
	/**
	 * 分隔等分数据
	 * @param list
	 * @param len
	 * @return
	 */
	public static List<List<?>> splitList(List<?> list, int len) {  
		if (list == null || list.size() == 0 || len < 1) {  
			return null;  
		}  
		List<List<?>> result = new ArrayList<List<?>>();  
		int size = list.size();  
		int count = (size + len - 1) / len;  
		for (int i = 0; i < count; i++) {  
			List<?> subList = list.subList(i * len, ((i + 1) * len > size ? size : len * (i + 1)));  
			result.add(subList);  
		}  
		return result;  
	}  
	
	
	/**************************************  分销抽奖  *****************************************************************
	
	/**
	 * 分销抽奖
	 * @param todayTotal 当天已抽金额
	 * @param configId 活动代号
	 * @return
	 */
	public static Map<String,Object> fxDrawHb(double totalAmt, double todayTotal, String configId){
		int fxHbMaxLimit = SysParamsUtil.getIntParamByKey("fxHbMaxLimit", 3000);// 分销系统抽红包总最大额度
		if(totalAmt >= fxHbMaxLimit - 10){
			// 差不多超过总额限值，给10以内的红包
			return getLess10Lottery(configId);
		}
		boolean drawDefault = false;// 是否直接给默认奖项
		int fxHbDayLimit = SysParamsUtil.getIntParamByKey("fxHbDayLimit", 300);// 分销系统抽红包每天最大额度
		if(todayTotal >= fxHbDayLimit){
			drawDefault = true;
		}
		// 根据配置的概率抽取奖品
		Map<String,Object> thisLottery = getFxLottery(configId,drawDefault);
		String lotteryId = StringUtil.getString(thisLottery.get("lotteryId"));
		String isDefault = StringUtil.getString(thisLottery.get("isDefault"));// 是否默认奖项
		
		// 减总数量
		AppResult updateResult = subLotteryTotalCount(configId, lotteryId);
		if(!"1".equals(isDefault) 
				&& 0 == Integer.valueOf(updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE).toString())){
			// 奖品如果抽完，给默认奖项
			thisLottery = fxDrawHb(totalAmt, todayTotal, configId);
		}
		return thisLottery;
	}
	
	/**
	 * 当超过活动总金额时，直接给10以下的红包
	 * @param configId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Map<String,Object> getLess10Lottery(String configId){
		Map<String,Object> lotteryCfg = getLotteryConfig(configId);
		List<Map<String,Object>> lotteryList = (List<Map<String, Object>>) lotteryCfg.get("lotteryList");
		// 默认奖品
		Map<String,Object> thisLottery = null;
		double amount = 10;
		while(amount >= 10){
			//随机数的最大值
			int endNum = Integer.valueOf(lotteryCfg.get("endNum").toString());
			int randomInt =  RandomUtils.nextInt(new Random(),endNum);
			for(Map<String,Object> lotter : lotteryList){
				int lotteryStart = Integer.valueOf(lotter.get("lotteryStart").toString());
				int lotteryEnd = Integer.valueOf(lotter.get("lotteryEnd").toString());
				//随机数 为开始和结束之间
				if (lotteryStart <= randomInt && lotteryEnd >= randomInt) {
					amount = NumberUtil.getDouble(lotter.get("amount"));
					if(amount < 10){
						thisLottery = lotter;
						break;
					}
				}
			}
		}
		if (thisLottery == null) {
			throw new SysException("当前抽奖配置不正确,请联系客服。");
		}
		// 减总数量
		String lotteryId = StringUtil.getString(thisLottery.get("lotteryId"));
		AppResult updateResult = subLotteryTotalCount(configId, lotteryId);
		if(!updateResult.isSuccess()){
			throw new SysException("抽奖异常,请联系客服。");
		}
        return thisLottery;
	}
	
	/**
	 * 抽取奖品
	 * @param lotteryId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Map<String,Object> getFxLottery(String configId,boolean drawDefault){
		Map<String,Object> thisLottery = null;
		Map<String,Object> lotteryCfg = getLotteryConfig(configId);
		List<Map<String,Object>> lotteryList = (List<Map<String, Object>>) lotteryCfg.get("lotteryList");
		//随机数的最大值
		int endNum = Integer.valueOf(lotteryCfg.get("endNum").toString());
		int randomInt =  RandomUtils.nextInt(new Random(),endNum);
		for(Map<String,Object> lotter:lotteryList){
			int lotteryStart = Integer.valueOf(lotter.get("lotteryStart").toString());
			int lotteryEnd = Integer.valueOf(lotter.get("lotteryEnd").toString());
			//随机数 为开始和结束之间
			if (lotteryStart <= randomInt && lotteryEnd >= randomInt) {
				thisLottery = lotter;
				break;
			}
		}
		// 没找到奖品，若存在合适奖励，则使用默认奖品
		if (thisLottery == null || drawDefault
				|| NumberUtil.getInt(thisLottery.get("leftCount"), 0) == 0) {
			thisLottery = getDefaultLottery(lotteryList);
		}
		if (thisLottery == null) {
			throw new SysException("当前摇奖配置不正确,请联系客服。");
		}
       return thisLottery;
	}
	

}
