package com.qiguliuxing.dts.admin.web;

import static com.qiguliuxing.dts.admin.util.AdminResponseCode.ROLE_NAME_EXIST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
import com.qiguliuxing.dts.admin.util.AdminResponseCode;
import com.qiguliuxing.dts.admin.util.AdminResponseUtil;
import com.qiguliuxing.dts.admin.util.PermVo;
import com.qiguliuxing.dts.admin.util.Permission;
import com.qiguliuxing.dts.admin.util.PermissionUtil;
import com.qiguliuxing.dts.core.util.JacksonUtil;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.validator.Order;
import com.qiguliuxing.dts.core.validator.Sort;
import com.qiguliuxing.dts.db.domain.DtsPermission;
import com.qiguliuxing.dts.db.domain.DtsRole;
import com.qiguliuxing.dts.db.service.DtsPermissionService;
import com.qiguliuxing.dts.db.service.DtsRoleService;

@RestController
@RequestMapping("/admin/role")
@Validated
public class AdminRoleController {
	private static final Logger logger = LoggerFactory.getLogger(AdminRoleController.class);

	@Autowired
	private DtsRoleService roleService;
	@Autowired
	private DtsPermissionService permissionService;

	@RequiresPermissions("admin:role:list")
	@RequiresPermissionsDesc(menu = { "系统管理", "角色管理" }, button = "角色查询")
	@GetMapping("/list")
	public Object list(String name, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer limit,
			@Sort @RequestParam(defaultValue = "add_time") String sort,
			@Order @RequestParam(defaultValue = "desc") String order) {
		logger.info("【请求开始】系统管理->角色管理->角色查询,请求参数,name:{},page:{}", name, page);

		List<DtsRole> roleList = roleService.querySelective(name, page, limit, sort, order);
		long total = PageInfo.of(roleList).getTotal();
		Map<String, Object> data = new HashMap<>();
		data.put("total", total);
		data.put("items", roleList);

		logger.info("【请求结束】系统管理->角色管理->角色查询,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	@GetMapping("/options")
	public Object options() {
		List<DtsRole> roleList = roleService.queryAll();
		logger.info("【请求开始】系统管理->角色管理->查询所有角色");

		List<Map<String, Object>> options = new ArrayList<>(roleList.size());
		for (DtsRole role : roleList) {
			Map<String, Object> option = new HashMap<>(2);
			option.put("value", role.getId());
			option.put("label", role.getName());
			options.add(option);
		}

		logger.info("【请求结束】系统管理->角色管理->查询所有角色,响应结果:{}", JSONObject.toJSONString(options));
		return ResponseUtil.ok(options);
	}

	@RequiresPermissions("admin:role:read")
	@RequiresPermissionsDesc(menu = { "系统管理", "角色管理" }, button = "角色详情")
	@GetMapping("/read")
	public Object read(@NotNull Integer id) {
		logger.info("【请求开始】系统管理->角色管理->角色详情,请求参数,id:{}", id);

		DtsRole role = roleService.findById(id);

		logger.info("【请求结束】系统管理->角色管理->角色详情,响应结果:{}", JSONObject.toJSONString(role));
		return ResponseUtil.ok(role);
	}

	private Object validate(DtsRole role) {
		String name = role.getName();
		if (StringUtils.isEmpty(name)) {
			return ResponseUtil.badArgument();
		}

		return null;
	}

	@RequiresPermissions("admin:role:create")
	@RequiresPermissionsDesc(menu = { "系统管理", "角色管理" }, button = "角色添加")
	@PostMapping("/create")
	public Object create(@RequestBody DtsRole role) {
		logger.info("【请求开始】系统管理->角色管理->角色添加,请求参数:{}", JSONObject.toJSONString(role));

		Object error = validate(role);
		if (error != null) {
			return error;
		}

		if (roleService.checkExist(role.getName())) {
			logger.info("系统管理->角色管理->角色添加错误:{}", ROLE_NAME_EXIST.desc());
			return AdminResponseUtil.fail(ROLE_NAME_EXIST);
		}

		roleService.add(role);

		logger.info("【请求结束】系统管理->角色管理->角色添加,响应结果:{}", JSONObject.toJSONString(role));
		return ResponseUtil.ok(role);
	}

	@RequiresPermissions("admin:role:update")
	@RequiresPermissionsDesc(menu = { "系统管理", "角色管理" }, button = "角色编辑")
	@PostMapping("/update")
	public Object update(@RequestBody DtsRole role) {
		logger.info("【请求开始】系统管理->角色管理->角色编辑,请求参数:{}", JSONObject.toJSONString(role));

		Object error = validate(role);
		if (error != null) {
			return error;
		}

		roleService.updateById(role);
		logger.info("【请求结束】系统管理->角色管理->角色编辑,响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

	@RequiresPermissions("admin:role:delete")
	@RequiresPermissionsDesc(menu = { "系统管理", "角色管理" }, button = "角色删除")
	@PostMapping("/delete")
	public Object delete(@RequestBody DtsRole role) {
		logger.info("【请求开始】系统管理->角色管理->角色删除,请求参数,id:{}", JSONObject.toJSONString(role));

		Integer id = role.getId();
		if (id == null) {
			return ResponseUtil.badArgument();
		}
		roleService.deleteById(id);

		logger.info("【请求结束】系统管理->角色管理->角色删除,响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

	@Autowired
	private ApplicationContext context;
	private List<PermVo> systemPermissions = null;
	private Set<String> systemPermissionsString = null;

	private List<PermVo> getSystemPermissions() {
		final String basicPackage = "com.qiguliuxing.dts.admin";
		if (systemPermissions == null) {
			List<Permission> permissions = PermissionUtil.listPermission(context, basicPackage);
			systemPermissions = PermissionUtil.listPermVo(permissions);
			systemPermissionsString = PermissionUtil.listPermissionString(permissions);
		}
		return systemPermissions;
	}

	private Set<String> getAssignedPermissions(Integer roleId) {
		// 这里需要注意的是，如果存在超级权限*，那么这里需要转化成当前所有系统权限。
		// 之所以这么做，是因为前端不能识别超级权限，所以这里需要转换一下。
		Set<String> assignedPermissions = null;
		if (permissionService.checkSuperPermission(roleId)) {
			getSystemPermissions();
			assignedPermissions = systemPermissionsString;
		} else {
			assignedPermissions = permissionService.queryByRoleId(roleId);
		}

		return assignedPermissions;
	}

	/**
	 * 管理员的权限情况
	 *
	 * @return 系统所有权限列表和管理员已分配权限
	 */
	@RequiresPermissions("admin:role:permission:get")
	@RequiresPermissionsDesc(menu = { "系统管理", "角色管理" }, button = "权限详情")
	@GetMapping("/permissions")
	public Object getPermissions(Integer roleId) {
		logger.info("【请求开始】系统管理->角色管理->权限详情,请求参数,roleId:{}", roleId);

		List<PermVo> systemPermissions = getSystemPermissions();
		Set<String> assignedPermissions = getAssignedPermissions(roleId);

		Map<String, Object> data = new HashMap<>();
		data.put("systemPermissions", systemPermissions);
		data.put("assignedPermissions", assignedPermissions);

		logger.info("【请求结束】系统管理->角色管理->权限详情,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	/**
	 * 更新管理员的权限
	 *
	 * @param body
	 * @return
	 */
	@RequiresPermissions("admin:role:permission:update")
	@RequiresPermissionsDesc(menu = { "系统管理", "角色管理" }, button = "权限变更")
	@PostMapping("/permissions")
	public Object updatePermissions(@RequestBody String body) {
		logger.info("【请求开始】系统管理->角色管理->权限变更,请求参数,body:{}", body);

		Integer roleId = JacksonUtil.parseInteger(body, "roleId");
		List<String> permissions = JacksonUtil.parseStringList(body, "permissions");
		if (roleId == null || permissions == null) {
			return ResponseUtil.badArgument();
		}

		// 如果修改的角色是超级权限，则拒绝修改。
		if (permissionService.checkSuperPermission(roleId)) {
			logger.error("系统管理->角色管理->权限变更 错误:{}", AdminResponseCode.ROLE_SUPER_SUPERMISSION.desc());
			return AdminResponseUtil.fail(AdminResponseCode.ROLE_SUPER_SUPERMISSION);
		}

		// 先删除旧的权限，再更新新的权限
		permissionService.deleteByRoleId(roleId);
		for (String permission : permissions) {
			DtsPermission DtsPermission = new DtsPermission();
			DtsPermission.setRoleId(roleId);
			DtsPermission.setPermission(permission);
			permissionService.add(DtsPermission);
		}

		logger.info("【请求结束】系统管理->角色管理->权限变更,响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

}
