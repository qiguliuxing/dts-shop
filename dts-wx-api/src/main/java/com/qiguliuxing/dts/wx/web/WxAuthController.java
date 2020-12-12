package com.qiguliuxing.dts.wx.web;

import static com.qiguliuxing.dts.wx.util.WxResponseCode.AUTH_CAPTCHA_FREQUENCY;
import static com.qiguliuxing.dts.wx.util.WxResponseCode.AUTH_CAPTCHA_UNMATCH;
import static com.qiguliuxing.dts.wx.util.WxResponseCode.AUTH_CAPTCHA_UNSUPPORT;
import static com.qiguliuxing.dts.wx.util.WxResponseCode.AUTH_INVALID_ACCOUNT;
import static com.qiguliuxing.dts.wx.util.WxResponseCode.AUTH_INVALID_MOBILE;
import static com.qiguliuxing.dts.wx.util.WxResponseCode.AUTH_MOBILE_REGISTERED;
import static com.qiguliuxing.dts.wx.util.WxResponseCode.AUTH_MOBILE_UNREGISTERED;
import static com.qiguliuxing.dts.wx.util.WxResponseCode.AUTH_NAME_REGISTERED;
import static com.qiguliuxing.dts.wx.util.WxResponseCode.AUTH_OPENID_BINDED;
import static com.qiguliuxing.dts.wx.util.WxResponseCode.AUTH_OPENID_UNACCESS;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.qiguliuxing.dts.core.consts.CommConsts;
import com.qiguliuxing.dts.core.notify.NotifyService;
import com.qiguliuxing.dts.core.notify.NotifyType;
import com.qiguliuxing.dts.core.type.UserTypeEnum;
import com.qiguliuxing.dts.core.util.CharUtil;
import com.qiguliuxing.dts.core.util.JacksonUtil;
import com.qiguliuxing.dts.core.util.RegexUtil;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.util.bcrypt.BCryptPasswordEncoder;
import com.qiguliuxing.dts.db.domain.DtsUser;
import com.qiguliuxing.dts.db.service.CouponAssignService;
import com.qiguliuxing.dts.db.service.DtsUserService;
import com.qiguliuxing.dts.wx.annotation.LoginUser;
import com.qiguliuxing.dts.wx.dao.UserInfo;
import com.qiguliuxing.dts.wx.dao.UserToken;
import com.qiguliuxing.dts.wx.dao.WxLoginInfo;
import com.qiguliuxing.dts.wx.service.CaptchaCodeManager;
import com.qiguliuxing.dts.wx.service.UserTokenManager;
import com.qiguliuxing.dts.wx.util.IpUtil;
import com.qiguliuxing.dts.wx.util.WxResponseUtil;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;

/**
 * 鉴权服务
 */
@RestController
@RequestMapping("/wx/auth")
@Validated
public class WxAuthController {
	private static final Logger logger = LoggerFactory.getLogger(WxAuthController.class);

	@Autowired
	private DtsUserService userService;

	@Autowired
	private WxMaService wxService;

	@Autowired
	private NotifyService notifyService;

	@Autowired
	private CouponAssignService couponAssignService;

	/**
	 * 账号登录
	 *
	 * @param body
	 *            请求内容，{ username: xxx, password: xxx }
	 * @param request
	 *            请求对象
	 * @return 登录结果
	 */
	@PostMapping("login")
	public Object login(@RequestBody String body, HttpServletRequest request) {
		logger.info("【请求开始】账户登录,请求参数,body:{}", body);

		String username = JacksonUtil.parseString(body, "username");
		String password = JacksonUtil.parseString(body, "password");
		if (username == null || password == null) {
			return ResponseUtil.badArgument();
		}

		List<DtsUser> userList = userService.queryByUsername(username);
		DtsUser user = null;
		if (userList.size() > 1) {
			logger.error("账户登录 出现多个同名用户错误,用户名:{},用户数量:{}", username, userList.size());
			return ResponseUtil.serious();
		} else if (userList.size() == 0) {
			logger.error("账户登录 用户尚未存在,用户名:{}", username);
			return ResponseUtil.badArgumentValue();
		} else {
			user = userList.get(0);
		}

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		if (!encoder.matches(password, user.getPassword())) {
			logger.error("账户登录 ,错误密码：{},{}", password, AUTH_INVALID_ACCOUNT.desc());// 错误的密码打印到日志中作为提示也无妨
			return WxResponseUtil.fail(AUTH_INVALID_ACCOUNT);
		}

		// userInfo
		UserInfo userInfo = new UserInfo();
		userInfo.setNickName(username);
		userInfo.setAvatarUrl(user.getAvatar());
		
		try {
			String registerDate = new SimpleDateFormat("yyyy-MM-dd")
					.format(user.getAddTime() == null ? user.getAddTime() : LocalDateTime.now());
			userInfo.setRegisterDate(registerDate);
			userInfo.setStatus(user.getStatus());
			userInfo.setUserLevel(user.getUserLevel());// 用户层级
			userInfo.setUserLevelDesc(UserTypeEnum.getInstance(user.getUserLevel()).getDesc());// 用户层级描述
		} catch (Exception e) {
			logger.error("账户登录：设置用户指定信息出错："+e.getMessage());
			e.printStackTrace();
		}

		// token
		UserToken userToken = null;
		try {
			userToken = UserTokenManager.generateToken(user.getId());
		} catch (Exception e) {
			logger.error("账户登录失败,生成token失败：{}", user.getId());
			e.printStackTrace();
			return ResponseUtil.fail();
		}

		Map<Object, Object> result = new HashMap<Object, Object>();
		result.put("token", userToken.getToken());
		result.put("tokenExpire", userToken.getExpireTime().toString());
		result.put("userInfo", userInfo);

		logger.info("【请求结束】账户登录,响应结果:{}", JSONObject.toJSONString(result));
		return ResponseUtil.ok(result);
	}

	/**
	 * 微信登录
	 *
	 * @param wxLoginInfo
	 *            请求内容，{ code: xxx, userInfo: xxx }
	 * @param request
	 *            请求对象
	 * @return 登录结果
	 */
	@PostMapping("login_by_weixin")
	public Object loginByWeixin(@RequestBody WxLoginInfo wxLoginInfo, HttpServletRequest request) {
		logger.info("【请求开始】微信登录,请求参数,wxLoginInfo:{}", JSONObject.toJSONString(wxLoginInfo));

		String code = wxLoginInfo.getCode();
		UserInfo userInfo = wxLoginInfo.getUserInfo();
		if (code == null || userInfo == null) {
			return ResponseUtil.badArgument();
		}

		Integer shareUserId = wxLoginInfo.getShareUserId();
		String sessionKey = null;
		String openId = null;
		try {
			WxMaJscode2SessionResult result = this.wxService.getUserService().getSessionInfo(code);
			sessionKey = result.getSessionKey();
			openId = result.getOpenid();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (sessionKey == null || openId == null) {
			logger.error("微信登录,调用官方接口失败：{}", code);
			return ResponseUtil.fail();
		}

		DtsUser user = userService.queryByOid(openId);
		
		if (user == null) {
			user = new DtsUser();
			user.setUsername(openId);
			user.setPassword(openId);
			user.setWeixinOpenid(openId);
			user.setAvatar(userInfo.getAvatarUrl());
			user.setNickname(userInfo.getNickName());
			user.setGender(userInfo.getGender());
			user.setUserLevel((byte) 0);
			user.setStatus((byte) 0);
			user.setLastLoginTime(LocalDateTime.now());
			user.setLastLoginIp(IpUtil.client(request));
			user.setShareUserId(shareUserId);

			userService.add(user);

			// 新用户发送注册优惠券
			couponAssignService.assignForRegister(user.getId());
		} else {
			user.setLastLoginTime(LocalDateTime.now());
			user.setLastLoginIp(IpUtil.client(request));
			if (userService.updateById(user) == 0) {
				return ResponseUtil.updatedDataFailed();
			}
		}

		// token
		UserToken userToken = null;
		try {
			userToken = UserTokenManager.generateToken(user.getId());
		} catch (Exception e) {
			logger.error("微信登录失败,生成token失败：{}", user.getId());
			e.printStackTrace();
			return ResponseUtil.fail();
		}
		userToken.setSessionKey(sessionKey);

		Map<Object, Object> result = new HashMap<Object, Object>();
		result.put("token", userToken.getToken());
		result.put("tokenExpire", userToken.getExpireTime().toString());
		userInfo.setUserId(user.getId());
		if (!StringUtils.isEmpty(user.getMobile())) {// 手机号存在则设置
			userInfo.setPhone(user.getMobile());
		}
		try {
			String registerDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
					.format(user.getAddTime() != null ? user.getAddTime() : LocalDateTime.now());
			userInfo.setRegisterDate(registerDate);
			userInfo.setStatus(user.getStatus());
			userInfo.setUserLevel(user.getUserLevel());// 用户层级
			userInfo.setUserLevelDesc(UserTypeEnum.getInstance(user.getUserLevel()).getDesc());// 用户层级描述
		} catch (Exception e) {
			logger.error("微信登录：设置用户指定信息出错："+e.getMessage());
			e.printStackTrace();
		}
		result.put("userInfo", userInfo);

		logger.info("【请求结束】微信登录,响应结果:{}", JSONObject.toJSONString(result));
		return ResponseUtil.ok(result);
	}

	/**
	 * 请求验证码
	 *
	 * @param body
	 *            手机号码{mobile}
	 * @return
	 */
	@PostMapping("regCaptcha")
	public Object registerCaptcha(@RequestBody String body) {
		logger.info("【请求开始】请求验证码,请求参数，body:{}", body);

		String phoneNumber = JacksonUtil.parseString(body, "mobile");
		if (StringUtils.isEmpty(phoneNumber)) {
			return ResponseUtil.badArgument();
		}
		if (!RegexUtil.isMobileExact(phoneNumber)) {
			return ResponseUtil.badArgumentValue();
		}

		if (!notifyService.isSmsEnable()) {
			logger.error("请求验证码出错:{}", AUTH_CAPTCHA_UNSUPPORT.desc());
			return WxResponseUtil.fail(AUTH_CAPTCHA_UNSUPPORT);
		}
		String code = CharUtil.getRandomNum(6);
		notifyService.notifySmsTemplate(phoneNumber, NotifyType.CAPTCHA, new String[] { code, "1" });

		boolean successful = CaptchaCodeManager.addToCache(phoneNumber, code);
		if (!successful) {
			logger.error("请求验证码出错:{}", AUTH_CAPTCHA_FREQUENCY.desc());
			return WxResponseUtil.fail(AUTH_CAPTCHA_FREQUENCY);
		}

		logger.info("【请求结束】请求验证码成功");
		return ResponseUtil.ok();
	}

	/**
	 * 账号注册
	 *
	 * @param body
	 *            请求内容 { username: xxx, password: xxx, mobile: xxx code: xxx }
	 *            其中code是手机验证码，目前还不支持手机短信验证码
	 * @param request
	 *            请求对象
	 * @return 登录结果 成功则 { errno: 0, errmsg: '成功', data: { token: xxx, tokenExpire:
	 *         xxx, userInfo: xxx } } 失败则 { errno: XXX, errmsg: XXX }
	 */
	@PostMapping("register")
	public Object register(@RequestBody String body, HttpServletRequest request) {
		logger.info("【请求开始】账号注册,请求参数，body:{}", body);

		String username = JacksonUtil.parseString(body, "username");
		String password = JacksonUtil.parseString(body, "password");
		String mobile = JacksonUtil.parseString(body, "mobile");
		String code = JacksonUtil.parseString(body, "code");
		String wxCode = JacksonUtil.parseString(body, "wxCode");

		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils.isEmpty(mobile)
				|| StringUtils.isEmpty(wxCode) || StringUtils.isEmpty(code)) {
			return ResponseUtil.badArgument();
		}

		List<DtsUser> userList = userService.queryByUsername(username);
		if (userList.size() > 0) {
			logger.error("请求账号注册出错:{}", AUTH_NAME_REGISTERED.desc());
			return WxResponseUtil.fail(AUTH_NAME_REGISTERED);
		}

		userList = userService.queryByMobile(mobile);
		if (userList.size() > 0) {
			logger.error("请求账号注册出错:{}", AUTH_MOBILE_REGISTERED.desc());
			return WxResponseUtil.fail(AUTH_MOBILE_REGISTERED);
		}
		if (!RegexUtil.isMobileExact(mobile)) {
			logger.error("请求账号注册出错:{}", AUTH_INVALID_MOBILE.desc());
			return WxResponseUtil.fail(AUTH_INVALID_MOBILE);
		}
		// 判断验证码是否正确
		String cacheCode = CaptchaCodeManager.getCachedCaptcha(mobile);
		if (cacheCode == null || cacheCode.isEmpty() || !cacheCode.equals(code)) {
			logger.error("请求账号注册出错:{}", AUTH_CAPTCHA_UNMATCH.desc());
			return WxResponseUtil.fail(AUTH_CAPTCHA_UNMATCH);
		}

		String openId = null;
		try {
			WxMaJscode2SessionResult result = this.wxService.getUserService().getSessionInfo(wxCode);
			openId = result.getOpenid();
		} catch (Exception e) {
			logger.error("请求账号注册出错:{}", AUTH_OPENID_UNACCESS.desc());
			e.printStackTrace();
			return WxResponseUtil.fail(AUTH_OPENID_UNACCESS);
		}
		userList = userService.queryByOpenid(openId);
		if (userList.size() > 1) {
			return ResponseUtil.serious();
		}
		if (userList.size() == 1) {
			DtsUser checkUser = userList.get(0);
			String checkUsername = checkUser.getUsername();
			String checkPassword = checkUser.getPassword();
			if (!checkUsername.equals(openId) || !checkPassword.equals(openId)) {
				logger.error("请求账号注册出错:{}", AUTH_OPENID_BINDED.desc());
				return WxResponseUtil.fail(AUTH_OPENID_BINDED);
			}
		}

		DtsUser user = null;
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String encodedPassword = encoder.encode(password);
		user = new DtsUser();
		user.setUsername(username);
		user.setPassword(encodedPassword);
		user.setMobile(mobile);
		user.setWeixinOpenid(openId);
		user.setAvatar(CommConsts.DEFAULT_AVATAR);
		user.setNickname(username);
		user.setGender((byte) 0);
		user.setUserLevel((byte) 0);
		user.setStatus((byte) 0);
		user.setLastLoginTime(LocalDateTime.now());
		user.setLastLoginIp(IpUtil.client(request));
		userService.add(user);

		// 给新用户发送注册优惠券
		try {
			couponAssignService.assignForRegister(user.getId());
		} catch (Exception e) {
			logger.error("账号注册失败,给新用户发送注册优惠券失败：{}", user.getId());
			e.printStackTrace();
			return ResponseUtil.fail();
		}

		// userInfo
		UserInfo userInfo = new UserInfo();
		userInfo.setNickName(username);
		userInfo.setAvatarUrl(user.getAvatar());

		// token
		UserToken userToken = null;
		try {
			userToken = UserTokenManager.generateToken(user.getId());
		} catch (Exception e) {
			logger.error("账号注册失败,生成token失败：{}", user.getId());
			e.printStackTrace();
			return ResponseUtil.fail();
		}

		Map<Object, Object> result = new HashMap<Object, Object>();
		result.put("token", userToken.getToken());
		result.put("tokenExpire", userToken.getExpireTime().toString());
		result.put("userInfo", userInfo);

		logger.info("【请求结束】账号注册,响应结果:{}", JSONObject.toJSONString(result));
		return ResponseUtil.ok(result);
	}

	/**
	 * 账号密码重置
	 *
	 * @param body
	 *            请求内容 { password: xxx, mobile: xxx code: xxx }
	 *            其中code是手机验证码，目前还不支持手机短信验证码
	 * @param request
	 *            请求对象
	 * @return 登录结果 成功则 { errno: 0, errmsg: '成功' } 失败则 { errno: XXX, errmsg: XXX }
	 */
	@PostMapping("reset")
	public Object reset(@RequestBody String body, HttpServletRequest request) {
		logger.info("【请求开始】账号密码重置,请求参数，body:{}", body);

		String password = JacksonUtil.parseString(body, "password");
		String mobile = JacksonUtil.parseString(body, "mobile");
		String code = JacksonUtil.parseString(body, "code");

		if (mobile == null || code == null || password == null) {
			return ResponseUtil.badArgument();
		}

		// 判断验证码是否正确
		String cacheCode = CaptchaCodeManager.getCachedCaptcha(mobile);
		if (cacheCode == null || cacheCode.isEmpty() || !cacheCode.equals(code)) {
			logger.error("账号密码重置出错:{}", AUTH_CAPTCHA_UNMATCH.desc());
			return WxResponseUtil.fail(AUTH_CAPTCHA_UNMATCH);
		}

		List<DtsUser> userList = userService.queryByMobile(mobile);

		DtsUser user = null;
		if (userList.size() > 1) {
			logger.error("账号密码重置出错,账户不唯一,查询手机号:{}", mobile);
			return ResponseUtil.serious();
		} else if (userList.size() == 0) {
			logger.error("账号密码重置出错,账户不存在,查询手机号:{},{}", mobile, AUTH_MOBILE_UNREGISTERED.desc());
			return WxResponseUtil.fail(AUTH_MOBILE_UNREGISTERED);
		} else {
			user = userList.get(0);
		}

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String encodedPassword = encoder.encode(password);
		user.setPassword(encodedPassword);

		if (userService.updateById(user) == 0) {
			logger.error("账号密码重置更新用户信息出错,id：{}", user.getId());
			return ResponseUtil.updatedDataFailed();
		}

		logger.info("【请求结束】账号密码重置成功!");
		return ResponseUtil.ok();
	}

	/**
	 * 绑定手机号码
	 * 
	 * @param userId
	 * @param body
	 * @return
	 */
	@PostMapping("bindPhone")
	public Object bindPhone(@LoginUser Integer userId, @RequestBody String body) {
		logger.info("【请求开始】绑定手机号码,请求参数，body:{}", body);

		String sessionKey = UserTokenManager.getSessionKey(userId);
		String encryptedData = JacksonUtil.parseString(body, "encryptedData");
		String iv = JacksonUtil.parseString(body, "iv");
		WxMaPhoneNumberInfo phoneNumberInfo = null;
		try {
			phoneNumberInfo = this.wxService.getUserService().getPhoneNoInfo(sessionKey, encryptedData, iv);
		} catch (Exception e) {
			logger.error("绑定手机号码失败,获取微信绑定的手机号码出错：{}", body);
			e.printStackTrace();
			return ResponseUtil.fail();
		}
		String phone = phoneNumberInfo.getPhoneNumber();
		DtsUser user = userService.findById(userId);
		user.setMobile(phone);
		if (userService.updateById(user) == 0) {
			logger.error("绑定手机号码,更新用户信息出错,id：{}", user.getId());
			return ResponseUtil.updatedDataFailed();
		}
		Map<Object, Object> data = new HashMap<Object, Object>();
		data.put("phone", phone);

		logger.info("【请求结束】绑定手机号码,响应结果：{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	/**
	 * 注销登录
	 * 
	 * @param userId
	 * @return
	 */
	@PostMapping("logout")
	public Object logout(@LoginUser Integer userId) {
		logger.info("【请求开始】注销登录,请求参数，userId:{}", userId);
		if (userId == null) {
			return ResponseUtil.unlogin();
		}
		try {
			UserTokenManager.removeToken(userId);
		} catch (Exception e) {
			logger.error("注销登录出错：userId:{}", userId);
			e.printStackTrace();
			return ResponseUtil.fail();
		}

		logger.info("【请求结束】注销登录成功!");
		return ResponseUtil.ok();
	}
}
