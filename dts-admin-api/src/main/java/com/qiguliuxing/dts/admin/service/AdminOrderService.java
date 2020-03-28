package com.qiguliuxing.dts.admin.service;

import static com.qiguliuxing.dts.admin.util.AdminResponseCode.ORDER_CONFIRM_NOT_ALLOWED;
import static com.qiguliuxing.dts.admin.util.AdminResponseCode.ORDER_REFUND_FAILED;
import static com.qiguliuxing.dts.admin.util.AdminResponseCode.ORDER_REPLY_EXIST;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.pagehelper.PageInfo;
import com.qiguliuxing.dts.admin.util.AdminResponseUtil;
import com.qiguliuxing.dts.core.notify.NotifyService;
import com.qiguliuxing.dts.core.notify.NotifyType;
import com.qiguliuxing.dts.core.util.JacksonUtil;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.domain.DtsComment;
import com.qiguliuxing.dts.db.domain.DtsOrder;
import com.qiguliuxing.dts.db.domain.DtsOrderGoods;
import com.qiguliuxing.dts.db.domain.UserVo;
import com.qiguliuxing.dts.db.service.DtsCommentService;
import com.qiguliuxing.dts.db.service.DtsGoodsProductService;
import com.qiguliuxing.dts.db.service.DtsOrderGoodsService;
import com.qiguliuxing.dts.db.service.DtsOrderService;
import com.qiguliuxing.dts.db.service.DtsUserService;
import com.qiguliuxing.dts.db.util.OrderUtil;

@Service
public class AdminOrderService {
	private static final Logger logger = LoggerFactory.getLogger(AdminOrderService.class);

	@Autowired
	private DtsOrderGoodsService orderGoodsService;
	@Autowired
	private DtsOrderService orderService;
	@Autowired
	private DtsGoodsProductService productService;
	@Autowired
	private DtsUserService userService;
	@Autowired
	private DtsCommentService commentService;
	/*
	 * @Autowired private WxPayService wxPayService;
	 */
	@Autowired
	private NotifyService notifyService;

	public Object list(Integer userId, String orderSn, List<Short> orderStatusArray, Integer page, Integer limit,
			String sort, String order) {
		List<DtsOrder> orderList = orderService.querySelective(userId, orderSn, orderStatusArray, page, limit, sort,
				order);
		long total = PageInfo.of(orderList).getTotal();

		Map<String, Object> data = new HashMap<>();
		data.put("total", total);
		data.put("items", orderList);

		logger.info("【请求结束】商场管理->订单管理->查询,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	public Object detail(Integer id) {
		DtsOrder order = orderService.findById(id);
		List<DtsOrderGoods> orderGoods = orderGoodsService.queryByOid(id);
		UserVo user = userService.findUserVoById(order.getUserId());
		Map<String, Object> data = new HashMap<>();
		data.put("order", order);
		data.put("orderGoods", orderGoods);
		data.put("user", user);

		logger.info("【请求结束】商场管理->订单管理->详情,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	/**
	 * 订单退款
	 * <p>
	 * 1. 检测当前订单是否能够退款; 2. 微信退款操作; 3. 设置订单退款确认状态； 4. 订单商品库存回库。
	 * <p>
	 * TODO 虽然接入了微信退款API，但是从安全角度考虑，建议开发者删除这里微信退款代码，采用以下两步走步骤： 1.
	 * 管理员登录微信官方支付平台点击退款操作进行退款 2. 管理员登录Dts管理后台点击退款操作进行订单状态修改和商品库存回库
	 *
	 * @param body 订单信息，{ orderId：xxx }
	 * @return 订单退款操作结果
	 */
	@Transactional
	public Object refund(String body) {
		Integer orderId = JacksonUtil.parseInteger(body, "orderId");
		String refundMoney = JacksonUtil.parseString(body, "refundMoney");
		if (orderId == null) {
			return ResponseUtil.badArgument();
		}
		if (StringUtils.isEmpty(refundMoney)) {
			return ResponseUtil.badArgument();
		}

		DtsOrder order = orderService.findById(orderId);
		if (order == null) {
			return ResponseUtil.badArgument();
		}

		if (order.getActualPrice().compareTo(new BigDecimal(refundMoney)) != 0) {
			return ResponseUtil.badArgumentValue();
		}

		// 如果订单不是退款状态，则不能退款
		if (!order.getOrderStatus().equals(OrderUtil.STATUS_REFUND)) {
			logger.info("商场管理->订单管理->订单退款失败:{}", ORDER_REFUND_FAILED.desc());
			return AdminResponseUtil.fail(ORDER_REFUND_FAILED);
		}

		// 微信退款
		WxPayRefundRequest wxPayRefundRequest = new WxPayRefundRequest();
		wxPayRefundRequest.setOutTradeNo(order.getOrderSn());
		wxPayRefundRequest.setOutRefundNo("refund_" + order.getOrderSn());
		// 元转成分
		Integer totalFee = order.getActualPrice().multiply(new BigDecimal(100)).intValue();
		wxPayRefundRequest.setTotalFee(totalFee);
		wxPayRefundRequest.setRefundFee(totalFee);

		/**
		 * 为了账号安全，暂时屏蔽api退款 WxPayRefundResult wxPayRefundResult = null; try {
		 * wxPayRefundResult = wxPayService.refund(wxPayRefundRequest); } catch
		 * (WxPayException e) { e.printStackTrace(); return
		 * ResponseUtil.fail(ORDER_REFUND_FAILED, "订单退款失败"); } if
		 * (!wxPayRefundResult.getReturnCode().equals("SUCCESS")) { logger.warn("refund
		 * fail: " + wxPayRefundResult.getReturnMsg()); return
		 * ResponseUtil.fail(ORDER_REFUND_FAILED, "订单退款失败"); } if
		 * (!wxPayRefundResult.getResultCode().equals("SUCCESS")) { logger.warn("refund
		 * fail: " + wxPayRefundResult.getReturnMsg()); return
		 * ResponseUtil.fail(ORDER_REFUND_FAILED, "订单退款失败"); }
		 */

		// 设置订单取消状态
		order.setOrderStatus(OrderUtil.STATUS_REFUND_CONFIRM);
		if (orderService.updateWithOptimisticLocker(order) == 0) {
			logger.info("商场管理->订单管理->订单退款失败:{}", "更新数据已失效");
			throw new RuntimeException("更新数据已失效");
		}

		// 商品货品数量增加
		List<DtsOrderGoods> orderGoodsList = orderGoodsService.queryByOid(orderId);
		for (DtsOrderGoods orderGoods : orderGoodsList) {
			Integer productId = orderGoods.getProductId();
			Short number = orderGoods.getNumber();
			if (productService.addStock(productId, number) == 0) {
				logger.info("商场管理->订单管理->订单退款失败:{}", "商品货品库存增加失败");
				throw new RuntimeException("商品货品库存增加失败");
			}
		}

		// TODO 发送邮件和短信通知，这里采用异步发送
		// 退款成功通知用户, 例如“您申请的订单退款 [ 单号:{1} ] 已成功，请耐心等待到账。”
		// 注意订单号只发后6位
		notifyService.notifySmsTemplate(order.getMobile(), NotifyType.REFUND,
				new String[] { order.getOrderSn().substring(8, 14) });

		logger.info("【请求结束】商场管理->订单管理->订单退款,响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

	/**
	 * 发货 1. 检测当前订单是否能够发货 2. 设置订单发货状态
	 *
	 * @param body 订单信息，{ orderId：xxx, shipSn: xxx, shipChannel: xxx }
	 * @return 订单操作结果 成功则 { errno: 0, errmsg: '成功' } 失败则 { errno: XXX, errmsg: XXX }
	 */
	public Object ship(String body) {
		Integer orderId = JacksonUtil.parseInteger(body, "orderId");
		String shipSn = JacksonUtil.parseString(body, "shipSn");
		String shipChannel = JacksonUtil.parseString(body, "shipChannel");
		if (orderId == null || shipSn == null || shipChannel == null) {
			return ResponseUtil.badArgument();
		}

		DtsOrder order = orderService.findById(orderId);
		if (order == null) {
			return ResponseUtil.badArgument();
		}

		// 如果订单不是已付款状态，则不能发货
		if (!order.getOrderStatus().equals(OrderUtil.STATUS_PAY)) {
			logger.info("商场管理->订单管理->订单发货失败:{}", ORDER_CONFIRM_NOT_ALLOWED.desc());
			return AdminResponseUtil.fail(ORDER_CONFIRM_NOT_ALLOWED);
		}

		order.setOrderStatus(OrderUtil.STATUS_SHIP);
		order.setShipSn(shipSn);
		order.setShipChannel(shipChannel);
		order.setShipTime(LocalDateTime.now());
		if (orderService.updateWithOptimisticLocker(order) == 0) {
			logger.info("商场管理->订单管理->订单发货失败:{}", "更新数据失败!");
			return ResponseUtil.updatedDateExpired();
		}

		// TODO 发送邮件和短信通知，这里采用异步发送
		// 发货会发送通知短信给用户: *
		// "您的订单已经发货，快递公司 {1}，快递单 {2} ，请注意查收"
		notifyService.notifySmsTemplate(order.getMobile(), NotifyType.SHIP, new String[] { shipChannel, shipSn });

		logger.info("【请求结束】商场管理->订单管理->订单发货,响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

	/**
	 * 回复订单商品
	 *
	 * @param body 订单信息，{ orderId：xxx }
	 * @return 订单操作结果 成功则 { errno: 0, errmsg: '成功' } 失败则 { errno: XXX, errmsg: XXX }
	 */
	public Object reply(String body) {
		Integer commentId = JacksonUtil.parseInteger(body, "commentId");
		if (commentId == null || commentId == 0) {
			return ResponseUtil.badArgument();
		}
		// 目前只支持回复一次
		if (commentService.findById(commentId) != null) {
			logger.info("商场管理->订单管理->订单商品回复:{}", ORDER_REPLY_EXIST.desc());
			return AdminResponseUtil.fail(ORDER_REPLY_EXIST);
		}
		String content = JacksonUtil.parseString(body, "content");
		if (StringUtils.isEmpty(content)) {
			return ResponseUtil.badArgument();
		}
		// 创建评价回复
		DtsComment comment = new DtsComment();
		comment.setType((byte) 2);
		comment.setValueId(commentId);
		comment.setContent(content);
		comment.setUserId(0); // 评价回复没有用
		comment.setStar((short) 0); // 评价回复没有用
		comment.setHasPicture(false); // 评价回复没有用
		comment.setPicUrls(new String[] {}); // 评价回复没有用
		commentService.save(comment);

		logger.info("【请求结束】商场管理->订单管理->订单商品回复,响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

}
