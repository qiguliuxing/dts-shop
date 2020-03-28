package com.qiguliuxing.dts.wx.web;

import static com.qiguliuxing.dts.wx.util.WxResponseCode.GOODS_NO_STOCK;
import static com.qiguliuxing.dts.wx.util.WxResponseCode.GOODS_UNSHELVE;
import static com.qiguliuxing.dts.wx.util.WxResponseCode.INVALID_COUPON;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.qiguliuxing.dts.core.system.SystemConfig;
import com.qiguliuxing.dts.core.util.JacksonUtil;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.domain.DtsAddress;
import com.qiguliuxing.dts.db.domain.DtsBrand;
import com.qiguliuxing.dts.db.domain.DtsCart;
import com.qiguliuxing.dts.db.domain.DtsCoupon;
import com.qiguliuxing.dts.db.domain.DtsCouponUser;
import com.qiguliuxing.dts.db.domain.DtsGoods;
import com.qiguliuxing.dts.db.domain.DtsGoodsProduct;
import com.qiguliuxing.dts.db.domain.DtsGrouponRules;
import com.qiguliuxing.dts.db.service.CouponVerifyService;
import com.qiguliuxing.dts.db.service.DtsAddressService;
import com.qiguliuxing.dts.db.service.DtsBrandService;
import com.qiguliuxing.dts.db.service.DtsCartService;
import com.qiguliuxing.dts.db.service.DtsCouponUserService;
import com.qiguliuxing.dts.db.service.DtsGoodsProductService;
import com.qiguliuxing.dts.db.service.DtsGoodsService;
import com.qiguliuxing.dts.db.service.DtsGrouponRulesService;
import com.qiguliuxing.dts.wx.annotation.LoginUser;
import com.qiguliuxing.dts.wx.dao.BrandCartGoods;
import com.qiguliuxing.dts.wx.util.WxResponseUtil;

/**
 * 用户购物车服务
 */
@RestController
@RequestMapping("/wx/cart")
@Validated
public class WxCartController {
	private static final Logger logger = LoggerFactory.getLogger(WxCartController.class);

	@Autowired
	private DtsCartService cartService;
	@Autowired
	private DtsGoodsService goodsService;
	@Autowired
	private DtsGoodsProductService productService;
	@Autowired
	private DtsAddressService addressService;
	@Autowired
	private DtsGrouponRulesService grouponRulesService;
	@Autowired
	private DtsCouponUserService couponUserService;
	@Autowired
	private CouponVerifyService couponVerifyService;
	@Autowired
	private DtsBrandService brandService;

	/**
	 * 用户购物车信息
	 *
	 * @param userId
	 *            用户ID
	 * @return 用户购物车信息
	 */
	@GetMapping("index")
	public Object index(@LoginUser Integer userId) {
		logger.info("【请求开始】用户购物车信息列表,请求参数,userId:{}", userId);
		if (userId == null) {
			logger.error("获取用户购物车信息列表失败:用户未登录！！！");
			return ResponseUtil.unlogin();
		}

		List<DtsCart> cartList = cartService.queryByUid(userId);
		Integer goodsCount = 0;
		BigDecimal goodsAmount = new BigDecimal(0.00);
		Integer checkedGoodsCount = 0;
		BigDecimal checkedGoodsAmount = new BigDecimal(0.00);
		for (DtsCart cart : cartList) {
			goodsCount += cart.getNumber();
			goodsAmount = goodsAmount.add(cart.getPrice().multiply(new BigDecimal(cart.getNumber())));
			if (cart.getChecked()) {
				checkedGoodsCount += cart.getNumber();
				checkedGoodsAmount = checkedGoodsAmount.add(cart.getPrice().multiply(new BigDecimal(cart.getNumber())));
			}
		}
		Map<String, Object> cartTotal = new HashMap<>();
		cartTotal.put("goodsCount", goodsCount);
		cartTotal.put("goodsAmount", goodsAmount);
		cartTotal.put("checkedGoodsCount", checkedGoodsCount);
		cartTotal.put("checkedGoodsAmount", checkedGoodsAmount);

		Map<String, Object> result = new HashMap<>();
		result.put("cartTotal", cartTotal);

		if (SystemConfig.isMultiOrderModel()) {// 如果需要拆订单，则需要按店铺显示购物车商品
			result.put("isMultiOrderModel", 1);
			List<BrandCartGoods> brandCartgoodsList = new ArrayList<BrandCartGoods>();
			for (DtsCart cart : cartList) {
				Integer brandId = cart.getBrandId();
				boolean hasExsist = false;
				for (int i = 0; i < brandCartgoodsList.size(); i++) {
					if (brandCartgoodsList.get(i).getBrandId().intValue() == brandId.intValue()) {
						brandCartgoodsList.get(i).getCartList().add(cart);
						hasExsist = true;
						break;
					}
				}
				if (!hasExsist) {// 还尚未加入，则需要查询品牌入驻商铺
					DtsBrand dtsBrand = brandService.findById(brandId);
					BrandCartGoods bandCartGoods = BrandCartGoods.init(dtsBrand);
					List<DtsCart> dtsCartList = new ArrayList<DtsCart>();
					dtsCartList.add(cart);
					bandCartGoods.setCartList(dtsCartList);
					brandCartgoodsList.add(bandCartGoods);
				}
			}
			result.put("brandCartgoods", brandCartgoodsList);
		} else {
			result.put("isMultiOrderModel", 0);
			result.put("cartList", cartList);
		}
		logger.info("【请求结束】用户购物车信息列表,响应结果：{}", JSONObject.toJSONString(result));
		return ResponseUtil.ok(result);
	}

	/**
	 * 加入商品到购物车
	 * <p>
	 * 如果已经存在购物车货品，则增加数量； 否则添加新的购物车货品项。
	 *
	 * @param userId
	 *            用户ID
	 * @param cart
	 *            购物车商品信息， { goodsId: xxx, productId: xxx, number: xxx }
	 * @return 加入购物车操作结果
	 */
	@PostMapping("add")
	public Object add(@LoginUser Integer userId, @RequestBody DtsCart cart) {
		logger.info("【请求开始】加入商品到购物车,请求参数,userId:{},cart:{}", userId, JSONObject.toJSONString(cart));

		if (userId == null) {
			logger.error("加入商品到购物车失败:用户未登录！！！");
			return ResponseUtil.unlogin();
		}
		if (cart == null) {
			return ResponseUtil.badArgument();
		}

		Integer productId = cart.getProductId();
		Integer number = cart.getNumber().intValue();
		Integer goodsId = cart.getGoodsId();
		if (!ObjectUtils.allNotNull(productId, number, goodsId)) {
			return ResponseUtil.badArgument();
		}

		// 判断商品是否可以购买
		DtsGoods goods = goodsService.findById(goodsId);
		if (goods == null || !goods.getIsOnSale()) {
			logger.error("加入商品到购物车失败:{}", GOODS_UNSHELVE.desc());
			return WxResponseUtil.fail(GOODS_UNSHELVE);
		}

		DtsGoodsProduct product = productService.findById(productId);
		// 判断购物车中是否存在此规格商品
		DtsCart existCart = cartService.queryExist(goodsId, productId, userId);
		if (existCart == null) {
			// 取得规格的信息,判断规格库存
			if (product == null || number > product.getNumber()) {
				logger.error("加入商品到购物车失败:{}", GOODS_NO_STOCK.desc());
				return WxResponseUtil.fail(GOODS_NO_STOCK);
			}

			cart.setId(null);
			cart.setGoodsSn(goods.getGoodsSn());
			cart.setBrandId(goods.getBrandId());// 新增入驻商户
			cart.setGoodsName((goods.getName()));
			cart.setPicUrl(goods.getPicUrl());
			cart.setPrice(product.getPrice());
			cart.setSpecifications(product.getSpecifications());
			cart.setUserId(userId);
			cart.setChecked(true);
			cartService.add(cart);
		} else {
			// 取得规格的信息,判断规格库存
			int num = existCart.getNumber() + number;
			if (num > product.getNumber()) {
				logger.error("加入商品到购物车失败:{}", GOODS_NO_STOCK.desc());
				return WxResponseUtil.fail(GOODS_NO_STOCK);
			}
			existCart.setNumber((short) num);
			if (cartService.updateById(existCart) == 0) {
				logger.error("加入商品到购物车失败:更新购物车信息失败!");
				return ResponseUtil.updatedDataFailed();
			}
		}
		logger.info("【请求结束】加入商品到购物车成功!");
		return goodscount(userId);
	}

	/**
	 * 立即购买
	 * <p>
	 * 和add方法的区别在于： 1. 如果购物车内已经存在购物车货品，前者的逻辑是数量添加，这里的逻辑是数量覆盖 2.
	 * 添加成功以后，前者的逻辑是返回当前购物车商品数量，这里的逻辑是返回对应购物车项的ID
	 *
	 * @param userId
	 *            用户ID
	 * @param cart
	 *            购物车商品信息， { goodsId: xxx, productId: xxx, number: xxx }
	 * @return 立即购买操作结果
	 */
	@PostMapping("fastadd")
	public Object fastadd(@LoginUser Integer userId, @RequestBody DtsCart cart) {
		logger.info("【请求开始】立即购买,请求参数,userId:{},cart:{}", userId, JSONObject.toJSONString(cart));

		if (userId == null) {
			logger.error("立即购买:用户未登录！！！");
			return ResponseUtil.unlogin();
		}
		if (cart == null) {
			return ResponseUtil.badArgument();
		}

		Integer productId = cart.getProductId();
		Integer number = cart.getNumber().intValue();
		Integer goodsId = cart.getGoodsId();
		if (!ObjectUtils.allNotNull(productId, number, goodsId)) {
			return ResponseUtil.badArgument();
		}

		// 判断商品是否可以购买
		DtsGoods goods = goodsService.findById(goodsId);
		if (goods == null || !goods.getIsOnSale()) {
			logger.error("立即购买失败:{}", GOODS_UNSHELVE.desc());
			return WxResponseUtil.fail(GOODS_UNSHELVE);
		}

		DtsGoodsProduct product = productService.findById(productId);
		// 判断购物车中是否存在此规格商品
		DtsCart existCart = cartService.queryExist(goodsId, productId, userId);
		if (existCart == null) {
			// 取得规格的信息,判断规格库存
			if (product == null || number > product.getNumber()) {
				logger.error("立即购买失败:{}", GOODS_NO_STOCK.desc());
				return WxResponseUtil.fail(GOODS_NO_STOCK);
			}

			cart.setId(null);
			cart.setGoodsSn(goods.getGoodsSn());
			cart.setBrandId(goods.getBrandId());// 新增入驻商户
			cart.setGoodsName((goods.getName()));
			cart.setPicUrl(goods.getPicUrl());
			cart.setPrice(product.getPrice());
			cart.setSpecifications(product.getSpecifications());
			cart.setUserId(userId);
			cart.setChecked(true);
			cartService.add(cart);
		} else {
			// 取得规格的信息,判断规格库存
			int num = number;
			if (num > product.getNumber()) {
				logger.error("立即购买失败:{}", GOODS_NO_STOCK.desc());
				return WxResponseUtil.fail(GOODS_NO_STOCK);
			}
			existCart.setNumber((short) num);
			if (cartService.updateById(existCart) == 0) {
				logger.error("立即购买失败:更新购物车信息失败!");
				return ResponseUtil.updatedDataFailed();
			}
		}
		logger.info("【请求结束】立即购买成功!");
		return ResponseUtil.ok(existCart != null ? existCart.getId() : cart.getId());
	}

	/**
	 * 修改购物车商品货品数量
	 *
	 * @param userId
	 *            用户ID
	 * @param cart
	 *            购物车商品信息， { id: xxx, goodsId: xxx, productId: xxx, number: xxx }
	 * @return 修改结果
	 */
	@PostMapping("update")
	public Object update(@LoginUser Integer userId, @RequestBody DtsCart cart) {
		logger.info("【请求开始】修改购物车,请求参数,userId:{},cart:{}", userId, JSONObject.toJSONString(cart));

		if (userId == null) {
			logger.error("修改购物车:用户未登录！！！");
			return ResponseUtil.unlogin();
		}
		if (cart == null) {
			return ResponseUtil.badArgument();
		}
		Integer productId = cart.getProductId();
		Integer number = cart.getNumber().intValue();
		Integer goodsId = cart.getGoodsId();
		Integer id = cart.getId();
		if (!ObjectUtils.allNotNull(id, productId, number, goodsId)) {
			return ResponseUtil.badArgument();
		}

		// 判断是否存在该订单
		// 如果不存在，直接返回错误
		DtsCart existCart = cartService.findById(id);
		if (existCart == null) {
			return ResponseUtil.badArgumentValue();
		}

		// 判断goodsId和productId是否与当前cart里的值一致
		if (!existCart.getGoodsId().equals(goodsId)) {
			return ResponseUtil.badArgumentValue();
		}
		if (!existCart.getProductId().equals(productId)) {
			return ResponseUtil.badArgumentValue();
		}

		// 判断商品是否可以购买
		DtsGoods goods = goodsService.findById(goodsId);
		if (goods == null || !goods.getIsOnSale()) {
			logger.error("修改购物车失败:{}", GOODS_UNSHELVE.desc());
			return WxResponseUtil.fail(GOODS_UNSHELVE);
		}

		// 取得规格的信息,判断规格库存
		DtsGoodsProduct product = productService.findById(productId);
		if (product == null || product.getNumber() < number) {
			logger.error("修改购物车失败:{}", GOODS_NO_STOCK.desc());
			return WxResponseUtil.fail(GOODS_NO_STOCK);
		}

		existCart.setNumber(number.shortValue());
		if (cartService.updateById(existCart) == 0) {
			logger.error("修改购物车失败:更新购物车信息失败!");
			return ResponseUtil.updatedDataFailed();
		}
		logger.info("【请求结束】修改购物车成功!");
		return ResponseUtil.ok();
	}

	/**
	 * 购物车商品货品勾选状态
	 * <p>
	 * 如果原来没有勾选，则设置勾选状态；如果商品已经勾选，则设置非勾选状态。
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            购物车商品信息， { productIds: xxx, isChecked: 1/0 }
	 * @return 购物车信息
	 */
	@PostMapping("checked")
	public Object checked(@LoginUser Integer userId, @RequestBody String body) {
		logger.info("【请求开始】勾选购物车商品,请求参数,userId:{},cart:{}", userId, JSONObject.toJSONString(body));

		if (userId == null) {
			logger.error("勾选购物车商品失败:用户未登录！！！");
			return ResponseUtil.unlogin();
		}
		if (body == null) {
			return ResponseUtil.badArgument();
		}

		List<Integer> productIds = JacksonUtil.parseIntegerList(body, "productIds");
		if (productIds == null) {
			return ResponseUtil.badArgument();
		}

		Integer checkValue = JacksonUtil.parseInteger(body, "isChecked");
		if (checkValue == null) {
			return ResponseUtil.badArgument();
		}
		Boolean isChecked = (checkValue == 1);
		try {
			cartService.updateCheck(userId, productIds, isChecked);
		} catch (Exception e) {
			logger.error("勾选购物车商品失败:更新购物车商品的勾选状态失败！");
			e.printStackTrace();
		}

		logger.info("【请求结束】勾选购物车商品成功!");
		return index(userId);
	}

	/**
	 * 购物车商品删除
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            购物车商品信息， { productIds: xxx }
	 * @return 购物车信息 成功则 { errno: 0, errmsg: '成功', data: xxx } 失败则 { errno: XXX,
	 *         errmsg: XXX }
	 */
	@PostMapping("delete")
	public Object delete(@LoginUser Integer userId, @RequestBody String body) {
		logger.info("【请求开始】购物车商品删除,请求参数,userId:{},cart:{}", userId, JSONObject.toJSONString(body));

		if (userId == null) {
			logger.error("购物车商品删除:用户未登录！！！");
			return ResponseUtil.unlogin();
		}

		if (body == null) {
			return ResponseUtil.badArgument();
		}

		List<Integer> productIds = JacksonUtil.parseIntegerList(body, "productIds");

		if (productIds == null || productIds.size() == 0) {
			return ResponseUtil.badArgument();
		}

		try {
			cartService.delete(productIds, userId);
		} catch (Exception e) {
			logger.error("购物车商品删除失败:操作数据库删除用户商品失败！");
			e.printStackTrace();
		}

		logger.info("【请求结束】购物车商品删除成功:清理的productIds:{}", JSONObject.toJSONString(productIds));
		return index(userId);
	}

	/**
	 * 购物车商品货品数量
	 * <p>
	 * 如果用户没有登录，则返回空数据。
	 *
	 * @param userId
	 *            用户ID
	 * @return 购物车商品货品数量
	 */
	@GetMapping("goodscount")
	public Object goodscount(@LoginUser Integer userId) {
		logger.info("【请求开始】登录用户购物车商品数量,请求参数,userId:{}", userId);

		if (userId == null) {// 如果用户未登录，则直接显示购物商品数量为0
			return ResponseUtil.ok(0);
		}

		int goodsCount = 0;
		try {
			List<DtsCart> cartList = cartService.queryByUid(userId);
			for (DtsCart cart : cartList) {
				goodsCount += cart.getNumber();
			}
		} catch (Exception e) {
			logger.error("获取登录用户购物车商品数量失败！");
			e.printStackTrace();
		}

		logger.info("【请求结束】获取登录用户购物车商品数量成功:商品总数：{}", goodsCount);
		return ResponseUtil.ok(goodsCount);
	}

	/**
	 * 购物车下单
	 *
	 * @param userId
	 *            用户ID
	 * @param cartId
	 *            购物车商品ID： 如果购物车商品ID是空，则下单当前用户所有购物车商品； 如果购物车商品ID非空，则只下单当前购物车商品。
	 * @param addressId
	 *            收货地址ID： 如果收货地址ID是空，则查询当前用户的默认地址。
	 * @param couponId
	 *            优惠券ID： 如果优惠券ID是空，则自动选择合适的优惠券。
	 * @return 购物车操作结果
	 */
	@GetMapping("checkout")
	public Object checkout(@LoginUser Integer userId, Integer cartId, Integer addressId, Integer couponId,
			Integer grouponRulesId) {
		logger.info("【请求开始】用户购物车下单,请求参数,userId:{}", userId);

		if (userId == null) {
			logger.error("用户购物车下单失败:用户未登录！！！");
			return ResponseUtil.unlogin();
		}

		// 收货地址
		DtsAddress checkedAddress = null;
		if (addressId == null || addressId.equals(0)) {
			checkedAddress = addressService.findDefault(userId);
			// 如果仍然没有地址，则是没有收获地址
			// 返回一个空的地址id=0，这样前端则会提醒添加地址
			if (checkedAddress == null) {
				checkedAddress = new DtsAddress();
				checkedAddress.setId(0);
				addressId = 0;
			} else {
				addressId = checkedAddress.getId();
			}

		} else {
			checkedAddress = addressService.findById(addressId);
			// 如果null, 则报错
			if (checkedAddress == null) {
				return ResponseUtil.badArgumentValue();
			}
		}

		// 团购优惠,如果是团购下单，只能单次购买一个商品，因为是从快速下单触发而来的。
		BigDecimal grouponPrice = new BigDecimal(0.00);
		DtsGrouponRules grouponRules = grouponRulesService.queryById(grouponRulesId);
		if (grouponRules != null) {
			grouponPrice = grouponRules.getDiscount();
		}

		// 商品价格
		List<DtsCart> checkedGoodsList = null;
		if (cartId == null || cartId.equals(0)) {// 如果未从购物车发起的下单，则获取用户选好的商品
			checkedGoodsList = cartService.queryByUidAndChecked(userId);
		} else {
			DtsCart cart = cartService.findById(cartId);
			if (cart == null) {
				return ResponseUtil.badArgumentValue();
			}
			checkedGoodsList = new ArrayList<>(1);
			checkedGoodsList.add(cart);
		}

		Map<String, Object> data = new HashMap<>();

		BigDecimal goodsTotalPrice = new BigDecimal(0.00);// 商品总价 （包含团购减免，即减免团购后的商品总价，多店铺需将所有商品相加）
		BigDecimal totalFreightPrice = new BigDecimal(0.00);// 总配送费 （单店铺模式一个，多店铺模式多个配送费的总和）

		// 如果需要拆订单，则按店铺进行拆分,如果不拆订单，则统一呈现
		if (SystemConfig.isMultiOrderModel()) {// 需要拆订单，则需要按店铺显示购物车商品
			// a.按入驻店铺归类checkout商品
			List<BrandCartGoods> brandCartgoodsList = new ArrayList<BrandCartGoods>();
			for (DtsCart cart : checkedGoodsList) {
				Integer brandId = cart.getBrandId();
				boolean hasExsist = false;
				for (int i = 0; i < brandCartgoodsList.size(); i++) {
					if (brandCartgoodsList.get(i).getBrandId().intValue() == brandId.intValue()) {
						brandCartgoodsList.get(i).getCartList().add(cart);
						hasExsist = true;
						break;
					}
				}
				if (!hasExsist) {// 还尚未加入，则需要查询品牌入驻商铺
					DtsBrand dtsBrand = brandService.findById(brandId);
					BrandCartGoods bandCartGoods = BrandCartGoods.init(dtsBrand);
					List<DtsCart> dtsCartList = new ArrayList<DtsCart>();
					dtsCartList.add(cart);
					bandCartGoods.setCartList(dtsCartList);
					brandCartgoodsList.add(bandCartGoods);
				}
			}

			// b.核算每个店铺的各项价格指标
			List<BrandCartGoods> checkBrandGoodsList = new ArrayList<BrandCartGoods>();
			for (BrandCartGoods bcg : brandCartgoodsList) {
				List<DtsCart> bandCarts = bcg.getCartList();
				BigDecimal bandGoodsTotalPrice = new BigDecimal(0.00);
				BigDecimal bandFreightPrice = new BigDecimal(0.00);
				for (DtsCart cart : bandCarts) {// 循环店铺各自的购物商品
					// 只有当团购规格商品ID符合才进行团购优惠
					if (grouponRules != null && grouponRules.getGoodsId().equals(cart.getGoodsId())) {
						bandGoodsTotalPrice = bandGoodsTotalPrice
								.add(cart.getPrice().subtract(grouponPrice).multiply(new BigDecimal(cart.getNumber())));
					} else {
						bandGoodsTotalPrice = bandGoodsTotalPrice
								.add(cart.getPrice().multiply(new BigDecimal(cart.getNumber())));
					}
				}

				// 每个店铺都单独计算运费，满xxx则免运费，否则按配置的邮寄费x元计算；
				if (bandGoodsTotalPrice.compareTo(SystemConfig.getFreightLimit()) < 0) {
					bandFreightPrice = SystemConfig.getFreight();
				}

				goodsTotalPrice = goodsTotalPrice.add(bandGoodsTotalPrice);
				totalFreightPrice = totalFreightPrice.add(bandFreightPrice);

				bcg.setBandGoodsTotalPrice(bandGoodsTotalPrice);
				bcg.setBandFreightPrice(bandFreightPrice);

				checkBrandGoodsList.add(bcg);
			}

			data.put("isMultiOrderModel", 1);
			data.put("goodsTotalPrice", goodsTotalPrice);
			data.put("freightPrice", totalFreightPrice);
			data.put("brandCartgoods", checkBrandGoodsList);
		} else {// 不拆订单，则统一呈现

			for (DtsCart cart : checkedGoodsList) {
				// 只有当团购规格商品ID符合才进行团购优惠
				if (grouponRules != null && grouponRules.getGoodsId().equals(cart.getGoodsId())) {
					goodsTotalPrice = goodsTotalPrice
							.add(cart.getPrice().subtract(grouponPrice).multiply(new BigDecimal(cart.getNumber())));
				} else {
					goodsTotalPrice = goodsTotalPrice.add(cart.getPrice().multiply(new BigDecimal(cart.getNumber())));
				}
			}

			// 根据订单商品总价计算运费，满66则免运费，否则6元；
			if (goodsTotalPrice.compareTo(SystemConfig.getFreightLimit()) < 0) {
				totalFreightPrice = SystemConfig.getFreight();
			}

			data.put("isMultiOrderModel", 0);
			data.put("goodsTotalPrice", goodsTotalPrice);
			data.put("freightPrice", totalFreightPrice);

			data.put("checkedGoodsList", checkedGoodsList);
		}

		// 计算优惠券可用情况
		BigDecimal tmpCouponPrice = new BigDecimal(0.00);
		Integer tmpCouponId = 0;
		int tmpCouponLength = 0;
		List<DtsCouponUser> couponUserList = couponUserService.queryAll(userId);
		for (DtsCouponUser couponUser : couponUserList) {
			DtsCoupon coupon = couponVerifyService.checkCoupon(userId, couponUser.getCouponId(), goodsTotalPrice);
			if (coupon == null) {
				continue;
			}
			tmpCouponLength++;
			if (tmpCouponPrice.compareTo(coupon.getDiscount()) == -1) {
				tmpCouponPrice = coupon.getDiscount();
				tmpCouponId = coupon.getId();
			}
		}
		// 获取优惠券减免金额，优惠券可用数量
		int availableCouponLength = tmpCouponLength;
		BigDecimal couponPrice = new BigDecimal(0);

		/**
		 * 这里存在三种情况: 1. 用户不想使用优惠券，则不处理 2. 用户想自动使用优惠券，则选择合适优惠券 3. 用户已选择优惠券，则测试优惠券是否合适
		 */
		if (couponId == null || couponId.equals(-1)) { // 1. 用户不想使用优惠券，则不处理
			couponId = -1;
		} else if (couponId.equals(0)) { // 2. 用户想自动使用优惠券，则选择合适优惠券
			couponPrice = tmpCouponPrice;
			couponId = tmpCouponId;
		} else { // 3. 用户已选择优惠券，则测试优惠券是否合适 ，购买商品总价（有团购商品需按团购商品后的价格计算）要超出券的最低消费金额
			DtsCoupon coupon = couponVerifyService.checkCoupon(userId, couponId, goodsTotalPrice);
			// 用户选择的优惠券有问题
			if (coupon == null) {
				logger.error("用户购物车下单失败:{}", INVALID_COUPON.desc());
				return WxResponseUtil.fail(INVALID_COUPON);
			}
			couponPrice = coupon.getDiscount();
		}

		// 用户积分减免
		BigDecimal integralPrice = new BigDecimal(0.00);

		BigDecimal orderTotalPrice = goodsTotalPrice.add(totalFreightPrice).subtract(couponPrice);
		BigDecimal actualPrice = orderTotalPrice.subtract(integralPrice);

		// 返回界面的通用数据
		data.put("addressId", addressId);
		data.put("checkedAddress", checkedAddress);
		data.put("couponId", couponId);
		data.put("availableCouponLength", availableCouponLength);
		data.put("grouponRulesId", grouponRulesId);

		data.put("grouponPrice", grouponPrice);// 团购优惠的商品价格（团购商品需减免的优惠金额）
		data.put("couponPrice", couponPrice);// 单店铺，多店铺 一个总订单都只能用一张券

		data.put("orderTotalPrice", orderTotalPrice);// 订单总价：goodsTotalPrice + totalFreightPrice - couponPrice
		data.put("actualPrice", actualPrice);// 订单实际付款金额：orderTotalPrice - integralPrice

		logger.info("【请求结束】用户购物车下单,响应结果：{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}
}