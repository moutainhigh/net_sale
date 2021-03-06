package org.xxjr.busiIn.store.record;

import java.util.Date;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.BorrowConstant;


@Lazy
@Service
public class ThirdExportPoolService extends BaseService {
	private static final String NAMESPACE = "THIRDEXPORTPOOL";

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
		params.addAttr("createBy", DuoduoSession.getUserName());
		return super.insert(params, NAMESPACE);
	}

	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
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
				param.addAttr("applyId", id);

				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("applyId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}

	/**
	 * 查询对外分配池列表
	 * @param params
	 * @return
	 */
	public AppResult queryForeignAllotPond(AppParam params){
		return super.queryByPage(params, NAMESPACE, "queryForeignAllotPond", "queryForeignAllotPondCount");
	}

	/**
	 * 加入第三方分配池
	 * @param params
	 * @return
	 */
	public AppResult checkJoinThreeDate(AppParam params){
		AppResult result = new AppResult();
		AppParam queryParam = new AppParam();
		//外部渠道人员用户ID
		String customerId = StringUtil.objectToStr(params.getAttr("customerId"));
		@SuppressWarnings("unchecked")
		Map<String,Object> orderMap = (Map<String, Object>) params.getAttr("orderMap");
		String applyId = StringUtil.objectToStr(orderMap.get("applyId"));
		String applyStatus = "0";
		String lastStore = "";
		//查询单子信息
		queryParam.addAttr("applyId", applyId);
		queryParam.setService("borrowApplyService");
		queryParam.setMethod("query");
		result =  SoaManager.getInstance().invoke(queryParam);
		if(result.isSuccess() && result.getRows().size() >0){
			applyStatus = StringUtil.getString(result.getRow(0).get("status"));
			lastStore = StringUtil.getString(result.getRow(0).get("lastStore"));
		}
		if(BorrowConstant.apply_status_5.equals(applyStatus) ||
				(BorrowConstant.apply_status_2.equals(applyStatus) && !StringUtils.isEmpty(lastStore))){//已转化或已分配的数据不再加入第三方
			result.setMessage("applyId:"+applyId+"该笔单已挂卖成功或已分配");
			return result;
		}
		AppParam updateOrder = new AppParam("borrowApplyService","update");
		updateOrder.addAttr("applyId", applyId);
		updateOrder.addAttr("status", 5);
		result = SoaManager.getInstance().invoke(updateOrder);

		AppParam updateApply = new AppParam("borrowStoreApplyService","update");
		updateApply.addAttr("applyId", applyId);
		updateApply.addAttr("status", 5);
		result = SoaManager.getInstance().invoke(updateApply);
		AppParam param = new AppParam("thirdExportPoolService","insert");
		param.addAttr("customerId", customerId);
		param.addAttrs(orderMap);
		param.addAttr("downloadStatus", 0);
		result = SoaManager.getInstance().invoke(param);

		//删除网销池数据
		if(BorrowConstant.apply_status_2.equals(applyStatus)){
			AppParam deleteParam = new AppParam("netStorePoolService","delete");
			deleteParam.addAttr("applyId", applyId);
			result = SoaManager.getInstance().invoke(deleteParam);
		}
	return result;
}



/**
 * 查询对外分配池列表(导出所用)
 * @param params
 * @return
 */
public AppResult queryForeignAllotList(AppParam params){
	if(params.getCurrentPage() == -1){
		params.setEveryPage(1000);
		params.setCurrentPage(1);
		return super.queryByPage(params, NAMESPACE, "queryForeignAllotList", "queryForeignAllotListCount");
	}else{
		return super.queryByPage(params, NAMESPACE, "queryForeignAllotList", "queryForeignAllotListCount");
	}
}
}
