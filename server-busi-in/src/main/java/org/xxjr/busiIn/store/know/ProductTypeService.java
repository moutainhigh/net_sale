package org.xxjr.busiIn.store.know;

import java.util.Date;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.springframework.context.annotation.Lazy;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Lazy
@Service
public class ProductTypeService extends BaseService {
	private static final String NAMESPACE = "PRODUCTTYPE";

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
		params.addAttr("createTime", new Date());
		params.addAttr("createBy", DuoduoSession.getUserName());
		return super.insert(params, NAMESPACE);
	}

	/**
	 * update
	 * @param params
	 * @return
	 */
	public AppResult update(AppParam params) {
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
				param.addAttr("typeId", id);

				result = super.delete(param, NAMESPACE);
			}
		} else if (!StringUtils.isEmpty(params.getAttr("typeId"))) {
			result = super.delete(params, NAMESPACE);
		} else {
			throw new AppException(DuoduoError.DELETE_NO_ID);
		}
		return result;
	}

	/***
	 * 查询产品类型列表
	 * @param params
	 * @return
	 */
	public AppResult queryTypeList(AppParam params){
		AppResult result = new AppResult();
		result = super.queryByPage(params, NAMESPACE, "queryTypeList", "queryTypeListCount");
		return result;
	}
	
	/***
	 * 查询最深子节点
	 * @param params
	 * @return
	 */
	public AppResult queryTypeMaxLevel(AppParam params){
		AppResult result = new AppResult();
		result = super.query(params, NAMESPACE, "queryTypeMaxLevel");
		return result;
	}
	
	/***
	 * 删除知识库类型
	 * @return
	 */
	public AppResult deleteProType(AppParam params){
		AppResult result = new AppResult();
		AppParam queryParam = new AppParam();
		queryParam.setService("productInfoService");
		queryParam.setMethod("query");
		queryParam.addAttr("productType", params.getAttr("typeId"));
		result = SoaManager.getInstance().callNoTx(queryParam);
		//查询该类型是否存在引用
		if(result.isSuccess() && result.getRows().size()>0){
			result.setSuccess(false);
			result.setMessage("该知识库目录下存在文件，不允许删除");
			return result;
		}
		
		//查询是否存在子目录
		queryParam.addAttr("parentId", params.getAttr("typeId"));
		result = this.query(queryParam);
		if(result.isSuccess() && result.getRows().size()>0){
			result.setSuccess(false);
			result.setMessage("该知识库目录下存在子目录，不允许删除");
			return result;
		}
		result = super.delete(params, NAMESPACE);
		return result;
	}
	
	/***
	 * 查询产品类型目录结构
	 * @param params
	 * @return
	 */
	public AppResult queryProTypeMenus(AppParam params){
		return super.query(params, NAMESPACE,"queryProTypeMenus");
	}
}
