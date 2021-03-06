package org.xxjr.busiIn.kf.ext;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class ChannelDtlOrigSumService extends BaseService {
	public static final String NAMESPACE = "CHANNELDTLORIGSUM";
	
	/**小渠道基本情况统计(原始)
	 * channelBase
	 * @param params
	 * @return
	 */
	public AppResult channelBase(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelBase", "channelBaseCount");
	}
	
	
	/**金额资质详细情况统计(原始)
	 * channelDtl
	 * @param params
	 * @return
	 */
	public AppResult channelDtl(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelDtl", "channelBaseCount");

	}
	
	public AppResult channelBaseCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "channelBaseCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**24 小时分析统计-每天归档
	 * channelSale
	 * @param params
	 * @return
	 */
	public AppResult channelDtl24Analysis(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelDtl24Analysis", "channelDtl24AnalysisCount");

	}
	/**
	 * channelDtl24AnalysisCount
	 * @param params
	 * @return
	 */
	public AppResult channelDtl24AnalysisCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "channelDtl24AnalysisCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**24 小时分析统计
	 * channelSale
	 * @param params
	 * @return
	 */
	/*public AppResult channel24Time(AppParam params) {
		if(params.getCurrentPage() == -1){
			params.setCurrentPage(1);
			params.setEveryPage(1000);
			return super.queryByPage(params, NAMESPACE, "channel24Time", "channel24TimeCount");
		}else{
			return super.queryByPage(params, NAMESPACE, "channel24Time", "channel24TimeCount");
		}

	}*/

}
