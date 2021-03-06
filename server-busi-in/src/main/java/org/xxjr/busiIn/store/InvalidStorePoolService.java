package org.xxjr.busiIn.store;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.BorrowConstant;
import org.xxjr.busiIn.utils.StoreAlllotUtils;
import org.xxjr.busiIn.utils.StoreOptUtil;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.store.util.StoreApplyUtils;
import org.xxjr.sys.util.NumberUtil;

@Lazy
@Service
public class InvalidStorePoolService extends BaseService {
	private static final String NAMESPACE = "INVALIDSTOREPOOL";

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
	 * 查询无效订单
	 * @param params
	 * @return
	 */
	public AppResult queryInvalidOrder(AppParam params) {
		return super.query(params, NAMESPACE,"queryInvalidOrder");
	}
	/**
	 * 批量同步无效订单池
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult batchDealInvalidPool(AppParam params) {
		AppResult result = new AppResult();
		List<Map<String, Object>> orderList = (List<Map<String, Object>>)params.getAttr("orderList");
		int totalSize = 0 ;
		if(!StringUtils.isEmpty(orderList)){
			totalSize = orderList.size();
		}
		if(totalSize > 0){
			int insertSize = getDao().batchInsert(NAMESPACE,"batchInsertInvalidPool",orderList,params.getDataBase());
			int deleteSize = 0;
			if(insertSize > 0){
				result.putAttr(DuoduoConstant.DAO_Insert_SIZE, insertSize);
				//查询已加入无效订单池但网销申请表暂未删除的订单
				AppResult queryResult = this.queryRelationInvalidOrder(params);
				if(queryResult.getRows().size() > 0){
					deleteSize = getDao().batchDelete(NAMESPACE,"batchDeleteInvalidOrder",queryResult.getRows(),params.getDataBase());
					result.putAttr(DuoduoConstant.DAO_Delete_SIZE, deleteSize);
					//删除专属单
					getDao().batchDelete(NAMESPACE,"batchDeleteExclusiveOrder",queryResult.getRows(),params.getDataBase());
					
					// 删除缓存
					for(Map<String,Object> map : queryResult.getRows()){
						String applyId = StringUtil.getString(map.get("applyId"));
						// 删除订单缓存
						RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_INFO + applyId);
						RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_MAININFO + applyId);
						RedisUtils.getRedisService().del(StoreApplyUtils.BORROW_APPLY_INFO + applyId);
						RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_EXEC_ORDER +  applyId);
					}
				}
			}
			result.setMessage("无效订单总共："+totalSize+"单，成功加入无效订单池："+insertSize+"单，成功删除无效订单："+deleteSize+"单。");
		}else{
			result.setMessage("暂时没有符合条件的无效订单");
		}
		return result;
	}
	/**
	 * 手动操作批量插入无效订单池
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult batchInsertInvalidPool(AppParam params) {
		AppResult result = new AppResult();
		List<Map<String, Object>> orderList = (List<Map<String, Object>>)params.getAttr("orderList");
		int totalSize = 0 ;
		if(!StringUtils.isEmpty(orderList)){
			totalSize = orderList.size();
		}
		if(totalSize > 0){
			//批量插入数据
			int insertSize = getDao().batchInsert(NAMESPACE,"batchInsertInvalidPool",orderList,params.getDataBase());
			int deleteSize = 0;
			if(insertSize > 0){
				result.putAttr(DuoduoConstant.DAO_Insert_SIZE, insertSize);
				AppParam queryParam = new AppParam();
				AppResult queryResult = null;
				for(Map<String, Object> map : orderList){
					queryParam.addAttr("applyId", map.get("applyId"));
					//查询是否插入成功
					queryResult = this.queryCount(queryParam);
					int count = NumberUtil.getInt(queryResult.getAttr(DuoduoConstant.TOTAL_SIZE),0);
					if(count > 0){
						//删除数据
						int deleteCount = getDao().delete(NAMESPACE,"deleteInvalidOrder",map,params.getDataBase());
						deleteSize += deleteCount;
						//删除专属单
						AppParam deleteParam = new AppParam("exclusiveOrderService","delete");
						String applyId = StringUtil.getString(map.get("applyId"));
						deleteParam.addAttr("applyId", applyId);
						SoaManager.getInstance().invoke(deleteParam);
						
						// 删除订单缓存
						RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_INFO + applyId);
						RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_MAININFO + applyId);
						RedisUtils.getRedisService().del(StoreApplyUtils.BORROW_APPLY_INFO + applyId);
						RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_EXEC_ORDER +  applyId);
					}
					queryParam.removeAttr("applyId");
					queryResult = null;
				}
				result.putAttr(DuoduoConstant.DAO_Delete_SIZE, deleteSize);
			}
			result.setMessage("无效订单总共："+totalSize+"单，成功加入无效订单池："+insertSize+"单，成功删除无效订单："+deleteSize+"单。");
		}else{
			result.setMessage("暂时没有符合条件的无效订单");
		}
		return result;
	}
	
	/**
	 * 查询已加入无效订单池但网销申请表暂未删除的订单
	 * @param params
	 * @return
	 */
	public AppResult queryRelationInvalidOrder(AppParam params) {
		return super.query(params, NAMESPACE,"queryRelationInvalidOrder");
	}
	
	/**
	 * 查询无效池的订单
	 */
	public AppResult queryInvalidPool(AppParam params){
		String tableName = StoreOptUtil.getTableName(null);
		params.addAttr("tableName", tableName);
		return super.queryByPage(params, NAMESPACE,"queryInvalidPool","queryInvalidPoolCount");
	}
	/**
	 * 无效池的单重新分配
	 */
	
	public AppResult invalidOrderAllot(AppParam params){

		AppResult result = new AppResult();
		//转信贷经理的用户ID
		String customerId = StringUtil.objectToStr(params.getAttr("customerId"));
		//门店
		String orgId = StringUtil.objectToStr(params.getAttr("orgId"));
		//当前登录人ID
		String custId = StringUtil.objectToStr(params.getAttr("custId"));
		// 获取用户信息
		String userOrgs = "";
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(custId);
		if (custInfo != null) {
			userOrgs = StringUtil.getString(custInfo.get("userOrgs"));
		}
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> orders = (List<Map<String, Object>>) params.getAttr("orders");
		int insertSize = 0;
		StringBuffer strBuff = new StringBuffer();
		for (Map<String, Object>orderMap : orders) {
			String applyId = StringUtil.objectToStr(orderMap.get("applyId"));
			// 没有管理门店权限的不允许转非本门店的单子
			if(!StoreAlllotUtils.isOrderTransOther(orgId, userOrgs)){
				result.setSuccess(false);
				result.setMessage("非本门店单不允许再分配");
				return result;
			}
			//转入门店申请单表中
			AppParam queryParam = new AppParam("borrowStoreApplyService","query");
			queryParam.addAttr("applyId", applyId);
			result = SoaManager.getInstance().callNoTx(queryParam);
			if(result.getRows().size()==0){
				AppParam insertParam = new AppParam("borrowStoreApplyService","insertByBorrowApply");
				insertParam.addAttr("applyId", applyId);
				result = SoaManager.getInstance().invoke(insertParam);
			}else{ // 如果存在且处理人不为空
				Map<String,Object> applyMap = result.getRow(0);
				String lastStore = StringUtil.getString(applyMap.get("lastStore"));
				if(!StringUtils.isEmpty(lastStore)){
					strBuff.append("applyId:"+applyId+"已分配");
					break;
				}
			}
			if(result.isSuccess()){
				AppParam updateParam = new AppParam("borrowStoreApplyService","update");
				updateParam.addAttr("applyId", applyId);
				updateParam.addAttr("orderStatus", "-1");
				updateParam.addAttr("orderType", "2");
				updateParam.addAttr("allotBy", custId);
				updateParam.addAttr("lastStore",customerId);
				updateParam.addAttr("orgId", orgId);//门店
				updateParam.addAttr("allotDesc", "无效池订单");
				result = SoaManager.getInstance().invoke(updateParam);
				
				//删除无效池数据
				if(result.isSuccess()){
					AppParam deleteParam = new AppParam();
					deleteParam.addAttr("applyId", applyId);
					delete(deleteParam);
				}
				
				StoreOptUtil.insertStoreRecord(applyId,customerId, BorrowConstant.STORE_OPER_0,
				"无效池订单[CUSTID=]" + custId, 0,2,0,1);
				
				Map<String,Object> dealMap = new HashMap<String, Object>();
				//同步orderStatus
				dealMap.put("applyId", applyId);
				dealMap.put("orderStatus", "-1");
				dealMap.put("lastStore", customerId);
				StoreOptUtil.dealStoreOrderByMq(customerId,"handelOrderType", dealMap);
			}
			insertSize ++;
		}
		result.setMessage("总共分单："+orders.size()+"单，成功分单："+insertSize+"单。失败描述："+ strBuff.toString());
		return result;
	}
}
