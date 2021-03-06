package org.xxjr.busiIn.kf.ext;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class ChannelOrigSumService extends BaseService {
	public static final String NAMESPACE = "CHANNELORIGSUM";
	
	/**大渠道基本情况统计(原始)
	 * channelOrigBase
	 * @param params
	 * @return
	 */
	public AppResult channelOrigBase(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelBase", "channelBaseCount");
	}
	
	
	/**金额资质详细情况统计(原始)
	 * channelOrigDtl
	 * @param params
	 * @return
	 */
	public AppResult channelOrigDtl(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelDtl", "channelBaseCount");
	}
	
	
	/**24 小时分析统计-每天归档
	 * channelSale
	 * @param params
	 * @return
	 */
	public AppResult channel24Analysis(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channel24Analysis", "channel24AnalysisCount");
	}
	/**
	 * channel24AnalysisCount
	 * @param params
	 * @return
	 */
	public AppResult channel24AnalysisCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "channel24AnalysisCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**24 小时分析统计
	 * channelSale
	 * @param params
	 * @return
	 */
	public AppResult channel24Time(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channel24Time", "channel24TimeCount");
	}
	/**计数(跟进后)
	 * 
	 * @param params
	 * @return
	 */
	public AppResult channelBaseCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "channelBaseCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}

}
