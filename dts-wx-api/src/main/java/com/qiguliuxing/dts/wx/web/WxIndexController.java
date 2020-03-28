package com.qiguliuxing.dts.wx.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qiguliuxing.dts.core.util.ResponseUtil;

/**
 * 测试服务
 */
@RestController
@RequestMapping("/wx/index")
public class WxIndexController {

	/**
	 * 测试数据
	 *
	 * @return 测试数据
	 */
	@RequestMapping("/index")
	public Object index() {
		return ResponseUtil.ok("hello dts");
	}

}