package org.xxjr.store.web.action.account.sys;

import java.util.List;
import java.util.Map;

import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xxjr.store.web.action.BaseController;
import org.xxjr.store.web.action.account.user.UserInfoAction;

@RestController
@RequestMapping("/account/config/")
public class RedisController extends BaseController{
	
	/**
	 * 查询缓存
	 * @param request
	 * @return
	 */
	@RequestMapping("redisset/query")
	public AppResult query(){
		AppResult result = new AppResult();
		try {	
			String key = request.getParameter("key");
			Object obj = RedisUtils.getRedisService().get(key);
			StringBuffer value = new StringBuffer("");
			String objectType = "String";
			if(obj!=null){
				if(obj instanceof String){
					value.append(obj.toString());
				}else if(obj instanceof List){
					List<?> list = (List<?>) obj;
					for(Object o:list){
						value.append(JsonUtil.getInstance().object2JSON(o));
						objectType = "java.util.List";
					}
				}else if(obj instanceof Map){
					value.append(JsonUtil.getInstance().object2JSON(obj));
					objectType = "java.util.Map";
				}else{
					value.append("未知对象：" + obj.toString());
					objectType = "object";
				}
			}
			result.putAttr("objectType", objectType);
			result.putAttr("values", value);
		} catch (Exception e) {
			LogerUtil.error(UserInfoAction.class,e, "查询缓存错误");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 删除缓存
	 * @param request
	 * @return
	 */
	@RequestMapping("redisset/delete")
	public AppResult delete(){
		AppResult result = new AppResult();
		try {	
			String key = request.getParameter("key");
			boolean del = RedisUtils.getRedisService().del(key);
			result.setSuccess(del);
		} catch (Exception e) {
			LogerUtil.error(UserInfoAction.class,e, "删除缓存错误");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 设置缓存
	 * @param request
	 * @return
	 */
	@RequestMapping("redisset/update")
	public AppResult update(){
		AppResult result = new AppResult();
		try {	
			String key = request.getParameter("key");
			String value = request.getParameter("values");
			String objectType = request.getParameter("objectType");
			if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)
					|| StringUtils.isEmpty(objectType)) {
				result.setMessage("缺少必传参数");
				result.setSuccess(false);
				return result;
			}
			Object valueObj = null;
			switch (objectType) {
			case "java.util.Map":
				valueObj = JsonUtil.getInstance().json2Object(value, Map.class);
				break;
			case "java.util.List":
				value = "["+value+"]";
				valueObj = JsonUtil.getInstance().json2Object(value, List.class);
				break;
			default:
				valueObj = value;
				break;
			}
			
			int length = RedisUtils.getRedisService().set(key, valueObj);
			result.setSuccess(length > 0);
		} catch (Exception e) {
			LogerUtil.error(UserInfoAction.class,e, "设置缓存错误");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
