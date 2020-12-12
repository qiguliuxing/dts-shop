package com.qiguliuxing.dts.wx.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.domain.DtsKeyword;
import com.qiguliuxing.dts.db.domain.DtsSearchHistory;
import com.qiguliuxing.dts.db.service.DtsKeywordService;
import com.qiguliuxing.dts.db.service.DtsSearchHistoryService;
import com.qiguliuxing.dts.wx.annotation.LoginUser;

/**
 * 商品搜索服务
 * <p>
 * 注意：目前搜索功能非常简单，只是基于关键字匹配。
 */
@RestController
@RequestMapping("/wx/search")
@Validated
public class WxSearchController {
	private static final Logger logger = LoggerFactory.getLogger(WxSearchController.class);

	@Autowired
	private DtsKeywordService keywordsService;
	@Autowired
	private DtsSearchHistoryService searchHistoryService;

	/**
	 * 搜索页面信息
	 * <p>
	 * 如果用户已登录，则给出用户历史搜索记录； 如果没有登录，则给出空历史搜索记录。
	 *
	 * @param userId
	 *            用户ID，可选
	 * @return 搜索页面信息
	 */
	@GetMapping("index")
	public Object index(@LoginUser Integer userId) {
		logger.info("【请求开始】搜索页面信息展示,请求参数,userId:{}", userId);

		// 取出输入框默认的关键词
		DtsKeyword defaultKeyword = keywordsService.queryDefault();

		// 取出热闹关键词
		List<DtsKeyword> hotKeywordList = keywordsService.queryHots();

		List<DtsSearchHistory> historyList = null;
		if (userId != null) {
			// 取出用户历史关键字
			historyList = searchHistoryService.queryByUid(userId);
		} else {
			historyList = new ArrayList<>(0);
		}

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("defaultKeyword", defaultKeyword);
		data.put("historyKeywordList", historyList);
		data.put("hotKeywordList", hotKeywordList);

		logger.info("【请求结束】搜索页面信息展示,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	/**
	 * 关键字提醒
	 * <p>
	 * 当用户输入关键字一部分时，可以推荐系统中合适的关键字。
	 *
	 * @param keyword
	 *            关键字
	 * @return 合适的关键字
	 */
	@GetMapping("helper")
	public Object helper(@NotEmpty String keyword, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer size) {
		logger.info("【请求开始】关键字提醒,请求参数,keyword:{}", keyword);

		List<DtsKeyword> keywordsList = keywordsService.queryByKeyword(keyword, page, size);
		String[] keys = new String[keywordsList.size()];
		int index = 0;
		for (DtsKeyword key : keywordsList) {
			keys[index++] = key.getKeyword();
		}

		logger.info("【请求结束】关键字提醒,响应结果:{}", JSONObject.toJSONString(keys));
		return ResponseUtil.ok(keys);
	}

	/**
	 * 清除用户搜索历史
	 *
	 * @param userId
	 *            用户ID
	 * @return 清理是否成功
	 */
	@PostMapping("clearhistory")
	public Object clearhistory(@LoginUser Integer userId) {
		logger.info("【请求开始】清除用户搜索历史,请求参数,userId:{}", userId);

		if (userId == null) {
			return ResponseUtil.unlogin();
		}
		searchHistoryService.deleteByUid(userId);

		logger.info("【请求结束】清除用户搜索历史,响应结果:{}", "成功");
		return ResponseUtil.ok();
	}
}
