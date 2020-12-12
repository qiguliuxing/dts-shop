package com.qiguliuxing.dts.wx.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.qiguliuxing.dts.core.util.JacksonUtil;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.validator.Order;
import com.qiguliuxing.dts.core.validator.Sort;
import com.qiguliuxing.dts.db.domain.DtsCart;
import com.qiguliuxing.dts.db.domain.DtsCoupon;
import com.qiguliuxing.dts.db.domain.DtsCouponUser;
import com.qiguliuxing.dts.db.domain.DtsGrouponRules;
import com.qiguliuxing.dts.db.service.CouponVerifyService;
import com.qiguliuxing.dts.db.service.DtsCartService;
import com.qiguliuxing.dts.db.service.DtsCouponService;
import com.qiguliuxing.dts.db.service.DtsCouponUserService;
import com.qiguliuxing.dts.db.service.DtsGrouponRulesService;
import com.qiguliuxing.dts.db.util.CouponConstant;
import com.qiguliuxing.dts.wx.annotation.LoginUser;
import com.qiguliuxing.dts.wx.dao.CouponVo;
import com.qiguliuxing.dts.wx.util.WxResponseCode;
import com.qiguliuxing.dts.wx.util.WxResponseUtil;

/**
 * 优惠券服务
 */
@RestController
@RequestMapping("/wx/coupon")
@Validated
public class WxCouponController {
	private static final Logger logger = LoggerFactory.getLogger(WxCouponController.class);

	@Autowired
	private DtsCouponService couponService;
	@Autowired
	private DtsCouponUserService couponUserService;
	@Autowired
	private DtsGrouponRulesService grouponRulesService;
	@Autowired
	private DtsCartService cartService;
	@Autowired
	private CouponVerifyService couponVerifyService;

	/**
	 * 优惠券列表
	 *
	 * @param page
	 * @param size
	 * @param sort
	 * @param order
	 * @return
	 */
	@GetMapping("list")
	public Object list(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size,
			@Sort @RequestParam(defaultValue = "add_time") String sort,
			@Order @RequestParam(defaultValue = "desc") String order) {
		logger.info("【请求开始】获取优惠券列表,请求参数,page:{},size:{}", page, size);

		List<DtsCoupon> couponList = couponService.queryList(page, size, sort, order);
		int total = couponService.queryTotal();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("data", couponList);
		data.put("count", total);

		logger.info("【请求结束】获取优惠券列表,响应内容:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	/**
	 * 个人优惠券列表
	 *
	 * @param userId
	 * @param status
	 * @param page
	 * @param size
	 * @param sort
	 * @param order
	 * @return
	 */
	@GetMapping("mylist")
	public Object mylist(@LoginUser Integer userId, @NotNull Short status,
			@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size,
			@Sort @RequestParam(defaultValue = "add_time") String sort,
			@Order @RequestParam(defaultValue = "desc") String order) {
		logger.info("【请求开始】个人优惠券列表,请求参数,userId:{},page:{},size:{}", userId, page, size);

		if (userId == null) {
			logger.error("个人优惠券列表失败:用户未登录！！！");
			return ResponseUtil.unlogin();
		}

		List<DtsCouponUser> couponUserList = couponUserService.queryList(userId, null, status, page, size, sort, order);
		List<CouponVo> couponVoList = change(couponUserList);
		long total = PageInfo.of(couponUserList).getTotal();
		//int total = couponService.queryTotal();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("data", couponVoList);
		data.put("count", total);

		logger.info("【请求结束】个人优惠券列表,响应内容:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	/**
	 * 个人可领取优惠券列表
	 *
	 * @param userId
	 * @param status
	 * @param page
	 * @param size
	 * @param sort
	 * @param order
	 * @return
	 */
	@GetMapping("getUserCoupon")
	public Object getUserCoupon(@LoginUser Integer userId) {
		logger.info("【请求开始】个人可领取优惠券列表,请求参数,userId:{}", userId);

		if (userId == null) {
			logger.error("个人可领取优惠券列表:用户未登录！！！");
			return ResponseUtil.unlogin();
		}
		List<DtsCoupon> coupons = couponService.queryAvailableList(userId, 0, 10);

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("couponList", coupons);

		logger.info("【请求结束】个人可领取优惠券列表,响应内容:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	private List<CouponVo> change(List<DtsCouponUser> couponList) {
		List<CouponVo> couponVoList = new ArrayList<>(couponList.size());
		for (DtsCouponUser couponUser : couponList) {
			Integer couponId = couponUser.getCouponId();
			DtsCoupon coupon = couponService.findById(couponId);
			CouponVo couponVo = new CouponVo();
			couponVo.setId(coupon.getId());
			couponVo.setName(coupon.getName());
			couponVo.setDesc(coupon.getDesc());
			couponVo.setTag(coupon.getTag());
			couponVo.setMin(coupon.getMin().toPlainString());
			couponVo.setDiscount(coupon.getDiscount().toPlainString());
			couponVo.setStartTime(couponUser.getStartTime());
			couponVo.setEndTime(couponUser.getEndTime());

			couponVoList.add(couponVo);
		}

		return couponVoList;
	}

	/**
	 * 当前购物车下单商品订单可用优惠券
	 *
	 * @param userId
	 * @param cartId
	 * @param grouponRulesId
	 * @return
	 */
	@GetMapping("selectlist")
	public Object selectlist(@LoginUser Integer userId, Integer cartId, Integer grouponRulesId) {
		logger.info("【请求开始】当前购物车下单商品订单可用优惠券,请求参数,userId:{},cartId:{},grouponRulesId:{}", userId, cartId,
				grouponRulesId);

		if (userId == null) {
			logger.error("当前购物车下单商品订单可用优惠券:用户未登录！！！");
			return ResponseUtil.unlogin();
		}

		// 团购优惠
		BigDecimal grouponPrice = new BigDecimal(0.00);
		DtsGrouponRules grouponRules = grouponRulesService.queryById(grouponRulesId);
		if (grouponRules != null) {
			grouponPrice = grouponRules.getDiscount();
		}

		// 商品价格
		List<DtsCart> checkedGoodsList = null;
		if (cartId == null || cartId.equals(0)) {// 购物车信息未传入，则查用户
			checkedGoodsList = cartService.queryByUidAndChecked(userId);
		} else {
			DtsCart cart = cartService.findById(cartId);
			if (cart == null) {
				return ResponseUtil.badArgumentValue();
			}
			checkedGoodsList = new ArrayList<>(1);
			checkedGoodsList.add(cart);
		}
		BigDecimal checkedGoodsPrice = new BigDecimal(0.00);
		for (DtsCart cart : checkedGoodsList) {
			// 只有当团购规格商品ID符合才进行团购优惠
			if (grouponRules != null && grouponRules.getGoodsId().equals(cart.getGoodsId())) {
				checkedGoodsPrice = checkedGoodsPrice
						.add(cart.getPrice().subtract(grouponPrice).multiply(new BigDecimal(cart.getNumber())));
			} else {
				checkedGoodsPrice = checkedGoodsPrice.add(cart.getPrice().multiply(new BigDecimal(cart.getNumber())));
			}
		}

		// 计算优惠券可用情况
		List<DtsCouponUser> couponUserList = couponUserService.queryAll(userId);
		List<DtsCouponUser> availableCouponUserList = new ArrayList<>(couponUserList.size());
		for (DtsCouponUser couponUser : couponUserList) {
			DtsCoupon coupon = couponVerifyService.checkCoupon(userId, couponUser.getCouponId(), checkedGoodsPrice);
			if (coupon == null) {
				continue;
			}
			availableCouponUserList.add(couponUser);
		}

		List<CouponVo> couponVoList = change(availableCouponUserList);

		logger.info("【请求结束】当前购物车下单商品订单可用优惠券,响应内容:{}", JSONObject.toJSONString(couponVoList));
		return ResponseUtil.ok(couponVoList);
	}

	/**
	 * 优惠券领取
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            请求内容， { couponId: xxx }
	 * @return 操作结果
	 */
	@PostMapping("receive")
	public Object receive(@LoginUser Integer userId, @RequestBody String body) {
		logger.info("【请求开始】优惠券领取,请求参数,userId:{},body:{}", userId, body);

		if (userId == null) {
			logger.error("优惠券领取:用户未登录！！！");
			return ResponseUtil.unlogin();
		}

		Integer couponId = JacksonUtil.parseInteger(body, "couponId");
		if (couponId == null) {
			return ResponseUtil.badArgument();
		}

		DtsCoupon coupon = couponService.findById(couponId);
		if (coupon == null) {
			return ResponseUtil.badArgumentValue();
		}

		// 当前已领取数量和总数量比较
		Integer total = coupon.getTotal();
		Integer totalCoupons = couponUserService.countCoupon(couponId);
		if ((total != 0) && (totalCoupons >= total)) {
			logger.error("优惠券领取出错:{}", WxResponseCode.COUPON_EXCEED_LIMIT.desc());
			return WxResponseUtil.fail(WxResponseCode.COUPON_EXCEED_LIMIT);
		}

		// 当前用户已领取数量和用户限领数量比较
		Integer limit = coupon.getLimit().intValue();
		Integer userCounpons = couponUserService.countUserAndCoupon(userId, couponId);
		if ((limit != 0) && (userCounpons >= limit)) {
			logger.error("优惠券领取出错:{}", WxResponseCode.COUPON_EXCEED_LIMIT.desc());
			return WxResponseUtil.fail(WxResponseCode.COUPON_EXCEED_LIMIT);
		}

		// 优惠券分发类型
		// 例如注册赠券类型的优惠券不能领取
		Short type = coupon.getType();
		if (type.equals(CouponConstant.TYPE_REGISTER) || type.equals(CouponConstant.TYPE_CODE)
				|| !type.equals(CouponConstant.TYPE_COMMON)) {
			logger.error("优惠券领取出错:{}", WxResponseCode.COUPON_CODE_INVALID.desc());
			return WxResponseUtil.fail(WxResponseCode.COUPON_CODE_INVALID);
		}

		// 优惠券状态，已下架或者过期不能领取
		Short status = coupon.getStatus();
		if (status.equals(CouponConstant.STATUS_OUT)) {
			logger.error("优惠券领取出错:{}", WxResponseCode.COUPON_EXCEED_LIMIT.desc());
			return WxResponseUtil.fail(WxResponseCode.COUPON_EXCEED_LIMIT);
		} else if (status.equals(CouponConstant.STATUS_EXPIRED)) {
			logger.error("优惠券领取出错:{}", WxResponseCode.COUPON_EXPIRED.desc());
			return WxResponseUtil.fail(WxResponseCode.COUPON_EXPIRED);
		}

		// 用户领券记录
		DtsCouponUser couponUser = new DtsCouponUser();
		couponUser.setCouponId(couponId);
		couponUser.setUserId(userId);
		Short timeType = coupon.getTimeType();
		if (timeType.equals(CouponConstant.TIME_TYPE_TIME)) {
			couponUser.setStartTime(coupon.getStartTime());
			couponUser.setEndTime(coupon.getEndTime());
		} else {
			LocalDate now = LocalDate.now();
			couponUser.setStartTime(now);
			couponUser.setEndTime(now.plusDays(coupon.getDays()));
		}
		couponUserService.add(couponUser);

		logger.info("【请求结束】优惠券领取成功!");
		return ResponseUtil.ok();
	}

	/**
	 * 一键领取优惠券
	 *
	 * @param userId
	 *            用户ID
	 * @return 操作结果
	 */
	@PostMapping("receiveAll")
	public Object receiveAll(@LoginUser Integer userId) {
		logger.info("【请求开始】一键领取优惠券，请求参数：userId:{}", userId);

		if (userId == null) {
			logger.error("一键领取优惠券:用户未登录！！！");
			return ResponseUtil.unlogin();
		}
		List<DtsCoupon> dtsCoupons = couponService.queryAvailableList(userId, 0, 10);
		if (dtsCoupons == null || dtsCoupons.size() == 0) {
			// return ResponseUtil.badArgument();
			return ResponseUtil.ok();// 没有可领取的优惠券也暂时不向前端报错
		}
		for (DtsCoupon dtsCoupon : dtsCoupons) {
			Integer couponId = dtsCoupon.getId();
			DtsCoupon coupon = couponService.findById(couponId);
			if (coupon == null) {
				continue;
			}
			// 当前用户已领取数量和用户限领数量比较
			Integer limit = coupon.getLimit().intValue();
			Integer userCounpons = couponUserService.countUserAndCoupon(userId, couponId);
			if ((limit != 0) && (userCounpons >= limit)) {// 用户已经领了此优惠券
				continue;
			}
			// 优惠券分发类型 例如兑换券类型的优惠券不能领取
			Short type = coupon.getType();
			if (type.equals(CouponConstant.TYPE_CODE)) {// 只能兑换
				continue;
			}
			// 优惠券状态，已下架或者过期不能领取
			Short status = coupon.getStatus();
			if (status.equals(CouponConstant.STATUS_OUT) || status.equals(CouponConstant.STATUS_EXPIRED)) {// 优惠券已领完 或
																											// 优惠券已经过期
				continue;
			}
			// 用户领券记录
			DtsCouponUser couponUser = new DtsCouponUser();
			couponUser.setCouponId(couponId);
			couponUser.setUserId(userId);
			Short timeType = coupon.getTimeType();
			if (timeType.equals(CouponConstant.TIME_TYPE_TIME)) {
				couponUser.setStartTime(coupon.getStartTime());
				couponUser.setEndTime(coupon.getEndTime());
			} else {
				LocalDate now = LocalDate.now();
				couponUser.setStartTime(now);
				couponUser.setEndTime(now.plusDays(coupon.getDays()));
			}
			couponUserService.add(couponUser);
		}

		logger.info("【请求结束】一键领取优惠券成功!");
		return ResponseUtil.ok();
	}

	/**
	 * 优惠券兑换
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            请求内容， { code: xxx }
	 * @return 操作结果
	 */
	@PostMapping("exchange")
	public Object exchange(@LoginUser Integer userId, @RequestBody String body) {
		logger.info("【请求开始】优惠券兑换，请求参数：userId:{},Body:{}", userId, body);

		if (userId == null) {
			logger.error("优惠券兑换:用户未登录！！！");
			return ResponseUtil.unlogin();
		}

		String code = JacksonUtil.parseString(body, "code");
		if (code == null) {
			return ResponseUtil.badArgument();
		}

		DtsCoupon coupon = couponService.findByCode(code);
		if (coupon == null) {
			logger.error("优惠券兑换出错:{}", WxResponseCode.COUPON_CODE_INVALID.desc());
			return WxResponseUtil.fail(WxResponseCode.COUPON_CODE_INVALID);
		}
		Integer couponId = coupon.getId();

		// 当前已领取数量和总数量比较
		Integer total = coupon.getTotal();
		Integer totalCoupons = couponUserService.countCoupon(couponId);
		if ((total != 0) && (totalCoupons >= total)) {
			logger.error("优惠券兑换出错:{}", WxResponseCode.COUPON_EXCEED_LIMIT.desc());
			return WxResponseUtil.fail(WxResponseCode.COUPON_EXCEED_LIMIT);
		}

		// 当前用户已领取数量和用户限领数量比较
		Integer limit = coupon.getLimit().intValue();
		Integer userCounpons = couponUserService.countUserAndCoupon(userId, couponId);
		if ((limit != 0) && (userCounpons >= limit)) {
			logger.error("优惠券兑换出错:{}", WxResponseCode.COUPON_EXCEED_LIMIT.desc());
			return WxResponseUtil.fail(WxResponseCode.COUPON_EXCEED_LIMIT);
		}

		// 优惠券分发类型
		// 例如注册赠券类型的优惠券不能领取
		Short type = coupon.getType();
		if (type.equals(CouponConstant.TYPE_REGISTER) || type.equals(CouponConstant.TYPE_COMMON)
				|| !type.equals(CouponConstant.TYPE_CODE)) {
			logger.error("优惠券兑换出错:{}", WxResponseCode.COUPON_NOT_CHANGE.desc());
			return WxResponseUtil.fail(WxResponseCode.COUPON_NOT_CHANGE);
		}

		// 优惠券状态，已下架或者过期不能领取
		Short status = coupon.getStatus();
		if (status.equals(CouponConstant.STATUS_OUT)) {
			logger.error("优惠券兑换出错:{}", WxResponseCode.COUPON_EXCEED_LIMIT.desc());
			return WxResponseUtil.fail(WxResponseCode.COUPON_EXCEED_LIMIT);
		} else if (status.equals(CouponConstant.STATUS_EXPIRED)) {
			logger.error("优惠券兑换出错:{}", WxResponseCode.COUPON_EXPIRED.desc());
			return WxResponseUtil.fail(WxResponseCode.COUPON_EXPIRED);
		}

		// 用户领券记录
		DtsCouponUser couponUser = new DtsCouponUser();
		couponUser.setCouponId(couponId);
		couponUser.setUserId(userId);
		Short timeType = coupon.getTimeType();
		if (timeType.equals(CouponConstant.TIME_TYPE_TIME)) {
			couponUser.setStartTime(coupon.getStartTime());
			couponUser.setEndTime(coupon.getEndTime());
		} else {
			LocalDate now = LocalDate.now();
			couponUser.setStartTime(now);
			couponUser.setEndTime(now.plusDays(coupon.getDays()));
		}
		couponUserService.add(couponUser);

		logger.info("【请求结束】优惠券兑换成功!");
		return ResponseUtil.ok();
	}

}