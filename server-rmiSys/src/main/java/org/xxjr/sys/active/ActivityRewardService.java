package org.xxjr.sys.active;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;

@Lazy
@Service
public class ActivityRewardService extends BaseService {
	private static final String NAMESPACE = "ACTIVITYREWARD";

	public AppResult query(AppParam context) {
		return super.query(context, NAMESPACE);
	}
	
	public AppResult queryByPage(AppParam context) {
		return super.queryByPage(context, NAMESPACE);
	}
	
	public AppResult queryCount(AppParam context) {
		int size = getDao().count(NAMESPACE, super.COUNT,context.getAttr(),context.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	public AppResult queryCustByPage(AppParam context) {
		return super.queryByPage(context, NAMESPACE, "queryCust", "queryCustCount");
	}
	
	public AppResult queryCustCount(AppParam context) {
		int size = getDao().count(NAMESPACE, "queryCustCount",context.getAttr(),context.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	public AppResult insert(AppParam context) {
		context.addAttr("createTime", new Date());
		context.addAttr("createBy", DuoduoSession.getUserName());
		return super.insert(context, NAMESPACE);
	}
	public AppResult update(AppParam context) {
		return super.update(context, NAMESPACE);
	}
	
	public AppResult delete(AppParam context) {
		String ids = (String) context.getAttr("ids");
		AppResult  result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.addAttr("rewardDtlId", id);
				param.setDataBase(DBConst.Key_sys_DB);
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(context.getAttr("rewardDtlId"))) {
			result = super.delete(context, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
}
