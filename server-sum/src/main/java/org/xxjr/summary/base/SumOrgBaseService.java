package org.xxjr.summary.base;

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
import org.xxjr.sys.util.DBConst;

@Lazy
@Service
public class SumOrgBaseService extends BaseService {
	private static final String NAMESPACE = "SUMORGBASE";

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
	 * updateOnlineCount
	 * @param params
	 * @return
	 */
	public AppResult updateOnlineCount(AppParam params) {
		AppResult result = new AppResult();
		int size = getDao().update(NAMESPACE, "updateOnlineCount",params.getAttr(),params.getDataBase());
		if(size == 0){
			result = this.insert(params);
		}else{
			result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
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
				param.setDataBase(DBConst.Key_sum_DB);
				param.addAttr("recordDate", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("recordDate"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/**
	 * 保存门店统计数据
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult save(AppParam params){
		AppResult result = new AppResult();
		//删除老的数据
		super.getDao().delete(NAMESPACE, "deleteByDay", params.getAttr(), params.getDataBase().toString());
		//插入新的数据
		List<Map<String, Object>> list = (List<Map<String, Object>>) params.getAttr("list");
		int size = super.getDao().batchInsert(NAMESPACE, "batchInsert",
				list, params.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Insert_SIZE, size);
		return result;
	}
	
	/**门店日统计
	 * queryByStore
	 * @param params
	 * @return
	 */
	public AppResult queryByStore(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryByStore", "queryByStoreCount");
	}
	/**
	 * queryByStoreCount
	 * @param params
	 * @return
	 */
	public AppResult queryByStoreCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryByStoreCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**
	 * queryStoreMonthCount
	 * @param params
	 * @return
	 */
	public AppResult queryStoreMonthCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryStoreMonthCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**门店月统计
	 * queryByStore
	 * @param params
	 * @return
	 */
	public AppResult queryStoreMonth(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryStoreMonth", "queryStoreMonthCount");
	}
	
	/**城市日统计
	 * queryByCity
	 * @param params
	 * @return
	 */
	public AppResult queryByCity(AppParam params) {
		if(params.getCurrentPage() == -1){
			params.setEveryPage(1000);
			params.setCurrentPage(1);
			return super.queryByPage(params, NAMESPACE, "queryByCity", "queryByCityCount");
		}else{
			return super.queryByPage(params, NAMESPACE, "queryByCity", "queryByCityCount");
		}
	}
	
	/**城市月统计
	 * queryByCity
	 * @param params
	 * @return
	 */
	public AppResult queryMonthByCity(AppParam params) {
		if(params.getCurrentPage() == -1){
			params.setEveryPage(1000);
			params.setCurrentPage(1);
			return super.queryByPage(params, NAMESPACE, "queryMonthByCity", "queryMonthByCityCount");
		}else{
			return super.queryByPage(params, NAMESPACE, "queryMonthByCity", "queryMonthByCityCount");
		}
	}
	/**
	 * queryByCityCount
	 * @param params
	 * @return
	 */
	public AppResult queryByCityCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryByCityCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**
	 * queryMonthByCityCount
	 * @param params
	 * @return
	 */
	public AppResult queryMonthByCityCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryMonthByCityCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**门店本月统计总计
	 * queryByStoreTotal
	 * @param params
	 * @return
	 */
	public AppResult queryByStoreTotal(AppParam params) {
		return super.query(params, NAMESPACE, "queryByStoreTotal");
	}
	
	/**门店月度统计总计
	 * queryOrgMonthTotal
	 * @param params
	 * @return
	 */
	public AppResult queryOrgMonthTotal(AppParam params) {
		return super.query(params, NAMESPACE, "queryOrgMonthTotal");
	}
	
}
