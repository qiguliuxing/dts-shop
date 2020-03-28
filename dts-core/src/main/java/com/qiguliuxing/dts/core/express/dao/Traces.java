/**
 * Copyright 2018 bejson.com
 */
package com.qiguliuxing.dts.core.express.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Auto-generated: 2018-07-19 22:27:22
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Traces {

	@JsonProperty("AcceptStation")
	private String acceptStation;
	@JsonProperty("AcceptTime")
	private String acceptTime;

	public String getAcceptStation() {
		return acceptStation;
	}

	public void setAcceptStation(String acceptStation) {
		this.acceptStation = acceptStation;
	}

	public String getAcceptTime() {
		return acceptTime;
	}

	public void setAcceptTime(String acceptTime) {
		this.acceptTime = acceptTime;
	}

}