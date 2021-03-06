package org.xxjr.busi.util.kefu;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.StringUtil;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;

public class AfterMethodUtil {
	public volatile static AfterMethodUtil methodUtil = null;
	public static AfterMethodUtil getInstance(){
		if(methodUtil == null){
			synchronized (AfterMethodUtil.class) {
				if(methodUtil == null){
					methodUtil = new AfterMethodUtil();
				}
			}
		}
		return methodUtil;
	}
	
	public  void invoke(String methodStr,AppResult result,AppParam params) throws Exception {
		Class<AfterMethodUtil> clazz = AfterMethodUtil.class;
		Method method = clazz.getMethod(methodStr,AppResult.class,AppParam.class);
		method.invoke(getInstance(),result,params);  //执行具体方法
	}
	/**
	 * 在结果集中添加访问数字段(1)
	 * @return
	 */
	public  AppResult addRecordCount(AppResult result,AppParam appParam) {
		AppParam params = new AppParam();
		String dateType = StringUtil.getString(appParam.getAttr("dateType"));
//		String startRecordDate = StringUtil.getString(appParam.getAttr("startRecordDate"));
//		String endRecordDate = StringUtil.getString(appParam.getAttr("endDateStr"));
		DecimalFormat df = new DecimalFormat("######0.00");
		List<Map<String, Object>> list = result.getRows();
		if ("day".equals(dateType)) {
			for (Map<String, Object> map : list) {
				
				int dealStoreCount = NumberUtil.getInt(map.get("dealStoreCount"),0);//已更进数量
				int sucBookCount = NumberUtil.getInt(map.get("sucBookCount"),0);//上门数量
				int totalSignCount = NumberUtil.getInt(map.get("totalSignCount"),0);//签单数量
				int sucRetCount = NumberUtil.getInt(map.get("sucRetCount"),0);//回款数
				int repCount = NumberUtil.getInt(map.get("repCount"),0);//重复数量
				int applyCount = NumberUtil.getInt(map.get("applyCount"),0);//总申请数量
				
				double sucDeal = (sucBookCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);
				map.put("sucDeal", df.format(sucDeal) + "%");//上门率
				double totalDeal = (totalSignCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);//签单率
				map.put("totalDeal", df.format(totalDeal) + "%");
				double sucRetDeal = (sucRetCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);//回款率
				map.put("sucRetDeal", df.format(sucRetDeal) + "%");
				double sucTotal = (sucRetCount*100.0)/(totalSignCount == 0 ? 1:totalSignCount);//回款数/签单数
				map.put("sucTotal", df.format(sucTotal) + "%");
				
				double repRate = (repCount*100.0)/((applyCount + repCount)== 0 ? 1:(applyCount + repCount));//重复数量/总申请数量
				map.put("repRate", df.format(repRate) + "%");
				
//				params.addAttr("channelCode", map.get("channelCode"));
//				params.addAttr("countDate", map.get("recordDate"));
//				params.addAttr("datePattern", "%Y-%m-%d");
//				params.setService("pageCountService");
//				params.setMethod("queryUVCount");
//				params.setRmiServiceName(AppProperties
//						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
//				AppResult result2 = RemoteInvoke.getInstance().callNoTx(params);
//				if(result2.getRows().size()>0){
//					if (result2.getRow(0)!=null) {
//						int recordCount = NumberUtil.getInt(result2.getRow(0).get("recordCount"), 0);
//						int regCount = NumberUtil.getInt(result2.getRow(0).get("regCount"), 0);
//						double updateRate = ((applyCount-regCount)*100.0)/(applyCount== 0 ? 1:applyCount );
//						map.put("updateRate", df.format(updateRate) + "%");
//						map.put("recordCount", recordCount);
//						double result1 = (applyCount*100.0)/((recordCount == 0 ? 1 : recordCount));
//						map.put("recordCount1", df.format(result1) + "%");
//					}else {
//						map.put("recordCount", 0);
//					}
//				}else {
//					map.put("recordCount", 0);
//				}
			}
		}
		
		if ("range".equals(dateType)) {
			for (Map<String, Object> row : result.getRows()) {
				int dealStoreCount = NumberUtil.getInt(row.get("dealStoreCount"),0);//已更进数量
				int sucBookCount = NumberUtil.getInt(row.get("sucBookCount"),0);//上门数量
				int totalSignCount = NumberUtil.getInt(row.get("totalSignCount"),0);//签单数量
				int sucRetCount = NumberUtil.getInt(row.get("sucRetCount"),0);//回款数
				int repCount = NumberUtil.getInt(row.get("repCount"),0);//重复数量
				int applyCount = NumberUtil.getInt(row.get("applyCount"),0);//总申请数量
				double sucDeal = (sucBookCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);
				row.put("sucDeal", df.format(sucDeal) + "%");//上门率
				double totalDeal = (totalSignCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);//签单率
				row.put("totalDeal", df.format(totalDeal) + "%");
				double sucRetDeal = (sucRetCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);//回款率
				row.put("sucRetDeal", df.format(sucRetDeal) + "%");
				double sucTotal = (sucRetCount*100.0)/(totalSignCount == 0 ? 1:totalSignCount);//回款数/签单数
				row.put("sucTotal", df.format(sucTotal) + "%");
				
				double repRate = (repCount*100.0)/((applyCount + repCount)== 0 ? 1:(applyCount + repCount));//重复数量/总申请数量
				row.put("repRate", df.format(repRate) + "%");
				
				
//				AppParam param = new AppParam("pageCountService", "queryRangeUVCount");
//				param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
//				param.addAttr("channelCode", row.get("channelCode"));
//				param.addAttr("startRecordDate", startRecordDate);
//				param.addAttr("endRecordDate", endRecordDate);
//				AppResult queryResult = RemoteInvoke.getInstance().callNoTx(param);
//				double uv = NumberUtil.getDouble(queryResult.getRow(0).get("recordCount"), 0.0);
//				double regCount = NumberUtil.getDouble(queryResult.getRow(0).get("regCount"), 0.0);
//				row.put("recordCount", uv);
//				double updateRate = ((applyCount-regCount)*100.0)/(applyCount== 0 ? 1:applyCount );
//				row.put("updateRate", df.format(updateRate) + "%");
//				double cvr = 0.0;
//				if (uv > 0) {
//					cvr = (applyCount / uv) * 100.0;
//				}
//				row.put("recordCount1", df.format(cvr) + "%");
			}
		}
		
		if ("month".equals(dateType)) {
			for (Map<String, Object> map : list) {
				
				int dealStoreCount = NumberUtil.getInt(map.get("dealStoreCount"),0);//已更进数量
				int sucBookCount = NumberUtil.getInt(map.get("sucBookCount"),0);//上门数量
				int totalSignCount = NumberUtil.getInt(map.get("totalSignCount"),0);//签单数量
				int sucRetCount = NumberUtil.getInt(map.get("sucRetCount"),0);//回款数
				int repCount = NumberUtil.getInt(map.get("repCount"),0);//重复数量
				int applyCount = NumberUtil.getInt(map.get("applyCount"),0);//总申请数量
				
				double sucDeal = (sucBookCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);
				map.put("sucDeal", df.format(sucDeal) + "%");//上门率
				double totalDeal = (totalSignCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);//签单率
				map.put("totalDeal", df.format(totalDeal) + "%");
				double sucRetDeal = (sucRetCount*100.0)/(dealStoreCount == 0 ? 1:dealStoreCount);//回款率
				map.put("sucRetDeal", df.format(sucRetDeal) + "%");
				double sucTotal = (sucRetCount*100.0)/(totalSignCount == 0 ? 1:totalSignCount);//回款数/签单数
				map.put("sucTotal", df.format(sucTotal) + "%");
				double repRate = (repCount*100.0)/((applyCount + repCount)== 0 ? 1:(applyCount + repCount));//重复数量/总申请数量
				map.put("repRate", df.format(repRate) + "%");
				
//				params.addAttr("channelCode", map.get("channelCode"));
//				params.addAttr("countDate", map.get("recordDate"));
//				params.addAttr("datePattern", "%Y-%m");
//				params.setService("pageCountService");
//				params.setMethod("queryUVCount");
//				params.setRmiServiceName(AppProperties
//						.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
//				AppResult result2 = RemoteInvoke.getInstance().callNoTx(params);
//				if(result2.getRow(0)!=null){
//					double regCount = NumberUtil.getDouble(result2.getRow(0).get("regCount"), 0.0);
//					double updateRate = ((applyCount-regCount)*100.0)/(applyCount== 0 ? 1:applyCount );
//					map.put("updateRate", df.format(updateRate) + "%");
//					int recordCount = NumberUtil.getInt(result2.getRow(0).get("recordCount"), 0);
//					map.put("recordCount", recordCount);
//					map.put("recordCount1", df.format((applyCount*100.0)/((recordCount == 0 ? 1 : recordCount)))+"%");
//				}else {
//					map.put("recordCount", 0);
//				}
			}
		}
		return result;
	}
	
	/**
	 * 解析assetCount字段给页面(2)
	 * @param result
	 * @return
	 */
	public  AppResult analyzedAssetCountToPage(AppResult result,AppParam params) {
		List<Map<String, Object>> list = result.getRows();
		for (Map<String, Object> map : list) {
			String assetCount1 = StringUtil.getString(map.remove("assetCount1"));
			String[] split1 = assetCount1.split("-");
			for (int i = 0; i < split1.length; i++) {
				String sign ="assetCount1num"+i;
				map.put(sign, split1[i]);
			}
			String assetCount2 = StringUtil.getString(map.remove("assetCount2"));
			String[] split2 = assetCount2.split("-");
			for (int i = 0; i < split2.length; i++) {
				String sign ="assetCount2num"+i;
				map.put(sign, split2[i]);
			}
			String assetCount3 = StringUtil.getString(map.remove("assetCount3"));
			String[] split3 = assetCount3.split("-");
			for (int i = 0; i < split3.length; i++) {
				String sign ="assetCount3num"+i;
				map.put(sign, split3[i]);
			}
			String assetCount4 = StringUtil.getString(map.remove("assetCount4"));
			String[] split4 = assetCount4.split("-");
			for (int i = 0; i < split4.length; i++) {
				String sign ="assetCount4num"+i;
				map.put(sign, split4[i]);
			}
			String assetCount5 = StringUtil.getString(map.remove("assetCount5"));
			String[] split5 = assetCount5.split("-");
			for (int i = 0; i < split5.length; i++) {
				String sign ="assetCount5num"+i;
				map.put(sign, split5[i]);
			}
		}
		return result;
	}
	/**
	 * 解析assetCount字段给页面(时段分析统计使用)(3)
	 * @param result
	 * @return
	 */
	public  AppResult analyzedAssetCountToPage2(AppResult result,AppParam params) {
		List<Map<String, Object>> list = result.getRows();
		for (Map<String, Object> map : list) {
			String assetCount1 = StringUtil.getString(map.remove("assetCount1"));
			String[] split1 = assetCount1.split("-");
			for (int i = 0; i < split1.length; i++) {
				String sign ="assetCount1num_"+i;
				map.put(sign, split1[i]);
			}
			String assetCount2 = StringUtil.getString(map.remove("assetCount2"));
			String[] split2 = assetCount2.split("-");
			for (int i = 0; i < split2.length; i++) {
				String sign ="assetCount2num_"+i;
				map.put(sign, split2[i]);
			}
			String assetCount3 = StringUtil.getString(map.remove("assetCount3"));
			String[] split3 = assetCount3.split("-");
			for (int i = 0; i < split3.length; i++) {
				String sign ="assetCount3num_"+i;
				map.put(sign, split3[i]);
			}
			String assetCount4 = StringUtil.getString(map.remove("assetCount4"));
			String[] split4 = assetCount4.split("-");
			for (int i = 0; i < split4.length; i++) {
				String sign ="assetCount4num_"+i;
				map.put(sign, split4[i]);
			}
			String assetCount5 = StringUtil.getString(map.remove("assetCount5"));
			String[] split5 = assetCount5.split("-");
			for (int i = 0; i < split5.length; i++) {
				String sign ="assetCount5num_"+i;
				map.put(sign, split5[i]);
			}
		}
		return result;
	}
	/**
	 * 处理数据（时段分析统计使用）(4)
	 * @param result
	 * @param k
	 * @return
	 */
	public  AppResult processingData(AppResult result,AppParam params) {
		List<Map<String, Object>> rows = result.getRows();
		int time = NumberUtil.getInt(params.getAttr("time"));
		if (time==1) {
			for (Map<String, Object> map : rows) {
				int hours1 = NumberUtil.getInt(map.get("hours"));
				map.put("hour", hours1);
			}
		}else {
			for (int i = 0; i < rows.size(); i++) {
				Map<String, Object> map = rows.get(i);
				int hours1 = NumberUtil.getInt(map.get("hours"));
				String channelCode1 = StringUtil.getString(map.get("channelCode"));
				for (int l = 0; l*time<24; l++) {
					if (hours1>=l*time&&hours1<(l+1)*time) {
						map.put("hour", "["+l*time+","+(l+1)*time+")");
						for (int j = i+1; j < rows.size(); j++) {
							Map<String, Object> map2 = rows.get(j);
							int hours2 = NumberUtil.getInt(map2.get("hours"));
							String channelCode2 = StringUtil.getString(map2.get("channelCode"));
							if (hours2>=l*time&&hours2<(l+1)*time&&channelCode1.equals(channelCode2)) {
								Set<String> keySet = map.keySet();
								for (String key : keySet) {
									if (key.contains("_")) {
										int sum = NumberUtil.getInt(map.get(key))+NumberUtil.getInt(map2.get(key));
										map2.put(key, sum);
									}
								}
								map2.put("hour", "["+l*time+","+(l+1)*time+")");
								rows.remove(i);
								i--;
								break;
							}
						}
					}
				}
			}
		}
		result.getPage().setTotalRecords(rows.size());
		return result;
	}
	
	/**
	 * 小渠道处理数据（时段分析统计使用）(5)
	 * @param result
	 * @param k
	 * @return
	 */
	public  AppResult processingDataDtl(AppResult result,AppParam params) {
		List<Map<String, Object>> rows = result.getRows();
		int time = NumberUtil.getInt(params.getAttr("time"));
		if (time==1) {
			for (Map<String, Object> map : rows) {
				int hours1 = NumberUtil.getInt(map.get("hours"));
				map.put("hour", hours1);
			}
		}else {
			for (int i = 0; i < rows.size(); i++) {
				Map<String, Object> map = rows.get(i);
				int hours1 = NumberUtil.getInt(map.get("hours"));
				String channelCode1 = StringUtil.getString(map.get("channelDetail"));
				for (int l = 0; l*time<24; l++) {
					if (hours1>=l*time&&hours1<(l+1)*time) {
						map.put("hour", "["+l*time+","+(l+1)*time+")");
						for (int j = i+1; j < rows.size(); j++) {
							Map<String, Object> map2 = rows.get(j);
							int hours2 = NumberUtil.getInt(map2.get("hours"));
							String channelCode2 = StringUtil.getString(map2.get("channelDetail"));
							if (hours2>=l*time&&hours2<(l+1)*time&&channelCode1.equals(channelCode2)) {
								Set<String> keySet = map.keySet();
								for (String key : keySet) {
									if (key.contains("_")) {
										int sum = NumberUtil.getInt(map.get(key))+NumberUtil.getInt(map2.get(key));
										map2.put(key, sum);
									}
								}
								map2.put("hour", "["+l*time+","+(l+1)*time+")");
								rows.remove(i);
								i--;
								break;
							}
						}
					}
				}
			}
		}
		result.getPage().setTotalRecords(rows.size());
		return result;
	}
	/**
	 * (6)
	 * @param rows
	 * @param dateType
	 * @param startRecordDate
	 * @param endRecordDate
	 */
	public  void getChannelDtlRecodeAndAddList (AppResult result, AppParam params) {
		List<Map<String, Object>> rows = result.getRows();
		String dateType = StringUtil.getString(params.getAttr("dateType"));
		String startRecordDate = StringUtil.getString(params.getAttr("startRecordDate"));
		String endRecordDate = StringUtil.getString(params.getAttr("endRecordDate"));
		DecimalFormat df = new DecimalFormat("######0.00");
		if ("day".equals(dateType)) {
			for (Map<String, Object> row : rows) {
				AppParam param = new AppParam("pageCountService", "queryChannelDtlDayUv");
				param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				param.addAttr("channelDetail", row.get("channelDetail"));
				param.addAttr("countDate", row.get("recordDate"));
				AppResult uvResult = RemoteInvoke.getInstance().callNoTx(param);
				double uv = NumberUtil.getDouble(uvResult.getRow(0).get("recordCount"), 0.0);
				row.put("uv", uv);
				int applyCount = NumberUtil.getInt(row.get("applyCount"), 0);
				double cvr = 0.0;
				if (uv > 0) {
					cvr = (applyCount / uv) * 100.0;
				}
				row.put("cvr", df.format(cvr) + "%");
			}
		}
		
		if ("range".equals(dateType)) {
			for (Map<String, Object> row : rows) {
				AppParam param = new AppParam("pageCountService", "queryChannelDtlRangeUv");
				param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				param.addAttr("channelDetail", row.get("channelDetail"));
				param.addAttr("startRecordDate", startRecordDate);
				param.addAttr("endRecordDate", endRecordDate);
				AppResult queryResult = RemoteInvoke.getInstance().callNoTx(param);
				double uv = NumberUtil.getDouble(queryResult. getRow(0).get("recordCount"), 0.0);
				row.put("uv", uv);
				int applyCount = NumberUtil.getInt(row.get("applyCount"), 0);
				double cvr = 0.0;
				if (uv > 0) {
					cvr = (applyCount / uv) * 100.0;
				}
				row.put("cvr", df.format(cvr) + "%");
			}
		}
		
		if ("month".equals(dateType)) {
			for (Map<String, Object> row : rows) {
				AppParam param = new AppParam("pageCountService", "queryChannelDtlMonthUv");
				param.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
				param.addAttr("channelDetail", row.get("channelDetail"));
				String recordDate = StringUtil.getString(row.get("recordDate")) + "-01";
				param.addAttr("startRecordDate", recordDate);
				Date now = DateUtil.toDateByString(recordDate, DateUtil.DATE_PATTERN_YYYY_MM_DD);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(now);
				calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
				String endDateStr = DateUtil.toStringByParttern(calendar.getTime(), DateUtil.DATE_PATTERN_YYYY_MM_DD);
				param.addAttr("endRecordDate", endDateStr);
				AppResult uvResult = RemoteInvoke.getInstance().callNoTx(param);
				double uv = NumberUtil.getDouble(uvResult.getRow(0).get("recordCount"), 0.0);
				row.put("uv", uv);
				int applyCount = NumberUtil.getInt(row.get("applyCount"), 0);
				double cvr = 0.0;
				if (uv > 0) {
					cvr = (applyCount / uv) * 100.0;
				}
				if (!StringUtils.isEmpty(uvResult.getRow(0).get("pageEn"))) {
					row.put("pageReferer", uvResult.getRow(0).get("pageEn"));
				}
				row.put("cvr", cvr > 0 ? df.format(cvr) + "%" : cvr + "%");
			}
		}
		
	}
	/**
	 * (7)
	 * @param result
	 */
	public  void addField(AppResult result, AppParam params) {
		DecimalFormat df = new DecimalFormat("######0.00");
		List<Map<String, Object>> rows = result.getRows();
		for (Map<String, Object> map : rows) {
			int failCount = NumberUtil.getInt(map.get("failCount"));
			int sucCount = NumberUtil.getInt(map.get("sucCount"));
			int allCount = failCount + sucCount;
			double temp = (sucCount*100.00)/allCount;
			map.put("allCount", allCount);
			map.put("svr", df.format(temp)+"%");
		}
	}
}
