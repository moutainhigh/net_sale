package org.xxjr.busiIn;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.SysException;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.store.util.StoreApplyUtils;

@Lazy
@Service
public class BusiCustService extends BaseService {
	private static final String NAMESPACE = "BUSICUST";

	/**
	 * 查寻数据
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
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
	 * 数据修改或添加处理
	 * @param params
	 * @return
	 */
	public AppResult saveOrUpdate(AppParam params){
		AppParam newParam = new AppParam();
		newParam.addAttrs(params.getAttr());
		if(StringUtils.isEmpty(newParam.getAttr("customerId"))){
			throw new SysException("用户信息缺少主键!");
		}
		try{
			//先调用修改功能
			AppResult result = this.update(params);
			//没有数据时，直接添加
			if((int)result.getAttr(DuoduoConstant.DAO_Update_SIZE) ==0){
				return this.insert(newParam);
			}
			StoreApplyUtils.refreshStoreOrg(params);
			return  result;
		}catch(Exception e){
			return this.insert(newParam);
		}
	}
	
	
	
	/**
	 * 添加数据处理
	 * @param params
	 * @return
	 */
	public AppResult insert(AppParam params) {
		params.addAttr("createTime", new Date());
		return super.insert(params, NAMESPACE);
	}
	
	/**
	 * 修改数据处理
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
		AppResult result = new AppResult();
		params.addAttr("updateTime", new Date());
		result = super.update(params, NAMESPACE);
		return result;
	}
	
	/**
	 * 查询客户信息
	 * @param params
	 * @return
	 */
	public AppResult queryCust(AppParam params) {
		return super.query(params, NAMESPACE,"queryCust");
	}
	/**
	 * 查询客户基本信息
	 * @param params
	 * @return
	 */
	public AppResult queryCustBaseInfo(AppParam params) {
		return super.query(params, NAMESPACE,"queryCustBaseInfo");
	}
	
	/**
	 * updateByCustId
	 * @param params
	 * @return
	 */
	public AppResult updateByCustId(AppParam params) {
		params.addAttr("updateTime", new Date());
		int size = getDao().update(NAMESPACE, "updateByCustId",params.getAttr(),params.getDataBase());
		AppResult result = new AppResult();
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		StoreApplyUtils.refreshStoreOrg(params);
		return result;
	}
	
	/**
	 * 查询门店分组列表
	 * @param params
	 * @return
	 */
	public AppResult queryOrgGroupList(AppParam params) {
		return super.query(params, NAMESPACE,"queryOrgGroupList");
	}
	
	/**
	 * 查询门店组下所有队列表
	 * @param params
	 * @return
	 */
	public AppResult queryOrgTeamList(AppParam params) {
		return super.query(params, NAMESPACE,"queryOrgTeamList");
	}
	
	/**
	 * 查询门店人员基本信息（CFS获取员工编号）
	 * @param params
	 * @return
	 */
	public AppResult queryStoreCustList(AppParam params) {
		return super.queryByPage(params, NAMESPACE,"queryStoreCustList","queryStoreCustCount");
	}
	
}
