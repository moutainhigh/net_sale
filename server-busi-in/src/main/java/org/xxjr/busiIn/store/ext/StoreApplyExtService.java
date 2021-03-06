package org.xxjr.busiIn.store.ext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busiIn.utils.StoreOptUtil;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.store.util.StoreApplyUtils;
import org.xxjr.sys.util.NumberUtil;
@Lazy
@Service
public class StoreApplyExtService extends BaseService{
	public static final String NAMESPACE = "STOREAPPLYEXT";

	/**
	 * 门店订单评分今日统计
	 * @param param
	 * @return
	 */
	public AppResult queryRateToday(AppParam params){
		return super.query(params, NAMESPACE,"queryRateToday");
	}
 
	/**
	 * 门店订单评分今日统计分页
	 */
	public AppResult queryRateTodayByPage(AppParam params){
		return super.queryByPage(params, NAMESPACE,"queryRateToday","queryRateTodayCount");
	}
	/**
	 * queryRateTodayCount
	 * @param params
	 * @return
	 */
	public AppResult queryRateTodayCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryRateTodayCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**
	 * 门店订单评分今日统计总计
	 * @param param
	 * @return
	 */
	public AppResult queryRateTodaySum(AppParam params){
		return super.query(params, NAMESPACE,"queryRateTodaySum");
	}

	/**
	 * 订单评分处理
	 * 
	 * @param params
	 */
	public AppResult updateOrderRate(AppParam params) {
		AppResult result = new AppResult();
		String applyId = StringUtil.getString(params.getAttr("applyId"));
		String customerId = StringUtil.getString(params.getAttr("customerId"));
		String orderRate = StringUtil.getString(params.getAttr("orderRate"));
		//管理员的用户ID
		String adminCustomerId = StringUtil.getString(params.getAttr("adminCustomerId"));
		Map<String, Object> applyInfo = StoreOptUtil.queryByApplyId(applyId);
		String lastStore = StringUtil.getString(applyInfo.get("lastStore"));
		//管理员可以执行订单类型处理
		if((StringUtils.hasText(lastStore) && customerId.equals(lastStore)) 
				|| !StringUtils.isEmpty(adminCustomerId)){
			AppParam applyParams = new AppParam("borrowStoreApplyService","update");
			applyParams.addAttr("applyId", applyId);
			applyParams.addAttr("orderRate", orderRate);
			result = SoaManager.getInstance().invoke(applyParams);
			int updateSize = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Update_SIZE), 0);
			if(result.isSuccess() && updateSize == 1) {
				Date applyDate = DateUtil.toDateByString(applyInfo.get("applyTime").toString(), DateUtil.DATE_PATTERN_YYYY_MM_DD);
				String recordDate = DateUtil.toStringByParttern(applyDate, DateUtil.DATE_PATTERN_YYYY_MM_DD);
				
				Map<String, Object> dealMap = new HashMap<String, Object>();
				dealMap.put("recordDate",recordDate);
				dealMap.put("orgId", StringUtil.getString(applyInfo.get("orgId")));
				StoreOptUtil.dealStoreOrderByMq(customerId,"orderRateType", dealMap);
				
				// 删除缓存
				RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_INFO + applyId);
				RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_MAININFO + applyId);				
			}		
			return result;
		}else{
			return CustomerUtil.retErrorMsg("你不是当前处理人，不能进行订单类型处理");
		}
	}



	/**
	 * 签单失败渠道今日统计
	 * @param param
	 * @return
	 */
	public AppResult querySignFailToday(AppParam params){
		return super.query(params, NAMESPACE,"querySignFailToday");
	}

	/**
	 * 签单失败渠道今日分页统计
	 * @param param
	 * @return
	 */
	public AppResult querySignFailTodayByPage(AppParam params){
		return super.queryByPage(params, NAMESPACE,"querySignFailToday","querySignFailTodayCount");
	}
	/**
	 * querySignFailTodayCount
	 * @param params
	 * @return
	 */
	public AppResult querySignFailTodayCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "querySignFailTodayCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**
	 * 签单失败渠道今日统计总计
	 * @param param
	 * @return
	 */
	public AppResult querySignFailTodaySum(AppParam params){
		return super.query(params, NAMESPACE,"querySignFailTodaySum");
	}
	
	/**
	 * 订单状态渠道今日统计
	 * @param param
	 * @return
	 */
	public AppResult queryOrderChannelToday(AppParam params){
		return super.query(params, NAMESPACE,"queryOrderChannelToday");
	}
	
	/**
	 * 订单状态渠道今日统计分页查询
	 * @param param
	 * @return
	 */
	public AppResult queryOrderChannelTodayByPage(AppParam params){
		return super.queryByPage(params, NAMESPACE,"queryOrderChannelToday","queryOrderChannelTodayCount");
	}
	
	/**
	 * queryOrderChannelTodayCount
	 * @param params
	 * @return
	 */
	public AppResult queryOrderChannelTodayCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryOrderChannelTodayCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * 订单状态渠道今日统计总计
	 * @param param
	 * @return
	 */
	public AppResult queryOrderChannelTodaySum(AppParam params){
		return super.query(params, NAMESPACE,"queryOrderChannelTodaySum");
	}
	
	/**
	 *查询订单处理状态详情信息
	 * @param params
	 * @return
	 */
	public AppResult queryOrgDealDetail(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryOrgDealDetail","queryOrgDealDetailCount");
	}
}
