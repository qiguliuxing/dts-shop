package com.qiguliuxing.dts.wx.dao;

import java.io.Serializable;

public class WxLoginInfo implements Serializable {

	private static final long serialVersionUID = -7722430332896313642L;

	private String code;
	private UserInfo userInfo;
	private Integer shareUserId;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public Integer getShareUserId() {
		return shareUserId;
	}

	public void setShareUserId(Integer shareUserId) {
		this.shareUserId = shareUserId;
	}
}
