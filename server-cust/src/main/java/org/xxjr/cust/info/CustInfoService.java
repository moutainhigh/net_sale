package org.xxjr.cust.info;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.exception.SysException;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.sys.util.DBConst;

@Lazy
@Service
public class CustInfoService extends BaseService {
	private static final String NAMESPACE = "CUSTINFO";

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
	 * queryByPage
	 * @param params
	 * @return
	 */
	public AppResult queryCustInfoByOpenId(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryCustInfoByOpenId",super.COUNT);
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
	 * 查询提现参数
	 * @param params
	 * @return
	 */
	public AppResult queryPayInfo(AppParam params) {
		return super.query(params, NAMESPACE, "queryPayInfo");
	}
	
	/**
	 * 查询简要的用户信息
	 * @param params
	 * @return
	 */
	public AppResult queryPriefCustInfo(AppParam params) {
		return super.query(params, NAMESPACE, "queryPriefCustInfo");
	}
	
	
	/**
	 * queryViewCount
	 * @param params
	 * @return
	 */
	public AppResult queryViewCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryViewCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * queryGradeInfo
	 * @param params
	 * @return
	 */
	public AppResult queryGradeInfo(AppParam params) {
		return super.query(params, NAMESPACE, "queryGradeInfo");
	}
	
	/**
	 * insert
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		params.addAttr("createTime", new Date());
		params.addAttr("createBy", DuoduoSession.getUserName());
		params.addAttr("company", params.getAttr("orgName"));
		return super.insert(params, NAMESPACE);
	}
	
	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		params.addAttr("updateTime", new Date());
		params.addAttr("company", params.getAttr("orgName"));
		AppResult result = super.update(params, NAMESPACE);
		return result;
	}
	
	/**
	 * 批量审核头像（通过/不通过）
	 * autoAudit
	 * @param params
	 * @return
	 */
	public AppResult auditAll(AppParam params) {
		String ids = (String) params.getAttr("ids");
		AppResult  result = new AppResult();
		if (StringUtils.isEmpty(ids)) {
			throw new SysException("没有选择数据！");
		}
		for (String id : ids.split(",")) {
			AppParam updateParam = new AppParam();
			updateParam.addAttr("customerId",id);
			updateParam.addAttr("headStatus",params.getAttr("status"));
			updateParam.addAttr("auditDesc",params.getAttr("auditDesc"));
			updateParam.addAttr("auditBy", StringUtils.isEmpty(params.getAttr("auditBy")) ?
					DuoduoSession.getUserName() :params.getAttr("auditBy")
				);
			updateParam.addAttr("auditTime",new Date());
			updateParam.setDataBase(DBConst.Key_cust_DB);
			AppResult updateResult = this.update(updateParam);
			if(updateResult.isSuccess()){
				RedisUtils.getRedisService().del(CustomerIdentify.CacheKey_PASS + id);
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
				param.setDataBase(DBConst.Key_cust_DB);
				param.addAttr("customerId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("customerId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	
	/**信贷经理个人信息
	 * lendCustInfo
	 * @param params
	 * @return
	 */
	public AppResult lendCustInfo(AppParam params) {
		return super.query(params, NAMESPACE, "lendCustInfo");
	}
}
