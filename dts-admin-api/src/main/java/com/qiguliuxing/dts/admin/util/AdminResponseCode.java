package com.qiguliuxing.dts.admin.util;

/**
 * 返回码定义
 * 
 * @author CHENBO
 * @since 1.0.0
 * @QQ 623659388
 * 
 */
public enum AdminResponseCode {

	ADMIN_INVALID_NAME(600, "管理员名称不符合规定"), ADMIN_INVALID_PASSWORD(601, "管理员密码长度不能小于6"),
	ADMIN_NAME_EXIST(602, "管理员已经存在"),
	// ADMIN_ALTER_NOT_ALLOWED(603,""),
	// ADMIN_DELETE_NOT_ALLOWED(604,""),
	ADMIN_INVALID_ACCOUNT_OR_PASSWORD(605, "用户帐号或密码不正确"), ADMIN_LOCK_ACCOUNT(606, "用户帐号已锁定不可用"),
	ADMIN_INVALID_AUTH(607, "认证失败"), GOODS_UPDATE_NOT_ALLOWED(610, "商品已经在订单或购物车中，不能修改"),
	GOODS_NAME_EXIST(611, "商品名已经存在"), ORDER_CONFIRM_NOT_ALLOWED(620, "当前订单状态不能确认收货"),
	ORDER_REFUND_FAILED(621, "当前订单状态不能退款"), ORDER_REPLY_EXIST(622, "订单商品已回复！"),
	ADMIN_INVALID_OLD_PASSWORD(623, "原始密码不正确！"),
	// USER_INVALID_NAME(630,""),
	// USER_INVALID_PASSWORD(631,""),
	// USER_INVALID_MOBILE(632,""),
	// USER_NAME_EXIST(633,""),
	// USER_MOBILE_EXIST(634,""),
	ROLE_NAME_EXIST(640, "角色已经存在"), ROLE_SUPER_SUPERMISSION(641, "当前角色的超级权限不能变更"),
	ARTICLE_NAME_EXIST(642,"公告或通知文章已经存在");

	private final Integer code;
	private final String desc;

	AdminResponseCode(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static AdminResponseCode getInstance(Integer code) {
		if (code != null) {
			for (AdminResponseCode tmp : AdminResponseCode.values()) {
				if (tmp.code.intValue() == code.intValue()) {
					return tmp;
				}
			}
		}
		return null;
	}

	public Integer code() {
		return code;
	}

	public String desc() {
		return desc;
	}
}
