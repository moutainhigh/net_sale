package org.xxjr.busiIn.kf.ext;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class ChannelModifySumService extends BaseService {
	public static final String NAMESPACE = "CHANNELMONDIFYSUM";
	
	/**大渠道基本情况统计(跟进后)
	 * channelBase
	 * @param params
	 * @return
	 */
	public AppResult channelBase(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelBase", "channelBaseCount");
	}
	
	
	/**金额资质详细情况统计(跟进后)
	 * channelDtl
	 * @param params
	 * @return
	 */
	public AppResult channelDtl(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelDtl", "channelBaseCount");
	}
	
	
	/**线索流向情况统计(跟进后)
	 * channelSale
	 * @param params
	 * @return
	 */
	public AppResult channelSale(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelSale", "channelBaseCount");
	}
	
	
	/**网销门店情况统计(跟进后)
	 * channelNet
	 * @param params
	 * @return
	 */
	public AppResult channelNet(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelNet", "channelBaseCount");
	}
	/**网销门店情况统计(实时的)
	 * channelNet
	 * @param params
	 * @return
	 */
	public AppResult channelNetReal(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelNetReal", "channelNetRealCount");
	}
	/**
	 * 计数-网销门店情况统计(实时的)
	 * @param params
	 * @return
	 */
	public AppResult channelNetRealCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "channelNetRealCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**城市统计
	 * citySumary
	 * @param params
	 * @return
	 */
	public AppResult citySumary(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "citySumary", "citySumaryCount");
	}
	/**
	 * 城市统计
	 * @param params
	 * @return
	 */
	public AppResult citySumaryCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "citySumaryCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	/**渠道城市情况统计(跟进后)
	 * channelCity
	 * @param params
	 * @return
	 */
	public AppResult channelCity(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelCity", "channelCityCount");
	}
	/**渠道城市情况统计(跟进后-本月-月度)
	 * channelCityDate
	 * @param params
	 * @return
	 */
	public AppResult channelCityDate(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelCityDate", "channelCityDateCount");
	}
	
	/**
	 * 渠道城市情况统计(跟进后-本月-月度)
	 * @param params
	 * @return
	 */
	public AppResult channelCityDateCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "channelCityDateCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/**
	 * 计数(渠道城市情况统计)
	 * @param params
	 * @return
	 */
	public AppResult channelCityCount(AppParam params) {
		int size = getDao().count(NAMESPACE, "channelCityCount",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	
	
	/**ROI情况统计(跟进后)
	 * channelROI
	 * @param params
	 * @return
	 */
	public AppResult channelROI(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "channelROI", "channelBaseCount");
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
