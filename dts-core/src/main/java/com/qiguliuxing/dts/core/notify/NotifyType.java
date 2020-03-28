package com.qiguliuxing.dts.core.notify;

public enum NotifyType {
	PAY_SUCCEED("paySucceed"), SHIP("ship"), APPLYREFUND("applyRefund"), // 申请退款
	REFUND("refund"), CAPTCHA("captcha");

	private String type;

	NotifyType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}
}
