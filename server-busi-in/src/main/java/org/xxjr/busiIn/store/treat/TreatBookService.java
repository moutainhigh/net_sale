package org.xxjr.busiIn.store.treat;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Lazy
@Service
public class TreatBookService extends BaseService {
	private static final String NAMESPACE = "TREATBOOK";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * 查询预约信息
	 * @param params
	 * @return
	 */
	public AppResult queryBookInfo(AppParam params) {
		return super.query(params, NAMESPACE, "queryBookInfo");
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
	 * queryShow
	 * @param params
	 * @return
	 */
	public AppResult queryShow(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryShow","queryShowCount");
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
		params.addAttr("createBy", params.getAttr("sessionUserName"));
		params.addAttr("updateTime", new Date());
		return super.insert(params, NAMESPACE);
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		params.addAttr("updateBy", params.getAttr("sessionUserName"));
		params.addAttr("updateTime", new Date());
		AppResult result =  super.update(params, NAMESPACE);
		if((Integer)result.getAttr(DuoduoConstant.DAO_Update_SIZE) == 0){
			result = this.insert(params);
		}
		
		return result;
	}
	
	/**
	 * deleteBoook
	 * @param params
	 * @return
	 */
	public AppResult deleteBoook(AppParam params) {
		int size = getDao().delete(NAMESPACE, "deleteBoook",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
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
}
