package org.xxjr.busiIn.kf;

import java.util.Date;
import java.util.List;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.SysException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.NumberUtil;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;

@Lazy
@Service
public class BorrowTeamService extends BaseService {
	private static final String NAMESPACE = "BORROWTEAM";

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
		params.addAttr("updateTime", new Date());
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
				param.addAttr("teamNo", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("teamNo"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	public AppResult save (AppParam params) {
		AppResult result = new AppResult();
		result = this.update(params);
		int count = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Update_SIZE), 0);
		if (0 >= count) {
			result = this.insert(params);
		}
		return result;
	}
	
	public AppResult queryChannels(AppParam params){
		AppResult result = new AppResult();
		if (StringUtils.isEmpty(params.getAttr("teamNo"))) {
			throw new SysException(DuoduoError.UPDATE_NO_PARAMS);
		}
		AppResult queryResult = this.query(params, NAMESPACE, "queryChannels");
		if (queryResult.getRows().size() > 0 && !StringUtils.isEmpty(queryResult.getRow(0).get("channels"))) {
			String channelStr = StringUtil.objectToStr(queryResult.getRow(0).get("channels"));
			result.putAttr("channels", channelStr.split(","));
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public AppResult updateChannels(AppParam params){
		AppResult result = new AppResult();
		List<String> channels = (List<String>) params.getAttr("channels");
		if (channels == null || channels.size() == 0) {
			throw new SysException(DuoduoError.UPDATE_NO_PARAMS);
		}
		int size = getDao().update(NAMESPACE, "updateChannels", params.getAttr(), params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}
}
