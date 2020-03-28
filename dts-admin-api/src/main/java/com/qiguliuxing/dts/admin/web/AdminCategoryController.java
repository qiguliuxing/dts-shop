package com.qiguliuxing.dts.admin.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.qiguliuxing.dts.admin.annotation.RequiresPermissionsDesc;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.validator.Order;
import com.qiguliuxing.dts.core.validator.Sort;
import com.qiguliuxing.dts.db.domain.DtsCategory;
import com.qiguliuxing.dts.db.service.DtsCategoryService;

@RestController
@RequestMapping("/admin/category")
@Validated
public class AdminCategoryController {
	private static final Logger logger = LoggerFactory.getLogger(AdminCategoryController.class);

	@Autowired
	private DtsCategoryService categoryService;

	@RequiresPermissions("admin:category:list")
	@RequiresPermissionsDesc(menu = { "商场管理", "类目管理" }, button = "查询")
	@GetMapping("/list")
	public Object list(String id, String name, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer limit,
			@Sort @RequestParam(defaultValue = "add_time") String sort,
			@Order @RequestParam(defaultValue = "desc") String order) {
		logger.info("【请求开始】商场管理->类目管理->查询,请求参数:name:{},page:{}", name, page);

		List<DtsCategory> collectList = categoryService.querySelective(id, name, page, limit, sort, order);
		long total = PageInfo.of(collectList).getTotal();
		Map<String, Object> data = new HashMap<>();
		data.put("total", total);
		data.put("items", collectList);

		logger.info("【请求结束】商场管理->类目管理->查询:total:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	private Object validate(DtsCategory category) {
		String name = category.getName();
		if (StringUtils.isEmpty(name)) {
			return ResponseUtil.badArgument();
		}

		String level = category.getLevel();
		if (StringUtils.isEmpty(level)) {
			return ResponseUtil.badArgument();
		}
		if (!level.equals("L1") && !level.equals("L2")) {
			return ResponseUtil.badArgumentValue();
		}

		Integer pid = category.getPid();
		if (level.equals("L2") && (pid == null)) {
			return ResponseUtil.badArgument();
		}

		return null;
	}

	@RequiresPermissions("admin:category:create")
	@RequiresPermissionsDesc(menu = { "商场管理", "类目管理" }, button = "添加")
	@PostMapping("/create")
	public Object create(@RequestBody DtsCategory category) {
		logger.info("【请求开始】商场管理->类目管理->添加,请求参数:{}", JSONObject.toJSONString(category));

		Object error = validate(category);
		if (error != null) {
			return error;
		}
		categoryService.add(category);

		logger.info("【请求结束】商场管理->类目管理->添加:响应结果:{}", JSONObject.toJSONString(category));
		return ResponseUtil.ok(category);
	}

	@RequiresPermissions("admin:category:read")
	@RequiresPermissionsDesc(menu = { "商场管理", "类目管理" }, button = "详情")
	@GetMapping("/read")
	public Object read(@NotNull Integer id) {
		logger.info("【请求开始】商场管理->类目管理->详情,请求参数,id:{}", id);

		DtsCategory category = categoryService.findById(id);

		logger.info("【请求结束】商场管理->类目管理->详情:响应结果:{}", JSONObject.toJSONString(category));
		return ResponseUtil.ok(category);
	}

	@RequiresPermissions("admin:category:update")
	@RequiresPermissionsDesc(menu = { "商场管理", "类目管理" }, button = "编辑")
	@PostMapping("/update")
	public Object update(@RequestBody DtsCategory category) {
		logger.info("【请求开始】商场管理->类目管理->编辑,请求参数:{}", JSONObject.toJSONString(category));

		Object error = validate(category);
		if (error != null) {
			return error;
		}

		if (categoryService.updateById(category) == 0) {
			logger.error("商场管理->类目管理->编辑 失败，更新数据失败！");
			return ResponseUtil.updatedDataFailed();
		}

		logger.info("【请求结束】商场管理->类目管理->编辑:响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

	@RequiresPermissions("admin:category:delete")
	@RequiresPermissionsDesc(menu = { "商场管理", "类目管理" }, button = "删除")
	@PostMapping("/delete")
	public Object delete(@RequestBody DtsCategory category) {
		logger.info("【请求开始】商场管理->类目管理->删除,请求参数:{}", JSONObject.toJSONString(category));

		Integer id = category.getId();
		if (id == null) {
			return ResponseUtil.badArgument();
		}
		categoryService.deleteById(id);

		logger.info("【请求结束】商场管理->类目管理->删除:响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

	@RequiresPermissions("admin:category:list")
	@GetMapping("/l1")
	public Object catL1() {
		logger.info("【请求开始】商场管理->类目管理->一级分类目录查询");

		// 所有一级分类目录
		List<DtsCategory> l1CatList = categoryService.queryL1();
		List<Map<String, Object>> data = new ArrayList<>(l1CatList.size());
		for (DtsCategory category : l1CatList) {
			Map<String, Object> d = new HashMap<>(2);
			d.put("value", category.getId());
			d.put("label", category.getName());
			data.add(d);
		}

		logger.info("【请求结束】商场管理->类目管理->一级分类目录查询:total:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}
}
