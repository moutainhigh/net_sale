package org.xxjr.busiIn.kf.ext;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class ChannelDtlModifySumService extends BaseService {
	public static final String NAMESPACE = "CHANNELDTLMONDIFYSUM";
	
	/**小渠道基本情况统计(跟进后)
	 * channelBase
	 * @param params
	 * @return
	 */
	public AppResult channelDtlBase(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelBase", "channelBaseCount");

	}
	/**
	 * 共用的数量查询sql
	 * @param params
	 * @return
	 */
	public AppResult channelBaseCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "channelBaseCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**金额资质详细情况统计(跟进后)
	 * channelDtlAmount
	 * @param params
	 * @return
	 */
	public AppResult channelDtlAmount(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelDtl", "channelBaseCount");

	}
	
	
	/**线索流向情况统计(跟进后)
	 * channelSale
	 * @param params
	 * @return
	 */
	public AppResult channelDtlSale(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelSale", "channelBaseCount");

	}
	
	
	/**网销门店情况统计(跟进后)
	 * channelNet
	 * @param params
	 * @return
	 */
	public AppResult channelDtlNet(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelNet", "channelBaseCount");
	}
	
	/**第三方渠道数据查询(对外)
	 * channelNet
	 * @param params
	 * @return
	 */
	public AppResult thirdChannel(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "thirdChannel", "thirdChannelCount");
	}
	
	public AppResult thirdChannelCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "thirdChannelCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
}
