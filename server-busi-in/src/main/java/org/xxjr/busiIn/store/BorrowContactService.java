package org.xxjr.busiIn.store;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

@Lazy
@Service
public class BorrowContactService extends BaseService {
	private static final String NAMESPACE = "BORROWCONTACT";

	/**
	 * querys
	 * 
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}

	/**
	 * queryByPage
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE);
	}

	/**
	 * queryCount
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryCount(AppParam params) {
		int size = getDao().count(NAMESPACE, super.COUNT, params.getAttr(),
				params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}

	/**
	 * insert
	 * 
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		params.addAttr("createTime", new Date());
		params.addAttr("createBy", DuoduoSession.getUserName());
		return super.insert(params, NAMESPACE);
	}

	/**
	 * update
	 * 
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		AppResult updateResult = super.update(params, NAMESPACE);
		int count = (Integer) updateResult
				.getAttr(DuoduoConstant.DAO_Update_SIZE);
		if (count <= 0) {
			updateResult = this.insert(params);
		}
		return updateResult;
	}

	/**
	 * delete
	 * 
	 * @param params
	 * @return
	 */
	public AppResult delete(AppParam params) {
		String ids = (String) params.getAttr("ids");
		AppResult result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.addAttr("contactId", id);

				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("contactId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}

	/**
	 * 保存
	 * 
	 * @param param
	 * @return
	 */
	public AppResult save(AppParam param) {
		AppResult result = null;
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> contacts = (List<Map<String, Object>>) param
				.getAttr("contacts");
		for (Map<String, Object> map : contacts) {
			AppParam params = new AppParam();
			if (!StringUtils.isEmpty(map.get("telephone"))
					&& !StringUtils.isEmpty(map.get("name"))) {
				params.addAttrs(map);
				result = this.update(params);
			}
		}
		return result;
	}
}
