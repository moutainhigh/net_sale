package org.xxjr.cust.store;

import java.util.Date;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;

@Lazy
@Service
public class OrgService extends BaseService {


	private static final String NAMESPACE = "ORG";
	
	/**
	 * 查询所有门店
	 * @param params
	 * @return
	 */
	public AppResult queryOrgList(AppParam params) {
		return super.query(params, NAMESPACE,"queryOrgList");
	}
	
	/**
	 * 分页所有门店
	 * @param params
	 * @return
	 */
	public AppResult queryOrgPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryOrgList","queryOrgCount");
	}
	


	/**
	 * 查询用户管理门店
	 * @param params
	 * @return
	 */
	public AppResult queryUserOrgs(AppParam params) {
		return super.query(params, NAMESPACE,"queryUserOrgs");
	}

	/**
	 * 查寻数据
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
	 * 添加数据处理
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		params.addAttr("createTime", new Date());
		AppResult result =  super.insert(params, NAMESPACE);
		result.putAttr("orgId", params.getAttr("orgId"));
		return result;
	}
	
	/**
	 * 修改数据处理
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
				param.setDataBase(DBConst.Key_sum_DB);
				param.addAttr("orgId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("orgId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	


}
