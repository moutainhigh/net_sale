package org.xxjr.cust.info;

import java.util.Map;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.util.StringUtil;
import org.llw.common.core.service.BaseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.info.CustomerIdentify;
import org.xxjr.sys.util.SysParamsUtil;

@Lazy
@Service
public class CustomerExtService extends BaseService {
	private static final String NAMESPACE = "CUSTOMEREXT";

	/**
	 * 批量修改用户角色
	 * @param param
	 * @return
	 */
	public AppResult batchUpdateRole(AppParam param) {
		AppResult result = new AppResult();
		int size = super.getDao().update(NAMESPACE, "batchUpdateRole", param.getAttr(), param.getDataBase());
		result.putAttr(DuoduoConstant.DAO_Update_SIZE, size);
		return result;
	}

	/**
	 * 查询门店及管理用户列表
	 * @param 
	 * @return
	 */
	public AppResult queryStoreCustList(AppParam params) {
		return super.queryByPage(params, NAMESPACE, "queryStoreCustList", "queryStoreCustCount");
	}

	/**
	 * 查询门店及用户信息
	 * @param 
	 * @return
	 */
	public AppResult queryStoreCustInfo(AppParam params) {
		return super.query(params, NAMESPACE, "queryStoreCustList");
	}
	/**
	 * 查询queryCustomerId
	 * @param 
	 * @return
	 */
	public AppResult queryCustomerId(AppParam params) {
		return super.query(params, NAMESPACE, "queryCustomerId");
	}

	/***
	 * 变更微信信息
	 * @param params
	 * @return
	 */
	public AppResult updateWexinInfo(AppParam params){
		AppResult result = new AppResult();
		String telephone = StringUtil.getString(params.getAttr("telephone"));
		String reCustomerId = StringUtil.getString(params.getAttr("customerId"));
		//查询号码是否存在
		AppParam queryParams = new AppParam("customerService","query");
		queryParams.addAttr("telephone", telephone);
		AppResult custResult = SoaManager.getInstance().invoke(queryParams);
		if(custResult.isSuccess() && custResult.getRows().size() >0){
			String unionid = StringUtil.getString(custResult.getRow(0).get("unionid"));
			String openid = StringUtil.getString(custResult.getRow(0).get("openid"));
			String customerId = StringUtil.getString(custResult.getRow(0).get("customerId"));
			if(StringUtils.isEmpty(unionid) || StringUtils.isEmpty(openid)){
				result.setSuccess(false);
				result.setMessage("unionid或openid不存在");
				return result;
			}
			//获取用户的旧号码
			Map<String, Object> custInfo = CustomerIdentify.getCustIdentify(reCustomerId);
			String oldTel = "";//旧号码
			String oldUnionId = "";//旧unionId
			String oldOpenId = "";//旧openId
			if(custInfo !=null){
				 oldTel = StringUtil.getString(custInfo.get("telephone"));
				 oldUnionId = StringUtil.getString(custInfo.get("unionid"));
				 oldOpenId = StringUtil.getString(custInfo.get("openid"));
			}else{
				result.setSuccess(false);
				result.setMessage("处理人信息不存在");
				return result;
			}
			
			//修改新号码的unionid和openid等信息
			AppParam updateParams = new AppParam("customerService","update");
			updateParams.addAttr("customerId", customerId);
			updateParams.addAttr("unionid", customerId + unionid);
			updateParams.addAttr("openid", customerId + openid);
			updateParams.addAttr("telephone", customerId + telephone);
			result = SoaManager.getInstance().invoke(updateParams);

			//修改原号码的unionid和openid等信息
			if(result.isSuccess()){
				AppParam oldParams = new AppParam("customerService","update");
				oldParams.addAttr("customerId", reCustomerId);
				oldParams.addAttr("unionid", unionid);
				oldParams.addAttr("openid", openid);
				oldParams.addAttr("telephone", telephone);
				result = SoaManager.getInstance().invoke(oldParams);
				//再次将旧号码赋给新注册的customerId
				if(!StringUtils.isEmpty(oldOpenId) && !StringUtils.isEmpty(oldUnionId)){
					updateParams.addAttr("telephone", oldTel);
					updateParams.addAttr("openid", oldOpenId);
					updateParams.addAttr("unionid", oldUnionId);
					result = SoaManager.getInstance().invoke(updateParams);
				}else{
					//把原号码改成新号码
					AppParam updateNewParams = new AppParam("customerService","update");
					updateNewParams.addAttr("customerId", reCustomerId);
					updateNewParams.addAttr("telephone", telephone);
					SoaManager.getInstance().invoke(updateNewParams);
					
					//把新号码的手机号改成原号码
					AppParam updateOldParams = new AppParam("customerService","update");
					updateOldParams.addAttr("customerId", customerId);
					updateOldParams.addAttr("telephone", oldTel);
					SoaManager.getInstance().invoke(updateOldParams);
				}
			}
		}else{
			//修改旧号码的unionid和openid等信息
			AppParam updateParams = new AppParam("customerService","updateTelephone");
			updateParams.addAttr("customerId", reCustomerId);
			updateParams.addAttr("telephone", telephone);
			result = SoaManager.getInstance().invoke(updateParams);
		}
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
	 * 查询信贷经理信息
	 * @param params
	 * @return
	 */
	public AppResult queryManagerInfo(AppParam params){
		return super.query(params, NAMESPACE, "queryManagerInfo");
	}
	
	/**
	 * 我的奖励(赠送免单券)
	 * @param params
	 * @return
	 */
	public AppResult myReward(AppParam params){
		AppResult result = new AppResult();
		AppResult queryResult = super.query(params, NAMESPACE, "myRewardCount");
		result.putAttr("myRewardCount", queryResult.getRow(0));
		int isSelfRobReward = 0;
		int robNums = 0;
		AppParam cfgParams = new AppParam();
		cfgParams.setService("custRobConfigService");
		cfgParams.setMethod("queryConfig");
		cfgParams.addAttr("customerId", params.getAttr("customerId"));
		AppResult cfgResult = SoaManager.getInstance().invoke(cfgParams);
		if (cfgResult.getRows().size() > 0) {
			Map<String, Object> map = cfgResult.getRow(0);
			robNums = (Integer)map.get("robNums");
			isSelfRobReward = (Integer)map.get("isSelfRobReward");
		}
		result.putAttr("pageType", isSelfRobReward > 0?1:2);//跳转页面类型
		result.putAttr("robNums", robNums);					//好友每抢几单就送券
		int presentCount = SysParamsUtil.getIntParamByKey("presentFreeTicketCount", 1);
		result.putAttr("presentCount", presentCount);		//送券数量
		result.putAttr("isSelfRobReward", isSelfRobReward);	//自己抢单送券  1-送
	//	result.putAttr("activityDate", CustInviteUtil.getTitleByFreeTicketActivity());
		return result;
	}
	
	/**
	 * 我的奖励列表(赠送免单券)
	 * @param params
	 * @return
	 */
	public AppResult myRewardList(AppParam params){
		return super.query(params, NAMESPACE, "myRewardList");
	}
	
	/**
	 * 邀请奖励明细
	 * @param params
	 * @return
	 */
	public AppResult rewardListByInviteFriend(AppParam params){
		return super.query(params, NAMESPACE, "rewardDetailByInviteFriend");
	}
	
	/**
	 * 我的奖励
	 * @param parmas
	 * @return
	 */
	public AppResult myRewardByInviteFriend(AppParam params){
		return super.query(params, NAMESPACE, "myRewardByInviteFriend");
	}
	
	/**
	 * 邀请有礼按天统计
	 * @param parmas
	 * @return
	 */
	public AppResult inviteActivityOfDay(AppParam params){
		AppResult result = new AppResult();
		result = super.queryByPage(params, NAMESPACE, "inviteActivityOfDay","inviteActivityOfDayCount");
		
		//查询信贷经理额外获得券数量
		AppParam param =new AppParam();
		param.setService("custTicketService");
		param.setMethod("queryCount");
		param.addAttr("activityType", 2);
		param.addAttr("type", 14);
		param.addAttr("startTime", params.getAttr("startTime"));
		param.addAttr("endDate", params.getAttr("endDate"));
		AppResult ticketResult = SoaManager.getInstance().invoke(param);
		result.putAttr("ticketCount", ticketResult.getAttr(DuoduoConstant.TOTAL_SIZE));
		return result;
	}
}
