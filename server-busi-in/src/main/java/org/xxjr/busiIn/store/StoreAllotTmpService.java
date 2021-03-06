package org.xxjr.busiIn.store;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.ddq.active.mq.store.StoreTaskSend;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.BorrowConstant;
import org.xxjr.busi.util.StoreConstant;
import org.xxjr.busi.util.StoreSeparateUtils;
import org.xxjr.busiIn.utils.StoreAlllotUtils;
import org.xxjr.busiIn.utils.StoreOptUtil;
import org.xxjr.sys.util.NumberUtil;

@Lazy
@Service
@Slf4j
public class StoreAllotTmpService extends BaseService {
	private static final String NAMESPACE = "STOREALLOTTMP";

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
	 * queryGroupByCity
	 * @param params
	 * @return
	 */
	public AppResult queryGroupByCity(AppParam params) {
		return super.query(params, NAMESPACE, "queryGroupByCity");
	}


	/**
	 *给业务员分一手单
	 * @param params
	 * @return
	 */
	public AppResult storeAllotOrder1(AppParam params){
		AppResult result = new AppResult();		

		// 查询给业务员分单的一手单子
		AppResult storeAllotResult = super.query(params, NAMESPACE, "getStoreAllotOrder1");
		int size = storeAllotResult.getRows().size();
		Object customerId = params.getAttr("customerId");
		int applyType = NumberUtil.getInt(params.getAttr("applyType"));
		String recordDate = DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD);

		result.putAttr("realAllotCount", size);

		if(size > 0){
			for(Map<String,Object>storeAllotMap : storeAllotResult.getRows()){
				Object applyId = storeAllotMap.get("applyId");
				Object orderType = params.getAttr("orderType");	

				params.addAttr("applyId", applyId);
				int updateSize = getDao().update(NAMESPACE, "storeAllotOrderByUpdate", 
						params.getAttr(), params.getDataBase());

				if(updateSize > 0){

					//插入门店人员操作记录
					StoreOptUtil.insertStoreRecord(applyId, customerId, BorrowConstant.STORE_OPER_0, 
							"系统自动分单", 0, orderType, 0, 1);

					//预先删除workList 表记录
					AppParam workListParam = new AppParam("workListService","deleteByExtraId"); 
					workListParam.addAttr("extraId", applyId);
					workListParam.addAttr("workType", 1);
					SoaManager.getInstance().invoke(workListParam);

					//增加新的任务记录
					workListParam = new AppParam("workListService","batchInsertWork"); 
					workListParam.addAttr("applyId", applyId);
					workListParam.addAttr("customerId", customerId);
					SoaManager.getInstance().invoke(workListParam);

					//删除分配表里面的数据
					workListParam = new AppParam(); 
					workListParam.addAttr("applyId", applyId);
					this.delete(workListParam);

					workListParam = null;


				}
			}

			//更新分配数量的记录
			AppParam updateParam = new AppParam("storeAllotRecordService","queryCount");
			updateParam.addAttr("customerId", customerId);
			updateParam.addAttr("recordDate", recordDate);
			AppResult updateResult = SoaManager.getInstance().invoke(updateParam);
			int count = NumberUtil.getInt(updateResult.getAttr(DuoduoConstant.TOTAL_SIZE));


			if(count > 0){
				updateParam.setMethod("update");
				updateParam.addAttr("addCount", size);
				updateParam.addAttr("addTotalCount", 1);
				if(applyType == 1){
					updateParam.addAttr("addAllotSeniorCount", 1);
				}
				if(applyType == 2){
					updateParam.addAttr("addAllotNotFillCount", 1);
				}
			}else{
				updateParam.setMethod("insert");
				updateParam.addAttr("totalCount", size);
				if(applyType == 1){
					updateParam.addAttr("allotSeniorCount", size);
				}
				if(applyType == 2){
					updateParam.addAttr("allotNotFillCount", size);
				}
			}

			SoaManager.getInstance().invoke(updateParam);

			updateResult = null;
			updateParam = null;
		}
		return result;
	}

	/**
	 * 批量插入待分配的新单
	 * @param params
	 * @return
	 */
	public AppResult batchInsertNewOrder(AppParam params) {
		int size = getDao().insert(NAMESPACE, "batchInsertNewOrder", params.getAttr(), params.getDataBase());
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Insert_SIZE, size);
		return backContext;
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
	 * updateAllotFlag
	 * @param params
	 * @return
	 */
	public AppResult updateAllotFlag(AppParam params) {
		int size = getDao().update(NAMESPACE, "updateAllotFlag", params.getAttr(), params.getDataBase());
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return backContext;
	}
	

	/**
	 * deleteAll
	 * @param params
	 * @return
	 */
	public AppResult deleteAll(AppParam params) {
		int size = getDao().delete(NAMESPACE, "deleteAll", params.getAttr(), params.getDataBase());
		AppResult backContext = new AppResult();
		backContext.putAttr(DuoduoConstant.DAO_Delete_SIZE, size);
		return backContext;
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
	 *业务员分单
	 * @param params
	 * @return
	 */
	public AppResult storeAllotNewOrder(AppParam params){
		AppResult result = new AppResult();
		AppResult custResult = new AppResult();//用户分单信息
		StoreTaskSend storeSend = (StoreTaskSend)SpringAppContext.getBean(StoreTaskSend.class);//MQ处理对象
		int totalSize = 0;//分给业务员的真实笔数
		int orderType = NumberUtil.getInt(params.getAttr("orderType"));

		//查询该用户的等级配置信息
		Map<String,Object> gradeMap = new HashMap<String,Object>();//用户等级配置
		Map<String,Object> custMap = new HashMap<String,Object>();//用户基本参数信息
		AppParam queryParams = new AppParam("custLevelService","queryLoginStatus");

		//取出需要分配给业务员的新单或再分配单
		AppResult storeAllotResult = super.query(params, NAMESPACE, orderType==1?"getStoreAllotNewOrder":"getStoreAllotAgainOrder");
		int size = storeAllotResult.getRows().size();
		Object customerId = params.getAttr("customerId");
		String recordDate = DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD);
		if(size > 0){
			for(Map<String,Object>storeAllotMap : storeAllotResult.getRows()){

				//查询当前用户的基本信息，每次判断是否能分单
				queryParams.addAttr("customerId", customerId);
				custResult = SoaManager.getInstance().invoke(queryParams);
				//更新分配数量的记录
				int allotCount = 0;
				AppParam queryParam = new AppParam("storeAllotRecordService","query");
				queryParam.addAttr("customerId", customerId);
				queryParam.addAttr("recordDate", recordDate);
				AppResult queryAloteResult = SoaManager.getInstance().invoke(queryParam);
				if(queryAloteResult.getRows().size()>0){
					allotCount = NumberUtil.getInt(queryAloteResult.getRow(0).get("allotNotFillCount"));
				}
			
				if(custResult.isSuccess() && custResult.getRows().size() > 0){
					custMap = custResult.getRow(0);
					custMap.put("agAllotCount", allotCount);
					custMap.put("orderType", orderType);
//					custMap.put("loginStatus", loginStatus);
				}
				if((gradeMap == null || gradeMap.size() <=0) && custMap != null ){
					gradeMap = StoreSeparateUtils.getRankConfigByGrade(NumberUtil.getInt(custMap.get("gradeCode")));
				}
				if(NumberUtil.getInt(custMap.get("isAllotOrder")) == 1){
					//对比该业务员的基本参数,不满足要求则退出分单
					if(!StoreAlllotUtils.compareParam(custMap, gradeMap)){
						log.info("can't alloct customerId:" + customerId);
						break;
					}
				}

				Object applyId = storeAllotMap.get("applyId");

				params.addAttr("applyId", applyId);
				int updateSize = getDao().update(NAMESPACE, "storeAllotOrderByUpdate", params.getAttr(), params.getDataBase());

				if(updateSize > 0){
					//插入门店人员操作记录
					StoreOptUtil.insertStoreRecord(applyId, customerId, BorrowConstant.STORE_OPER_0, 
							"系统自动分单", 0, orderType, 0, 1);
					//删除分配表里面的数据
					AppParam tmpParam = new AppParam(); 
					tmpParam.addAttr("applyId", applyId);
					tmpParam.addAttr("orderType", orderType);
					this.delete(tmpParam);

					//加入MQ处理
					try{
						Map<String, Object> sendParam = new HashMap<String, Object>();		
						sendParam.put("ordTotalCount", "1");//总的分单数处理
						if(orderType==1){
							sendParam.put("notDealCount", "1");//新单未处理数
						}else{
							sendParam.put("agNotDealCount", "1");//再分配未处理数
						}
						sendParam.put("recordDate", recordDate);//记录日期
						storeSend.sendStoreMessage(customerId.toString(),"countDealType" , sendParam);
					}catch(Exception e){
						log.error("StoreAllotTmpService 加入MQ error", e);
					}

					totalSize ++;
					tmpParam = null;
				}
			}

			//更新分配数量的记录
			AppParam updateParam = new AppParam("storeAllotRecordService","queryCount");
			updateParam.addAttr("customerId", customerId);
			updateParam.addAttr("recordDate", recordDate);
			AppResult updateResult = SoaManager.getInstance().invoke(updateParam);
			int count = NumberUtil.getInt(updateResult.getAttr(DuoduoConstant.TOTAL_SIZE));


			if(count > 0){
				updateParam.setMethod("update");
				updateParam.addAttr("addCount", totalSize);
				updateParam.addAttr("addTotalCount", 1);
				if(orderType == 1){
					updateParam.addAttr("addAllotSeniorCount", 1);
				}
				if(orderType == 2){
					updateParam.addAttr("addAllotNotFillCount", 1);
				}
			}else{
				updateParam.setMethod("insert");
				updateParam.addAttr("totalCount", totalSize);
				if(orderType == 1){
					updateParam.addAttr("allotSeniorCount", totalSize);
				}
				if(orderType == 2){
					updateParam.addAttr("allotNotFillCount", totalSize);
				}
			}

			SoaManager.getInstance().invoke(updateParam);

			custResult = null;
			updateResult = null;
			updateParam = null;
		}
		result.putAttr("realAllotCount", totalSize);
		return result;
	}

	/**
	 * 插入订单分配池
	 * @param treatyNo
	 * @param applyId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AppResult insertAllot(AppParam params){
		AppResult result = new AppResult();
		String orderType = StringUtil.getString(params.getAttr("orderType"));
		int allotCount = 0;
		List<Map<String,Object>> listMap = (List<Map<String,Object>>) params.getAttr("orderList");
		String notSign = StringUtil.getString(params.getAttr("notSign"));
		for(Map<String,Object> map : listMap){
			AppParam queryParam = new AppParam();
			queryParam.addAttr("applyId", map.get("applyId"));
			AppResult quertResult = this.queryCount(queryParam);
			if(quertResult.isSuccess()){
				int size = Integer.valueOf(StringUtil.getString(quertResult.getAttr(DuoduoConstant.TOTAL_SIZE)));
				if(size == 0){
					AppParam applyParam = new AppParam();
					applyParam.addAttr("applyId", map.get("applyId"));
					applyParam.addAttr("cityName", map.get("cityName"));
					applyParam.addAttr("applyType", map.get("applyType"));
					applyParam.addAttr("orderArea", 1);
					applyParam.addAttr("orderType", StringUtils.isEmpty(orderType) ? map.get("orderType") : orderType);
					applyParam.addAttr("recordDate", DateUtil.getSimpleFmt(new Date()));
					applyParam.addAttr("applyTime", map.get("applyTime"));
					applyParam.addAttr("orgId", map.get("orgId"));
					applyParam.addAttr("lastStore", map.get("lastStore"));
					applyParam.addAttr("nextRecordDate", DateUtil.getSimpleFmt(new Date()));
					result = this.insert(applyParam);
					allotCount ++ ;
					if(result.isSuccess()){
						AppParam updateParam = new AppParam("borrowApplyService","update");
						updateParam.addAttr("applyId", map.get("applyId"));
						updateParam.addAttr("orderStatus", "-1");
						updateParam.addAttr("lastStore", "");
						if(!StringUtils.isEmpty(orderType)){
							updateParam.addAttr("orderType", orderType);
							SoaManager.getInstance().invoke(updateParam);
						}else{
							SoaManager.getInstance().invoke(updateParam);
						}
						//没签单的订单删除预约记录
						if(!StringUtils.isEmpty(notSign)){
							AppParam bookParam = new AppParam("treatBookService","delete");
							bookParam.addAttr("applyId", map.get("applyId"));
							SoaManager.getInstance().invoke(bookParam);
						}
						String lastStore = StringUtil.getString(map.get("lastStore"));
						//查询用户等级
						AppResult appResult = StoreOptUtil.queryCustLevel(lastStore);
						int ordTotalCount = 0;
						int dealOrderCount = 0;
						if(appResult.getRows().size() > 0 && !StringUtils.isEmpty(appResult.getRow(0))){
							ordTotalCount = NumberUtil.getInt(appResult.getRow(0).get("ordTotalCount"));
							dealOrderCount = NumberUtil.getInt(appResult.getRow(0).get("dealOrderCount"));
						}
						AppParam levelParam = new AppParam("custLevelService","update");
						levelParam.addAttr("customerId", lastStore);
						if(ordTotalCount > 0){
							ordTotalCount --;
							levelParam.addAttr("ordTotalCount", ordTotalCount);
						}
						
						if(dealOrderCount > 0){
							dealOrderCount --;
							levelParam.addAttr("dealOrderCount", dealOrderCount);
						}
						if(!StringUtils.isEmpty(levelParam.getAttr("ordTotalCount")) ||
								!StringUtils.isEmpty(levelParam.getAttr("dealOrderCount"))){
							SoaManager.getInstance().invoke(levelParam);
						}
						
						// 插入操作记录
						StoreOptUtil.insertStoreRecord(map.get("applyId"),map.get("lastStore"),
								StoreConstant.STORE_OPER_29,params.getAttr("desc"), 0,  StringUtils.isEmpty(orderType) ? map.get("orderType") : orderType,0,1);
					}
				}
			}
		}
		result.putAttr("allotCount", allotCount);
		return result;
	}
}
