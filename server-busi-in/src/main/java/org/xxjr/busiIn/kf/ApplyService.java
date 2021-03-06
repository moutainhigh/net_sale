 package org.xxjr.busiIn.kf;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.security.md5.Md5;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.ApplyAllotUtil;
import org.xxjr.busi.util.ApplyUnionUtil;
import org.xxjr.busi.util.CountGradeUtil;
import org.xxjr.busi.util.SeniorCfgUtils;
import org.xxjr.busiIn.utils.AllotCostUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;

import lombok.extern.slf4j.Slf4j;

@Lazy
@Service
@Slf4j
public class ApplyService extends BaseService {
	private static final String NAMESPACE = "APPLY";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * queryApplyStatus
	 * @param params
	 * @return
	 */
	public AppResult queryApplyStatus(AppParam params) {
		return super.query(params, NAMESPACE, "queryApplyStatus");
	}
	
	/**
	 * queryByPage
	 * @param params
	 * @return
	 */
	public AppResult queryByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE);
	}
	
	/**
	 * queryCount
	 * @param params
	 * @return
	 */
	public AppResult queryCount(AppParam params) {
		int size = getDao().count(NAMESPACE, super.COUNT,params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	public AppResult queryViewByPage (AppParam param) {
		return this.queryByPage(param, NAMESPACE, "queryView", "queryViewCount");
	}
	
	public AppResult queryViewCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryViewCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	public Map<String,Object> queryBaseByUid (AppParam param) {
		Map<String,Object> map = new HashMap<String,Object>();
		AppResult queryResult = this.query(param, NAMESPACE, "queryBaseByUid");
		if (queryResult.getRows().size() > 0) {
			map = queryResult.getRow(0);
		}
		return map;
	}
	
	public AppResult queryBaseInfo (AppParam param) {
		AppResult result = new AppResult();
		result.putAttr("baseInfo", queryBaseByUid(param));
		result.putAttr("isOldUser", isOldUser(null, StringUtil.getString(param.getAttr("uid"))));
		return result;
	}
	
	/**
	 * insert
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		AppResult result = new AppResult();
		params.addAttr("createTime", new Date());
		result = super.insert(params, NAMESPACE);
		result.putAttr("applyId", params.getAttr("applyId"));
		return result;
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		return super.update(params, NAMESPACE);
	}
	
	/**
	 * delete
	 * @param params
	 * @return
	 */
	public AppResult delete(AppParam params) {
		String ids = (String) params.getAttr("ids");
		AppResult  result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.addAttr("applyId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("applyId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**申请第一步**/
	public AppResult newLoanApply1 (AppParam params) {
		AppResult result = new AppResult();
		AppParam queryParam = new AppParam();
		String telephone = StringUtil.getString(params.getAttr("telephone"));
		queryParam.addAttr("telephone", telephone);
		queryParam.setOrderBy("applyId");
		queryParam.setOrderValue("DESC");
		
		String uid = null;
		String applyId = null;
		
		double applyAmount = NumberUtil.getDouble(params.getAttr("applyAmount"), 0);
		String channelDetail = StringUtil.getString(params.getAttr("channelDetail"));
		
		AppResult queryResult = this.queryApplyStatus(queryParam);
		if (applyAmount >= 1000) {
			params.addAttr("applyAmount", applyAmount / 1000);
		}
		
		if(StringUtils.hasText(channelDetail) && channelDetail.indexOf("?") > -1){
			params.addAttr("channelDetail", channelDetail.substring(0, channelDetail.indexOf("?")));
		}
		
		Integer status = NumberUtil.getInt(ApplyUnionUtil.isCanApply(telephone));//二次申请判断
		
		if (ApplyUnionUtil.isTestUser(telephone) && StringUtils.isEmpty(params.getAttr("applyName"))) {
			params.addAttr("applyName", "内部测试");
		}
		
		emptyToNull(params.getAttr());
		
		if (1 == status || 4 == status) {// 没有申请或重新申请
			uid = getUid();
			params.addAttr("uid", uid);
			if (4 == status) {
				if (queryResult.getRows().size() > 0) {
					params.addAttr("isAgain", NumberUtil.getInt(queryResult.getRow(0).get("count"), 0)+1);
				}
			}
			result = this.insert(params);
			if (result.isSuccess()) {//注册统计
				params.addAttr("sumType", "register");
				AllotCostUtil.pageCount(params);
			}
			applyId = StringUtil.objectToStr(params.getAttr("applyId"));
			result.putAttr("uid", uid);
		}else if (2 == status && queryResult.getRows().size() <= 0) {//是api先有的数据，后来在落地页又进行了申请这种不会重新分配，算修改操作
			uid = getUid();
			params.addAttr("uid", uid);
			Map<String, Object> queryApplyInfo = ApplyUnionUtil.queryApplyInfo(null, telephone);//borrow_apply肯定
			if (queryApplyInfo == null) {//可能存在第三方数据union表插入成功但是borrow_apply插入失败导致的borrow_apply表数据为空的情况
				result = this.insert(params);
				if (result.isSuccess()) {//注册统计
					params.addAttr("sumType", "register");
					AllotCostUtil.pageCount(params);
				}
			}else {
				params.addAttr("unionId",  queryApplyInfo.get("unionId"));
				params.addAttr("borrowApplyId",  queryApplyInfo.get("applyId"));
				params.addAttr("status", "1");
				result = this.insert(params);
			}
			applyId = StringUtil.objectToStr(params.getAttr("applyId"));
			result.putAttr("uid", uid);
		}else if(2 == status){//申请过，但是可以修改数据
			uid = queryResult.getRow(0).get("uid").toString();
			applyId = queryResult.getRow(0).get("applyId").toString();
			String applyStatus = StringUtil.getString(queryResult.getRow(0).get("status"));
			params.addAttr("applyId", applyId);
//			if (!"0".equals(applyStatus)) {
//				params.removeAttr("cityName");//如果已经分过了，修改时不做城市修改
//			}
			params.addAttr("ischannelDetail", 1);
			result = this.update(params);
			result.putAttr("uid", uid);
			//需要同步t_borrow_base t_dai_borrow 表的资产信息,条件是可以修改，并且必须是已经转过的数据
			if (!"0".equals(applyStatus)) {
				updateBaseInfo(applyId, null);//修改资产信息
			}
		}else if (3 == status) {
			return ApplyUnionUtil.applyError();
		}
		result.putAttr("isOldUser", isOldUser(applyId, null));
		return result;
	}
	
	/**申请第二步**/
	public AppResult newLoanApply2 (AppParam params) {
		AppResult result = new AppResult();
		
		AppParam queryParam = new AppParam();
		queryParam.addAttr("uid", params.getAttr("uid"));
		AppResult queryResult = this.query(queryParam);
		int size = queryResult.getRows().size();//判断uid是否正确
		if (size <= 0) {
			result.setSuccess(false);
			result.setMessage("数据保存错误，请稍后再试!");
			return result;
		}
		
		Map<String, Object> newApplyInfo = queryResult.getRow(0);
		String telephone = StringUtil.getString(newApplyInfo.get("telephone"));
		Integer status = NumberUtil.getInt(ApplyUnionUtil.isCanApply(telephone));
		if (status == 3) {//查看小贷页面
			return ApplyUnionUtil.applyError();
		}
		
		if (StringUtils.isEmpty(params.getAttr("haveDetail"))) {//如果haveDetail没传，默认为1
			params.addAttr("haveDetail", 1);
		}
		double applyAmount = NumberUtil.getDouble(params.getAttr("applyAmount"), 0);
		if (applyAmount >= 1000) {
			params.addAttr("applyAmount", applyAmount / 10000);
		}
		emptyToNull(params.getAttr());
		
		//测试号码直接相关信息，并修改状态
		if (ApplyUnionUtil.isTestUser(telephone)) {
			result = this.update(params);
			Map<String,Object> queryMap = this.queryBaseByUid(queryParam);
			if (queryMap != null && !queryMap.isEmpty()) {
				result.putAttrs(queryMap);
			}
			
			return result;
		}
		
		if (2 == status) {//必须是可以修改的数据,只有在第一步才会出现status=1或4的情况
			String applyId = StringUtil.objectToStr(newApplyInfo.get("applyId"));
			int haveDetail = NumberUtil.getInt(params.getAttr("haveDetail"), 0);
			int sysHaveDetail = SysParamsUtil.getIntParamByKey("sysHaveDetail", 2);
			int applyStatus = NumberUtil.getInt(newApplyInfo.get("status"), -1);
			
			params.addAttr("applyId", applyId);
			result = this.update(params);
			if(haveDetail == sysHaveDetail && applyStatus == 0){
			 
			  if(StringUtils.isEmpty(params.getAttr("cityName"))){
				  params.addAttr("cityName", newApplyInfo.get("cityName"));
			  }
			 
			 int judeHaveDetail = SeniorCfgUtils.haveDetail(params.getAttr());
		
			 if(judeHaveDetail == 1){
				newApplyInfo.putAll(params.getAttr());
				AppResult immedTranApply = AllotCostUtil.immedTranApply(newApplyInfo);
				result.putAttr("borrowId", immedTranApply.getAttr("borrowId"));
			 }
				
			}
			
			//需要同步t_borrow_base t_dai_borrow 表的资产信息,条件是可以修改，并且必须是已经转过的数据
			if(applyStatus != 0) {
				updateBaseInfo(applyId, null);
			}
			
			
			Map<String,Object> queryMap = this.queryBaseByUid(queryParam);
			if (queryMap != null && !queryMap.isEmpty()) {
				result.putAttrs(queryMap);
				try {
					String grade = StringUtil.objectToStr(queryMap.get("grade"));
					if (!StringUtils.isEmpty(grade)) {
						String sysGrade = SysParamsUtil.getStringParamByKey("sys_cfg_apply_grade", "B");
						result.putAttr("maxGrade", sysGrade.compareToIgnoreCase(grade) > -1);
					}
				} catch (Exception e) {
					log.error("convert grade error", e);
				}
			}  
		}
	
		return result;
	}
	
	/**
	 * 判断是否是新用户
	 * @param uid
	 * @return
	 */
	public Integer isOldUser (String applyId, String uid) {
		AppResult result = new AppResult();
		AppParam param = new AppParam();
		param.addAttr("applyId", applyId);
		param.addAttr("uid", uid);
		result = this.query(param, NAMESPACE, "isOldUser");
		if (result.getRows().size() > 0) {
			return NumberUtil.getInt(result.getRow(0).get("isLoan"), 0);
		}
		return 0;
	}
	
	/**app贷款申请，新的**/
	public AppResult newAppLoanApply (AppParam params) {
		emptyToNull(params.getAttr());
		params.addAttr("haveDetail", 1);
		AppResult result = new AppResult();
		AppParam queryParam = new AppParam();
		String telephone = StringUtil.getString(params.getAttr("telephone"));
		queryParam.addAttr("telephone", telephone);
		queryParam.setOrderBy("applyId");
		queryParam.setOrderValue("DESC");
		
		String uid = null;
		double applyAmount = NumberUtil.getDouble(params.getAttr("applyAmount"), 0);
		if (applyAmount >= 1000) {
			params.addAttr("applyAmount", applyAmount / 1000);
		}
		
		AppResult queryResult = this.queryApplyStatus(queryParam);
		Integer status = NumberUtil.getInt(ApplyUnionUtil.isCanApply(telephone));//二次申请判断
		if (1 == status || 4 == status) {// 没有申请或重新申请
			uid = getUid();
			params.addAttr("uid", uid);
			if (4 == status) {
				if (queryResult.getRows().size() > 0) {
					params.addAttr("isAgain", NumberUtil.getInt(queryResult.getRow(0).get("count"), 0)+1);
				}
			}
			result = this.insert(params);
			if (result.isSuccess()) {//注册统计
				params.addAttr("sumType", "register");
				AllotCostUtil.pageCount(params);
			}
			result.putAttr("uid", uid);
		}else if (2 == status && queryResult.getRows().size() <= 0) {//是api先有的数据，后来在落地页又进行了申请这种不会重新分配，算修改操作
			Map<String, Object> queryApplyInfo = ApplyUnionUtil.queryApplyInfo(null, telephone);//borrow_apply肯定
			params.addAttr("unionId",  queryApplyInfo.get("unionId"));
			params.addAttr("borrowApplyId",  queryApplyInfo.get("applyId"));
			uid = getUid();
			params.addAttr("uid", uid);
			params.addAttr("status", "1");
			result = this.insert(params);
			result.putAttr("uid", uid);
		}else if(2 == status){//申请过，但是可以修改数据
			String applyId = queryResult.getRow(0).get("applyId").toString();
			String applyStatus = StringUtil.getString(queryResult.getRow(0).get("status"));
			params.addAttr("applyId", applyId);
			result = this.update(params);
			//需要同步t_borrow_base t_dai_borrow 表的资产信息,条件是可以修改，并且必须是已经转过的数据
			if (!"0".equals(applyStatus)) {
				updateBaseInfo(applyId, null);//修改资产信息
			}
			result.putAttr("uid", queryResult.getRow(0).get("uid"));
		}else if (3 == status) {
			return ApplyUnionUtil.applyError();
		}
		return result;
	}
	
	private void emptyToNull (Map<String, Object> row) {
		if (StringUtils.isEmpty(row.get("telephone"))) {
			row.remove("telephone");
		}
		
		if (StringUtils.isEmpty(row.get("applyName"))) {
			row.remove("applyName");
		}
		
		if (!StringUtils.isEmpty(row.get("workType"))) {
			int workType = NumberUtil.getInt(row.get("workType"), 1);
			if (workType == 5) {
				row.put("workType", 1);
			}
		}
		
		if (!StringUtils.isEmpty(row.get("houseType"))) {
			int houseType = NumberUtil.getInt(row.get("houseType"), 2);
			if (houseType > 4) {
				row.put("houseType", 1);
			}
		}
		
		if (!StringUtils.isEmpty(row.get("carType"))) {
			int carType = NumberUtil.getInt(row.get("carType"), 2);
			if (carType == 1) {
				row.put("carType", 3);
			}else if (carType == 5) {
				row.put("carType", 2);
			}
		}
		
		if (!StringUtils.isEmpty(row.get("creditType"))) {
			int creditType = NumberUtil.getInt(row.get("creditType"), 2);
			if (creditType == 3) {
				row.put("creditType", 5);
			}
		}
		
		
		String applyIp = StringUtil.getString(row.get("applyIp"));
		if (!StringUtils.isEmpty(applyIp)) {
			int index = applyIp.indexOf(",");//多次反向代理后会有多个ip值，第一个ip才是真实ip
	        if(index != -1){
	        	row.put("applyIp", applyIp.substring(0,index));
	        }
		}
	}
	
	/**apply表数据批量转移**/
	@SuppressWarnings({ "unchecked" })
	public AppResult batchTransfer (AppParam params) {
		Map<String, Object> now  =  (Map<String, Object>) params.getAttr("applyList");
		return AllotCostUtil.immedTranApply(now);
	}
	
	/**
	 * 重新转移失败的数据
	 * @return
	 */
	public AppResult handlerErrData (AppParam params) {
		AppResult result = new AppResult();
		if (StringUtils.isEmpty(params.getAttr("applyId"))) {
			result.setSuccess(false);
			result.setMessage("缺少必传参数!");
			return result;
		}
		AppParam queryParam = new AppParam();
		queryParam.addAttr("applyId", params.getAttr("applyId"));
		AppResult queryResult = this.query(queryParam);
		if (queryResult.getRows().size() <= 0) {
			result.setSuccess(false);
			result.setMessage("申请数据不存在!");
			return result;
		}
		return AllotCostUtil.immedTranApply(queryResult.getRow(0));
	}
	
	/**
	 * 第三方失败的数据重新分单
	 * @param params
	 * @return
	 */
	public AppResult thirdFailReApply (AppParam params) {
		AppResult result = new AppResult();
		
		Map<String, Object> loanInfo = null;
		
		AppParam queryParam = new AppParam();
		queryParam.addAttr("borrowApplyId", params.getAttr("applyId"));
		AppResult queryResult = this.query(queryParam);
		if (queryResult.getRows().size() > 0) {
			loanInfo = queryResult.getRow(0);
		} else {
			queryParam = new AppParam("borrowBaseService", "queryBaseInfo");
			queryParam.addAttr("applyId", params.getAttr("applyId"));
			queryResult = SoaManager.getInstance().invoke(queryParam);
			if (queryResult.getRows().size() <= 0) {
				result.setSuccess(false);
				result.setMessage("申请数据不存在!");
				return result;
			}
			loanInfo = queryResult.getRow(0);
			loanInfo.remove("applyId");
			loanInfo.put("telephone", params.getAttr("telephone"));
		}
		loanInfo.put("pushStatus", 1);
		return AllotCostUtil.immedTranApply(loanInfo);
	}
	
	public static boolean isTestUser (String telephone) {
		String telephones = SysParamsUtil.getParamByKey("tgTestTelephone");
		if (!StringUtils.isEmpty(telephones)) {
			if (telephones.indexOf(telephone) > -1) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 修改贷款信息
	 * @param params
	 * @return
	 */
	public AppResult updateLoanInfo(AppParam params){
		if (StringUtils.isEmpty(params.getAttr("uid"))) {
			return null;
		}
		AppParam queryParam = new AppParam();
		queryParam.addAttr("uid", params.getAttr("uid"));
		AppResult result = this.query(queryParam);
		if (result.getRows().size() > 0 && "0".equals(result.getRow(0).get("status").toString())) {
			params.addAttr("haveDetail", 1);	//已填写信息
			this.update(params);
		}
		return result;
	}
	
	/**
	 * 查询贷款客户的订单跟进情况
	 * @param params
	 * @return
	 */
	public AppResult queryLoanInfo(AppParam params){
		return super.query(params, NAMESPACE, "queryLoanInfo");
	}
	
	/**
	 * 清除加速时间（重新申请时）
	 * @param params
	 * @return
	 */
	public AppResult clearSpeedupTime (AppParam params) {
		int count = super.getDao().update(NAMESPACE, "clearSpeedupTime", params.getAttr(), params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, count);
		return result;
	}
	
	private void updateBaseInfo (String applyId,String uid) {
		Map<String, Object> row = null;
		AppResult queryResult = new AppResult();
		AppParam queryParam = new AppParam("applyService", "query");
		queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
		queryParam.addAttr("applyId", applyId);
		queryParam.addAttr("uid", uid);
		queryResult = SoaManager.getInstance().invoke(queryParam);
		
		row = queryResult.getRow(0);//获取申请原始数据
		String status = StringUtil.getString(row.remove("status"));
		String applyName = StringUtil.getString(row.get("applyName"));
		row.remove("applyType");
		if ("0".equals(status) || applyName.indexOf("测试")> -1) {//原始数据没转移不需要同步
			return;
		}
		
		//这个borrow_apply 中的applyId必须存在，才能修改下面的信息
		if(!StringUtils.isEmpty(row.get("borrowApplyId"))){
			
			AppParam brrowParam = new AppParam("borrowApplyService", "query");
			brrowParam.addAttr("applyId", row.get("borrowApplyId"));
			AppResult borrowResult = SoaManager.getInstance().invoke(brrowParam);
			
			int oldAllotFlag = NumberUtil.getInt(borrowResult.getRow(0).get("allotFlag"), 0);
			
			
			ApplyAllotUtil.conversionType(row);
			
			String grade = CountGradeUtil.getGrade(row) ;
		
			int insure = NumberUtil.getInt(row.get("insure"),0);//是否勾选保险协议
			String cityName = StringUtil.getString(row.get("cityName"));
			
			double loanAmount = NumberUtil.getDouble(row.get("loanAmount"), 0);
			int allotFlag = ApplyAllotUtil.allot(grade, cityName,insure,loanAmount);//分单
			
			AppParam updateParam = new AppParam();
			updateParam.addAttrs(row);
			updateParam.addAttr("applyId", row.get("borrowApplyId"));
			updateParam.addAttr("grade", grade);
			updateParam.addAttr("allotFlag", oldAllotFlag == 8 ? oldAllotFlag : allotFlag);
			updateParam.setService("borrowBaseService");
			updateParam.setMethod("updateBaseInfo");
			
			//修改busiIn里面相关资产表的信息
			AppResult updateResult = SoaManager.getInstance().invoke(updateParam);
			int size = NumberUtil.getInt(updateResult.getAttr(DuoduoConstant.DAO_Insert_SIZE), 0);
			if (size == 0) {
				return;
			}
			
			if (oldAllotFlag != 8) {//必须是旧的分配标志不等于
				if(allotFlag == 1){//如果之前没有分给网销，重新分给网销
					ApplyAllotUtil.newOrderAllotOrNet(updateParam);
				}
			}
			
		}
	}
	
	
	private static String getUid () {
		String uid = DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERNYYYYMMDDHHMMSSSSS)
				+ StringUtil.getUUID();
		return Md5.getInstance().encrypt(uid);
	}
}
