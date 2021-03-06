package org.xxjr.busiIn.store.treat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Lazy
@Service
public class TreatLoanRecordService extends BaseService {
	private static final String NAMESPACE = "TREATLOANRECORD";

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
				param.addAttr("recordId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("recordId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	/**
	 * 删除数据
	 * @param params
	 * @return
	 */
	public AppResult deleteBy(AppParam params) {
		int size = getDao().delete(NAMESPACE, "deleteBy",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * 保存数据
	 * @param params
	 * @return
	 * @throws ParseException 
	 */
	@SuppressWarnings("unchecked")
	public AppResult saveData(AppParam params) throws ParseException {
		String applyId = StringUtil.getString(params.getAttr("applyId"));
		String reContractId = StringUtil.getString(params.getAttr("reContractId"));
		AppResult result = new AppResult();
		if(!StringUtils.isEmpty(applyId)){
			AppResult appResult = this.deleteBy(params);
			if(appResult.isSuccess()){
				List<Map<String,Object>> listRecord = (List<Map<String,Object>>)params.getAttr("listRecord");
				for(Map<String,Object> listMap : listRecord){
					String lendDate = null;
					if(!StringUtils.isEmpty(listMap.get("LendDate"))){
						DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						Date date = sdf.parse(StringUtil.getString(listMap.get("LendDate")));
						SimpleDateFormat simpFormat = new SimpleDateFormat ("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK);
				        Date payDate =  simpFormat.parse(date.toString());
				        lendDate = DateUtil.toStringByParttern(payDate, DateUtil.DATE_PATTERN_YYYY_MM_DD);
					}
					AppParam recordParams  = new AppParam();
					recordParams.addAttr("applyId",applyId);
					recordParams.addAttr("reContractId",reContractId);
					recordParams.addAttr("loanNo",params.getAttr("loanNo"));
					recordParams.addAttr("lendAmount",listMap.get("LendAmount"));
					recordParams.addAttr("lendDate",lendDate);
					result = this.insert(recordParams);
				}
			}
		}
		return result;
	}
}
