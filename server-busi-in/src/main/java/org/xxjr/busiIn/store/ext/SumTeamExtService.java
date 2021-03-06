package org.xxjr.busiIn.store.ext;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.xxjr.busiIn.utils.StoreOptUtil;

@Lazy
@Service
public class SumTeamExtService extends BaseService{
	
	public static final String NAMESPACE = "SUMTEAMEXT";
	
	/**
	 * 汇总基本数据统计 
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
	 * 总的签单统计(按处理时间) 
	 * @param params
	 * @return
	 */
	public AppResult sumTotalSign(AppParam params) {
		return super.query(params, NAMESPACE, "sumTotalSign");
	}
	
	/**
	 * 按回款时间基本统计
	 * @param params
	 * @return
	 */
	public AppResult retByBase(AppParam params) {
		return super.query(params, NAMESPACE, "retByBase");
	}
	
	
	/**
	 * 团队花费
	 * 
	 * @param params
	 * @return
	 */
	public AppResult queryTeamCost(AppParam params) {
		return super.query(params, NAMESPACE, "queryTeamCost");
	}
}
