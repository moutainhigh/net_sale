package org.xxjr.sys.job;

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
public class JobLockService extends BaseService {
	private static final String NAMESPACE = "JOBLOCK";

	public AppResult query(AppParam context) {
		return super.query(context, NAMESPACE);
	}
	
	/**
	 * 查询锁定30分钟及以上的锁定任务
	 * @param context
	 * @return
	 */
	
	public AppResult queryoften(AppParam context){
		return super.query(context,NAMESPACE,"queryoften");
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
	
	public AppResult insert(AppParam context) {
		context.addAttr("createTime", new Date());
		context.addAttr("createBy", DuoduoSession.getUserName());
		AppResult result = super.insert(context, NAMESPACE);
		result.putAttr("lockId", context.getAttr("lockId"));
		return result;
	}
	public AppResult update(AppParam context) {
		return super.update(context, NAMESPACE);
	}
	
	public AppResult delete(AppParam context) {
		AppResult  result = null;
		if (!StringUtils.isEmpty(context.getAttr("ids"))) {
			String ids = (String) context.getAttr("ids");
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.addAttr("lockId", id);
				param.setDataBase(DBConst.Key_sys_DB);
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(context.getAttr("lockId"))) {
			result = super.delete(context, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
}
