package org.xxjr.busiIn.kf;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.BorrowConstant;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.SysParamsUtil;

@Lazy
@Service
public class BorrowBaseService extends BaseService {
	private static final String NAMESPACE = "BORROWBASE";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}

	/**
	 * queryText
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryText(AppParam params) {
		return super.query(params, NAMESPACE, "queryText");
	}

	/**
	 * queryDetail
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryDetail(AppParam params) {
		return super.query(params, NAMESPACE, "queryDetail");
	}

	/**
	 * querySeniorInfo
	 * 
	 * @param params
	 * @return
	 */
	public AppResult querySeniorInfo(AppParam params) {
		return super.query(params, NAMESPACE, "querySeniorInfo");
	}

	/**
	 * queryByPage
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE);
	}

	/**
	 * queryCount
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryCount(AppParam params) {
		int size = getDao().count(NAMESPACE, super.COUNT, params.getAttr(),
				params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}

	/**
	 * insert
	 * 
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		Date d1 = new Date();
		params.addAttr("createTime", d1);
		params.addAttr("applyTime", d1);
		return super.insert(params, NAMESPACE);
	}

	/**
	 * update
	 * 
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		params.addAttr("updateTime", new Date());
		AppResult updateResult = super.update(params, NAMESPACE);
		int count = (Integer) updateResult
				.getAttr(DuoduoConstant.DAO_Update_SIZE);
		if (count <= 0) {
			updateResult = this.insert(params);
		}
		return updateResult;
	}

	/**
	 * delete
	 * 
	 * @param params
	 * @return
	 */
	public AppResult delete(AppParam params) {
		String ids = (String) params.getAttr("ids");
		AppResult result = null;
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

	private double setRealIncome(AppParam params) {
		double income = 0;
		String workType = StringUtil.getString(params.getAttr("workType"));

		if (BorrowConstant.work_type_2.equals(workType)
				|| BorrowConstant.work_type_3.equals(workType)) {

			String incomeStr = StringUtil.getString(params
					.getAttr("incomeMonth"));
			if (StringUtils.hasText(incomeStr)) {
				income = Double.parseDouble(incomeStr) * 10000;// 企业主和个体户总经营流水已万为单位
			}

		} else {
			String incomeStr = StringUtil.getString(params.getAttr("income"));
			if (StringUtils.hasText(incomeStr)) {
				income = Double.parseDouble(incomeStr);
			}
		}

		return income;
	}

	/**
	 * 保存贷款基本信息
	 * 
	 * @param params
	 * @return
	 */
	public AppResult saveBaseInfo(AppParam params) {
		Object applyId = params.getAttr("applyId");
		Object workTypeObj = params.getAttr("workType");

		if (StringUtils.isEmpty(applyId) || StringUtils.isEmpty(workTypeObj)) {
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}

		double income = this.setRealIncome(params);

		AppResult result = this.update(params);
		if (result.isSuccess()) {
			if (!BorrowConstant.work_type_5.equals(params.getAttr("workType")
					.toString())) {
				AppParam incomeParams = new AppParam("borrowIncomeService",
						"update");
				incomeParams.addAttrs(params.getAttr());
				incomeParams.addAttr("income", income);// 真是收入
				SoaManager.getInstance().invoke(incomeParams);
			}
			// 保持城市一致
			if (!StringUtils.isEmpty(params.getAttr("cityName"))) {
				AppParam applyParams = new AppParam("borrowApplyService",
						"update");
				applyParams.addAttr("applyId", applyId);
				applyParams.addAttr("cityName", params.getAttr("cityName"));
				SoaManager.getInstance().invoke(applyParams);
			}
		}

		return result;
	}

	
	/**
	 * 保存贷款资产其他信息
	 * 
	 * @param params
	 * @return
	 */
	public AppResult saveAssetInfo(AppParam params) {
		Object applyId = params.getAttr("applyId");
		String houseType = StringUtil.getString(params.getAttr("houseType"));
		String carType = StringUtil.getString(params.getAttr("carType"));

		if (StringUtils.isEmpty(applyId) || StringUtils.isEmpty(houseType)
				|| StringUtils.isEmpty(carType)) {
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}

		AppParam baseParams = new AppParam();
		baseParams.addAttr("applyId", applyId);
		baseParams.addAttr("houseType", houseType);
		baseParams.addAttr("carType", carType);
		AppResult result = this.update(baseParams);

		if (result.isSuccess()) {
			// 11-有房,但不确认房产类型 2-无房产 3-商品住宅 4-商住两用房 5-军产房 6-办公楼 8-商铺 7-厂房
			// 9-自建房/小产权房 10-经适/限价房 11-房改/危改房
			if (!StringUtils.isEmpty(params.getAttr("houseVal"))) {
				AppParam houseParam = new AppParam("borrowHouseService",
						"update");
				houseParam.addAttr("applyId", params.getAttr("applyId"));
				houseParam.addAttr("houseVal", params.getAttr("houseVal"));
				houseParam.addAttr("houseYears", params.getAttr("houseYears"));
				houseParam.addAttr("housePlace", params.getAttr("housePlace"));
				houseParam.addAttr("houseMortgage",
						params.getAttr("houseMortgage"));
				houseParam.addAttr("houseLoans", params.getAttr("houseLoans"));
				houseParam.addAttr("houseCard", params.getAttr("houseCard"));

				result = SoaManager.getInstance().invoke(houseParam);
				if (!result.isSuccess()) {
					result.setMessage("保存房产信息失败！");
				}
			}

			if (!StringUtils.isEmpty(params.getAttr("carPrice"))) {
				AppParam carParam = new AppParam("borrowCarService", "update");
				carParam.addAttr("applyId", params.getAttr("applyId"));
				carParam.addAttr("carYears", params.getAttr("carYears"));
				carParam.addAttr("carPrice", params.getAttr("carPrice"));
				carParam.addAttr("carUsed", params.getAttr("carUsed"));
				carParam.addAttr("carMortgage", params.getAttr("carMortgage"));
				carParam.addAttr("carBrand", params.getAttr("carBrand"));
				carParam.addAttr("carModel", params.getAttr("carModel"));
				carParam.addAttr("carLocal", params.getAttr("carLocal"));

				result = SoaManager.getInstance().invoke(carParam);
				if (!result.isSuccess()) {
					result.setMessage("保存车产信息失败！");
				}
			}
		}

		return result;
	}

	/**
	 * 保存贷款其他信息
	 * 
	 * @param params
	 * @return
	 */
	public AppResult saveOtherInfo(AppParam params) {
		Object applyId = params.getAttr("applyId");

		if (StringUtils.isEmpty(applyId)) {
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}

		// 优质单修改
		AppParam updateParams = new AppParam();
		
		updateParams = new AppParam("borrowApplyService", "update");
		updateParams.addAttr("applyId", applyId);
		updateParams.addAttr("applyType", "1");
		updateParams.addAttr("haveDetail", 1);// 0未填写 1 填写完全
		SoaManager.getInstance().invoke(updateParams);


		AppParam baseParams = new AppParam();
		baseParams.addAttr("applyId", applyId);
		baseParams.addAttr("sex", params.getAttr("sex"));
		baseParams.addAttr("age", params.getAttr("age"));
		baseParams.addAttr("score", 10000);
		baseParams.addAttr("price", 10000);
		baseParams.addAttr("isLocal", params.getAttr("isLocal"));
		baseParams.addAttr("havePinan", params.getAttr("havePinan"));
		AppResult result = this.update(baseParams);

		Object insurType = params.getAttr("insurType");

		if (!StringUtils.isEmpty(insurType)
				&& !"0".equals(insurType.toString())) {
			AppParam insurParams = new AppParam("borrowInsureService", "update");
			insurParams.addAttr("applyId", applyId);
			insurParams.addAttr("insurType", insurType);
			insurParams.addAttr("insurMonth", params.getAttr("insurMonth"));
			insurParams.addAttr("insurMonthAmt",
					params.getAttr("insurMonthAmt"));
			insurParams.addAttr("insurSelf", params.getAttr("insurSelf"));
			result = SoaManager.getInstance().invoke(insurParams);
			if (!result.isSuccess()) {
				result.setMessage("保存保险信息失败！");
			}
		}

		return result;
	}

	//新一套贷款第一步
	public AppResult saveNewApply1(AppParam params) {
		AppResult result = new AppResult();
		AppParam applyparam = new AppParam("borrowApplyService", "insert");
		Object applyId = params.getAttr("applyId"); 
		if (StringUtils.isEmpty(applyId)) {
			applyparam.addAttrs(params.getAttr());
			result = SoaManager.getInstance().invoke(applyparam);
			applyId = result.getAttr("applyId");
		} else {
			applyparam.setMethod("applyUpdate");
			applyparam.addAttrs(params.getAttr());
			result = SoaManager.getInstance().invoke(applyparam);
		}
		params.addAttr("applyId", applyId);
		result = this.update(params);
		result.putAttr("applyId", applyId);
		return result;
	}

	//新一套贷款第二步
	public AppResult saveNewApply2(AppParam params) {
		AppResult result = new AppResult();
		Object applyId = params.getAttr("applyId");
		if (StringUtils.isEmpty(applyId)) {
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		result = this.update(params);

		AppParam queryParams = new AppParam();
		queryParams.addAttr("applyId", applyId);
		
		int applyType = BorrowConstant.apply_type_1;
		
		queryParams = new AppParam("borrowApplyService", "update");
		queryParams.addAttr("applyId", applyId);
		queryParams.addAttr("applyType", applyType);
		
		queryParams.addAttr("haveDetail", 1);// 0未填写 1 填写完全
		result = SoaManager.getInstance().invoke(queryParams);

		if (result.isSuccess()) {
			params.addAttr("score", 10000);
			params.addAttr("price", 10000);
			result = this.update(params);

			AppParam insurParam = new AppParam("borrowInsureService", "query");// 保险信息
			insurParam.addAttr("applyId", applyId);
			AppResult insurResult = SoaManager.getInstance().invoke(insurParam);
			if (insurResult.getRows().size() == 0) {
				insurParam.setMethod("insert");
				SoaManager.getInstance().invoke(insurParam);
			}
			AppParam houseParam = new AppParam("borrowHouseService", "query");// 房产信息
			houseParam.addAttr("applyId", applyId);
			AppResult houseResult = SoaManager.getInstance().invoke(houseParam);
			if (houseResult.getRows().size() == 0) {
				houseParam.setMethod("insert");
				SoaManager.getInstance().invoke(houseParam);
			}
			AppParam carParam = new AppParam("borrowCarService", "query");// 车产信息
			carParam.addAttr("applyId", applyId);
			AppResult carResult = SoaManager.getInstance().invoke(carParam);
			if (carResult.getRows().size() == 0) {
				carParam.setMethod("insert");
				SoaManager.getInstance().invoke(carParam);
			}

		}
		result.putAttr("applyId", applyId);
		return result;
	}

	//新一套贷款第三步
	public AppResult saveNewApply3(AppParam params) {
		AppResult result = new AppResult();
		Object applyId = params.getAttr("applyId");
		if (StringUtils.isEmpty(applyId)) {
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		result = this.update(params);
		if (result.isSuccess()) {
			AppParam incomeParam = new AppParam("borrowIncomeService", "update");
			incomeParam.addAttrs(params.getAttr());
			SoaManager.getInstance().invoke(incomeParam);
		}

		return result;
	}

	/**
	 * 查询最新的贷款信息
	 * @param params
	 * @return
	 */
	public AppResult queryNewBorrow(AppParam params){
		return super.query(params, NAMESPACE, "queryNewBorrow");
	}

	/**
	 * 查询基本信息(包括房产、车产、收入、保单)
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryBaseInfo(AppParam params) {
		return super.query(params, NAMESPACE,"queryBaseInfo");
	}
	
	/**
	 * 修改资产信息,并返回t_borrow_apply的status字段
	 * @param param
	 * @return
	 */
	public AppResult updateBaseInfo (AppParam param) {
		AppResult result = new AppResult();
		Object applyId = param.getAttr("applyId");
		if (StringUtils.isEmpty(applyId)) {
			throw new SysException("缺少applyId");
		}
		
		int allotFlag =  NumberUtil.getInt(param.getAttr("allotFlag"), 0);
		
		AppParam updateApply = new AppParam("borrowApplyService", "update");
		updateApply.addAttr("grade", param.getAttr("grade"));
		updateApply.addAttr("applyId", applyId);
		updateApply.addAttr("pageReferer", param.getAttr("pageReferer"));
		updateApply.addAttr("channelCode", param.getAttr("channelCode"));
		updateApply.addAttr("channelDetail", param.getAttr("channelDetail"));
		updateApply.addAttr("cityName", param.getAttr("cityName"));
		updateApply.addAttr("applyTime", param.getAttr("applyTime"));
		if(allotFlag == 1){//如果是判断能给网销分配，需要重新改标志，其他分配标志其他地方有改
			updateApply.addAttr("allotFlag", allotFlag);
		}
		SoaManager.getInstance().invoke(updateApply);
		
		
		if (SysParamsUtil.getBoleanByKey("isUpdate_store_applyTime", true)) {
			AppParam storeParam = new AppParam("borrowStoreApplyService", "update");
			storeParam.addAttr("channelCode", param.getAttr("channelCode"));
			storeParam.addAttr("channelDetail", param.getAttr("channelDetail"));
			storeParam.addAttr("applyId", applyId);
			storeParam.addAttr("applyTime", param.getAttr("applyTime"));
			SoaManager.getInstance().invoke(storeParam);
		}
		
		AppParam delParam = new AppParam();
		delParam.setService("borrowBaseService");
		delParam.setMethod("delete");
		delParam.addAttr("applyId", applyId);
		SoaManager.getInstance().invoke(delParam);//清除基本数据 
		
		result = this.insert(param);
		
		if(StringUtils.isEmpty(param.getAttr("income"))){
			param.addAttr("income", 0);
		}
		
		delParam.setService("borrowIncomeService");
		SoaManager.getInstance().invoke(delParam);//清除收入数据
		
		param.setService("borrowIncomeService");
		param.setMethod("insert");
		SoaManager.getInstance().invoke(param);//保存收入信息	
		
		String insurType = StringUtil.getString(param.getAttr("insurType"));
		if(StringUtils.hasText(insurType)){//保存保险信息
			delParam.setService("borrowInsureService");
			SoaManager.getInstance().invoke(delParam);//清除数据
			
			param.setService("borrowInsureService");
			param.setMethod("insert");
			SoaManager.getInstance().invoke(param);
		}
		
		if(!StringUtils.isEmpty(param.getAttr("carType"))){
			delParam.setService("borrowCarService");
			SoaManager.getInstance().invoke(delParam);//清除数据
			
			param.setService("borrowCarService");
			param.setMethod("insert");
			SoaManager.getInstance().invoke(param);
		}
		
		if(!StringUtils.isEmpty(param.getAttr("houseType"))){
			delParam.setService("borrowHouseService");
			SoaManager.getInstance().invoke(delParam);//清除数据
			
			param.setService("borrowHouseService");
			param.setMethod("insert");
			SoaManager.getInstance().invoke(param);
		}
		return result;
	}
	
	/**
	 * 查询转化中的申请的资产信息,以t_dai_borrow表字段为准
	 * @param params
	 * @return
	 */
	public AppResult queryTransferToXxjr (AppParam params) {
		AppResult result = new AppResult();
		result = this.query(params, NAMESPACE, "queryTransferToXxjr");
		return result;
	}

}
