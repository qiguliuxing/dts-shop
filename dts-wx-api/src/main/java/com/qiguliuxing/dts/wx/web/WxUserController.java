package com.qiguliuxing.dts.wx.web;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.domain.DtsUser;
import com.qiguliuxing.dts.db.domain.DtsUserAccount;
import com.qiguliuxing.dts.db.service.DtsAccountService;
import com.qiguliuxing.dts.db.service.DtsCouponService;
import com.qiguliuxing.dts.db.service.DtsOrderService;
import com.qiguliuxing.dts.db.service.DtsUserService;
import com.qiguliuxing.dts.wx.annotation.LoginUser;
import com.qiguliuxing.dts.wx.util.WxResponseCode;
import com.qiguliuxing.dts.wx.util.WxResponseUtil;

/**
 * 用户服务
 */
@RestController
@RequestMapping("/wx/user")
@Validated
public class WxUserController {
	private static final Logger logger = LoggerFactory.getLogger(WxUserController.class);

	@Autowired
	private DtsOrderService orderService;

	@Autowired
	private DtsAccountService accountService;
	
	@Autowired
	private DtsUserService userService;

	@Autowired
	private DtsCouponService couponService;

	/**
	 * 用户个人页面数据
	 * <p>
	 * @param userId
	 *            用户ID
	 * @return 用户个人页面数据
	 */
	@GetMapping("index")
	public Object list(@LoginUser Integer userId) {
		logger.info("【请求开始】用户个人页面数据,请求参数,userId:{}", userId);

		if (userId == null) {
			logger.error("用户个人页面数据查询失败:用户未登录！！！");
			return ResponseUtil.unlogin();
		}

		Map<Object, Object> data = new HashMap<Object, Object>();
		data.put("order", orderService.orderInfo(userId));

		// 查询用户账号的总金额和剩余金额
		DtsUserAccount userAccount = accountService.findShareUserAccountByUserId(userId);
		BigDecimal totalAmount = new BigDecimal(0.00);
		BigDecimal remainAmount = new BigDecimal(0.00);
		if (userAccount != null) {
			totalAmount = userAccount.getTotalAmount();
			remainAmount = userAccount.getRemainAmount();
		}
		data.put("totalAmount", totalAmount);
		data.put("remainAmount", remainAmount);

		// 查询用户的优惠券
		int total = couponService.queryUserCouponCnt(userId);
		data.put("couponCount", total);

		logger.info("【请求结束】用户个人页面数据,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}
	
	/**
	 * 申请代理用户
	 * <p>
	 * @param userId
	 *            用户ID
	 * @return 用户个人页面数据
	 */
	@PostMapping("applyAgency")
	public Object applyAgency(@LoginUser Integer userId) {
		logger.info("【请求开始】用户个人页面代理申请,请求参数,userId:{}", userId);

		if (userId == null) {
			logger.error("用户个人页面代理申请:用户未登录！！！");
			return ResponseUtil.unlogin();
		}
		
		DtsUser user = userService.findById(userId);
		
		//用户存在且未注销，未禁用
		if (user != null && user.getStatus().intValue() != 1 && user.getStatus().intValue() != 2) {
			// 查询用户账号,不存在则删除，如已经存在，不管状态如何都不做改变
			DtsUserAccount userAccount = accountService.findShareUserAccountByUserId(userId);
			if (userAccount == null) {//如果不存在则新建一个账户
				userAccount = new DtsUserAccount();
				userAccount.setRemainAmount(new BigDecimal(0));
				userAccount.setSettlementRate(5);//默认5%的比例
				userAccount.setStatus((byte) 1);//生效
				userAccount.setTotalAmount(new BigDecimal(0));
				userAccount.setUserId(userId);
				accountService.add(userAccount);
			}
			user.setStatus((byte) 3);//代理申请中
			userService.updateById(user);
		} else {
			logger.error("用户个人页面代理申请出错:{}", WxResponseCode.COUPON_EXCEED_LIMIT.desc());
			return WxResponseUtil.fail(WxResponseCode.INVALID_USER);
		}
		
		logger.info("【请求结束】用户个人页面代理申请,响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}
	
	/**
	 * 获取用户
	 * <p>
	 * @param userId
	 *            用户ID
	 * @return 用户个人页面数据
	 */
	@GetMapping("getSharedUrl")
	public Object getSharedUrl(@LoginUser Integer userId) {
		logger.info("【请求开始】获取用户推广二维码图片URL,请求参数,userId:{}", userId);

		Map<String, Object> data = new HashMap<>();
		data.put("userSharedUrl", "");//默认设置没有
		if (userId == null) {
			logger.error("获取用户推广二维码图片URL:用户未登录！！！");
		} else {
			DtsUserAccount userAccount = accountService.findShareUserAccountByUserId(userId);
			//如果没申请，数据则不存在，存在数据且审批通过则会形成推广二维码
			if (userAccount != null && StringUtils.isNotBlank(userAccount.getShareUrl())) {
				data.put("userSharedUrl", userAccount.getShareUrl());
			}
		}
		
		logger.info("【请求结束】获取用户推广二维码图片URL,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}
}