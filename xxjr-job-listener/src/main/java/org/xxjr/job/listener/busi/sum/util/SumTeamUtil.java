package org.xxjr.job.listener.busi.sum.util;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.LogerUtil;
import org.llw.job.util.JobUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;

public class SumTeamUtil {
	
	private static final String[] methods = {"totalBase","saleByBase","sumTotalSign","retByBase"};
	
	
	public static void sumBaseTeamData (Object processId, String today) {
		try {
			LogerUtil.log("SumTeamUtil sumBaseTeamData>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			List<AppParam> appParams = new ArrayList<AppParam>();
			
			for (int i = 0; i < methods.length; i++) {
				AppParam param = new AppParam("sumTeamExtService", methods[i]);
				param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
				param.addAttr("today", today);
				appParams.add(param);
			}
			Instant sta = Instant.now();
			Optional<List<Map<String, Object>>> reduce = appParams.stream().map((p) -> {
				AppResult result = RemoteInvoke.getInstance().call(p);
				return result.getRows();
			}).reduce((x,y) -> {
				Map<String, Map<String, Object>> sets = new HashMap<String, Map<String, Object>>();
				for (Map<String, Object> e : x) {
					sets.put(e.get("teamNo").toString(), e);
				}
				for (Map<String, Object> e : y) {
					String key = e.get("teamNo").toString();
					if (sets.containsKey(key)) {
						Map<String, Object> temp = sets.get(key);
						temp.putAll(e);
						sets.put(key, temp);
					}else {
						sets.put(key, e);
					}
				}
				List<Map<String, Object>> nowList = new ArrayList<Map<String,Object>>();
				for (Entry<String, Map<String, Object>> entry : sets.entrySet()) {
					nowList.add(entry.getValue());
				}
				return nowList;
			});
			Instant end = Instant.now();
			System.out.println("耗时:" + Duration.between(sta, end).getSeconds());
			List<Map<String, Object>> dataList = reduce.orElse(new ArrayList<Map<String,Object>>());
			if (dataList.size() > 0) {
				AppParam saveParam = new AppParam("sumTotalTeamService", "batchSave");
				saveParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				saveParam.addAttr("dataList", dataList);
				AppResult saveResult = RemoteInvoke.getInstance().call(saveParam);
				LogerUtil.log("SumTeamUtil sumBaseTeamData >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end insert count=" + saveResult.getAttr("count"));
			}
		} catch (Exception e) {
			LogerUtil.error(SumTeamUtil.class,e, "SumTeamUtil sumBaseTeamData error");
			JobUtil.addProcessExecute(processId, "总的统计(按团队) 报错：" + e.getMessage() );
		}
		
	}

	/**
	 * 更新退款信息
	 * @param processId
	 */
	public static void updateFailSale (Object processId) {
		AppResult result = new AppResult();
		try {
			LogerUtil.log("SumTeamUtil updateFailSale>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start");
			AppParam queryParam = new AppParam("sumTeamExtService","queryNowRefundDate");
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			if (result.getRows().size() <= 0) {
				LogerUtil.log("SumTeamUtil updateFailSale>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end");
				return;
			}
			List<Map<String, Object>> recordDates = result.getRows();
			queryParam.setMethod("queryTeamRefund");
			queryParam.addAttr("recordDates", recordDates);
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			if (result.getRows().size() > 0) {//更新渠道的
				AppParam updateParam = new AppParam("sumTotalTeamService","updateTeamFailSale");
				updateParam.addAttr("dataList", result.getRows());
				updateParam.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				result = RemoteInvoke.getInstance().callNoTx(updateParam);
				int size = NumberUtil.getInt(result.getAttr(DuoduoConstant.DAO_Update_SIZE), 0);
				LogerUtil.log("SumTeamUtil teamSale >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end batchUpdate count=" +size);
			}
			LogerUtil.log("SumTeamUtil updateFailSale>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end");
		} catch (Exception e) {
			LogerUtil.error(SumTeamUtil.class,e, "SaleSumUtil updateFailSale error");
			JobUtil.addProcessExecute(processId, "更新团队和基本失败数,失败金额 报错：" + e.getMessage() );
		}
		
	}
}
