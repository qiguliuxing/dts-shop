package com.qiguliuxing.dts.admin.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 公告，通知文章等类型定义
 * 
 * @author CHENBO
 * @since 1.0.0
 * @QQ 623659388
 * 
 */
public enum ArticleType {

	NOTICE("0", "通知"), ANNOUNCE("1", "公告");

	private final String type;
	private final String desc;

	ArticleType(String type, String desc) {
		this.type = type;
		this.desc = desc;
	}

	public static ArticleType getInstance(String type) {
		if (StringUtils.isNotBlank(type)) {
			for (ArticleType tmp : ArticleType.values()) {
				if (type.equals(tmp.type)) {
					return tmp;
				}
			}
		}
		return null;
	}

	public String type() {
		return type;
	}

	public String desc() {
		return desc;
	}
}
