package org.xxjr.store.web.action.account.know;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.llw.common.web.util.FileUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.xxjr.busi.util.store.StoreMenuUtils;
import org.xxjr.busi.util.store.StoreUserUtil;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.FileGroupUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.tools.util.QcloudUploader;
@Controller()
@RequestMapping("/account/know")
public class KnowledgeAction {

	/***
	 * 获取知识库产品管理列表
	 * @param request
	 * @return
	 */
	@RequestMapping("/knowManager/queryProductList")
	@ResponseBody
	public AppResult queryProductList(HttpServletRequest request){
		AppResult result = new AppResult();
		AppParam queryParam = new AppParam();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("处理人不能为空");
			return result;
		}
		try{
			RequestUtil.setAttr(queryParam, request);
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			if(custInfo != null && custInfo.size() > 0){
				String orgId = StringUtil.getString(custInfo.get("orgId"));//门店
				String authType = StringUtil.getString(custInfo.get("roleType")); //角色
				if(CustConstant.CUST_ROLETYPE_6.equals(authType)
						|| CustConstant.CUST_ROLETYPE_7.equals(authType)){
					queryParam.addAttr("orgId", orgId);//管理所属门店
				} else if (!CustConstant.CUST_ROLETYPE_1.equals(authType)){
					queryParam.addAttr("lastUpload", customerId);// 管理自己的文件
				}
			}
			queryParam.setService("productInfoService");
			queryParam.setMethod("queryProductList");
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
		}catch(Exception e){
			LogerUtil.error(KnowledgeAction.class,e, "queryProductList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;

	}	

	/***
	 * 添加知识库产品
	 * @param request
	 * @return
	 */
	@RequestMapping("/knowManager/addProductInfo")
	@ResponseBody
	public AppResult addProductInfo(HttpServletRequest request){
		AppResult result = new AppResult();
		AppParam updateParam = new AppParam();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("处理人不能为空");
			return result;
		}
		String productName = request.getParameter("productName");
		String productType = request.getParameter("productType");
		String fileUrl = request.getParameter("fileUrl");
		if(StringUtils.isEmpty(productName)){
			result.setSuccess(false);
			result.setMessage("产品名称不能为空");
			return result;
		}
		if(StringUtils.isEmpty(productType)){
			result.setSuccess(false);
			result.setMessage("产品类型不能为空");
			return result;
		}

		//获取处理人的orgId
		String orgId = null;
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
		if(custInfo != null){
			orgId = StringUtil.getString(custInfo.get("orgId"));//门店
			updateParam.addAttr("lastUpload", customerId);
			updateParam.addAttr("orgId", orgId);
		}
		if(StringUtils.isEmpty(orgId)){
			result.setSuccess(false);
			result.setMessage("处理人门店不能为空");
			return result;
		}
		if(StringUtils.isEmpty(fileUrl)){
			result.setSuccess(false);
			result.setMessage("文件不能为空");
			return result;
		}
		try{
			RequestUtil.setAttr(updateParam, request);
			updateParam.setService("productInfoService");
			updateParam.setMethod("insert");
			updateParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(updateParam);
		}catch(Exception e){
			LogerUtil.error(KnowledgeAction.class,e, "addProductInfo error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}


	/***
	 * 修改知识库产品
	 * @param request
	 * @return
	 */
	@RequestMapping("/knowManager/updateProductInfo")
	@ResponseBody
	public AppResult updateProductInfo(HttpServletRequest request){
		AppResult result = new AppResult();
		AppParam updateParam = new AppParam();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("处理人不能为空");
			return result;
		}
		String productName = request.getParameter("productName");
		String productType = request.getParameter("productType");
		String fileUrl = request.getParameter("fileUrl");
		String productId = request.getParameter("productId");
		if(StringUtils.isEmpty(productId)){
			result.setSuccess(false);
			result.setMessage("产品编号不能为空");
			return result;
		}
		if(StringUtils.isEmpty(productName)){
			result.setSuccess(false);
			result.setMessage("产品名称不能为空");
			return result;
		}
		if(StringUtils.isEmpty(productType)){
			result.setSuccess(false);
			result.setMessage("产品类型不能为空");
			return result;
		}
		if(StringUtils.isEmpty(fileUrl)){
			result.setSuccess(false);
			result.setMessage("文件不能为空");
			return result;
		}
		//获取处理人的orgId
		String orgId = null;
		Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
		if(custInfo != null){
			orgId = StringUtil.getString(custInfo.get("orgId"));//门店
			String authType = StringUtil.getString(custInfo.get("roleType"));
			if(!CustConstant.CUST_ROLETYPE_1.equals(authType)){
				updateParam.addAttr("lastUpload", customerId);
			}
			updateParam.addAttr("orgId", orgId);
		}
		if(StringUtils.isEmpty(orgId)){
			result.setSuccess(false);
			result.setMessage("处理人门店不能为空");
			return result;
		}
		try{
			RequestUtil.setAttr(updateParam, request);
			updateParam.setService("productInfoService");
			updateParam.setMethod("update");
			updateParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(updateParam);
		}catch(Exception e){
			LogerUtil.error(KnowledgeAction.class,e, "updateProductInfo error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/***
	 * 删除知识库产品
	 * @param request
	 * @return
	 */
	@RequestMapping("/knowManager/delProductInfo")
	@ResponseBody
	public AppResult delProductInfo(HttpServletRequest request){
		AppResult result = new AppResult();
		AppParam delParam = new AppParam();
		String productId = request.getParameter("productId");
		if(StringUtils.isEmpty(productId)){
			result.setSuccess(false);
			result.setMessage("产品Id不能为空");
			return result;
		}
		try{
			RequestUtil.setAttr(delParam, request);
			delParam.setService("productInfoService");
			delParam.setMethod("delete");
			delParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(delParam);
		}catch(Exception e){
			LogerUtil.error(KnowledgeAction.class,e, "delProductInfo error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}


	/***
	 * 获取知识库产品类型列表
	 * @param request
	 * @return
	 */
	@RequestMapping("/knowTypeList/queryProTypeList")
	@ResponseBody
	public AppResult queryProTypeList(HttpServletRequest request){
		AppResult result = new AppResult();
		AppParam queryParam = new AppParam();
		try{
			RequestUtil.setAttr(queryParam, request);
			queryParam.setService("productTypeService");
			queryParam.setMethod("queryTypeList");
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
		}catch(Exception e){
			LogerUtil.error(KnowledgeAction.class,e, "queryProTypeList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/***
	 * 添加知识库产品类型
	 * @param request
	 * @return
	 */
	@RequestMapping("/knowTypeList/addProType")
	@ResponseBody
	public AppResult addProType(HttpServletRequest request){
		AppResult result = new AppResult();
		AppParam updateParam = new AppParam();
		String typeName = request.getParameter("typeName");
		if(StringUtils.isEmpty(typeName)){
			result.setSuccess(false);
			result.setMessage("类型名称不能为空");
			return result;
		}
		try{
			RequestUtil.setAttr(updateParam, request);
			updateParam.addAttr("typeName", typeName);
			updateParam.setService("productTypeService");
			updateParam.setMethod("insert");
			updateParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(updateParam);
			if(result.isSuccess()){
				StoreMenuUtils.refreshProductMenusTree();
			}
		}catch(Exception e){
			LogerUtil.error(KnowledgeAction.class,e, "addProType error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}


	/***
	 * 修改知识库产品类型
	 * @param request
	 * @return
	 */
	@RequestMapping("/knowTypeList/updateProType")
	@ResponseBody
	public AppResult updateProType(HttpServletRequest request){
		AppResult result = new AppResult();
		AppParam updateParam = new AppParam();
		String typeId = request.getParameter("typeId");
		if(StringUtils.isEmpty(typeId)){
			result.setSuccess(false);
			result.setMessage("类型Id不能为空");
			return result;
		}
		try{
			RequestUtil.setAttr(updateParam, request);
			updateParam.addAttr("typeId", typeId);
			updateParam.setService("productTypeService");
			updateParam.setMethod("update");
			updateParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(updateParam);
			if(result.isSuccess()){
				StoreMenuUtils.refreshProductMenusTree();
			}
		}catch(Exception e){
			LogerUtil.error(KnowledgeAction.class,e, "updateProType error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/***
	 * 删除知识库产品类型
	 * @param request
	 * @return
	 */
	@RequestMapping("/knowTypeList/delProType")
	@ResponseBody
	public AppResult delProType(HttpServletRequest request){
		AppResult result = new AppResult();
		AppParam delParam = new AppParam();
		String typeId = request.getParameter("typeId");
		if(StringUtils.isEmpty(typeId)){
			result.setSuccess(false);
			result.setMessage("类型Id不能为空");
			return result;
		}
		try{
			delParam.addAttr("typeId", typeId);
			delParam.setService("productTypeService");
			delParam.setMethod("deleteProType");
			delParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().call(delParam);
			if(result.isSuccess()){
				StoreMenuUtils.refreshProductMenusTree();
			}
		}catch(Exception e){
			LogerUtil.error(KnowledgeAction.class,e, "delProType error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/***
	 * 上传知识库文件
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/knowManager/uploadKonwFile")
	@ResponseBody
	public AppResult uploadKonwFile(MultipartHttpServletRequest request) {
		AppResult result = new AppResult();
		File tempFile = null;
		try {
			String customerId = StoreUserUtil.getCustomerId(request);
			if (StringUtils.isEmpty(customerId)) {
				result.setSuccess(false);
				result.setMessage("用户ID不能为空");
				return result;
			}
			String fileType = request.getParameter("fileType");
			if (StringUtils.isEmpty(fileType)) {
				result.setSuccess(false);
				result.setMessage("文件类型不能为空");
				return result;
			}
			String fileTypes = FileGroupUtil.getFileTypes(fileType);
			MultipartFile file = FileUtil.getUploadFiles(request, fileTypes);
			InputStream inputStream = file.getInputStream();
	    	String contentType = file.getContentType();
	    	long fileSize = file.getSize();
	    	String desCosPath = "/upfile/" + fileType + "/" 
	    			+ DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD) + "/";
	    	FileUtil.fileDirMake(desCosPath);
	    	String originaFileName = file.getOriginalFilename();
	    	if(originaFileName.indexOf(".xls") !=-1){
				Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
				if(custInfo != null){
					String authType = StringUtil.getString(custInfo.get("roleType"));
					if(!CustConstant.CUST_ROLETYPE_1.equals(authType) 
							&& !CustConstant.CUST_ROLETYPE_6.equals(authType)
							&& !CustConstant.CUST_ROLETYPE_7.equals(authType)){
						return CustomerUtil.retErrorMsg("抱歉，非门店经理不允许上传xls或xlsx文件");
					}
				}
			}
			String uploadFileType = ".pdf";
			if(originaFileName.lastIndexOf(".") > 0){
				uploadFileType = originaFileName.substring(originaFileName.lastIndexOf("."));
			}
	    	String saveName = StringUtil.getUUID() + uploadFileType;
	    	QcloudUploader.createDirOnNotExists(desCosPath);
			boolean isSuccess = QcloudUploader.uploadFile(desCosPath + saveName, inputStream, fileSize, contentType);
			if (isSuccess) {
				String filePath = FileGroupUtil.Static_File_Host + desCosPath + saveName;
				result.putAttr("fileUrl", filePath);
			} else {
				result.setSuccess(false);
				result.setMessage("文件上传不成功，请重新上传");
			}

		} catch (Throwable e) {
			LogerUtil.error(KnowledgeAction.class, e, "uploadKonwFile error");
			ExceptionUtil.setExceptionMessage(e, result,
					DuoduoSession.getShowLog());
		} finally {
			if (tempFile != null){
				FileUtil.deleteQuietly(tempFile);
			}
		}
		return result;
	}


	/***
	 * 获取知识库产品类型最深的子节点，用于默认展示
	 * @param request
	 * @return
	 */
	@RequestMapping("/knowList/queryTypeMaxLevel")
	@ResponseBody
	public AppResult queryTypeMaxLevel(HttpServletRequest request){
		AppResult result = new AppResult();
		AppParam queryParam = new AppParam();
		try{
			queryParam.setService("productTypeService");
			queryParam.setMethod("queryTypeMaxLevel");
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			if(result.isSuccess() && result.getRows().size() >0){
				result.putAttr("typeId", result.getRow(0).get("typeId"));
				result.putAttr("typeName", result.getRow(0).get("typeName"));
			}
		}catch(Exception e){
			LogerUtil.error(KnowledgeAction.class,e, "queryTypeMaxLevel error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/***
	 * 查询知识库列表
	 * @param request
	 * @return
	 */
	@RequestMapping("/knowList/queryKnowList")
	@ResponseBody
	public AppResult queryKnowList(HttpServletRequest request){
		AppResult result = new AppResult();
		AppParam queryParam = new AppParam();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("处理人不能为空");
			return result;
		}
		try{
			RequestUtil.setAttr(queryParam, request);
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
			if(custInfo != null && custInfo.size() > 0){
				String orgId = StringUtil.getString(custInfo.get("orgId"));//门店
				String authType = StringUtil.getString(custInfo.get("roleType")); //角色
				if(!CustConstant.CUST_ROLETYPE_1.equals(authType)){
					queryParam.addAttr("orgId", orgId);
				}
			}
			queryParam.setService("productInfoService");
			queryParam.setMethod("queryProductList");
			queryParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
		}catch(Exception e){
			LogerUtil.error(KnowledgeAction.class,e, "queryKnowList error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/***
	 * 查询知识库详情
	 * @param request
	 * @return
	 */
	@RequestMapping("/knowList/queryKnowDetail")
	@ResponseBody
	public AppResult queryKnowDetail(HttpServletRequest request){
		AppResult result = new AppResult();
		AppParam queryParam = new AppParam();
		String customerId = StoreUserUtil.getCustomerId(request);
		if(StringUtils.isEmpty(customerId)){
			result.setSuccess(false);
			result.setMessage("处理人不能为空");
			return result;
		}
		String productId = request.getParameter("productId");
		if(StringUtils.isEmpty(productId)){
			result.setSuccess(false);
			result.setMessage("产品编号不能为空");
			return result;
		}
		try{
			RequestUtil.setAttr(queryParam, request);
			queryParam.setService("productInfoService");
			queryParam.setMethod("query");
			queryParam.setRmiServiceName(AppProperties.getProperties(
					DuoduoConstant.RMI_SERVICE_START + ServiceKey.Key_busi_in));
			result = RemoteInvoke.getInstance().callNoTx(queryParam);
			if(result.isSuccess() && result.getRows().size() > 0){
				Map<String,Object> resultMap = result.getRow(0);
				String curOrgId = StringUtil.getString(resultMap.get("orgId"));
				Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(customerId);
				if(custInfo != null && custInfo.size() > 0){
					String orgId = StringUtil.getString(custInfo.get("orgId"));//门店
					String authType = StringUtil.getString(custInfo.get("roleType")); //角色
					//管理员可查询所有产品详情，其他角色只能查看本门店的产品
					if(!CustConstant.CUST_ROLETYPE_1.equals(authType) && !curOrgId.equals(orgId)){
						result.setSuccess(false);
						result.setMessage("您没有权限查看详情");
						return result;
					}
				}
			}
		}catch(Exception e){
			LogerUtil.error(KnowledgeAction.class,e, "queryKnowDetail error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
}
