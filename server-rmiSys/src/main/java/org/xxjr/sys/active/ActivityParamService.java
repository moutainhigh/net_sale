package org.xxjr.sys.active;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.xxjr.sys.util.DBConst;

@Lazy
@Service
public class ActivityParamService extends BaseService {
	static final String NAMESPACE = "ACTIVITYPARAM";

	public AppResult query(AppParam context) {
		return super.query(context, NAMESPACE);
	}
	
	public AppResult queryByPage(AppParam context) {
		return super.queryByPage(context, NAMESPACE);
	}
	
	public AppResult queryCount(AppParam context) {
		int size = getDao().count(NAMESPACE, super.COUNT,context.getAttr(), context.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	public AppResult insert(AppParam context) {
		context.addAttr("createTime", new Date());
		context.addAttr("createBy", DuoduoSession.getUserName());
		AppResult result = super.insert(context, NAMESPACE);
		return result;
	}
	
	public AppResult update(AppParam context) {
		return super.update(context, NAMESPACE);
	}
	
	public AppResult delete(AppParam context) {
		String ids = (String) context.getAttr("ids");
		AppResult result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.addAttr("paramId", id);
				param.setDataBase(DBConst.Key_sys_DB);
				result = super.delete(param, NAMESPACE);
			}
		} else if (context.getAttr("paramId") != null) {
			result = super.delete(context, NAMESPACE);
		} else if (context.getAttr("activeId") != null) {
			result = super.delete(context, NAMESPACE);	
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
}
