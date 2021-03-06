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
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;

import com.alibaba.fastjson.JSONArray;
@Lazy
@Service
public class RuleService extends BaseService {
	public static final String NAMESPACE = "RULE";

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
	
	/****
	 * 
	 * @param context
	 * @return
	 */
	public AppResult insert(AppParam context) {
		//insert rule table
		context.addAttr("createTime", new Date());
		AppResult result = super.insert(context, NAMESPACE);
		//insert ruleCondition
		String paramKey = String.valueOf(context.getAttr().get("paramKeys"));
		JSONArray paramKeyJSON = JSONArray.parseArray(paramKey);
		
		List<Map<String, Object>> paramKeyList = JsonUtil.getListFromJsonArray(paramKeyJSON);
		for (Map<String, Object> param : paramKeyList) {
			param.put("ruleId", context.getAttr("ruleId"));
			super.getDao().insert(RuleConditionService.NAMESPACE, super.INSERT,param, context.getDataBase());
		}
		return result;
	}
	
	public AppResult update(AppParam context) {
		// update condition
		JSONArray paramArray = (JSONArray) JSONArray.parse(context.getAttr("paramKeys").toString());
		List<Map<String, Object>> paramList = JsonUtil.getListFromJsonArray(paramArray);
		
		//update ruleInfo
		AppResult result = super.update(context, NAMESPACE);
		
		//delete ruleCondtion
		AppParam condition = new AppParam();
		condition.addAttr("ruleId", context.getAttr("ruleId"));
		condition.setDataBase(DBConst.Key_sys_DB);
		super.delete(condition, RuleConditionService.NAMESPACE);
		//add ruleCondtion
		for(Map<String, Object> obj : paramList) {
			obj.put("ruleId", context.getAttr("ruleId"));
			obj.put("conditionId", "");
			super.getDao().insert(RuleConditionService.NAMESPACE,super.INSERT, obj,context.getDataBase());
		}
		return result;
	}
	
	
	
	
	public AppResult delete(AppParam context) {
		String ids = (String) context.getAttr("ids");
		AppResult result = null;
		if (!StringUtils.isEmpty(ids)) {
			for (String id : ids.split(",")) {
				AppParam param = new AppParam();
				param.addAttr("ruleId", id);
				param.setDataBase(DBConst.Key_sys_DB);
				result = super.delete(param, NAMESPACE);
			}
		} else if (context.getAttr("ruleId") != null) {
			result = super.delete(context, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}
}
