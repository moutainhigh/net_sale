package org.xxjr.busiIn.store.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.DuoduoError;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


@Lazy
@Service
public class ThirdExportCfgService extends BaseService {
	private static final String NAMESPACE = "THIRDEXPORTCFG";

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
		List<String> titleList = getTitleList(params);
		String exportTitile = getJsonArray(titleList).toString();
		if(!StringUtils.isEmpty(exportTitile)){
			params.addAttr("exportTitile", exportTitile);
		}
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
	
	
	/**
	 * querys
	 * @param params
	 * @return
	 */
	public AppResult queryForeginExport(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryForeginExport", "queryForeginExportCount");
	}
	
	
	/**
	 * insert
	 * @param params
	 * @return
	 */
	public AppResult insertForeginExport(AppParam params) {
		List<String> titleList = getTitleList(params);
		String exportTitile = getJsonArray(titleList).toString();
		if(!StringUtils.isEmpty(exportTitile)){
			params.addAttr("exportTitile", exportTitile);
		}
		params.addAttr("createTime", new Date());
		params.addAttr("createBy", DuoduoSession.getUserName());
		return super.insert(params, NAMESPACE);
	}
	
	
	/**
	 * 添加
	 * @param params
	 * @return
	 */
	public AppResult addForeginExport(AppParam params) {
		//查询是否存在配置
		AppParam queryParam = new AppParam();
		queryParam.addAttr("customerId", params.getAttr("customerId"));
		AppResult result = this.query(queryParam);
		if(result.isSuccess() && result.getRows().size() >0){
			params.addAttr("recordId", result.getRow(0).get("recordId"));
			return super.update(params,NAMESPACE);
		}else{
			return this.insertForeginExport(params);
		}
	}
	
	
	/**
	 * 获取选中标题
	 * @param params
	 * @return
	 */
	public List<String> getTitleList(AppParam params){
		List<String> list = new ArrayList<String>();
		list.add("houseType");
		list.add("houseType");
		list.add("carType");
		list.add("insure");
		list.add("loanAmount");
		list.add("havePinan");
		list.add("fundType");
		list.add("socialType");
		list.add("incomeData");
		list.add("wagesType");
		return list;
	}
	
	
	/**
	 * 生成导出Title
	 * @param screenInfo
	 * @return
	 */
	public JSONArray getJsonArray(List<String> titleList){
		JSONArray jsonArray = new JSONArray();  
		JSONObject json1 = new JSONObject();
		json1.put("name", "applyName");  
		json1.put("title", "客户姓名");  
		JSONObject json2 = new JSONObject();
		json2.put("name", "telephone");  
		json2.put("title", "客户手机");
		JSONObject json3 = new JSONObject();
		json3.put("name", "cityName");  
		json3.put("title", "城市");
		JSONObject json4 = new JSONObject();
		json4.put("name", "createTime");  
		json4.put("title", "创建时间");
		jsonArray.add(0, json1);
		jsonArray.add(1, json2);
		jsonArray.add(2, json3);
		jsonArray.add(3, json4);
		int i = 4;
		if(titleList != null && titleList.size() > 0){
			for(String key:titleList){  
				JSONObject json = new JSONObject();  
				if("houseType".equals(key)){
					json.put("name", "houseType");  
					json.put("title", "房产情况");  
				}
				if("carType".equals(key)){
					json.put("name", "carType");  
					json.put("title", "车产情况");  
				}
				if("insure".equals(key)){
					json.put("name", "insure");  
					json.put("title", "保险情况");  
				}
				if("loanAmount".equals(key)){
					json.put("name", "loanAmount");  
					json.put("title", "申请金额(万元)");  
				}
				if("havePinan".equals(key)){
					json.put("name", "havePinan");  
					json.put("title", "微粒贷(元)");  
				}
				if("fundType".equals(key)){
					json.put("name", "fundType");  
					json.put("title", "公积金");  
				}
				if("socialType".equals(key)){
					json.put("name", "socialType");  
					json.put("title", "社保情况");  
				}
				if("incomeData".equals(key)){
					json.put("name", "incomeData");  
					json.put("title", "月收入");  
				}
				if("wagesType".equals(key)){
					json.put("name", "wagesType");  
					json.put("title", "工资发放形式");  
				}
				jsonArray.add(i,json);
				i++;
			}
		}
		System.out.println(jsonArray);
		return jsonArray;
	}
}
