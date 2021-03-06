package org.xxjr.busiIn.store;

import java.util.Date;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.llw.model.cache.RedisUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.store.util.StoreApplyUtils;


@Lazy
@Service
public class InfoNotifyService extends BaseService {
	private static final String NAMESPACE = "INFONOTIFY";

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
		params.addAttr("updateTime", new Date());
		result = super.insert(params, NAMESPACE);
		//删除缓存
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_CUST_ALL_NOTIFY_INFO + params.getAttr("customerId"));
		RedisUtils.getRedisService().del(StoreApplyUtils.STORE_CUST_NOTIFY_INFO + params.getAttr("customerId"));
		return result;
	}

	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		params.addAttr("updateTime", new Date());
		AppResult result =  super.update(params, NAMESPACE);
		if((Integer)result.getAttr(DuoduoConstant.DAO_Update_SIZE) == 0){
			result = this.insert(params);
		}else{
			//删除缓存
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_CUST_ALL_NOTIFY_INFO + params.getAttr("customerId"));
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_CUST_NOTIFY_INFO + params.getAttr("customerId"));
		}
		return result;
	}
	/**
	 * updateNotifyByType
	 * @param params
	 * @return
	 */
	public AppResult updateNotifyByType(AppParam params) {
		AppResult result = new AppResult();
		int size = getDao().update(NAMESPACE, "updateNotifyByType",params.getAttr(),params.getDataBase());
		if(size == 0){
			result = this.insert(params);
		}else{
			//删除缓存
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_CUST_ALL_NOTIFY_INFO + params.getAttr("customerId"));
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_CUST_NOTIFY_INFO + params.getAttr("customerId"));
			result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		}
		return result;
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
				param.addAttr("notifyId", id);

				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("notifyId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}


	/**
	 * 获取待通知列表
	 * @param params
	 * @return
	 */
	public AppResult queryNotifyList(AppParam params) {
		AppResult result = new AppResult();
		//查询需要通知的列表
		result = this.queryByPage(params, NAMESPACE,"queryNotifyList","queryNotifyListCount");
		return result;
	}


	/**
	 * 获取全部待通知列表
	 * @param params
	 * @return
	 */
	public AppResult queryNotifyAllList(AppParam params) {
		AppResult result = new AppResult();
		result = this.query(params, NAMESPACE,"queryNotifyAllList");
		return result;
	}

	/**
	 * 处理通知
	 * @param params
	 * @return
	 */
	public AppResult dealNotify(AppParam params) {
		AppResult result = new AppResult();
		String notifyIds = StringUtil.getString(params.getAttr("notifyIds"));
		String currentCustId = StringUtil.getString(params.getAttr("currentCustId"));
		if (!StringUtils.isEmpty(notifyIds)) {
			for (String id : notifyIds.split(",")) {
				AppParam notifyParam = new AppParam();
				notifyParam.addAttr("notifyId", id);
				//先查询通知
				result = this.query(notifyParam, NAMESPACE);
				if(result.isSuccess() && result.getRows().size() >0){
					//删除通知原始表
					AppResult delResult = this.delete(notifyParam, NAMESPACE);
					if(delResult.isSuccess()){
						//插入通知完成表
						Map<String,Object> map =  result.getRow(0);
						AppParam updateParam = new AppParam("infoNotifyFishService","insert");
						updateParam.addAttr("notifyId", map.get("notifyId"));
						updateParam.addAttr("applyId", map.get("applyId"));
						updateParam.addAttr("customerId", map.get("customerId"));
						updateParam.addAttr("applyName", map.get("applyName"));
						updateParam.addAttr("type", map.get("type"));
						updateParam.addAttr("notifyTime", map.get("notifyTime"));
						result = SoaManager.getInstance().invoke(updateParam);
					}
				}
			}
			//删除缓存
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_CUST_ALL_NOTIFY_INFO + currentCustId);
			RedisUtils.getRedisService().del(StoreApplyUtils.STORE_CUST_NOTIFY_INFO + currentCustId);
		}else{
			result.setSuccess(false);
			result.setMessage("通知不存在");
		}
		return result;
	}
}
