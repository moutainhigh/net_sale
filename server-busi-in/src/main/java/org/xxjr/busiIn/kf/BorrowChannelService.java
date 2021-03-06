package org.xxjr.busiIn.kf;

import java.util.Date;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.BorrowChannelUtil;


@Lazy
@Service
public class BorrowChannelService extends BaseService {
	private static final String NAMESPACE = "BORROWCHANNEL";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	
	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult exsitChannel(AppParam params) {
		return super.query(params, NAMESPACE,"exsitChannel");
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
	
	public AppResult queryInfos(AppParam params){
		return super.query(params, NAMESPACE, "queryInfos");
	}
	
	/**
	 * insert
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		Object channelCode = params.getAttr("channelCode");
		if(StringUtils.isEmpty(channelCode)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		AppParam queryParam = new AppParam();
		queryParam.addAttr("channelCode", channelCode);
		AppResult queryResult = this.exsitChannel(queryParam);
		if(queryResult.getRows().size() > 0){
			Map<String,Object> channelMap = queryResult.getRow(0);
			if(channelMap != null && !StringUtils.isEmpty(channelMap.get("channelCodes"))){
				String channelCodes = StringUtil.getString(channelMap.get("channelCodes"));
				throw new SysException("跟渠道[" + channelCodes +"]存在包含关系，添加无效");
			}
		}
		params.addAttr("createTime", new Date());
		params.addAttr("createBy", DuoduoSession.getUserName());
		AppResult result = super.insert(params, NAMESPACE);
		if(result.isSuccess()){
			BorrowChannelUtil.refreshBorrowChannel();
		}
		return result;
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		Object channelCode = params.getAttr("channelCode");
		if(StringUtils.isEmpty(channelCode)){
			throw new AppException(DuoduoError.UPDATE_NO_PARAMS);
		}
		AppResult result = super.update(params, NAMESPACE);
		if(result.isSuccess()){
			BorrowChannelUtil.refreshBorrowChannel();
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
				param.addAttr("channelCode", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("channelCode"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		BorrowChannelUtil.refreshBorrowChannel();
		return result;
	}
}
