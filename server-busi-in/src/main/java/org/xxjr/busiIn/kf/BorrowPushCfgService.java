package org.xxjr.busiIn.kf;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.push.PushPlatformUtils;
import org.xxjr.sys.util.NumberUtil;

@Lazy
@Service
public class BorrowPushCfgService extends BaseService {
	private static final String NAMESPACE = "BORROWPUSHCFG";

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
		AppResult result = super.insert(params, NAMESPACE);
		if (result.isSuccess() && NumberUtil.getInt(params.getAttr("isAllot"), 0) == 1) {
			RedisUtils.getRedisService().del(PushPlatformUtils.REDIS_AVG_THRID_COUNT);
		}
		return result;
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		params.addAttr("updateTime", new Date());
		AppResult result = super.update(params, NAMESPACE);
		if (result.isSuccess()) {
			String pushType = StringUtil.objectToStr(params.getAttr("pushCode"));
			RedisUtils.getRedisService().del(PushPlatformUtils.REDIS_MAX_COUNT_PREFIX+pushType);
			if (NumberUtil.getInt(params.getAttr("isAllot"), 0) == 1) {
				RedisUtils.getRedisService().del(PushPlatformUtils.REDIS_AVG_THRID_COUNT);
				RedisUtils.getRedisService().del(PushPlatformUtils.REDIS_MAX_API_COUNT_PREFIX+pushType);
				RedisUtils.getRedisService().del(PushPlatformUtils.REDIS_MAX_OTHER_COUNT_PREFIX+pushType);
			}
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
				param.addAttr("pushCode", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("pushCode"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
}
