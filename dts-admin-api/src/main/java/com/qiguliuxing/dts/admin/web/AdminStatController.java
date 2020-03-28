package com.qiguliuxing.dts.admin.web;

import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.qiguliuxing.dts.admin.annotation.RequiresPermissionsDesc;
import com.qiguliuxing.dts.admin.util.StatVo;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.service.StatService;

@SuppressWarnings("rawtypes")
@RestController
@RequestMapping("/admin/stat")
@Validated
public class AdminStatController {
	private static final Logger logger = LoggerFactory.getLogger(AdminStatController.class);

	@Autowired
	private StatService statService;

	@RequiresPermissions("admin:stat:user")
	@RequiresPermissionsDesc(menu = { "统计管理", "用户统计" }, button = "查询")
	@GetMapping("/user")
	public Object statUser() {
		logger.info("【请求开始】统计管理->用户统计->查询");

		List<Map> rows = statService.statUser();
		String[] columns = new String[] { "day", "users" };
		StatVo statVo = new StatVo();
		statVo.setColumns(columns);
		statVo.setRows(rows);

		logger.info("【请求结束】统计管理->用户统计->查询,响应结果:{}", JSONObject.toJSONString(statVo));
		return ResponseUtil.ok(statVo);
	}

	@RequiresPermissions("admin:stat:order")
	@RequiresPermissionsDesc(menu = { "统计管理", "订单统计" }, button = "查询")
	@GetMapping("/order")
	public Object statOrder() {
		logger.info("【请求开始】统计管理->订单统计->查询");

		List<Map> rows = statService.statOrder();
		String[] columns = new String[] { "day", "orders", "customers", "amount", "pcr" };
		StatVo statVo = new StatVo();
		statVo.setColumns(columns);
		statVo.setRows(rows);

		logger.info("【请求结束】统计管理->订单统计->查询,响应结果:{}", JSONObject.toJSONString(statVo));
		return ResponseUtil.ok(statVo);
	}

	@RequiresPermissions("admin:stat:goods")
	@RequiresPermissionsDesc(menu = { "统计管理", "商品统计" }, button = "查询")
	@GetMapping("/goods")
	public Object statGoods() {
		logger.info("【请求开始】统计管理->商品统计->查询");

		List<Map> rows = statService.statGoods();
		String[] columns = new String[] { "day", "orders", "products", "amount" };
		StatVo statVo = new StatVo();
		statVo.setColumns(columns);
		statVo.setRows(rows);

		logger.info("【请求结束】统计管理->商品统计->查询,响应结果:{}", JSONObject.toJSONString(statVo));
		return ResponseUtil.ok(statVo);
	}

}
