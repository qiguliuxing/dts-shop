package com.qiguliuxing.dts.core.type;

/**
 * 用户类型的枚举类
 * 
 * @author CHENBO
 * @QQ 623659388
 * @since 1.0.0
 */
public enum UserTypeEnum {

	COMM_USER((byte) 0, "普通用户"), VIP_USER((byte) 1, "VIP"), REGIONAL_AGENCY((byte) 2, "区域代理");

	private Byte level;
	private String desc;

	private UserTypeEnum(Byte level, String desc) {
		this.level = level;
		this.desc = desc;
	}

	public static UserTypeEnum getInstance(Byte level2) {
		if (level2 != null) {
			for (UserTypeEnum tmp : UserTypeEnum.values()) {
				if (tmp.level.intValue() == level2.intValue()) {
					return tmp;
				}
			}
		}
		return null;
	}

	public Byte getLevel() {
		return level;
	}

	public void setLevel(Byte level) {
		this.level = level;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
