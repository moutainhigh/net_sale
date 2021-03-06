package org.xxjr.busiIn.kf;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.exception.SysException;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.BorrowChannelUtil;


@Lazy
@Service
public class BorrowChannelDtlService extends BaseService {
	private static final String NAMESPACE = "BORROWCHANNELDTL";

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
	 * queryShowByPage
	 * @param params
	 * @return
	 */
	public AppResult queryShowByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryShow", "queryShowCount");
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
		Object detailCode = params.getAttr("detailCode");
		if(StringUtils.isEmpty(detailCode)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		AppParam queryParam = new AppParam();
		queryParam.addAttr("detailCode", detailCode);
		AppResult queryResult = this.query(queryParam);
		if(queryResult.getRows().size() > 0){
			throw new SysException("渠道详细代号" + detailCode + "已存在");
		}
		params.addAttr("createTime", new Date());
		params.addAttr("createBy", DuoduoSession.getUserName());
		AppResult result = super.insert(params, NAMESPACE);
		if(result.isSuccess()){
			BorrowChannelUtil.refreshBorrowChannelDtl();
		}
		
		return result;
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		Object detailCode = params.getAttr("detailCode");
		if(StringUtils.isEmpty(detailCode)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		AppResult result = super.update(params, NAMESPACE);
		if(result.isSuccess()){
			BorrowChannelUtil.refreshBorrowChannelDtl();
		}
		return result;
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
				param.addAttr("detailCode", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("detailCode"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		
		if(result.isSuccess()){
			BorrowChannelUtil.refreshBorrowChannelDtl();
		}
		return result;
	}
	
}
