package org.xxjr.busi.util.kefu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.llw.common.web.util.FileUtil;
import org.springframework.util.StringUtils;
import org.xxjr.busi.util.kf.ExportUtil;
import org.xxjr.sys.util.NumberUtil;
import org.xxjr.sys.util.ServiceKey;

import com.alibaba.fastjson.JSONArray;

/**
 * 导出异步处理
 * @author LHS
 *
 */
public class KfExportUtil {
	
	private static final int EVERY_PAGE_SIZE = 100;
	
	@SuppressWarnings("unchecked")
	public static void  exportExcel(AppParam param){
		AppResult result = new AppResult();
		try{
			String recordId = StringUtil.getString(param.removeAttr("recordId"));
			String exportType = StringUtil.getString(param.getAttr("exportType"));
			String fileName = StringUtil.getString(param.getAttr("fileName"));
			int totalSize = NumberUtil.getInt(param.getAttr("totalSize"));
			param.setEveryPage(EVERY_PAGE_SIZE);
			Map<String, Object> exportCfg = ExportCfgUtil.queryExportCfg(exportType);
			String exportTitles = StringUtil.getString(exportCfg.get("titile"));
			String pageMethod = StringUtil.getString(exportCfg.get("pageMethod"));
			String afterMethod = StringUtil.getString(exportCfg.get("afterMethod"));
			String serviceKey = StringUtil.getString(exportCfg.get("serviceKey"));
			String serviceName = StringUtil.getString(exportCfg.get("serviceName"));
			String orderBy = StringUtil.getString(exportCfg.get("orderBy"));
			String orderValue = StringUtil.getString(exportCfg.get("orderValue"));
			//标题处理
			LinkedHashMap<String, String> exchangeTitle = new LinkedHashMap<String, String>();
			JSONArray titleJson = JSONArray.parseArray(exportTitles);
			for (int i = 0; i < titleJson.size(); i++) {
				Map<String,String> titleMap=(Map<String,String>)titleJson.get(i);
				if(titleMap.containsKey("title")&&titleMap.containsKey("name")){
					exchangeTitle.put(titleMap.get("title"),titleMap.get("name"));
				}
			}
			
			if (!StringUtils.isEmpty(pageMethod)&&
					!StringUtils.isEmpty(serviceKey) && 
					!StringUtils.isEmpty(serviceName)) {
				param.setMethod(pageMethod);
				param.setService(serviceName);
				param.setRmiServiceName(AppProperties
						.getProperties(DuoduoConstant.RMI_SERVICE_START + serviceKey));
				if (!StringUtils.isEmpty(orderBy) && !StringUtils.isEmpty(orderValue)) {
					param.setOrderBy(orderBy);
					param.setOrderValue(orderValue);
				}
			}else {
				//将计数方法重新修改成查询列表方法
				param.setMethod(StringUtil.getString(param.getAttr("pageMethod")));
			}
			//分批查询
			for (int j = 0; j < (totalSize % EVERY_PAGE_SIZE == 0 ? 
					totalSize / EVERY_PAGE_SIZE 
					: totalSize / EVERY_PAGE_SIZE + 1); j++) {
				param.setCurrentPage(j+1);
				result = RemoteInvoke.getInstance().callNoTx(param);
				if (!StringUtils.isEmpty(afterMethod)) {
					String[] split = afterMethod.split(",");
					for (String methodStr : split) {
						//执行后续操作的方法
						AfterMethodUtil.getInstance().invoke(methodStr, result, param);
					}
				}
				upfile(fileName,exchangeTitle,result.getRows());
			}
			updateFileStatus(recordId);
		}catch(Exception e){
			LogerUtil.error(KfExportUtil.class, e,"KfExportUtil exportExcel error");
		}
	}
	/**
	 * 更新导出文件的状态
	 * @param recordId
	 */
	private static void updateFileStatus(String recordId) {
		AppParam param = new AppParam("exportRecordService","update");
		//已生成文件
		param.addAttr("status", 2);
		param.addAttr("recordId", recordId);
		ServiceKey.doCall(param, ServiceKey.Key_sum);
	}
	/**
	 * 在本地生成文件
	 * @param fileName
	 * @param exchangeTitle
	 * @param list
	 */
   public static void upfile(String fileName,
		   						LinkedHashMap<String, String> exchangeTitle, 
		   	
		   						List<Map<String, Object>> list) {
	   OutputStream os = null;
	   try {
		   String todayStr = DateUtil.toStringByParttern(new Date(),DateUtil.DATE_PATTERN_YYYYMMDD);
		   String desCosPath = AppProperties
					.getProperties("kf.static.path")+"/upfile/export/"+todayStr+"/";
		   //创建文件夹
		   FileUtil.fileDirMake(desCosPath);
		   // 声明File对象
		   File file = new File(desCosPath,fileName);  
      	   if (!file.exists()) {
	  		   os = new FileOutputStream(file); 
	  		   ExportUtil.writeExcel(os, exchangeTitle, list);
		   }else {
			   //生成Excel或追加数据
			   ExportUtil.writeExcel(file, exchangeTitle, list);
		   }
	   } catch (Exception e) {
		   LogerUtil.error(KfExportUtil.class, e, "export error");
	   } finally{
		   if (os != null) {
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	   }
   }
   
   public static void main(String[] args) throws Exception {
//       String path = "D:/page/upfile2/export/20181119/";  
//       FileUtil.fileDirMake(path);
//       	File file = new File("D:/workspace/work/xxjr-static/src/main/webapp/upfile/export/20181121/004.txt");
//       	if (!file.exists()) {
//       		file.createNewFile();
//		}
//       OutputStream os = new FileOutputStream(file); 
//       os.write('c');
//       os.close();
//	   File file = new File("D:/page/upfile/export");
//	   FileUtils.deleteDirectory(file);
   }
}
