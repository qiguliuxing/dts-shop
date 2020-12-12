package com.qiguliuxing.dts.admin.web;

import static com.qiguliuxing.dts.admin.util.AdminResponseCode.ADMIN_INVALID_NAME;
import static com.qiguliuxing.dts.admin.util.AdminResponseCode.ADMIN_INVALID_PASSWORD;
import static com.qiguliuxing.dts.admin.util.AdminResponseCode.ADMIN_NAME_EXIST;

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
import com.qiguliuxing.dts.admin.util.AdminResponseUtil;
import com.qiguliuxing.dts.core.util.RegexUtil;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.util.bcrypt.BCryptPasswordEncoder;
import com.qiguliuxing.dts.core.validator.Order;
import com.qiguliuxing.dts.core.validator.Sort;
import com.qiguliuxing.dts.db.domain.DtsAdmin;
import com.qiguliuxing.dts.db.service.DtsAdminService;

@RestController
@RequestMapping("/admin/admin")
@Validated
public class AdminAdminController {
	private static final Logger logger = LoggerFactory.getLogger(AdminAdminController.class);

	@Autowired
	private DtsAdminService adminService;

	@RequiresPermissions("admin:admin:list")
	@RequiresPermissionsDesc(menu = { "系统管理", "管理员管理" }, button = "查询")
	@GetMapping("/list")
	public Object list(String username, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer limit,
			@Sort @RequestParam(defaultValue = "add_time") String sort,
			@Order @RequestParam(defaultValue = "desc") String order) {
		logger.info("【请求开始】系统管理->管理员管理->查询,请求参数:username:{},page:{}", username, page);

		List<DtsAdmin> adminList = adminService.querySelective(username, page, limit, sort, order);
		long total = PageInfo.of(adminList).getTotal();
		Map<String, Object> data = new HashMap<>();
		data.put("total", total);
		data.put("items", adminList);

		logger.info("【请求结束】系统管理->管理员管理->查询,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	private Object validate(DtsAdmin admin) {
		String name = admin.getUsername();
		if (StringUtils.isEmpty(name)) {
			return ResponseUtil.badArgument();
		}
		if (!RegexUtil.isUsername(name)) {
			logger.error("校验错误：{}", ADMIN_INVALID_NAME.desc());
			return AdminResponseUtil.fail(ADMIN_INVALID_NAME);
		}
		String password = admin.getPassword();
		if (StringUtils.isEmpty(password) || password.length() < 6) {
			logger.error("校验错误：{}", ADMIN_INVALID_PASSWORD.desc());
			return AdminResponseUtil.fail(ADMIN_INVALID_PASSWORD);
		}
		return null;
	}

	@RequiresPermissions("admin:admin:create")
	@RequiresPermissionsDesc(menu = { "系统管理", "管理员管理" }, button = "添加")
	@PostMapping("/create")
	public Object create(@RequestBody DtsAdmin admin) {
		logger.info("【请求开始】系统管理->管理员管理->添加,请求参数:{}", JSONObject.toJSONString(admin));

		Object error = validate(admin);
		if (error != null) {
			return error;
		}

		String username = admin.getUsername();
		List<DtsAdmin> adminList = adminService.findAdmin(username);
		if (adminList.size() > 0) {
			logger.error("系统管理->管理员管理->添加 ,错误：{}", ADMIN_NAME_EXIST.desc());
			return AdminResponseUtil.fail(ADMIN_NAME_EXIST);
		}

		String rawPassword = admin.getPassword();
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String encodedPassword = encoder.encode(rawPassword);
		admin.setPassword(encodedPassword);
		adminService.add(admin);

		logger.info("【请求结束】系统管理->管理员管理->添加,响应结果:{}", JSONObject.toJSONString(admin));
		return ResponseUtil.ok(admin);
	}

	@RequiresPermissions("admin:admin:read")
	@RequiresPermissionsDesc(menu = { "系统管理", "管理员管理" }, button = "详情")
	@GetMapping("/read")
	public Object read(@NotNull Integer id) {
		logger.info("【请求开始】系统管理->管理员管理->详情,请求参数,id:{}", id);

		DtsAdmin admin = adminService.findById(id);

		logger.info("【请求结束】系统管理->管理员管理->详情,响应结果:{}", JSONObject.toJSONString(admin));
		return ResponseUtil.ok(admin);
	}

	@RequiresPermissions("admin:admin:update")
	@RequiresPermissionsDesc(menu = { "系统管理", "管理员管理" }, button = "编辑")
	@PostMapping("/update")
	public Object update(@RequestBody DtsAdmin admin) {
		logger.info("【请求开始】系统管理->管理员管理->编辑,请求参数:{}", JSONObject.toJSONString(admin));

		Object error = validate(admin);
		if (error != null) {
			return error;
		}

		Integer anotherAdminId = admin.getId();
		if (anotherAdminId == null) {
			return ResponseUtil.badArgument();
		}

		String rawPassword = admin.getPassword();
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String encodedPassword = encoder.encode(rawPassword);
		admin.setPassword(encodedPassword);

		if (adminService.updateById(admin) == 0) {
			logger.error("系统管理->管理员管理-编辑 ,错误：{}", "更新数据失败！");
			return ResponseUtil.updatedDataFailed();
		}

		logger.info("【请求结束】系统管理->管理员管理->编辑,响应结果:{}", JSONObject.toJSONString(admin));
		return ResponseUtil.ok(admin);
	}

	@RequiresPermissions("admin:admin:delete")
	@RequiresPermissionsDesc(menu = { "系统管理", "管理员管理" }, button = "删除")
	@PostMapping("/delete")
	public Object delete(@RequestBody DtsAdmin admin) {
		logger.info("【请求开始】系统管理->管理员管理->删除,请求参数:{}", JSONObject.toJSONString(admin));

		Integer anotherAdminId = admin.getId();
		if (anotherAdminId == null) {
			return ResponseUtil.badArgument();
		}

		adminService.deleteById(anotherAdminId);

		logger.info("【请求结束】系统管理->管理员管理->删除 成功！");
		return ResponseUtil.ok();
	}
}
