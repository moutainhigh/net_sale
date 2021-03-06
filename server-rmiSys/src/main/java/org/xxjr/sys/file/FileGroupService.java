package org.xxjr.sys.file;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;
import org.xxjr.sys.util.FileGroupUtil;

@Lazy
@Service
public class FileGroupService extends BaseService {
	private static final String NAMESPACE = "FILEGROUP";

	/**
	 * 查寻数据
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * 分页查寻数据
	 * @param params
	 * @return
	 */
	public AppResult queryByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE);
	}
	
	/**
	 * querySummary
	 * @param params
	 * @return
	 */
	public AppResult querySummary(AppParam params) {
		return super.query(params, NAMESPACE, "querySummary");
	}
	
	/**
	 * 查寻分页统计数据
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
	 * 添加数据处理
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		AppResult result = new AppResult();
		params.addAttr("updateTime", new Date());
		params.addAttr("updateBy", DuoduoSession.getUserName());
		result = super.insert(params, NAMESPACE);
		if(result.isSuccess()){
			RedisUtils.getRedisService().del(FileGroupUtil.KEY_CONFIG);
		}
		return result;
	}
	
	/**
	 * 修改数据处理
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		AppResult result = new AppResult();
		params.addAttr("updateTime", new Date());
		params.addAttr("updateBy", DuoduoSession.getUserName());
		result = super.update(params, NAMESPACE);
		if(result.isSuccess()){
			RedisUtils.getRedisService().del(FileGroupUtil.KEY_CONFIG);
		}
		return result;
	}
	
	public AppResult delete(AppParam params) {
		String ids = (String) params.getAttr("ids");
		AppResult  result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.setDataBase(DBConst.Key_sys_DB);
				param.addAttr("groupCode", id);
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("groupCode"))) {
			result = super.delete(params, NAMESPACE);
			if(result.isSuccess()){
				RedisUtils.getRedisService().del(FileGroupUtil.KEY_CONFIG);
			}
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
}
