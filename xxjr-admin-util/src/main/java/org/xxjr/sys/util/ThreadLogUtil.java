package org.xxjr.sys.util;

import java.net.InetAddress;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ddq.common.context.AppParam;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadLogUtil {

	private static ThreadPoolTaskExecutor taskExecutor;

	public static ThreadPoolTaskExecutor getInstance(){
		if(taskExecutor == null){
			synchronized (ThreadLogUtil.class) {
				if(taskExecutor == null){
					taskExecutor = SpringAppContext.getApplicationContext().getBean(ThreadPoolTaskExecutor.class);
				}
			}
		}
		return taskExecutor;
	}
	
	

	/***
	 * 设置日志参数
	 * @param url
	 * @param params
	 * @param ip
	 * @param signId
	 * @param custInfo
	 * @param request
	 * @return
	 */
	public static AppParam setLogParam(String url,String params,String ip,
			String signId,Map<String, Object> custInfo,HttpServletRequest request){
		AppParam param = new AppParam();
		try{
			param.addAttr("requestUrl", url);
			if(custInfo!=null){
				param.addAttr("customerId", StringUtils.isEmpty(custInfo.get("customerId")) ? custInfo.get("userId"):
					custInfo.get("customerId"));
				param.addAttr("telephone", custInfo.get("telephone"));
				param.addAttr("realName", custInfo.get("realName"));
			}
			if(StringUtils.isEmpty(param.getAttr("telephone"))){
				param.addAttr("telephone", request.getParameter("telephone"));
			}
			param.addAttr("uuid", request.getParameter("UUID"));
			param.addAttr("appVersion", request.getParameter("appVersion"));
			param.addAttr("signId", signId);
			param.addAttr("userAgent", subStrLength(request.getHeader("user-agent")));
			param.addAttr("referer", subStrLength(request.getHeader("Referer")));
			param.addAttr("ipAddress", ip);
			param.addAttr("serverIp",InetAddress.getLocalHost().getHostAddress());
			param.addAttr("startTime", new Date());
			param.addAttr("params", params);
			DuoduoSession.setShowLog(param);
		}catch(Exception e){
			log.error(" set log error:", e);
		}
		return param;
	}
	
	/***
	 * 截取字符长度
	 * @param param
	 * @return
	 */
	public static String subStrLength(String param){
		if(!StringUtils.isEmpty(param) && param.length() >300){
			param = param.substring(0,300);
		}
		return param;
	}
	
	/***
	 * 通过线程池发送消息
	 * @param param
	 */
	public static void sendMessageNewThread(AppParam param){
		getInstance().execute(new Runnable() {
			@Override
			public void run() {
				try{
					RemoteInvoke.getInstance().call(param);
				}catch(Exception e){
					LogerUtil.error(ThreadLogUtil.class, e, "sendMessageNewThread error");
				}
			}
		});
	}
}
