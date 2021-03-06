package org.xxjr.summary.cust;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.SysException;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.sys.util.DBConst;

@Lazy
@Service
public class SumBusiCustService extends BaseService {
	private static final String NAMESPACE = "SUMBUSICUST";

	/**
	 * 查寻数据
	 * @param params
	 * @return
	 */
	public AppResult query(AppParam params) {
		return super.query(params, NAMESPACE);
	}

	
	/**
	 * 数据修改或添加处理
	 * @param params
	 * @return
	 */
	public AppResult saveOrUpdate(AppParam params){
		AppParam newParam = new AppParam();
		newParam.addAttrs(params.getAttr());
		newParam.setDataBase(DBConst.Key_sum_DB);
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
		params.addAttr("updateTime", new Date());
		return super.update(params, NAMESPACE);
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
		return result;
	}
	
}
