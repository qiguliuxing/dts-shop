package com.qiguliuxing.dts.wx.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

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

import com.qiguliuxing.dts.wx.annotation.LoginUser;
import com.qiguliuxing.dts.wx.service.WxOrderService;

@RestController
@RequestMapping("/wx/order")
@Validated
public class WxOrderController {
	private static final Logger logger = LoggerFactory.getLogger(WxOrderController.class);

	@Autowired
	private WxOrderService wxOrderService;

	/**
	 * 订单列表
	 *
	 * @param userId
	 *            用户ID
	 * @param showType
	 *            订单信息
	 * @param page
	 *            分页页数
	 * @param size
	 *            分页大小
	 * @return 订单列表
	 */
	@GetMapping("list")
	public Object list(@LoginUser Integer userId, @RequestParam(defaultValue = "0") Integer showType,
			@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size) {
		logger.info("【请求开始】订单列表,请求参数,userId:{},showType:{},page:{}", userId, showType, page);
		return wxOrderService.list(userId, showType, page, size);
	}

	/**
	 * 订单详情
	 *
	 * @param userId
	 *            用户ID
	 * @param orderId
	 *            订单ID
	 * @return 订单详情
	 */
	@GetMapping("detail")
	public Object detail(@LoginUser Integer userId, @NotNull Integer orderId) {
		logger.info("【请求开始】查库订单详情,请求参数,userId:{},orderId:{}", userId, orderId);
		return wxOrderService.detail(userId, orderId);
	}

	/**
	 * 物流跟踪
	 * 
	 * @param userId
	 * @param orderId
	 * @return
	 */
	@GetMapping("expressTrace")
	public Object expressTrace(@LoginUser Integer userId, @NotNull Integer orderId) {
		logger.info("【请求开始】查库订单物流跟踪,请求参数,userId:{},orderId:{}", userId, orderId);
		return wxOrderService.expressTrace(userId, orderId);
	}

	/**
	 * 提交订单
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            订单信息，{ cartId：xxx, addressId: xxx, couponId: xxx, message: xxx,
	 *            grouponRulesId: xxx, grouponLinkId: xxx}
	 * @return 提交订单操作结果
	 */
	@PostMapping("submit")
	public Object submit(@LoginUser Integer userId, @RequestBody String body) {
		logger.info("【请求开始】提交用户订单,请求参数,userId:{},body:{}", userId, body);
		return wxOrderService.submit(userId, body);
	}

	/**
	 * 取消订单
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            订单信息，{ orderId：xxx }
	 * @return 取消订单操作结果
	 */
	@PostMapping("cancel")
	public Object cancel(@LoginUser Integer userId, @RequestBody String body) {
		logger.info("【请求开始】取消用户订单,请求参数,userId:{},body:{}", userId, body);
		return wxOrderService.cancel(userId, body);
	}

	/**
	 * 付款订单的预支付会话标识
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            订单信息，{ orderId：xxx }
	 * @return 支付订单ID
	 */
	@PostMapping("prepay")
	public Object prepay(@LoginUser Integer userId, @RequestBody String body, HttpServletRequest request) {
		logger.info("【请求开始】付款订单的预支付会话标识,请求参数,userId:{},body:{}", userId, body);
		return wxOrderService.prepay(userId, body, request);
	}

	/**
	 * 微信付款成功或失败回调接口
	 * <p>
	 * TODO 注意，这里pay-notify是示例地址，建议开发者应该设立一个隐蔽的回调地址
	 *
	 * @param request
	 *            请求内容
	 * @param response
	 *            响应内容
	 * @return 操作结果
	 */
	@PostMapping("dtsNotify")
	public Object payNotify(HttpServletRequest request, HttpServletResponse response) {
		logger.info("【请求开始】微信付款成功或失败回调...");
		return wxOrderService.dtsPayNotify(request, response);
	}

	/**
	 * 订单申请退款
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            订单信息，{ orderId：xxx }
	 * @return 订单退款操作结果
	 */
	@PostMapping("refund")
	public Object refund(@LoginUser Integer userId, @RequestBody String body) {
		logger.info("【请求开始】订单申请退款,请求参数,userId:{},body:{}", userId, body);
		return wxOrderService.refund(userId, body);
	}

	/**
	 * 确认收货
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            订单信息，{ orderId：xxx }
	 * @return 订单操作结果
	 */
	@PostMapping("confirm")
	public Object confirm(@LoginUser Integer userId, @RequestBody String body) {
		logger.info("【请求开始】用户确认收货,请求参数,userId:{},body:{}", userId, body);
		return wxOrderService.confirm(userId, body);
	}

	/**
	 * 删除订单
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            订单信息，{ orderId：xxx }
	 * @return 订单操作结果
	 */
	@PostMapping("delete")
	public Object delete(@LoginUser Integer userId, @RequestBody String body) {
		logger.info("【请求开始】用户删除订单,请求参数,userId:{},body:{}", userId, body);
		return wxOrderService.delete(userId, body);
	}

	/**
	 * 待评价订单商品信息
	 *
	 * @param userId
	 *            用户ID
	 * @param orderId
	 *            订单ID
	 * @param goodsId
	 *            商品ID
	 * @return 待评价订单商品信息
	 */
	@GetMapping("goods")
	public Object goods(@LoginUser Integer userId, @NotNull Integer orderId, @NotNull Integer goodsId) {
		logger.info("【请求开始】获取待评价订单商品信息,请求参数,userId:{},orderId:{},goodsId:{}", userId, orderId, goodsId);
		return wxOrderService.goods(userId, orderId, goodsId);
	}

	/**
	 * 评价订单商品
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            订单信息，{ orderId：xxx }
	 * @return 订单操作结果
	 */
	@PostMapping("comment")
	public Object comment(@LoginUser Integer userId, @RequestBody String body) {
		logger.info("【请求开始】评价订单商品,请求参数,userId:{},body:{}", userId, body);
		return wxOrderService.comment(userId, body);
	}

}