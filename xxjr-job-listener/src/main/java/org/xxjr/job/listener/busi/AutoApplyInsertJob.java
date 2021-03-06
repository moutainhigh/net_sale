package org.xxjr.job.listener.busi;

import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.LogerUtil;
import org.llw.job.core.BaseExecteJob;
import org.llw.job.util.JobConstant;
import org.llw.job.util.JobUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.xxjr.job.listener.busi.store.StoreDepartureUtils;
import org.xxjr.sys.util.ServiceKey;
/***
 * 申请数据批量插入
 *
 */
@Lazy
@Component
public class AutoApplyInsertJob implements BaseExecteJob {
	@Override
	public AppResult executeJob(AppParam param) {
		AppResult result = new AppResult();
		
		Object processId = param.getAttr(JobConstant.KEY_processId);
//		
//		try {
//			int allotApplyDataCount = SysParamsUtil.getIntParamByKey("allotApplyDataCount", 500);
//			int currentPage = 1;
//			AppParam queryParam = new AppParam("applyService", "queryByPage");
//			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
//			queryParam.setOrderBy("applyTime");
//			queryParam.setOrderValue("DESC");
//			queryParam.addAttr("status", "0");
//			queryParam.addAttr("endApplyTime", DateUtil.toStringByParttern(DateUtil.getNextMinutes(new Date(), -5), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS));
//			queryParam.addAttr("startApplyTime", DateUtil.toStringByParttern(DateUtil.getNextDay(new Date(), -3), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS));
//			queryParam.addAttr("notLikeName", "测试");
//			queryParam.setCurrentPage(currentPage);
//			queryParam.setEveryPage(allotApplyDataCount);
//			AppResult queryResult = RemoteInvoke.getInstance().callNoTx(queryParam);
//			transfer(queryResult.getRows(), processId);//开始转移第一页的
//		} catch (Exception e) {
//			LogerUtil.error(AutoApplyInsertJob.class, e, "AutoApplyInsertJob >>>>>>>>>>>>>>>>>>error");
//			JobUtil.addProcessExecute(processId, "申请数据批量插入 报错：" + e.getMessage() );
//		}
		
	   // 判断业务员下线
		try {
			StoreDepartureUtils.dealStoreOffline(processId);
		}catch (Exception e) {
			LogerUtil.error(AutoApplyInsertJob.class, e, "StoreDepartureUtils dealStoreOffline >>>>>>>>>>>>>>>>>>error");
			JobUtil.addProcessExecute(processId, "执行业务员下线判断报错：" + e.getMessage() );
		}
		return result;
	}
	
	/**转移操作**/
	private void transfer (List<Map<String, Object>> applyList, Object processId) {
		AppResult result = new AppResult();
		AppParam batchParam = new AppParam("applyService", "batchTransfer");
		batchParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
		int size =0;
		int falureSize = 0;
		for(Map<String, Object> applyInfo:applyList){
			batchParam.addAttr("applyList", applyInfo);
			try{
				result = RemoteInvoke.getInstance().call(batchParam);
				if(result.isSuccess()){
					size++;
				}else{
					falureSize++;
					error(applyInfo, result.getMessage());
					JobUtil.addProcessExecute(processId, "申请数据批量异常：" + result.getMessage());
				}
			}catch(Exception e){
				JobUtil.addProcessExecute(processId, "申请数据批量出错：" +e.getMessage());
				falureSize++;
				error(applyInfo, result.getMessage());
			}
		}
		JobUtil.addProcessExecute(processId, "申请数据批量插入，成功" + size + "条，" + "失败" + falureSize + "条");
	}
	
	/***如果转移失败，status=3**/
	private AppResult error(Map<String, Object> applyInfo,String errorMsg) {
		AppResult result = new AppResult();
		AppParam errParam = new AppParam("applyService", "update");
		errParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
		errParam.addAttr("status", "3");//转移失败
		errParam.addAttr("errMsg", errorMsg);//失败信息
		errParam.addAttr("applyId", applyInfo.get("applyId"));
		result = RemoteInvoke.getInstance().call(errParam);
		return result;
	}

}
