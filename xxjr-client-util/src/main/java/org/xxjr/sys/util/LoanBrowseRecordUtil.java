package org.xxjr.sys.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;

public class LoanBrowseRecordUtil {


	public AppResult recordCount(AppParam param) {
		AppResult result = new AppResult();
		Date day = new Date();
		param = new AppParam("loanBrowseRecordService", "save");
		param.addAttr("recordTime",
				new SimpleDateFormat("yyyy-MM-dd").format(day));
		param.addAttr("dayCount", 1);
		param.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_sum));
		RemoteInvoke.getInstance().call(param);
		return result;
	}


	/***
	 * 设置点击的次数
	 * @param params
	 * @return
	 */
	public static AppResult updateBrowseCount(AppParam params){
		AppResult result = new AppResult();
		
		params.addAttr("cardCount", "1");
		String sysCount = params.getAttr("sysCode")+"Count";
		params.addAttr(sysCount, "1");

		params.setService("loanBrowseRecordService");
		params.setMethod("updateBroCount");
		params.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START
						+ ServiceKey.Key_sum));
		RemoteInvoke.getInstance().call(params);

		return result;
	}


}
