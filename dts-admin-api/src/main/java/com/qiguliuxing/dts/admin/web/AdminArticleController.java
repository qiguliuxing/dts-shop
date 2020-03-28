package com.qiguliuxing.dts.admin.web;

import static com.qiguliuxing.dts.admin.util.AdminResponseCode.ARTICLE_NAME_EXIST;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.qiguliuxing.dts.admin.annotation.RequiresPermissionsDesc;
import com.qiguliuxing.dts.admin.util.AdminResponseUtil;
import com.qiguliuxing.dts.admin.util.ArticleType;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.validator.Order;
import com.qiguliuxing.dts.core.validator.Sort;
import com.qiguliuxing.dts.db.domain.DtsArticle;
import com.qiguliuxing.dts.db.service.DtsArticleService;

/**
 * 公告管理
 */
@RestController
@RequestMapping("/admin/article")
@Validated
public class AdminArticleController {
	private static final Logger logger = LoggerFactory.getLogger(AdminArticleController.class);

	@Autowired
	private DtsArticleService articleService;

	/**
	 * 查询公告列表
	 *
	 * @param goodsSn
	 * @param name
	 * @param page
	 * @param limit
	 * @param sort
	 * @param order
	 * @return
	 */
	@RequiresPermissions("admin:article:list")
	@RequiresPermissionsDesc(menu = { "推广管理", "公告管理" }, button = "查询")
	@GetMapping("/list")
	public Object list(String title, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer limit,
			@Sort @RequestParam(defaultValue = "add_time") String sort,
			@Order @RequestParam(defaultValue = "desc") String order) {
		logger.info("【请求开始】推广管理->公告管理->查询,请求参数:title:{},page:{}", title, page);

		List<DtsArticle> articleList = articleService.querySelective(title, page, limit, sort, order);
		long total = PageInfo.of(articleList).getTotal();
		Map<String, Object> data = new HashMap<>();
		data.put("total", total);
		data.put("items", articleList);

		logger.info("【请求结束】推广管理->公告管理->查询,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
		
	}

	/**
	 * 编辑公告
	 *
	 * @param goodsAllinone
	 * @return
	 */
	@RequiresPermissions("admin:article:update")
	@RequiresPermissionsDesc(menu = { "推广管理", "公告管理" }, button = "编辑")
	@PostMapping("/update")
	public Object update(@RequestBody DtsArticle article) {
		logger.info("【请求开始】推广管理->公告管理->编辑,请求参数:{}", JSONObject.toJSONString(article));
		Object error = validate(article);
		if (error != null) {
			return error;
		}
		if (StringUtils.isEmpty(article.getType())) {
			article.setType(ArticleType.ANNOUNCE.type());//如果没有传入类型，默认为信息公告
		}
		if (articleService.updateById(article) == 0) {
			logger.error("推广管理->公告管理->编辑错误:{}", "更新数据失败");
			throw new RuntimeException("更新数据失败");
		}
		logger.info("【请求结束】推广管理->公告管理->编辑,响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

	/**
	 * 删除商品
	 *
	 * @param goods
	 * @return
	 */
	@RequiresPermissions("admin:article:delete")
	@RequiresPermissionsDesc(menu = { "推广管理", "公告管理" }, button = "删除")
	@PostMapping("/delete")
	public Object delete(@RequestBody DtsArticle article) {
		logger.info("【请求开始】推广管理->公告管理->删除,请求参数:{}", JSONObject.toJSONString(article));
		Integer id = article.getId();
		if (id == null) {
			return ResponseUtil.badArgument();
		}
		
		articleService.deleteById(id);

		logger.info("【请求结束】推广管理->公告管理->删除,响应结果:{}", "成功");
		return ResponseUtil.ok();
	}
	
	
	/**
	 * 文章公告信息
	 *
	 * @param id
	 *            文章ID
	 * @return 文章详情
	 */
	@RequiresPermissions("admin:article:read")
	@RequiresPermissionsDesc(menu = { "推广管理", "公告管理" }, button = "详情")
	@GetMapping("/detail")
	public Object detail(@NotNull Integer id) {
		logger.info("【请求开始】推广管理->公告管理->详情,请求参数,id:{}", id);
		DtsArticle article = null;
		try {
			article = articleService.findById(id);
		} catch (Exception e) {
			logger.error("获取文章公告失败,文章id：{}", id);
			e.printStackTrace();
		}
		// 这里不打印响应结果，文章内容信息较多
		// logger.info("【请求结束】获取公告文章,响应结果：{}",JSONObject.toJSONString(article));
		return ResponseUtil.ok(article);
	}
	
	/**
	 * 添加公告
	 *
	 * @param article
	 * @return
	 */
	@RequiresPermissions("admin:article:create")
	@RequiresPermissionsDesc(menu = { "推广管理", "公告管理" }, button = "发布")
	@PostMapping("/create")
	public Object create(@RequestBody DtsArticle article) {
		logger.info("【请求开始】推广管理->公告管理->发布公告,请求参数:{}", JSONObject.toJSONString(article));

		Object error = validate(article);
		if (error != null) {
			return error;
		}
		
		String title = article.getTitle();
		if (articleService.checkExistByTitle(title)) {
			logger.error("推广管理->公告管理->发布公告错误:{}", ARTICLE_NAME_EXIST.desc());
			return AdminResponseUtil.fail(ARTICLE_NAME_EXIST);
		}
		if (StringUtils.isEmpty(article.getType())) {
			article.setType(ArticleType.ANNOUNCE.type());//如果没有传入类型，默认为信息公告
		}
		articleService.add(article);
		
		logger.info("【请求结束】推广管理->公告管理->发布公告,响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}
	
	private Object validate(DtsArticle article) {
		String title = article.getTitle();
		String content = article.getContent();
		if (StringUtils.isEmpty(title) || StringUtils.isEmpty(content)) {
			return ResponseUtil.badArgument();
		}
		return null;
	}
}
