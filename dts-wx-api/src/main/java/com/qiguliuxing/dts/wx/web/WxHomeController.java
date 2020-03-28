package com.qiguliuxing.dts.wx.web;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.qiguliuxing.dts.core.system.SystemConfig;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.domain.DtsCategory;
import com.qiguliuxing.dts.db.domain.DtsGoods;
import com.qiguliuxing.dts.db.service.DtsAdService;
import com.qiguliuxing.dts.db.service.DtsArticleService;
import com.qiguliuxing.dts.db.service.DtsBrandService;
import com.qiguliuxing.dts.db.service.DtsCategoryService;
import com.qiguliuxing.dts.db.service.DtsCouponService;
import com.qiguliuxing.dts.db.service.DtsGoodsService;
import com.qiguliuxing.dts.db.service.DtsGrouponRulesService;
import com.qiguliuxing.dts.db.service.DtsTopicService;
import com.qiguliuxing.dts.wx.annotation.LoginUser;
import com.qiguliuxing.dts.wx.service.HomeCacheManager;

/**
 * 首页服务
 */
@RestController
@RequestMapping("/wx/home")
@Validated
public class WxHomeController {
	private static final Logger logger = LoggerFactory.getLogger(WxHomeController.class);

	@Autowired
	private DtsAdService adService;

	@Autowired
	private DtsGoodsService goodsService;

	@Autowired
	private DtsBrandService brandService;

	@Autowired
	private DtsTopicService topicService;

	@Autowired
	private DtsCategoryService categoryService;

	@Autowired
	private DtsGrouponRulesService grouponRulesService;

	@Autowired
	private DtsCouponService couponService;

	@Autowired
	private DtsArticleService articleService;

	private final static ArrayBlockingQueue<Runnable> WORK_QUEUE = new ArrayBlockingQueue<>(9);

	private final static RejectedExecutionHandler HANDLER = new ThreadPoolExecutor.CallerRunsPolicy();

	@SuppressWarnings("unused")
	private static ThreadPoolExecutor executorService = new ThreadPoolExecutor(9, 9, 1000, TimeUnit.MILLISECONDS,
			WORK_QUEUE, HANDLER);

	@GetMapping("/cache")
	public Object cache(@NotNull String key) {
		logger.info("【请求开始】缓存已清除,请求参数,key:{}", key);

		if (!key.equals("Dts_cache")) {
			logger.error("缓存已清除出错:非本平台标识！！！");
			return ResponseUtil.fail();
		}

		// 清除缓存
		HomeCacheManager.clearAll();

		logger.info("【请求结束】缓存已清除成功!");
		return ResponseUtil.ok("缓存已清除");
	}

	/**
	 * 首页数据
	 * 
	 * @param userId
	 *            当用户已经登录时，非空。为登录状态为null
	 * @return 首页数据
	 */
	@SuppressWarnings("rawtypes")
	@GetMapping("/index")
	public Object index(@LoginUser Integer userId) {
		logger.info("【请求开始】访问首页,请求参数,userId:{}", userId);

		Map<String, Object> data = new HashMap<String, Object>();
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		// 先查询和用户有关的信息
		Callable<List> couponListCallable = null;
		try {
			if (userId == null) {// 调整，用户未登录，不发送优惠券
				couponListCallable = () -> couponService.queryList(0, 3);
			} else {
				couponListCallable = () -> couponService.queryAvailableList(userId, 0, 3);
			}
			FutureTask<List> couponListTask = new FutureTask<>(couponListCallable);
			executorService.submit(couponListTask);

			// 优先从缓存中读取
			if (HomeCacheManager.hasData(HomeCacheManager.INDEX)) {
				data = HomeCacheManager.getCacheData(HomeCacheManager.INDEX);
				if (data != null) {// 加上这个判断，排除判断后到获取数据之间时间段清理的情况
					LocalDateTime expire = (LocalDateTime) data.get("expireTime");
					logger.info("访问首页,存在缓存数据，除用户优惠券信息外，加载缓存数据,有效期时间点："+ expire.toString());
					data.put("couponList", couponListTask.get());
					return ResponseUtil.ok(data);
				}
			}

			Callable<List> bannerListCallable = () -> adService.queryIndex();

			Callable<List> articleListCallable = () -> articleService.queryList(0, 5, "add_time", "desc");

			Callable<List> channelListCallable = () -> categoryService.queryChannel();

			Callable<List> newGoodsListCallable = () -> goodsService.queryByNew(0, SystemConfig.getNewLimit());

			Callable<List> hotGoodsListCallable = () -> goodsService.queryByHot(0, SystemConfig.getHotLimit());

			Callable<List> brandListCallable = () -> brandService.queryVO(0, SystemConfig.getBrandLimit());

			Callable<List> topicListCallable = () -> topicService.queryList(0, SystemConfig.getTopicLimit());

			// 团购专区
			Callable<List> grouponListCallable = () -> grouponRulesService.queryList(0, 6);

			Callable<List> floorGoodsListCallable = this::getCategoryList;

			FutureTask<List> bannerTask = new FutureTask<>(bannerListCallable);
			FutureTask<List> articleTask = new FutureTask<>(articleListCallable);
			FutureTask<List> channelTask = new FutureTask<>(channelListCallable);
			FutureTask<List> newGoodsListTask = new FutureTask<>(newGoodsListCallable);
			FutureTask<List> hotGoodsListTask = new FutureTask<>(hotGoodsListCallable);
			FutureTask<List> brandListTask = new FutureTask<>(brandListCallable);
			FutureTask<List> topicListTask = new FutureTask<>(topicListCallable);
			FutureTask<List> grouponListTask = new FutureTask<>(grouponListCallable);
			FutureTask<List> floorGoodsListTask = new FutureTask<>(floorGoodsListCallable);

			executorService.submit(bannerTask);
			executorService.submit(articleTask);
			executorService.submit(channelTask);
			executorService.submit(newGoodsListTask);
			executorService.submit(hotGoodsListTask);
			executorService.submit(brandListTask);
			executorService.submit(topicListTask);
			executorService.submit(grouponListTask);
			executorService.submit(floorGoodsListTask);

			data.put("banner", bannerTask.get());
			data.put("articles", articleTask.get());
			data.put("channel", channelTask.get());
			data.put("couponList", couponListTask.get());
			data.put("newGoodsList", newGoodsListTask.get());
			data.put("hotGoodsList", hotGoodsListTask.get());
			data.put("brandList", brandListTask.get());
			data.put("topicList", topicListTask.get());
			data.put("grouponList", grouponListTask.get());
			data.put("floorGoodsList", floorGoodsListTask.get());

			// 缓存数据首页缓存数据
			HomeCacheManager.loadData(HomeCacheManager.INDEX, data);
			executorService.shutdown();

		} catch (Exception e) {
			logger.error("首页信息获取失败：{}", e.getMessage());
			e.printStackTrace();
		}

		// logger.info("【请求结束】访问首页成功!");//暂不打印首页信息
		logger.info("【请求结束】访问首页,响应结果,优惠券信息：{}", JSONObject.toJSONString(data.get("couponList")));
		return ResponseUtil.ok(data);
	}

	@SuppressWarnings("rawtypes")
	private List<Map> getCategoryList() {
		List<Map> categoryList = new ArrayList<>();
		List<DtsCategory> catL1List = categoryService.queryL1WithoutRecommend(0, SystemConfig.getCatlogListLimit());
		for (DtsCategory catL1 : catL1List) {
			List<DtsCategory> catL2List = categoryService.queryByPid(catL1.getId());
			List<Integer> l2List = new ArrayList<>();
			for (DtsCategory catL2 : catL2List) {
				l2List.add(catL2.getId());
			}

			List<DtsGoods> categoryGoods;
			if (l2List.size() == 0) {
				categoryGoods = new ArrayList<>();
			} else {
				categoryGoods = goodsService.queryByCategory(l2List, 0, SystemConfig.getCatlogMoreLimit());
			}

			Map<String, Object> catGoods = new HashMap<>();
			catGoods.put("id", catL1.getId());
			catGoods.put("name", catL1.getName());
			catGoods.put("goodsList", categoryGoods);
			categoryList.add(catGoods);
		}
		return categoryList;
	}
}