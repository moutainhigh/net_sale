package org.xxjr.busiIn.store.ext;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.xxjr.busiIn.utils.StoreOptUtil;

/**
 * 统计工具
 * @author hwf
 *
 */
@Lazy
@Service
public class SumUtilExtService extends BaseService {
	public static final String NAMESPACE = "SUMUTILEXT";

	/**
	 * 渠道基本数据
	 * 
	 * @param params
	 * @return
	 */
	public AppResult channelBase(AppParam params) {
		String curDate = StringUtil.getString(params.getAttr("today"));
		String tableName = StoreOptUtil.getTableName(curDate);
		params.addAttr("tableName", tableName);
		return super.query(params, NAMESPACE, "channelBase");
	}

	
	/**
	 * 门店经理基本数据
	 * 
	 * @param params
	 * @return
	 */
	public AppResult storeBase(AppParam params) {
		String curDate = StringUtil.getString(params.getAttr("today"));
		String tableName = StoreOptUtil.getTableName(curDate);
		params.addAttr("tableName", tableName);
		return super.query(params, NAMESPACE, "storeBase");
	}
	
	/**
	 * 门店基本数据
	 * 
	 * @param params
	 * @return
	 */
	public AppResult orgBase(AppParam params) {
		String curDate = StringUtil.getString(params.getAttr("today"));
		String tableName = StoreOptUtil.getTableName(curDate);
		params.addAttr("tableName", tableName);
		return super.query(params, NAMESPACE, "orgBase");
	}
	
	/**
	 * 汇总基本数据
	 * 
	 * @param params
	 * @return
	 */
	public AppResult totalBase(AppParam params) {
		String curDate = StringUtil.getString(params.getAttr("today"));
		String tableName = StoreOptUtil.getTableName(curDate);
		params.addAttr("tableName", tableName);
		return super.query(params, NAMESPACE, "totalBase");
	}
	
	
	/**
	 * 渠道上门统计
	 * 
	 * @param params
	 * @return
	 */
	public AppResult channelBook(AppParam params) {
		return super.query(params, NAMESPACE, "channelBook");
	}
	
	
	/**
	 *总的上门统计(按处理时间)
	 * 
	 * @param params
	 * @return
	 */
	public AppResult storeBook(AppParam params) {
		return super.query(params, NAMESPACE, "storeBook");
	}
	
	/**
	 * -总的上门统计(按处理时间)
	 * 
	 * @param params
	 * @return
	 */
	public AppResult sumTotalBook(AppParam params) {
		return super.query(params, NAMESPACE, "sumTotalBook");
	}
	
	/**
	 * 渠道签单统计
	 * 
	 * @param params
	 * @return
	 */
	public AppResult channelSign(AppParam params) {
		return super.query(params, NAMESPACE, "channelSign");
	}
	
	
	/**
	 * 门店经理签单统计
	 * 
	 * @param params
	 * @return
	 */
	public AppResult storeSign(AppParam params) {
		return super.query(params, NAMESPACE, "storeSign");
	}
	
	
	/**
	 * 总的签单统计(按处理时间) 
	 * 
	 * @param params
	 * @return
	 */
	public AppResult sumTotalSign(AppParam params) {
		return super.query(params, NAMESPACE, "sumTotalSign");
	}

	/**
	 * 按门店人员回款统计
	 * 
	 * @param params
	 * @return
	 */
	public AppResult retSumaryByStore(AppParam params) {
		return super.query(params, NAMESPACE, "retSumaryByStore");
	}
	
	/**
	 * 按渠道回款统计
	 * 
	 * @param params
	 * @return
	 */
	public AppResult retByChannel(AppParam params) {
		return super.query(params, NAMESPACE, "retByChannel");
	}
	
	/**
	 * 回款基本统计
	 * 
	 * @param params
	 * @return
	 */
	public AppResult retByBase(AppParam params) {
		return super.query(params, NAMESPACE, "retByBase");
	}
	
	
	/**
	 * 总的花费
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryTotalCost(AppParam params) {
		return super.query(params, NAMESPACE, "queryTotalCost");
	}
	
	/**
	 * 渠道花费
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryChannelCost(AppParam params) {
		return super.query(params, NAMESPACE, "queryChannelCost");
	}

	/**
	 * 查询待处理分组统计
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryGroupCountByDeal(AppParam params) {
		return super.query(params, NAMESPACE,"queryGroupCountByDeal");
	}
	
	/**
	 * 查询接单及反馈相关分组统计
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryGroupByRecOrBack(AppParam params) {
		String curDate = StringUtil.getString(params.getAttr("startDate"));
		String tableName = StoreOptUtil.getTableName(curDate);
		params.addAttr("tableName", tableName);
		return super.query(params, NAMESPACE,"queryGroupByRecOrBack");
	}
	
	/**
	 * 查询上门相关分组统计
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryGroupCountByBook(AppParam params) {
		return super.query(params, NAMESPACE,"queryGroupCountByBook");
	}
	
	/**
	 * 查询签单相关分组统计
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryGroupCountBySign(AppParam params) {
		return super.query(params, NAMESPACE,"queryGroupCountBySign");
	}
	
	/**
	 * 查询回款相关分组统计
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryGroupCountByRet(AppParam params) {
		return super.query(params, NAMESPACE,"queryGroupCountByRet");
	}
	
	/**
	 * 门店经理基本数据
	 * 
	 * @param params
	 * @return
	 */
	public AppResult storeBaseMonth(AppParam params) {
		String curDate = StringUtil.getString(params.getAttr("toMonth"));
		String tableName = StoreOptUtil.getTableName(curDate);
		params.addAttr("tableName", tableName);
		return super.query(params, NAMESPACE, "storeBaseMonth");
	}
	
	
	/**
	 *总的上门统计(按处理时间)
	 * 
	 * @param params
	 * @return
	 */
	public AppResult storeBookMonth(AppParam params) {
		return super.query(params, NAMESPACE, "storeBookMonth");
	}
	
	/**
	 * 门店经理签单统计
	 * 
	 * @param params
	 * @return
	 */
	public AppResult storeSignMonth(AppParam params) {
		return super.query(params, NAMESPACE, "storeSignMonth");
	}
	
	
	/**
	 * 按门店人员回款统计
	 * 
	 * @param params
	 * @return
	 */
	public AppResult storeRetMonth(AppParam params) {
		return super.query(params, NAMESPACE, "storeRetMonth");
	}
	
	
	/**
	 * 查询今日概况
	 * @param params
	 * @return
	 */
	public AppResult queryToDayWork(AppParam params) {
		return super.query(params, NAMESPACE, "queryToDayWork");
	}
	
	
	/**
	 * 查询本周工作情况
	 * @param params
	 * @return
	 */
	public AppResult queryThisWeekCase(AppParam params) {
		return super.query(params, NAMESPACE, "queryThisWeekCase");
	}
	
	
	/**
	 * 门店人员今日统计
	 * @param params
	 * @return
	 */
	public AppResult queryStoreToDay(AppParam params) {
		String tableName = StoreOptUtil.getTableName(null);
		params.addAttr("tableName", tableName);
		return super.queryByPage(params, NAMESPACE, "queryStoreToDay","queryStoreToDayCount");
	}
	
	/**
	 * queryStoreToDayCount
	 * @param params
	 * @return
	 */
	public AppResult queryStoreToDayCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryStoreToDayCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * 门店今日统计
	 * @param params
	 * @return
	 */
	public AppResult queryOrgToDay(AppParam params) {
		String tableName = StoreOptUtil.getTableName(null);
		params.addAttr("tableName", tableName);
		return super.queryByPage(params, NAMESPACE, "queryOrgToDay","queryOrgToDayCount");
	}
	/**
	 * queryOrgToDayCount
	 * @param params
	 * @return
	 */
	public AppResult queryOrgToDayCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryOrgToDayCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * 总的今日统计
	 * @param params
	 * @return
	 */
	public AppResult queryTotalToDay(AppParam params) {
		String tableName = StoreOptUtil.getTableName(null);
		params.addAttr("tableName", tableName);
		return super.queryByPage(params, NAMESPACE, "queryTotalToDay","queryTotalToDayCount");
		
	}
	/**
	 * queryTotalToDayCount
	 * @param params
	 * @return
	 */
	public AppResult queryTotalToDayCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "queryTotalToDayCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**
	 * 门店在线人数统计
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryOrgOnline(AppParam params) {
		return super.query(params, NAMESPACE, "queryOrgOnline");
	}
	/**
	 * 门店分组环比增长
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryOrgGroupDuration(AppParam params) {
		return super.query(params, NAMESPACE, "queryOrgGroupDuration");
	}
	/**
	 * 门店分队环比增长
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryOrgTeamDuration(AppParam params) {
		return super.query(params, NAMESPACE, "queryOrgTeamDuration");
	}
	/**
	 * 门店分组统计排名
	 * @param params
	 * @return
	 */
	public AppResult queryOrgGroupRank(AppParam params) {
		if(params.getCurrentPage() == -1){
			params.setEveryPage(1000);
			params.setCurrentPage(1);
			return super.queryByPage(params, NAMESPACE, "queryOrgGroupRank","queryOrgGroupRankCount");
		}else{
			return super.queryByPage(params, NAMESPACE, "queryOrgGroupRank","queryOrgGroupRankCount");
		}
		
	}
	/**
	 * 门店分队统计排名
	 * @param params
	 * @return
	 */
	public AppResult queryOrgTeamRank(AppParam params) {
		if(params.getCurrentPage() == -1){
			params.setEveryPage(1000);
			params.setCurrentPage(1);
			return super.queryByPage(params, NAMESPACE, "queryOrgTeamRank","queryOrgTeamRankCount");
		}else{
			return super.queryByPage(params, NAMESPACE, "queryOrgTeamRank","queryOrgTeamRankCount");
		}
		
	}
	/**
	 * 门店分队统计求和
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryOrgSumRank(AppParam params) {
		return super.query(params, NAMESPACE, "queryOrgSumRank");
	}
	
	
	/**
	 * 城市今日分单情况
	 * @param params
	 * @return
	 */
	public AppResult queryCityAllotDay(AppParam params) {
		String tableName = StoreOptUtil.getTableName(null);
		params.addAttr("tableName", tableName);
		return super.queryByPage(params, NAMESPACE, "queryCityAllotDay","queryCityAllotDayCount");
	}
	
	/**
	 * 城市本周分单情况
	 * @param params
	 * @return
	 */
	public AppResult queryCityAllotWeek(AppParam params) {
		String tableName = StoreOptUtil.getTableName(null);
		params.addAttr("tableName", tableName);
		return super.queryByPage(params, NAMESPACE, "queryCityAllotWeek","queryCityAllotWeekCount");
	}
	
	/**
	 * 城市本月分单情况
	 * @param params
	 * @return
	 */
	public AppResult queryCityAllotMonth(AppParam params) {
		String tableName = StoreOptUtil.getTableName(null);
		params.addAttr("tableName", tableName);
		return super.queryByPage(params, NAMESPACE, "queryCityAllotMonth","queryCityAllotMonthCount");
	}
	
	/**
	 * 总的申请(实时-申请)
	 * @param params
	 * @return
	 */
	public AppResult totalApplyTimeSum(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "totalApplyTimeSum","totalApplyTimeCount");
	}
	
	/**
	 * 总的申请(实时-申请)的数量查询sql
	 * @param params
	 * @return
	 */
	public AppResult totalApplyTimeSumCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "totalApplyTimeCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**
	 * 总的申请(实时-申请) 区间
	 * @param params
	 * @return
	 */
	public AppResult totalApplyTimeSumRange(AppParam params) {
		AppResult result = new AppResult();
		result = super.query(params, NAMESPACE, "totalApplyTimeSumRange");
		result.getPage().setTotalRecords(1);
		return result;
	}
	/**
	 * 风控今日统计
	 * @param params
	 * @return
	 */
	public AppResult queryRiskToDay(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryRiskToDay","queryRiskToDayCount");
	}
	
	/**
	 * 风控基本数据统计
	 * @param params
	 * @return
	 */
	public AppResult riskBase(AppParam params) {
		return super.query(params, NAMESPACE, "riskBase");
	}
	
	/**
	 * 按今天简单实时统计（按时间，渠道）
	 * @param params
	 * @return
	 */
	public AppResult channelSimpleByToday(AppParam params) {
		return super.query(params, NAMESPACE,"channelSimpleByToday");
	}
}
