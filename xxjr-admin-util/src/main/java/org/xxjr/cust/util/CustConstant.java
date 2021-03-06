package org.xxjr.cust.util;

/***
 * 资金相关的常量
 * @author Administrator
 *
 */
public class CustConstant {
	/**用户来源 微信 openid*/
	public final static String USER_OPENID = "user_openid";
	/**用户来源 微信  公众号*/
	public final static String USER_GZHID = "user_gzhid";
	/** 缓存前缀*/
	public static final String ADMIN_PRE = "admin";
	/** 防重复登录的前缀*/
	public static final String ADIMIN_USER_SESSION = "admin_user_session";
	/** 缓存时间*/
	public static final int ADMIN_SIGN_CACHE_TIME = 30*60;
	/**微信  显示哪个公众号商品 */
	public final static String USER_SHOWID = "user_showid";
	/**用户来源 微信  公众号unionid */
	public final static String USER_UNIONID = "user_unionid";
	/**商城合作用户编号*/
	public final static String PARTNER_CODE ="partner_code";
	/** 登录后操作 */
	public final static String USER_LOGIN_OPER = "user_login_oper";
	
	/**用户来源 微信*/
	public static final String CUST_sourceType_WX="wx";
	/**用户来源 微信分销 */
	public static final String CUST_sourceType_WXFX = "wxfx";
	/**用户来源 微信*/
	public static final String CUST_sourceType_WXReferer="WXReferer";
	/**用户来源 WEB页面*/
	public static final String CUST_sourceType_WEB="web";
	/**用户来源房地产*/
	public static final String CUST_sourceType_FANG="fang";
	/**后台管理人员来源 admin页面*/
	public static final String CUST_sourceType_ADMIN="admin";
	/**用户来源 app*/
	public static final String CUST_sourceType_APP="app";
	
	/**用户来源推广页面*/
	public static final String CUST_sourceType_TG="tg";
	
	/**用户来源默认页面*/
	public static final String CUST_SOURCETYPE_DEFAULT="default";
	
	/**用户来源  百科  */
	public static final String CUST_sourceType_Baike="baike";
	/**用户来源 神马 */
	public static final String CUST_sourceType_Shenma="shenma";
	
	/**用户来源 小小快贷*/
	public static final String CUST_sourceType_XXKD="xxkd";
	/**用户来源  信贷经理*/
	public static final String CUST_sourceType_XDJL="xxxdjl";
	/**用户来源  信贷经理*/
	public static final String CUST_sourceType_XDYZ="xxxdyz";

	/**用户来源  信贷员之家*/
	public static final String CUST_sourceType_Xdyzj="xxxdyzj";
	/**用户来源  小小金融*/
	public static final String CUST_sourceType_XXJR="xxjr";
	/**用户来源  小小贷款*/
	public static final String CUST_sourceType_XXDK="xxdk";
	/**用户来源  小小认证*/
	public static final String CUST_sourceType_XXRZ="xxrz";
	
	/**实名状态0 待审核*/
	public static final String identify_status_0="0";
	/**实名状态1 审核通过*/
	public static final String identify_status_1="1";
	/**实名状态2 不正确*/
	public static final String identify_status_2="2";
	
	/**认证状态0人工审核*/
	public static final String auth_status_0="0";
	/**认证状态1自动审核*/
	public static final String auth_status_1="1";
	
	/**积分增加(1 )*/
	public static final String SCORE_ADD="1";
	/**积分减少(0 )*/
	public static final String SCORE_SUB="0";
	
	/**积分类型(1签到 )*/
	public static final String SCORE_TYPE_1="1";
	/**积分类型(2 认证 )*/
	public static final String SCORE_TYPE_2="2";
	/**积分类型(3 交单  )*/
	public static final String SCORE_TYPE_3="3";
	/**积分类型(4 分享名片  )*/
	public static final String SCORE_TYPE_4="4";
	/**积分类型(5  分享资讯 )*/
	public static final String SCORE_TYPE_5="5";
	
	/**积分类型(6推荐人实名认证  )*/
	public static final String SCORE_TYPE_6="6";
	/**积分类型(16二级推荐人认证  )*/
	public static final String SCORE_TYPE_16="16";
	
	/**积分类型(7推荐人交单 )*/
	public static final String SCORE_TYPE_7="7";
	/**积分类型( 8维护客户资料 )*/
	public static final String SCORE_TYPE_8="8";
	
	/**积分类型(9完善资料 )*/
	public static final String SCORE_TYPE_9="9";
	/**积分类型(10 抽奖 )*/
	public static final String SCORE_TYPE_10="10";
	/**积分类型(11 置顶 )*/
	public static final String SCORE_TYPE_11="11";
	/**积分类型(12 借款加急 )*/
	public static final String SCORE_TYPE_12="12";
	/**积分类型(13 抢单处理 )*/
	public static final String SCORE_TYPE_13="13";
	/**积分类型(13 添加成功案例 )*/
	public static final String SCORE_TYPE_14="14";
	
	/**积分类型(15 信贷员刷新置顶 )*/
	public static final String SCORE_TYPE_15="15";
	
	/**工作认证*/
	public static final String SCORE_TYPE_17="17";
	/**工作认证(16二级推荐人认证  )*/
	public static final String SCORE_TYPE_18="18";
	/**积分类型(积分兑换商品 )*/
	public static final String SCORE_TYPE_19="19";
	/**推荐注册积分奖励*/
	public static final String SCORE_TYPE_20="20";
	/** 退款申请积分 */
	public static final String SCORE_TYPE_21="21";
	/**积分类型(评论商品获取 )*/
	public static final String SCORE_TYPE_22="22";
	/**积分类型(抢单赠送积分 )*/
	public static final String SCORE_TYPE_23="23";
	/**积分类型(撤销工作认证扣除积分 )*/
	public static final String SCORE_TYPE_24="24";
	
	
	/** IM聊天信息发送者类型 信贷经理 */
	public final static String SENDER_USER_TYPE_1 = "1";
	/** IM聊天信息发送者类型 借款人*/
	public final static String SENDER_USER_TYPE_2 = "2";
	
	/** 客户vip等级 1 */
	public final static String CUST_VIP_GRADE_1 = "1";
	
	/** 普通用户 **/
	public final static String CUST_ROLETYPE_0 = "0";
	/** 管理员用户 **/
	public final static String CUST_ROLETYPE_1 = "1";
	/** 客服用户 **/
	public final static String CUST_ROLETYPE_2 = "2";
	/** 门店业务员 **/
	public final static String CUST_ROLETYPE_3 = "3";
	/** 可抢车贷单用户 **/
	public final static String CUST_ROLETYPE_4 = "4";
	/** 公司按揭员用户 **/
	public final static String CUST_ROLETYPE_5 = "5";
	/** 门店负责人 **/
	public final static String CUST_ROLETYPE_6 = "6";
	/** 门店管理员 **/
	public final static String CUST_ROLETYPE_7 = "7";
	/** 门店主管 **/
	public final static String CUST_ROLETYPE_8 = "8";
	/** 门店副主管 **/
	public final static String CUST_ROLETYPE_9 = "9";

	/** 外部渠道用户 **/
	public final static String CUST_ROLETYPE_10 = "10";
	
	/** 跟单处理人来源  后台客服  **/
	public final static String KF_HANDLER_FROM_1 = "1";
	/** 跟单处理人来源 前台门店 **/
	public final static String KF_HANDLER_FROM_2 = "2";
	
	/**马甲包  信贷员之家*/
	public static final String CUST_mjb_Xdyzj="2";
	
	/**用户来源  小小攒钱*/
	public static final String CUST_SOURCETYPE_XXZQ ="xxzq";
	
}
