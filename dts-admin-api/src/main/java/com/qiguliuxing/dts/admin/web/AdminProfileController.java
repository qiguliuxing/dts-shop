package com.qiguliuxing.dts.admin.web;

import static com.qiguliuxing.dts.admin.util.AdminResponseCode.ADMIN_INVALID_OLD_PASSWORD;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qiguliuxing.dts.admin.util.AdminResponseUtil;
import com.qiguliuxing.dts.core.util.JacksonUtil;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.util.bcrypt.BCryptPasswordEncoder;
import com.qiguliuxing.dts.db.domain.DtsAdmin;
import com.qiguliuxing.dts.db.service.DtsAdminService;

@RestController
@RequestMapping("/admin/profile")
@Validated
public class AdminProfileController {
	private static final Logger logger = LoggerFactory.getLogger(AdminProfileController.class);

	@Autowired
	private DtsAdminService adminService;

	@RequiresAuthentication
	@PostMapping("/password")
	public Object create(@RequestBody String body) {
		logger.info("【请求开始】系统管理->修改密码,请求参数,body:{}", body);

		String oldPassword = JacksonUtil.parseString(body, "oldPassword");
		String newPassword = JacksonUtil.parseString(body, "newPassword");
		if (StringUtils.isEmpty(oldPassword)) {
			return ResponseUtil.badArgument();
		}
		if (StringUtils.isEmpty(newPassword)) {
			return ResponseUtil.badArgument();
		}

		Subject currentUser = SecurityUtils.getSubject();
		DtsAdmin admin = (DtsAdmin) currentUser.getPrincipal();

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		if (!encoder.matches(oldPassword, admin.getPassword())) {
			logger.info("系统管理->修改密码 错误:{}", ADMIN_INVALID_OLD_PASSWORD.desc());
			return AdminResponseUtil.fail(ADMIN_INVALID_OLD_PASSWORD);
		}

		String encodedNewPassword = encoder.encode(newPassword);
		admin.setPassword(encodedNewPassword);

		adminService.updateById(admin);

		logger.info("【请求结束】系统管理->修改密码,响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

}
