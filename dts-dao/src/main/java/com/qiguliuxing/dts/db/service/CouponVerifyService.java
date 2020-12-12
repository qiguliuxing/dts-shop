package com.qiguliuxing.dts.db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qiguliuxing.dts.db.domain.DtsCoupon;
import com.qiguliuxing.dts.db.domain.DtsCouponUser;
import com.qiguliuxing.dts.db.util.CouponConstant;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CouponVerifyService {

	@Autowired
	private DtsCouponUserService couponUserService;
	@Autowired
	private DtsCouponService couponService;

	/**
	 * 检测优惠券是否适合
	 *
	 * @param userId
	 * @param couponId
	 * @param checkedGoodsPrice
	 * @return
	 */
	public DtsCoupon checkCoupon(Integer userId, Integer couponId, BigDecimal checkedGoodsPrice) {
		DtsCoupon coupon = couponService.findById(couponId);
		DtsCouponUser couponUser = couponUserService.queryOne(userId, couponId);
		if (coupon == null || couponUser == null) {
			return null;
		}

		// 检查是否超期
		Short timeType = coupon.getTimeType();
		Short days = coupon.getDays();
		LocalDate now = LocalDate.now();
		if (timeType.equals(CouponConstant.TIME_TYPE_TIME)) {
			if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
				return null;
			}
		} else if (timeType.equals(CouponConstant.TIME_TYPE_DAYS)) {
			LocalDateTime expired = couponUser.getAddTime().plusDays(days);
			if (LocalDateTime.now().isAfter(expired)) {
				return null;
			}
		} else {
			return null;
		}

		// 检测商品是否符合
		// TODO 目前仅支持全平台商品，所以不需要检测
		Short goodType = coupon.getGoodsType();
		if (!goodType.equals(CouponConstant.GOODS_TYPE_ALL)) {
			return null;
		}

		// 检测订单状态
		Short status = coupon.getStatus();
		if (!status.equals(CouponConstant.STATUS_NORMAL)) {
			return null;
		}
		// 检测是否满足最低消费
		if (checkedGoodsPrice.compareTo(coupon.getMin()) == -1) {
			return null;
		}

		return coupon;
	}

}