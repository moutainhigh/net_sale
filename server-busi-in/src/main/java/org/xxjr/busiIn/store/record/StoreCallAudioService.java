package org.xxjr.busiIn.store.record;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.StoreConstant;
import org.xxjr.store.util.StoreApplyUtils;


@Lazy
@Service
public class StoreCallAudioService extends BaseService {
	private static final String NAMESPACE = "STORECALLAUDIO";

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
		AppResult result = new AppResult();
		params.addAttr("uploadTime", new Date());
		result = super.insert(params, NAMESPACE);
		if(result.isSuccess()){
			// 删除通话录音缓存
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_CALL_AUDIO_RECORD 
					+ params.getAttr("applyId") + StoreConstant.IS_ADMIN_TRUE);
			// 删除通话录音缓存
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_CALL_AUDIO_RECORD 
					+ params.getAttr("applyId") + StoreConstant.IS_ADMIN_FALSE);
		}
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
				param.addAttr("audioId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("audioId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	
	/**
	 * queryByPage
	 * @param params
	 * @return
	 */
	public AppResult queryCallAudio(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryCallAudio","queryCallAudioCount");
	}
	
	/**
	 * queryShow
	 * @param params
	 * @return
	 */
	public AppResult queryShow(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryShow","queryShowCount");
	}
	
	/**
	 * 查询未关联的通话录音记录
	 * @param params
	 * @return
	 */
	public AppResult queryNotRelationAudio(AppParam params) {
		return super.query(params, NAMESPACE,"queryNotRelationAudio");
	}
	
}
