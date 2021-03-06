package org.xxjr.busiIn.store;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busiIn.utils.StoreOptUtil;
import org.xxjr.sys.util.NumberUtil;


@Lazy
@Service
public class StoreExcluesiveNotifyService extends BaseService {
	private static final String NAMESPACE = "STOREEXCLUESIVENOTIFY";

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
				param.addAttr("recordId", id);
				
				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("recordId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
	
	/***
	 * 保存专属单待通知信息
	 * @param param
	 * @return
	 */
	public AppResult save(AppParam params){
		AppResult  result = new AppResult();
		AppParam queryParam = new AppParam();
		int sucSize = 0; // 成功笔数
		int failSize = 0; // 失败笔数
		
		@SuppressWarnings("unchecked")
		List<Map<String,Object>> orderList = (List<Map<String, Object>>) params.getAttr("orderList");
		if(orderList != null && orderList.size() > 0){
			for(Map<String,Object> orderMap:orderList){
				queryParam.addAttr("recCustId", orderMap.get("customerId"));
				queryParam.addAttr("applyId", orderMap.get("applyId"));
				queryParam.addAttr("status", "0");
				result = this.queryCount(queryParam);
				int totalSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.TOTAL_SIZE),0);
				if(totalSize == 0){
					queryParam.addAttr("applyName", orderMap.get("applyName"));
					queryParam.addAttr("orgId", orderMap.get("orgId"));
					result = this.insert(queryParam);
					sucSize++;
					queryParam.clearAttr();
				}else{
					failSize ++;
					queryParam.clearAttr();
					continue;
				}
				
			}
		}
		result.putAttr("sucSize", sucSize);	
		result.putAttr("failSize", failSize);	
		return result;
	}
	
	/***
	 * 专属单七天未处理通知处理
	 * @param params
	 * @return
	 */
	public AppResult notDealNotify(AppParam params){
		int sucSize = 0;
		AppResult result = new AppResult();
		AppParam updateParam = new AppParam();
		params.addAttr("orgId", 236); // 只通知中欣门店
		result = this.query(params,NAMESPACE,"queryNotDealNotify");
		if(result.isSuccess() && result.getRows().size() >0){
			for(Map<String,Object> orderMap : result.getRows()){
				updateParam.setAttr(orderMap);
				// 通知
				StoreOptUtil.sendNotify(updateParam);
				// 修改为已通知
				updateParam.addAttr("status", "1");
				this.update(updateParam);
				updateParam.clearAttr();
				sucSize ++ ;
			}
		}
		result.putAttr("sucSize", sucSize);
		return result;
	}
}
