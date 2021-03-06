package org.xxjr.busi.util;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.CreditCardUtil;
import org.xxjr.sys.util.ServiceKey;

/**
 * 落地页统计相关方法
 * @author Administrator
 *
 */
public class PageCountUtil {

	public static AppResult pageCount (AppParam param) {
		AppParam updateParam = new AppParam("pageCountService", "recordCount");
		updateParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		Object pageId = param.getAttr("pageId");
		Object pageReferer = param.getAttr("pageReferer");
		if (StringUtils.isEmpty(pageReferer)) {
			pageReferer = param.getAttr("sysCode");
		}
		if (StringUtils.isEmpty(pageId) && StringUtils.isEmpty(pageReferer)) {
			return CustomerUtil.retErrorMsg("pageId或pageReferer至少有一个!");
		}
		if (StringUtils.isEmpty(param.getAttr("sumType"))) {//如果sumType不传，就默认统计pv
			param.addAttr("sumType", "pv");
		}
		
		Object channelDetail = param.getAttr("regSourceType");
		if (StringUtils.isEmpty(channelDetail)) {
			channelDetail = param.getAttr("channelDetail");
		}
		channelDetail = StringUtils.isEmpty(channelDetail) ? CustConstant.CUST_SOURCETYPE_DEFAULT
				: channelDetail;
		updateParam.addAttr("pageId", pageId);
		updateParam.addAttr("sumType", param.getAttr("sumType"));
		updateParam.addAttr("pageReferer", pageReferer);
		String channelCode = BorrowChannelUtil.getChannelByStartCode(channelDetail.toString());
		if (StringUtils.isEmpty(channelCode)) {//如果大渠道号不存在，就使用小渠道号
			AppResult result = new AppResult();
			result.setMessage("渠道号不存在!");
			return result;
		}
		updateParam.addAttr("channelCode", channelCode);
		updateParam.addAttr("channelDetail", channelDetail);
		if (SpringAppContext.getBean("pageCountService") == null) {
			return RemoteInvoke.getInstance().call(updateParam);
		}else {
			return SoaManager.getInstance().invoke(updateParam);
		}
	}
	
	public static AppResult btnCount(AppParam param) {
		
		//先更新缓存的点击数
		CreditCardUtil.setCardApplyCache(param);
		
		AppParam updateParam = new AppParam("btnCountService", "recordCount");
		updateParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_sum));
		Object pageId = param.getAttr("pageId");
		Object sysCode = param.getAttr("sysCode");
		Object btnId = param.getAttr("btnId");
		if (StringUtils.isEmpty(btnId)) {
			btnId = param.getAttr("recordId");
		}
		if (StringUtils.isEmpty(pageId) && StringUtils.isEmpty(sysCode)) {
			return CustomerUtil.retErrorMsg("pageId或sysCode至少有一个!");
		}
		if (StringUtils.isEmpty(btnId)) {
			return CustomerUtil.retErrorMsg("pageId或btnId不存在!");
		}
		
		updateParam.addAttr("pageId", pageId);
		updateParam.addAttr("sysCode", sysCode);
		updateParam.addAttr("btnId", btnId);
		if (SpringAppContext.getBean("btnCountService") == null) {
			return RemoteInvoke.getInstance().call(updateParam);
		}else {
			return SoaManager.getInstance().invoke(updateParam);
		}
	}
}
