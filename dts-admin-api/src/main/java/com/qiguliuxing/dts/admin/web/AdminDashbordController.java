package com.qiguliuxing.dts.admin.web;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.service.DtsGoodsProductService;
import com.qiguliuxing.dts.db.service.DtsGoodsService;
import com.qiguliuxing.dts.db.service.DtsOrderService;
import com.qiguliuxing.dts.db.service.DtsUserService;

@RestController
@RequestMapping("/admin/dashboard")
@Validated
public class AdminDashbordController {
	private static final Logger logger = LoggerFactory.getLogger(AdminDashbordController.class);

	@Autowired
	private DtsUserService userService;
	@Autowired
	private DtsGoodsService goodsService;
	@Autowired
	private DtsGoodsProductService productService;
	@Autowired
	private DtsOrderService orderService;

	@GetMapping("")
	public Object info() {
		logger.info("【请求开始】系统管理->首页仪表盘查询");

		int userTotal = userService.count();
		int goodsTotal = goodsService.count();
		int productTotal = productService.count();
		int orderTotal = orderService.count();
		Map<String, Integer> data = new HashMap<>();
		data.put("userTotal", userTotal);
		data.put("goodsTotal", goodsTotal);
		data.put("productTotal", productTotal);
		data.put("orderTotal", orderTotal);

		logger.info("【请求结束】系统管理->首页仪表盘查询:响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

}
