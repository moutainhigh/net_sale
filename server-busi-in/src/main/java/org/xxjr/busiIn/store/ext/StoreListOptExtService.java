package org.xxjr.busiIn.store.ext;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * 信贷经理列表操作的逻辑
 * @author 
 *
 */
@Lazy
@Service
public class StoreListOptExtService extends BaseService {
	public static final String NAMESPACE = "STORELISTOPTEXT";
	/**
	 * 查询待处理列表
	 * 
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}
	
	/**
	 * 查询我的订单(签单中)列表
	 * 
	 * @param params
	 * @return
	 */
	public AppResult querySigned(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "querySigned", "querySignedCount");
	}
	
	/**
	 * querySignedCount
	 * @param params
	 * @return
	 */
	public AppResult querySignedCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "querySignedCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}

	
	/**
	 * 查询我的订单(回款处理)列表
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryReLoan(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryReLoan", "queryReLoanCount");
	}
	
	/**
	 * queryReLoanCount
	 * @param params
	 * @return
	 */
	public AppResult queryReLoanCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryReLoanCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}

	/**
	 * 查询通知的门店人员抢单的业务员
	 * 
	 * @param params
	 * @return
	 */
	public AppResult getNoticeStore(AppParam params) {
		return super.query(params, NAMESPACE,"getNoticeStore");
	}
	
	/**
	 * 查询订单相关信息
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryOrderInfo(AppParam params) {
		return super.query(params, NAMESPACE,"queryOrderInfo");
	}
	
	/**
	 * 查询订单相关信息(t_borrow_apply为主表)
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryBorrowOrderInfo(AppParam params) {
		return super.query(params, NAMESPACE,"queryBorrowOrderInfo");
	}
	
	/**
	 * 再分配池列表
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryAllotPond(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryAllotPond",
				"queryAllotPondCount");
	}
	
	/**
	 *查询CFS签单列表
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryCfsSignList(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryCfsSignList",
				"queryCfsSignListCount");
	}
	
	/***
	 * 查询我的所有订单(更换主表，t_borrow_apply为主表查询)
	 * @param params
	 * @return
	 */
	public AppResult queryBorrowAllList(AppParam params){
		if(params.getCurrentPage() == -1){
			params.setEveryPage(500);
			params.setCurrentPage(1);
			return super.queryByPage(params, NAMESPACE, "queryBorrowAllList", "queryBorrowAllListCount");
		}else{
			return super.queryByPage(params, NAMESPACE, "queryBorrowAllList", "queryBorrowAllListCount");
		}
	}
	
	/***
	 * 查询我的所有订单统计(更换主表，t_borrow_apply为主表查询)
	 * @param params
	 * @return
	 */
	public AppResult queryBorrowAllSummary(AppParam params){
		return super.query(params,NAMESPACE,"queryBorrowAllSummary");
	}
	
	/**
	 * 查询订主要和基本信息(t_borrow_apply为主表)
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryBorrowMainInfo(AppParam params) {
		return super.query(params, NAMESPACE,"queryBorrowMainInfo");
	}
	
	/**
	 *查询CFS签单(不分页)
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryAllCfsSignList(AppParam params) {
		return super.query(params, NAMESPACE, "queryCfsSignList");
	}
	/**
	 *查询离职人员签单信息(天)
	 * 
	 * @param params
	 * @return
	 */
	public AppResult storeSignDeparture(AppParam params) {
		return super.query(params, NAMESPACE, "storeSignDeparture");
	}
	/**
	 *查询离职人员签单信息(月度)
	 * 
	 * @param params
	 * @return
	 */
	public AppResult storeSignDepartMonth(AppParam params) {
		return super.query(params, NAMESPACE, "storeSignDepartMonth");
	}
	/**
	 *查询离职人员回款信息(天)
	 * 
	 * @param params
	 * @return
	 */
	public AppResult storeReLoanDeparture(AppParam params) {
		return super.query(params, NAMESPACE, "storeReLoanDeparture");
	}
	/**
	 *查询离职人员回款信息(月度)
	 * 
	 * @param params
	 * @return
	 */
	public AppResult storeReLoanDepartMonth(AppParam params) {
		return super.query(params, NAMESPACE, "storeReLoanDepartMonth");
	}
	/**
	 *查询离职人员成本信息(天)
	 * 
	 * @param params
	 * @return
	 */
	public AppResult storeCostDeparture(AppParam params) {
		return super.query(params, NAMESPACE, "storeCostDeparture");
	}
	/**
	 *查询离职人员成本信息(月度)
	 * 
	 * @param params
	 * @return
	 */
	public AppResult storeCostDepartMonth(AppParam params) {
		return super.query(params, NAMESPACE, "storeCostDepartMonth");
	}
	
	/**
	 *查询订单渠道信息
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryOrderChannelInfo(AppParam params) {
		return super.query(params, NAMESPACE, "queryOrderChannelInfo");
	}
	
	/**
	 * 门店订单状态今日统计 按队名分组
	 */
	public AppResult queryDealOrderTypeToday(AppParam params){
		return super.query(params, NAMESPACE,"queryDealOrderTypeToday");
	}
	
	/**
	 * 门店订单状态今日统计分页查询
	 */
	public AppResult queryTypeTodayByPage(AppParam params){
		return super.queryByPage(params, NAMESPACE,"queryDealOrderTypeToday","queryDealOrderTypeCount");
	}
	/**
	 * queryDealOrderTypeCount
	 * @param params
	 * @return
	 */
	public AppResult queryDealOrderTypeCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryDealOrderTypeCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**
	 * 门店订单状态今日统计总计
	 */
	public AppResult queryDealOrderTypeTodaySum(AppParam params){
		return super.query(params, NAMESPACE,"queryDealOrderTypeTodaySum");
	}
	/**
	 * 订单状态分组统计（门店） 今日
	 */
	public AppResult queryOrgDealOrderToday(AppParam params){
		return super.query(params, NAMESPACE,"queryOrgDealOrderToday");
	}
	/**
	 * 订单状态分组统计（门店） 今日分页
	 */
	public AppResult queryOrgDealOrderTodayByPage(AppParam params){
		return super.queryByPage(params, NAMESPACE,"queryOrgDealOrderToday","queryOrgDealOrderTodayCount");
	}
	/**
	 * queryOrgDealOrderTodayCount
	 * @param params
	 * @return
	 */
	public AppResult queryOrgDealOrderTodayCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryOrgDealOrderTodayCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**
	 * 订单状态分组统计总计（门店） 今日
	 */
	public AppResult queryOrgDealOrderTodaySum(AppParam params){
		return super.query(params, NAMESPACE,"queryOrgDealOrderTodaySum");
	}
	
	/**
	 * 查询新申请和再分配、未了解总数
	 * @param params
	 * @return
	 */
	public AppResult queryApplayCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryApplayCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * 查询新申请和再分配、未了解列表
	 */
	public AppResult queryStoreApplyList(AppParam params){
		return super.queryByPage(params, NAMESPACE,"queryStoreApplyList","queryStoreApplyCount");
	}
	
	/**
	 * 查询退单列表
	 */
	public AppResult queryBackOrderList(AppParam params){
		return super.queryByPage(params, NAMESPACE,"queryBackOrderList","queryBackOrderCount");
	}
}