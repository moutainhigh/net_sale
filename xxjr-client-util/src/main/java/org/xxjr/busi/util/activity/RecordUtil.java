package org.xxjr.busi.util.activity;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.core.service.RemoteInvoke;

/**
 * 数据记录
 * @author Administrator
 *
 */
public class RecordUtil {

	
	/**
	 * 删除滴滴领礼包记录
	 * @param ticketId
	 */
	public static void delDidiRecord(Object ticketId){
		AppParam delParam = new AppParam();
		delParam.setService("didiTicketService");
		delParam.setMethod("delete");
		delParam.addAttr("ticketId", ticketId);
		delParam.setRmiServiceName(AppProperties
				.getProperties(DuoduoConstant.RMI_SERVICE_START + "busi"));
		RemoteInvoke.getInstance().call(delParam);
	}
}
