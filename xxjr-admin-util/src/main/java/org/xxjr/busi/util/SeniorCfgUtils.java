package org.xxjr.busi.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.StringUtil;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.SysParamsUtil;

/**
 * 优质配置
 * @author 2017-02-15 By liulw
 *
 */
public class SeniorCfgUtils {

	/**
	 * 贷款申请优质单配置
	 */
	public static final String BORROW_SENIOR_CFG = "senior_cfg_redis_key";

	/**
	 * 卡牛配置
	 */
	public static final String BORROW_KANIU_CFG = "borrow_kaniu_cfg";

	/**
	 * 定价配置缓存
	 */
	public static final String BORROW_PRICE_CFG = "borrow_price_cfg_";

	/** 平台定价配置 */
	public static final String XXJR_PRICE_TYPE = "1";
	/** 卡牛定价配置 */
	public static final String KANIU_PRICE_TYPE = "2";


	/** 优质单配置类型 */
	public static final String SENIOR_TYPE_1 = "1";
	/** 优质客户类型 */
	public static final String SENIOR_TYPE_2 = "2";
	/**优质配置缓存7天*/
	public static final int borrow_senior_cfg_time = 60*60*24*7;



	@SuppressWarnings("unchecked")
	public static Map<String,Object> parseMap(Object obj){
		if(null == obj){
			return new HashMap<String, Object>();
		}
		return JsonUtil.getInstance().json2Object(obj.toString(), Map.class);
	}

	
	/**1无固定职业 2企业主 3个体户 4上班族  5-学生
	 * 判断收入
	 * @param incomeCfg
	 * @param income
	 * @return
	 */
	public static double randomIncome(int workType, double income){
		switch (workType) {
		case 1://无固定职业 
			double jobless = 3000 + Math.random() * 4000;//产生5000-6000的随机数
			jobless =  income < 1000 ? jobless : income;
			return new BigDecimal(jobless).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
		case 2://企业主 
			double business = 8000 + Math.random() * 40000;//产生10000-50000的随机数
			business = income < 1000 ? business : income;
			return new BigDecimal(business).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
		case 3://个体户
			double person = 5000 + Math.random() * 5000;//产生6000-10000的随机数
			person = income < 1000 ? person : income;
			return new BigDecimal(person).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
		case 4://上班族
			double worker = 3000 + Math.random() * 6000;//产生4000-10000的随机数
			worker = income < 1000 ? worker : income;
			return new BigDecimal(worker).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
		case 5://学生
			double student = 1000 + Math.random() * 1000;//产生1000-3000的随机数
			student = income < 1000 ? student : income;
			return new BigDecimal(student).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();

		default:
			return 4000;
		}
	}
	
	
	/**转换对公流水
	 * @param incomeCfg
	 * @param income
	 * @return
	 */
	public static double tranPubLine(double pubManageLine){
		if(pubManageLine > 0 && pubManageLine < 1) return 0.9;
		if(pubManageLine >= 1 && pubManageLine < 3) return 2;
		if(pubManageLine >=3 && pubManageLine < 5) return 4;
		if(pubManageLine >=5 && pubManageLine < 10) return 8;
		if(pubManageLine >= 10 && pubManageLine < 30) return 20;
		if(pubManageLine >= 30 && pubManageLine < 1) return 31;
		else return 0;
	}
	
	/**转换对公流水
	 * @param incomeCfg
	 * @param income
	 * @return
	 */
	public static double tranTotalLine(double totalLine){
		if(totalLine > 0 && totalLine < 1) return 0.9;
		if(totalLine >= 1 && totalLine < 3) return 2;
		if(totalLine >=3 && totalLine < 5) return 4;
		if(totalLine >=5 && totalLine < 10) return 8;
		if(totalLine >= 10 && totalLine < 30) return 20;
		if(totalLine >= 30 && totalLine < 1) return 31;
		else return 0;
	}
	
    /**
     * 判断单的抢单类型 1-免费 2-积分抢  3-现金抢
     * @param loanAmount 金额
     * @param isSeniorCust 是否优质客户 1-是 0-否
     * @param grade 等级
     * @param isAutoSale 是否自动转 1-自动 0-手工
     * @return
     */
	public static int judgeRobType(double loanAmount,int isSeniorCust,String grade,int isAutoSale){
		if(isSeniorCust == 1 || isAutoSale == 0) return 3;//
		
		int loanAmtRobType = SysParamsUtil.getIntParamByKey("loanAmtRobType", 2);
		if(("E".equals(grade) || "F".equals(grade)) && loanAmount <= loanAmtRobType) 
			return 1;
		else 
			return 3;
	}
	
	
	/**
	 * 房、车 、保单、社保公积金 都没有就算未填信息
	 * noHouseType 和 noCarType 无车和无房的类型不统一，需要自己传类型,默认都是2
	 */
	public static void haveDetail(Map<String, Object> param, String noHouseType, String noCarType) {
		if (StringUtils.isEmpty(noHouseType)) {
			noHouseType = "2";
		}
		if (StringUtils.isEmpty(noCarType)) {
			noCarType = "2";
		}
		
		double income = NumberUtil.getDouble(param.get("income"), 0);
		int wagesType = NumberUtil.getInt(param.get("wagesType"), 0);
		if(wagesType ==1 && income >= 3000){
			return ;
		}
		
		if ((!"1".equals(StringUtil.getString(param.get("fundType")))) && (!"1".equals(StringUtil.getString(param.get("socialType"))))
			&& (StringUtils.isEmpty(param.get("insurType")) || "0".equals(StringUtil.getString(param.get("insurType"))))
			&& (StringUtils.isEmpty(param.get("houseType")) || noHouseType.equals(StringUtil.getString(param.get("houseType"))))
			&& (StringUtils.isEmpty(param.get("carType")) || noCarType.equals(StringUtil.getString(param.get("carType"))))) {
			param.put("haveDetail", 0);
		}
	}
	
	/**
	 * 房、车 、保单、社保公积金 都没有就算未填信息
	 * noHouseType 和 noCarType 无车和无房的类型不统一，需要自己传类型,默认都是2
	 */
	public static int haveDetail(Map<String, Object> param) {
	    int haveDetail = 1;
	    
		double loanAmount = 0;
        if(StringUtils.isEmpty(param.get("loanAmount"))){
       	 loanAmount = NumberUtil.getDouble(param.get("applyAmount"),0);
        }else{
       	 loanAmount = NumberUtil.getDouble(param.get("loanAmount"),0);
        }
        
	    if(loanAmount <= 0){
	    	return 0;
	    }
	    
	   if(StringUtils.isEmpty(param.get("cityName"))){
		   return 0;
	   }
	    
		double income = NumberUtil.getDouble(param.get("income"), 0);
		int wagesType = NumberUtil.getInt(param.get("wagesType"), 0);
		if(wagesType ==1 && income >= 3000){
			return haveDetail;
		}
		
		int fundType = NumberUtil.getInt(param.get("fundType"), 0);
		int socialType = NumberUtil.getInt(param.get("socialType"), 0);
		int insurType = NumberUtil.getInt(param.get("insurType"), 0);
		int houseType = NumberUtil.getInt(param.get("houseType"), 2);
		int carType = NumberUtil.getInt(param.get("carType"), 2);
		
		if(!CountGradeUtil.judgeFundType(fundType) && !CountGradeUtil.judgeCar(carType)
		  && !CountGradeUtil.judgeInsurType(insurType) && !CountGradeUtil.judgeHouse(houseType)
		  && !CountGradeUtil.judgeSocialType(socialType)){
			haveDetail = 0;
		}
		
		return haveDetail ;
	}

	
	
}
