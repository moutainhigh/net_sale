package org.xxjr.store.web.action.common;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.web.identify.ImageIdentify;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxjr.cust.util.info.CustomerUtil;
import org.xxjr.sys.util.ServiceKey;
import org.xxjr.sys.util.SysParamsUtil;
import org.xxjr.sys.util.ValidUtils;


/***
 * 初始化页面使用
 * @author qinxcb
 *
 */
@Controller
@RequestMapping("/sysAction")
public class SystemAction{
	/**
	 * 查询门店渠道类型开关
	 */
	@RequestMapping("/queryChannelSwitch")
	@ResponseBody
	public AppResult queryChannelSwitch(HttpServletRequest request){
		AppResult result =  new AppResult();
		try {
			//开启渠道类型标识
			int storeChannelFlag = SysParamsUtil.getIntParamByKey("openStoreChannelFlag", 0);
			result.putAttr("storeChannelFlag", storeChannelFlag);
		}catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "queryChannelSwitch error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}
	
	/**
	 * 修改门店渠道类型开关
	 */
	@RequestMapping("/updateChannelSwitch")
	@ResponseBody
	public AppResult updateChannelSwitch(HttpServletRequest request){
		AppResult result =  new AppResult();
		try {
			String storeChannelFlag = request.getParameter("storeChannelFlag");
			if (StringUtils.isEmpty(storeChannelFlag)) {
				return CustomerUtil.retErrorMsg("开启渠道类型标识不能为空");
			}
			AppParam param = new AppParam("sysParamsService", "update");
			param.addAttr("paramCode", "openStoreChannelFlag");
			param.addAttr("paramValue", storeChannelFlag);
			param.setRmiServiceName(AppProperties
					.getProperties(DuoduoConstant.RMI_SERVICE_START
							+ ServiceKey.Key_cust));
			result = RemoteInvoke.getInstance().call(param);
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "updateChannelSwitch error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
		return result;
	}

	/**
	 * 获取图形验证码
	 */
	@RequestMapping("/getImgCode/{imgCodeKey}")
	public void getImgCode(@PathVariable(required=true) String imgCodeKey,
			HttpServletRequest request,HttpServletResponse response){
		AppResult result =  new AppResult();
		try {
			if(imgCodeKey.length() >32) {
				return ;
			}
			ServletOutputStream responseOutputStream = response.getOutputStream();
			ImageIdentify identifyImg = ValidUtils.getImageCode(imgCodeKey, 4);
			ImageIO.write(identifyImg.getImage(), "JPEG", responseOutputStream);
			// 以下关闭输入流！
			responseOutputStream.flush();
			responseOutputStream.close();
		} catch (Exception e) {
			LogerUtil.error(this.getClass(), e, "getImgCode error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
		}
	}
	
	
}