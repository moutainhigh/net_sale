package org.xxjr.job.listener.busi;

import java.util.Date;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.llw.job.core.BaseExecteJob;
import org.llw.job.util.JobConstant;
import org.llw.job.util.JobUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.xxjr.job.listener.busi.store.StoreDepartureUtils;
import org.xxjr.job.listener.busi.store.StoreNotifyUtils;

/**
 * 自动更新申请总次数和关联未匹配订单的通话记录和录音自动化任务
 * @author Administrator
 *
 */
@Lazy
@Component
public class AutoUpdateBroCountJob implements BaseExecteJob  {

	/**
	 * 自动更新申请总次数和关联未匹配订单的通话记录和录音自动化任务
	 * @param processId
	 * @return
	 */
	@Override
	public AppResult executeJob(AppParam param){
		AppResult result = new AppResult();
		Object processId = param.getAttr(JobConstant.KEY_processId);
		try {
	
			//关联未匹配订单的通话记录
		//	StoreCallRecordUtils.relationCallRecord(processId);
			//关联未匹配订单的通话录音记录
		//	StoreCallRecordUtils.relationCallAudioRecord(processId);
			//删除一个月以上的个人消息
			StoreNotifyUtils.deleteMoreOneMonthNotify(processId);
			String today = DateUtil.toStringByParttern(new Date(),DateUtil.DATE_PATTERN_YYYY_MM_DD);
			String toMonth = DateUtil.toStringByParttern(new Date(),"yyyy-MM");
			//离职人员签单基本统计
			StoreDepartureUtils.storeSignDeparture(processId, today);
			//离职人员签单月度基本统计
			StoreDepartureUtils.storeSignDepartMonth(processId, toMonth);
			//离职人员回款基本统计
			StoreDepartureUtils.storeReLoanDeparture(processId, today);
			//离职人员回款月度基本统计
			StoreDepartureUtils.storeReloanDepartMonth(processId, toMonth);
			//离职人员成本基本统计
			StoreDepartureUtils.storeCostDeparture(processId, today);
			//离职人员成本月度基本统计
			StoreDepartureUtils.storeCostDepartMonth(processId, toMonth);
		} catch (Exception e) {
			LogerUtil.error(AutoUpdateBroCountJob.class, e, "AutoUpdateBroCountJob >>>>>>>>>>>>>>>>>>error");
			JobUtil.addProcessExecute(processId, "自动更新申请总次数和关联未匹配订单的通话记录和录音自动化任务" + e.getMessage() );
		}
		return result;
	}
}
