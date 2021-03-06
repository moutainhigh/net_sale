package org.xxjr.busiIn.store;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.ddq.active.mq.store.StoreTaskSend;
import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.DateUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.StoreConstant;
import org.xxjr.store.util.StoreApplyUtils;

@Lazy
@Service
@Slf4j
public class CustVisitService extends BaseService {
	private static final String NAMESPACE = "CUSTVISIT";

	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}

	/**
	 * queryByPage
	 * @param params
	 * @return
	 */
	public AppResult queryByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE);
	}

	/**
	 * 分页查询访客记录
	 * @param params
	 * @return
	 */
	public AppResult queryVisitByPage(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryVisitByPage","queryVisitByCount");
	}
	
	/**
	 * 查询访客记录（不分页）
	 * @param params
	 * @return
	 */
	public AppResult queryVisitByRecordId(AppParam params) {
		return super.query(params, NAMESPACE,"queryVisitByPage");
	}
	/**
	 * queryCount
	 * @param params
	 * @return
	 */
	public AppResult queryCount(AppParam params) {
		int size = getDao().count(NAMESPACE, super.COUNT,params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}


	/**
	 * insert
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		AppResult result = new AppResult();
		params.addAttr("createTime", new Date());
		result = super.insert(params, NAMESPACE);
		result.putAttr("recordId", params.getAttr("recordId"));
		return result;
	}

	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		params.addAttr("updateTime", new Date());
		return super.update(params, NAMESPACE);
	}

	/**
	 * delete
	 * @param params
	 * @return
	 */
	public AppResult delete(AppParam params) {
		String ids = (String) params.getAttr("ids");
		AppResult  result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.addAttr("recordId", id);

				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("recordId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}

	/***
	 * 保存上门客户信息
	 * @param params
	 * @return
	 */
	public AppResult saveVisitCust(AppParam params){
		AppResult result = new AppResult();

		String custTel = params.getAttr("custTel").toString();
		String receiverTel = params.getAttr("receiverTel").toString();
		String visitType = params.getAttr("visitType").toString();

		//根据用户手机号查询申请信息
		Object applyId = null;
		Object customerId = null;
		Object orgId = null;
		Object lastStore = null;
		AppParam queryParam = new AppParam("borrowStoreApplyService","query");
		queryParam.addAttr("telephone", custTel);
		AppResult custResult = SoaManager.getInstance().invoke(queryParam);
		if(custResult.isSuccess() && custResult.getRows().size() >0){
			applyId = custResult.getRow(0).get("applyId");
			lastStore = custResult.getRow(0).get("lastStore");
			params.addAttr("applyId", applyId);
		}

		//根据顾问手机号查询顾问信息
		AppParam recParam = new AppParam("busiCustService","query");
		recParam.addAttr("telephone", receiverTel);
		recParam.addAttr("roleTypeIn", "3,6,7,8,9");
		AppResult recResult = SoaManager.getInstance().invoke(recParam);
		if(recResult.isSuccess() && recResult.getRows().size() >0){
			customerId = recResult.getRow(0).get("customerId");
			orgId = recResult.getRow(0).get("orgId");
			params.addAttr("customerId", customerId);
		}else{
			//贷款咨询要求必须填写正确的顾问
			if("贷款咨询".equals(visitType)){
				result.setSuccess(false);
				result.setMessage("接待顾问不存在");
				return result;
			}
		}

		//保存上门登记信息
		result = this.insert(params);

		//修改预约上门状态(改成已上门)
		if(result.isSuccess() && !StringUtils.isEmpty(applyId) && !StringUtils.isEmpty(customerId)){
			try{
				if(!StringUtils.isEmpty(lastStore)){
					String visitTime = DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS);
					//增加上门明细,谁接待算谁的上门
					AppParam visitParams = new AppParam("treatVisitDetailService", "update");
					visitParams.addAttrs(params.getAttr());
					visitParams.addAttr("recCustId", customerId.toString());
					visitParams.addAttr("applyId", applyId);
					visitParams.addAttr("visitTime", visitTime);
					visitParams.addAttr("visitType", StoreConstant.STORE_VISIT_TYPE_2);//访客登记
					SoaManager.getInstance().invoke(visitParams);
					// 删除详情页缓存
					RedisUtils.getRedisService().del(StoreApplyUtils.STORE_APPLY_VISIT_RECORD + applyId);
					
					
					AppParam bookParams = new AppParam("treatBookService", "update");
					bookParams.addAttr("applyId", applyId);
					bookParams.addAttr("customerId", customerId.toString());
					bookParams.addAttr("visitTime", visitTime);
					bookParams.addAttr("orgId", orgId);
					bookParams.addAttr("status", StoreConstant.STORE_BOOK_3);//已上门
					bookParams.addAttr("bookDesc", "贷款咨询");
					SoaManager.getInstance().callNewTx(bookParams);
					
					try{
						//创建任务对象
						StoreTaskSend storeSend = (StoreTaskSend)SpringAppContext.getBean(StoreTaskSend.class);
						
						Map<String, Object> msgParam = new HashMap<String, Object>();
						msgParam.put("visitCount", "1");
						msgParam.put("recordDate", DateUtil.getSimpleFmt(new Date()));
						storeSend.sendStoreMessage(customerId.toString(),"countDealType" , msgParam);
					}catch (Exception e) {
						log.error("visitDeal error", e);
						ExceptionUtil.setExceptionMessage(e, result,
								DuoduoSession.getShowLog());
					}
					
				}
			}catch(Exception e){
				log.error("修改上门信息失败，customerId="+ customerId +",applyId=" +applyId, e);
			}
		}
		return result;

	}

	/***
	 * 到访客户列表
	 * @param params
	 * @return
	 */
	public AppResult queryVisitList(AppParam params){
		return super.queryByPage(params, NAMESPACE,"queryVisitList","queryVisitCount");
	}


	/***
	 * 关联申请ID
	 * @param params
	 */
	public AppResult relationDeal(AppParam params){
		AppResult result = new AppResult();

		Object applyId = null;
		Object lastStore = null;
		Object customerId = null;
		Object orgId = null;
		AppParam queryParam = new AppParam("borrowStoreApplyService","query");
		queryParam.addAttr("telephone", params.getAttr("telephone"));
		AppResult applyResult = SoaManager.getInstance().invoke(queryParam);

		if(applyResult.isSuccess() && applyResult.getRows().size() >0){
			applyId = applyResult.getRow(0).get("applyId");
			lastStore = applyResult.getRow(0).get("lastStore");
			orgId = applyResult.getRow(0).get("orgId");
			params.addAttr("applyId", applyId);
		}else{
			result.setSuccess(false);
			result.setMessage("该号码无申请记录无法关联");
			return result;
		}
		
		//查询接待人信息
		queryParam = new AppParam();
		queryParam.addAttr("recordId", params.getAttr("recordId"));
		AppResult recResult = this.query(queryParam, NAMESPACE, "queryReceviceInfo");
		if(recResult.isSuccess() && recResult.getRows().size() > 0){
			customerId = recResult.getRow(0).get("customerId");
		}
		
		//非本人处理单不允许关联
		if(StringUtils.isEmpty(customerId) || !customerId.equals(lastStore)){
			result.setSuccess(false);
			result.setMessage("非本人处理单不允许关联");
			return result;
		}

		int size = getDao().update(NAMESPACE,"relationDeal",params.getAttr(),params.getDataBase());		

		//修改上门状态
		if(size > 0){
			if(result.isSuccess() && !StringUtils.isEmpty(applyId) && !StringUtils.isEmpty(lastStore)){
				try{
					AppParam bookParams = new AppParam("treatBookService", "update");
					bookParams.addAttr("applyId", applyId);
					bookParams.addAttr("customerId", lastStore);
					bookParams.addAttr("orgId", orgId);
					bookParams.addAttr("status", "3");//已上门
					bookParams.addAttr("bookDesc", "贷款咨询");
					SoaManager.getInstance().callNewTx(bookParams);

				}catch(Exception e){
					log.error("修改上门信息失败，customerId="+ lastStore +",applyId=" +applyId, e);
				}
			}
		}
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}

	/***
	 * 转单处理
	 * 将非本人的单子转到本人接待的名下
	 * @param params
	 * @return
	 */
	public AppResult checkTransDeal(AppParam params){
		AppResult result = new AppResult();

		Object recordId = params.getAttr("recordId");
		Object applyId = null;
		Object customerId = null;
		Object orgId = null;
		AppParam queryParam = new AppParam();
		queryParam.addAttr("recordId", recordId);
		result = this.query(queryParam, NAMESPACE, "queryReceviceInfo");

		//转单
		if(result.isSuccess() && result.getRows().size() >0){
			applyId = result.getRow(0).get("applyId");
			customerId = result.getRow(0).get("customerId");
			orgId = result.getRow(0).get("orgId");
			if(!StringUtils.isEmpty(applyId)){
				AppParam updateParam = new AppParam("storeOptExtService","transDeal");
				updateParam.addAttr("applyId", applyId);
				updateParam.addAttr("lastStore", customerId);
				updateParam.addAttr("orgId",  orgId);
				result = SoaManager.getInstance().callNewTx(updateParam);
			}else{
				result.setSuccess(false);
				result.setMessage("转单处理单子不存在");
				return result;
			}
		}else{
			result.setSuccess(false);
			result.setMessage("转单处理单子不存在");
			return result;
		}

		return result;
	}
}
