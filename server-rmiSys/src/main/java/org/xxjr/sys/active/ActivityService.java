package org.xxjr.sys.active;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;
import org.xxjr.sys.util.active.ActiveCache;
import org.xxjr.sys.util.active.ActiveConstants;

import com.alibaba.fastjson.JSONArray;

@Lazy
@Service
public class ActivityService extends BaseService {
	private static final String NAMESPACE = "ACTIVITY";
	
	
	public AppResult query(AppParam context) {
		return super.query(context, NAMESPACE);
	}
	
	public AppResult queryByPage(AppParam context) {
		return super.queryByPage(context, NAMESPACE);
	}
	
	public AppResult queryCount(AppParam context) {
		int size = getDao().count(NAMESPACE, super.COUNT, context.getAttr(), context.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.TOTAL_SIZE, size);
		return result;
	}
	/***
	 * 插入信息
	 * @param context
	 * @return
	 */
	public AppResult insert(AppParam context) {
		context.addAttr("createTime", new Date());
		context.addAttr("createBy", DuoduoSession.getUserName());
		AppResult result = super.insert(context, NAMESPACE);
		Object activeId = context.getAttr("activeId");
		//add
		addPrams(context, activeId);
		//refresh cache
		ActiveCache.refreshActiveInfo(Long.valueOf(activeId.toString()));
		return result;
	}
	
	/***
	 * 修改
	 * @param context
	 * @return
	 */
	public AppResult update(AppParam context) {
		if (StringUtils.isEmpty(context.getAttr("activeId"))) {
			throw new AppException("缺少参数:activeId");
		}
		context.addAttr("updateTime", new Date());
		context.addAttr("updateBy", DuoduoSession.getUserName());
		
		AppResult result = super.update(context, NAMESPACE);
		Object activeId = context.getAttr("activeId");
		///delete params
		super.delete(context, ActivityParamService.NAMESPACE);	
		//add
		addPrams(context, activeId);
		//refresh cache
		ActiveCache.refreshActiveInfo(Long.valueOf(activeId.toString()));
		return result;
	
	}

	/***
	 * 修改状态
	 * @param context
	 * @return
	 */
	public AppResult updateStatus(AppParam context) {
		if (StringUtils.isEmpty(context.getAttr("activeId")) ||
				StringUtils.isEmpty(context.getAttr("activeStatus"))) {
			throw new AppException("缺少参数:activeId");
		}
		int status = Integer.valueOf(context.getAttr("activeStatus").toString());
		AppParam param = new AppParam();
		param.setDataBase(DBConst.Key_sys_DB);
		if (ActiveConstants.ACTIVE_STATUS_3 == status || 
				ActiveConstants.ACTIVE_STATUS_1 == status) {
			param.addAttr("fromStatus", ActiveConstants.ACTIVE_STATUS_0 + "");
		}else if (ActiveConstants.ACTIVE_STATUS_2 == status) {
			param.addAttr("fromStatus", ActiveConstants.ACTIVE_STATUS_1 + "");
		}else{
			throw new AppException("相应的状态不正确:status =" + status);
		}
		Long activeId = Long.valueOf(context.getAttr("activeId").toString());
		param.addAttr("activeId", context.getAttr("activeId"));
		param.addAttr("activeStatus", context.getAttr("activeStatus").toString());
		AppResult result = super.update(param, NAMESPACE);
		//refresh cache
		ActiveCache.refreshActiveInfo(activeId);
		return result;
	
	}
	/***
	 * add active Params
	 * 
	 * @param context
	 * @param activeId
	 */
	private void addPrams(AppParam context, Object activeId) {
		Object paramKeys = context.getAttr("paramKeys");
		if (StringUtils.isEmpty(paramKeys)) {
			throw new AppException("缺少参数:paramKeys");
		}
		JSONArray paramArray = (JSONArray) JSONArray
				.parse(paramKeys.toString());
		List<Map<String, Object>> paramList = JsonUtil
				.getListFromJsonArray(paramArray);
		for (Map<String, Object> param : paramList) {
			param.put("activeId", activeId);
			super.getDao().insert(ActivityParamService.NAMESPACE, super.INSERT,param,context.getDataBase());
			
		}
	}
	
	/***
	 * del params order activeId and rule and condition
	 * @param context
	 * @param activeId
	 * **/
	private void delParams(AppParam context, String activeId) {
		if(StringUtils.isEmpty(activeId)) {
			throw new AppException("缺少参数:activeId");
		}
		AppParam param = new AppParam();
		param.addAttr("activeId", activeId);
		param.setDataBase(DBConst.Key_sys_DB);
		super.getDao().delete(ActivityParamService.NAMESPACE, super.DELETE, param.getAttr(),context.getDataBase());
		
		//delete rule
		AppParam ruleContext = new AppParam();
		ruleContext.addAttr("activeId", activeId);
		ruleContext.setDataBase(DBConst.Key_sys_DB);
		AppResult queryRule = super.query(ruleContext, RuleService.NAMESPACE);
		
		this.deleteRule(queryRule.getRows());
		
	}
	/**
	 * 删除规则
	 * @param list
	 */
	private void deleteRule(List<Map<String, Object>> list) {
		for(Map<String, Object> m : list) {
			Object ruleId = m.get("ruleId");
			AppParam duoduoRule = new AppParam();
			duoduoRule.setDataBase(DBConst.Key_sys_DB);
			duoduoRule.addAttr("ruleId", ruleId);
			//delete condition
			super.delete(duoduoRule, RuleConditionService.NAMESPACE);
			//delete rule
			super.delete(duoduoRule, RuleService.NAMESPACE);
		}
	}
	
	
	/**
	 * 删除
	 * @param context
	 * @return
	 */
	public AppResult delete(AppParam context) {
		String ids = (String) context.getAttr("ids");
		AppResult result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.addAttr("activeId", id);
				this.delParams(param, ids);
				result = super.delete(param, NAMESPACE);
				ActiveCache.deleteActiveCache(Long.valueOf(id.toString()));
			}
		} else if (context.getAttr("activeId") != null) {
			result = super.delete(context, NAMESPACE);
			ActiveCache.deleteActiveCache(Long.valueOf(context.getAttr("activeId").toString()));
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
}
