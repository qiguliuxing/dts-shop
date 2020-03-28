package com.qiguliuxing.dts.admin.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.qiguliuxing.dts.admin.annotation.RequiresPermissionsDesc;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.validator.Order;
import com.qiguliuxing.dts.core.validator.Sort;
import com.qiguliuxing.dts.db.domain.DtsFootprint;
import com.qiguliuxing.dts.db.service.DtsFootprintService;

@RestController
@RequestMapping("/admin/footprint")
@Validated
public class AdminFootprintController {
	private static final Logger logger = LoggerFactory.getLogger(AdminFootprintController.class);

	@Autowired
	private DtsFootprintService footprintService;

	@RequiresPermissions("admin:footprint:list")
	@RequiresPermissionsDesc(menu = { "用户管理", "用户足迹" }, button = "查询")
	@GetMapping("/list")
	public Object list(String userId, String goodsId, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer limit,
			@Sort @RequestParam(defaultValue = "add_time") String sort,
			@Order @RequestParam(defaultValue = "desc") String order) {
		logger.info("【请求开始】用户管理->用户足迹->查询,请求参数:userId:{},goodsId:{},page:{}", userId, goodsId, page);

		List<DtsFootprint> footprintList = footprintService.querySelective(userId, goodsId, page, limit, sort, order);
		long total = PageInfo.of(footprintList).getTotal();
		Map<String, Object> data = new HashMap<>();
		data.put("total", total);
		data.put("items", footprintList);

		logger.info("【请求结束】用户管理->用户足迹->查询,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}
}
