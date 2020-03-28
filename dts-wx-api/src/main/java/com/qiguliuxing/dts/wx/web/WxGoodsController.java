package com.qiguliuxing.dts.wx.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.mysql.jdbc.StringUtils;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.validator.Order;
import com.qiguliuxing.dts.core.validator.Sort;
import com.qiguliuxing.dts.db.domain.DtsBrand;
import com.qiguliuxing.dts.db.domain.DtsCategory;
import com.qiguliuxing.dts.db.domain.DtsComment;
import com.qiguliuxing.dts.db.domain.DtsFootprint;
import com.qiguliuxing.dts.db.domain.DtsGoods;
import com.qiguliuxing.dts.db.domain.DtsSearchHistory;
import com.qiguliuxing.dts.db.domain.DtsUser;
import com.qiguliuxing.dts.db.service.DtsBrandService;
import com.qiguliuxing.dts.db.service.DtsCategoryService;
import com.qiguliuxing.dts.db.service.DtsCollectService;
import com.qiguliuxing.dts.db.service.DtsCommentService;
import com.qiguliuxing.dts.db.service.DtsFootprintService;
import com.qiguliuxing.dts.db.service.DtsGoodsAttributeService;
import com.qiguliuxing.dts.db.service.DtsGoodsProductService;
import com.qiguliuxing.dts.db.service.DtsGoodsService;
import com.qiguliuxing.dts.db.service.DtsGoodsSpecificationService;
import com.qiguliuxing.dts.db.service.DtsGrouponRulesService;
import com.qiguliuxing.dts.db.service.DtsIssueService;
import com.qiguliuxing.dts.db.service.DtsSearchHistoryService;
import com.qiguliuxing.dts.db.service.DtsUserService;
import com.qiguliuxing.dts.wx.annotation.LoginUser;

/**
 * 商品服务
 */
@RestController
@RequestMapping("/wx/goods")
@Validated
public class WxGoodsController {
	private static final Logger logger = LoggerFactory.getLogger(WxGoodsController.class);

	@Autowired
	private DtsGoodsService goodsService;

	@Autowired
	private DtsGoodsProductService productService;

	@Autowired
	private DtsIssueService goodsIssueService;

	@Autowired
	private DtsGoodsAttributeService goodsAttributeService;

	@Autowired
	private DtsBrandService brandService;

	@Autowired
	private DtsCommentService commentService;

	@Autowired
	private DtsUserService userService;

	@Autowired
	private DtsCollectService collectService;

	@Autowired
	private DtsFootprintService footprintService;

	@Autowired
	private DtsCategoryService categoryService;

	@Autowired
	private DtsSearchHistoryService searchHistoryService;

	@Autowired
	private DtsGoodsSpecificationService goodsSpecificationService;

	@Autowired
	private DtsGrouponRulesService rulesService;

	private final static ArrayBlockingQueue<Runnable> WORK_QUEUE = new ArrayBlockingQueue<>(9);

	private final static RejectedExecutionHandler HANDLER = new ThreadPoolExecutor.CallerRunsPolicy();

	private static ThreadPoolExecutor executorService = new ThreadPoolExecutor(16, 16, 1000, TimeUnit.MILLISECONDS,
			WORK_QUEUE, HANDLER);

	/**
	 * 商品详情
	 * <p>
	 * 用户可以不登录。 如果用户登录，则记录用户足迹以及返回用户收藏信息。
	 *
	 * @param userId
	 *            用户ID
	 * @param id
	 *            商品ID
	 * @return 商品详情
	 */
	@SuppressWarnings("rawtypes")
	@GetMapping("detail")
	public Object detail(@LoginUser Integer userId, @NotNull Integer id) {
		logger.info("【请求开始】商品详情,请求参数,userId:{},id:{}", userId, id);

		// 商品信息
		DtsGoods info = goodsService.findById(id);

		// 商品属性
		Callable<List> goodsAttributeListCallable = () -> goodsAttributeService.queryByGid(id);

		// 商品规格 返回的是定制的GoodsSpecificationVo
		Callable<Object> objectCallable = () -> goodsSpecificationService.getSpecificationVoList(id);

		// 商品规格对应的数量和价格
		Callable<List> productListCallable = () -> productService.queryByGid(id);

		// 商品问题，这里是一些通用问题
		Callable<List> issueCallable = () -> goodsIssueService.query();

		// 商品品牌商
		Callable<DtsBrand> brandCallable = () -> {
			Integer brandId = info.getBrandId();
			DtsBrand brand;
			if (brandId == 0) {
				brand = new DtsBrand();
			} else {
				brand = brandService.findById(info.getBrandId());
			}
			return brand;
		};

		// 评论
		Callable<Map> commentsCallable = () -> {
			List<DtsComment> comments = commentService.queryGoodsByGid(id, 0, 2);
			List<Map<String, Object>> commentsVo = new ArrayList<>(comments.size());
			long commentCount = PageInfo.of(comments).getTotal();
			for (DtsComment comment : comments) {
				Map<String, Object> c = new HashMap<>();
				c.put("id", comment.getId());
				c.put("addTime", comment.getAddTime());
				c.put("content", comment.getContent());
				DtsUser user = userService.findById(comment.getUserId());
				c.put("nickname", user.getNickname());
				c.put("avatar", user.getAvatar());
				c.put("picList", comment.getPicUrls());
				commentsVo.add(c);
			}
			Map<String, Object> commentList = new HashMap<>();
			commentList.put("count", commentCount);
			commentList.put("data", commentsVo);
			return commentList;
		};

		// 团购信息
		Callable<List> grouponRulesCallable = () -> rulesService.queryByGoodsId(id);

		// 用户收藏
		int userHasCollect = 0;
		if (userId != null) {
			userHasCollect = collectService.count(userId, id);
		}

		// 记录用户的足迹 异步处理
		if (userId != null) {
			executorService.execute(() -> {
				DtsFootprint footprint = new DtsFootprint();
				footprint.setUserId(userId);
				footprint.setGoodsId(id);
				footprintService.add(footprint);
				short num = 1;
				productService.addBrowse(id, num);// 新增商品点击量
			});
		}

		FutureTask<List> goodsAttributeListTask = new FutureTask<>(goodsAttributeListCallable);
		FutureTask<Object> objectCallableTask = new FutureTask<>(objectCallable);
		FutureTask<List> productListCallableTask = new FutureTask<>(productListCallable);
		FutureTask<List> issueCallableTask = new FutureTask<>(issueCallable);
		FutureTask<Map> commentsCallableTsk = new FutureTask<>(commentsCallable);
		FutureTask<DtsBrand> brandCallableTask = new FutureTask<>(brandCallable);
		FutureTask<List> grouponRulesCallableTask = new FutureTask<>(grouponRulesCallable);

		executorService.submit(goodsAttributeListTask);
		executorService.submit(objectCallableTask);
		executorService.submit(productListCallableTask);
		executorService.submit(issueCallableTask);
		executorService.submit(commentsCallableTsk);
		executorService.submit(brandCallableTask);
		executorService.submit(grouponRulesCallableTask);

		Map<String, Object> data = new HashMap<>();

		try {
			data.put("info", info);
			data.put("userHasCollect", userHasCollect);
			data.put("issue", issueCallableTask.get());
			data.put("comment", commentsCallableTsk.get());
			data.put("specificationList", objectCallableTask.get());
			data.put("productList", productListCallableTask.get());
			data.put("attribute", goodsAttributeListTask.get());
			data.put("brand", brandCallableTask.get());
			data.put("groupon", grouponRulesCallableTask.get());
		} catch (Exception e) {
			logger.error("获取商品详情出错:{}", e.getMessage());
			e.printStackTrace();
		}

		// 商品分享图片地址
		data.put("shareImage", info.getShareUrl());

		logger.info("【请求结束】获取商品详情成功!");// 这里不打印返回的信息，因为此接口查询量大，太耗日志空间
		return ResponseUtil.ok(data);
	}

	/**
	 * 商品分类类目
	 *
	 * @param id
	 *            分类类目ID
	 * @return 商品分类类目
	 */
	@GetMapping("category")
	public Object category(@NotNull Integer id) {
		logger.info("【请求开始】商品分类类目,请求参数,id:{}", id);

		DtsCategory cur = categoryService.findById(id);
		DtsCategory parent = null;
		List<DtsCategory> children = null;

		if (cur.getPid() == 0) {
			parent = cur;
			children = categoryService.queryByPid(cur.getId());
			cur = children.size() > 0 ? children.get(0) : cur;
		} else {
			parent = categoryService.findById(cur.getPid());
			children = categoryService.queryByPid(cur.getPid());
		}
		Map<String, Object> data = new HashMap<>();
		data.put("currentCategory", cur);
		data.put("parentCategory", parent);
		data.put("brotherCategory", children);

		logger.info("【请求结束】商品分类类目,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	/**
	 * 根据条件搜素商品
	 * <p>
	 * 1. 这里的前五个参数都是可选的，甚至都是空 2. 用户是可选登录，如果登录，则记录用户的搜索关键字
	 *
	 * @param categoryId
	 *            分类类目ID，可选
	 * @param brandId
	 *            品牌商ID，可选
	 * @param keyword
	 *            关键字，可选
	 * @param isNew
	 *            是否新品，可选
	 * @param isHot
	 *            是否热买，可选
	 * @param userId
	 *            用户ID
	 * @param page
	 *            分页页数
	 * @param size
	 *            分页大小
	 * @param sort
	 *            排序方式，支持"add_time", "retail_price"或"name",浏览量 "browse",销售量："sales"
	 * @param order
	 *            排序类型，顺序或者降序
	 * @return 根据条件搜素的商品详情
	 */
	@GetMapping("list")
	public Object list(Integer categoryId, Integer brandId, String keyword, Boolean isNew, Boolean isHot,
			@LoginUser Integer userId, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer size,
			@Sort(accepts = { "sort_order","add_time", "retail_price", "browse","name",
					"sales" }) @RequestParam(defaultValue = "sort_order") String sort,
			@Order @RequestParam(defaultValue = "asc") String order) {

		logger.info("【请求开始】根据条件搜素商品,请求参数,categoryId:{},brandId:{},keyword:{}", categoryId, brandId, keyword);

		// 添加到搜索历史
		if (userId != null && !StringUtils.isNullOrEmpty(keyword)) {
			DtsSearchHistory searchHistoryVo = new DtsSearchHistory();
			searchHistoryVo.setKeyword(keyword);
			searchHistoryVo.setUserId(userId);
			searchHistoryVo.setFrom("wx");
			searchHistoryService.save(searchHistoryVo);
		}

		// 查询列表数据
		List<DtsGoods> goodsList = goodsService.querySelective(categoryId, brandId, keyword, isHot, isNew, page, size,
				sort, order);
		
		// 查询商品所属类目列表。
		List<Integer> goodsCatIds = goodsService.getCatIds(brandId, keyword, isHot, isNew);
		List<DtsCategory> categoryList = null;
		if (goodsCatIds.size() != 0) {
			categoryList = categoryService.queryL2ByIds(goodsCatIds);
		} else {
			categoryList = new ArrayList<>(0);
		}

		Map<String, Object> data = new HashMap<>();
		data.put("goodsList", goodsList);
		long count = PageInfo.of(goodsList).getTotal();
		int totalPages = (int) Math.ceil((double) count / size);
		data.put("count", PageInfo.of(goodsList).getTotal());
		data.put("filterCategoryList", categoryList);
		data.put("totalPages", totalPages);

		logger.info("【请求结束】根据条件搜素商品,响应结果:查询的商品数量:{},总数：{},总共 {} 页", goodsList.size(),count,totalPages);
		return ResponseUtil.ok(data);
	}

	/**
	 * 商品详情页面“大家都在看”推荐商品
	 *
	 * @param id,
	 *            商品ID
	 * @return 商品详情页面推荐商品
	 */
	@GetMapping("related")
	public Object related(@NotNull Integer id) {
		logger.info("【请求开始】商品详情页面“大家都在看”推荐商品,请求参数,id:{}", id);

		DtsGoods goods = goodsService.findById(id);
		if (goods == null) {
			return ResponseUtil.badArgumentValue();
		}

		// 目前的商品推荐算法仅仅是推荐同类目的其他商品
		int cid = goods.getCategoryId().intValue();
		int brandId = goods.getBrandId().intValue();

		// 查找六个相关商品,同店铺，同类优先
		int limitBid = 10;
		List<DtsGoods> goodsListBrandId = goodsService.queryByBrandId(brandId, cid, 0, limitBid);
		List<DtsGoods> relatedGoods = goodsListBrandId == null ? new ArrayList<DtsGoods>() : goodsListBrandId;
		if (goodsListBrandId == null || goodsListBrandId.size() < 6) {// 同店铺，同类商品小于6件，则获取其他店铺同类商品
			int limitCid = 6;
			List<DtsGoods> goodsListCategory = goodsService.queryByCategoryAndNotSameBrandId(brandId, cid, 0, limitCid);
			relatedGoods.addAll(goodsListCategory);
		}

		Map<String, Object> data = new HashMap<>();
		data.put("goodsList", relatedGoods);

		logger.info("【请求结束】商品详情页面“大家都在看”推荐商品,响应结果:查询的商品数量:{}", relatedGoods.size());
		return ResponseUtil.ok(data);
	}

	/**
	 * 在售的商品总数
	 *
	 * @return 在售的商品总数
	 */
	@GetMapping("count")
	public Object count() {
		logger.info("【请求开始】在售的商品总数...");
		Integer goodsCount = goodsService.queryOnSale();
		Map<String, Object> data = new HashMap<>();
		data.put("goodsCount", goodsCount);

		logger.info("【请求结束】在售的商品总数,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

}