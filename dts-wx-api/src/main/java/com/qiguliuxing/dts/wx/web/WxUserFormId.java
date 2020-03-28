package com.qiguliuxing.dts.wx.web;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.domain.DtsUser;
import com.qiguliuxing.dts.db.domain.DtsUserFormid;
import com.qiguliuxing.dts.db.service.DtsUserFormIdService;
import com.qiguliuxing.dts.db.service.DtsUserService;
import com.qiguliuxing.dts.wx.annotation.LoginUser;

@RestController
@RequestMapping("/wx/formid")
@Validated
public class WxUserFormId {
	private static final Logger logger = LoggerFactory.getLogger(WxUserFormId.class);

	@Autowired
	private DtsUserService userService;

	@Autowired
	private DtsUserFormIdService formIdService;

	/**
	 * 创建微信访问fromID
	 * 
	 * @param userId
	 * @param formId
	 * @return
	 */
	@GetMapping("create")
	public Object create(@LoginUser Integer userId, @NotNull String formId) {
		logger.info("【请求开始】创建微信访问fromID,请求参数,userId:{}", userId);

		if (userId == null) {
			logger.error("创建微信访问fromID失败:用户未登录！！！");
			return ResponseUtil.unlogin();
		}

		DtsUser user = userService.findById(userId);
		DtsUserFormid userFormid = new DtsUserFormid();
		userFormid.setOpenid(user.getWeixinOpenid());
		userFormid.setFormid(formId);
		userFormid.setIsprepay(false);
		userFormid.setUseamount(1);
		userFormid.setExpireTime(LocalDateTime.now().plusDays(7));
		formIdService.addUserFormid(userFormid);

		logger.info("【请求结束】创建微信访问fromID,响应结果:{}", JSONObject.toJSONString(userFormid));
		return ResponseUtil.ok();
	}
}
