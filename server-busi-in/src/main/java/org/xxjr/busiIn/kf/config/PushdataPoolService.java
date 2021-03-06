package org.xxjr.busiIn.kf.config;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.IDCardValidate;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.NumberUtil;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;

@Lazy
@Service
public class PushdataPoolService extends BaseService {
	public static final String NAMESPACE = "PUSHDATAPOOL";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * queryByPage
	 * @param params
	 * @return
	 */
	public AppResult queryByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE);
	}
	
	public AppResult queryPushData(AppParam params){
		return super.query(params, NAMESPACE, "queryPushData");
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
		
		AppParam queryParam = new AppParam();
		queryParam.addAttr("telephone", params.getAttr("telephone"));
		AppResult queryCount = this.queryCount(queryParam);
		if (NumberUtil.getInt(queryCount.getAttr(DuoduoConstant.TOTAL_SIZE), 0) > 0) {
			return CustomerUtil.retErrorMsg("数据已存在！");
		}
		
		if (StringUtils.isEmpty(params.getAttr("channelCode"))) {
			params.addAttr("channelCode", CustConstant.CUST_SOURCETYPE_DEFAULT);
			params.addAttr("channelDetail", CustConstant.CUST_SOURCETYPE_DEFAULT);
		}
		String identifyNo = StringUtil.objectToStr(params.getAttr("identifyNo"));
		if (!StringUtils.isEmpty(identifyNo)) {
			Integer age = IDCardValidate.getCardAge(identifyNo);
			String sex = IDCardValidate.getCardSex(identifyNo);
			params.addAttr("age", age);
			params.addAttr("sex", sex);
		}
		params.addAttr("createTime", new Date());
		return super.insert(params, NAMESPACE);
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
				param.addAttr("pushId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("pushId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
}
