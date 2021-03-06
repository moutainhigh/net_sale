package org.xxjr.sys.util;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;

/***
 * 界面浏览工具类
 * @author llw
 *
 */
public class PageRecordUtils {

	/***
	 * 设置点击的次数
	 * @param params
	 * @return
	 */
	public static AppResult updatePageBrowseCount(AppParam params){
		AppResult result = new AppResult();
		
		params.addAttr("dayCount", "1");
		String sysCount = params.getAttr("sysCode")+"Count";
		params.addAttr(sysCount, "1");
		if("downloadCount".equals(sysCount)){
			params.removeAttr("dayCount");
		}

		params.setService("pageRecordService");
		params.setMethod("updatePageBroCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_sum));
		RemoteInvoke.getInstance().call(params);

		return result;
	}
}
