package org.xxjr.busiIn.kf;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Lazy
@Service
public class BorrowHouseService extends BaseService {
	private static final String NAMESPACE = "BORROWHOUSE";

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
	 * @param params
	 * @return
	 */
	public AppResult queryText(AppParam params) {
		return super.query(params, NAMESPACE, "queryText");
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
	
	
	/**
	 * insert
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		params.addAttr("createTime", new Date());
		return super.insert(params, NAMESPACE);
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		AppResult updateResult = super.update(params, NAMESPACE);
		int count  = (Integer)updateResult.getAttr(DuoduoConstant.DAO_Update_SIZE);
		if(count <= 0){
			updateResult = this.insert(params);
		}
		return updateResult;
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
	
	/**
	 * 保存所有信息
	 * @param params
	 * @return
	 */
	public AppResult updateAssetInfo(AppParam params){
       AppResult result = new AppResult();
		Object applyId = params.getAttr("applyId");
		if(StringUtils.isEmpty(applyId)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		
		String houseType = StringUtil.getString(params.getAttr("houseType"));
		String carType = StringUtil.getString(params.getAttr("carType"));
		
		AppParam baseParam = new AppParam("borrowBaseService", "update");
		baseParam.addAttr("applyId", applyId);
		baseParam.addAttr("houseType", houseType);
		baseParam.addAttr("carType", carType);
		
		result = SoaManager.getInstance().invoke(baseParam);
				
		if(result.isSuccess()){
			// 修改房产信息
			if(!StringUtils.isEmpty(params.getAttr("houseVal"))){
				this.update(params);
			}
			
			// 修改车产信息
			if(!StringUtils.isEmpty(params.getAttr("carPrice"))){
				params.setService("borrowCarService");
				params.setMethod("update");
				result = SoaManager.getInstance().invoke(params);
			}
			
			// 修改保险信息
			if(!StringUtils.isEmpty(params.getAttr("insurType"))){
				
				params.setService("borrowInsureService");
				params.setMethod("update");
				result = SoaManager.getInstance().invoke(params);
			}
		}

		result.putAttr("applyId", applyId);
		return result;
	}
}
