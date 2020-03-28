package com.qiguliuxing.dts.wx.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
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
import com.github.pagehelper.PageInfo;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.domain.DtsComment;
import com.qiguliuxing.dts.db.service.DtsCommentService;
import com.qiguliuxing.dts.db.service.DtsGoodsService;
import com.qiguliuxing.dts.db.service.DtsTopicService;
import com.qiguliuxing.dts.wx.annotation.LoginUser;
import com.qiguliuxing.dts.wx.dao.UserInfo;
import com.qiguliuxing.dts.wx.service.UserInfoService;

/**
 * 用户评论服务
 */
@RestController
@RequestMapping("/wx/comment")
@Validated
public class WxCommentController {
	private static final Logger logger = LoggerFactory.getLogger(WxCollectController.class);

	@Autowired
	private DtsCommentService commentService;
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private DtsGoodsService goodsService;
	@Autowired
	private DtsTopicService topicService;

	private Object validate(DtsComment comment) {
		String content = comment.getContent();
		if (StringUtils.isEmpty(content)) {
			return ResponseUtil.badArgument();
		}

		Short star = comment.getStar();
		if (star == null) {
			return ResponseUtil.badArgument();
		}
		if (star < 0 || star > 5) {
			return ResponseUtil.badArgumentValue();
		}

		Byte type = comment.getType();
		Integer valueId = comment.getValueId();
		if (type == null || valueId == null) {
			return ResponseUtil.badArgument();
		}
		if (type == 0) {
			if (goodsService.findById(valueId) == null) {
				return ResponseUtil.badArgumentValue();
			}
		} else if (type == 1) {
			if (topicService.findById(valueId) == null) {
				return ResponseUtil.badArgumentValue();
			}
		} else {
			return ResponseUtil.badArgumentValue();
		}
		Boolean hasPicture = comment.getHasPicture();
		if (hasPicture == null || !hasPicture) {
			comment.setPicUrls(new String[0]);
		}
		return null;
	}

	/**
	 * 发表评论
	 *
	 * @param userId
	 *            用户ID
	 * @param comment
	 *            评论内容
	 * @return 发表评论操作结果
	 */
	@PostMapping("post")
	public Object post(@LoginUser Integer userId, @RequestBody DtsComment comment) {
		logger.info("【请求开始】用户收藏列表查询,请求参数,userId:{},comment:{}", userId, JSONObject.toJSONString(comment));

		if (userId == null) {
			logger.error("用户收藏列表查询失败:用户未登录！！！");
			return ResponseUtil.unlogin();
		}
		Object error = validate(comment);
		if (error != null) {
			return error;
		}
		comment.setUserId(userId);
		try {
			commentService.save(comment);
		} catch (Exception e) {
			logger.error("用户收藏列表查询失败:存储评论数据到库出错！");
			e.printStackTrace();
		}

		logger.info("【请求结束】用户收藏列表查询,响应内容:{}", JSONObject.toJSONString(comment));
		return ResponseUtil.ok(comment);
	}

	/**
	 * 评论数量
	 *
	 * @param type
	 *            类型ID。 如果是0，则查询商品评论；如果是1，则查询专题评论。
	 * @param valueId
	 *            商品或专题ID。如果type是0，则是商品ID；如果type是1，则是专题ID。
	 * @return 评论数量
	 */
	@GetMapping("count")
	public Object count(@NotNull Byte type, @NotNull Integer valueId) {
		logger.info("【请求开始】获取评论数量,请求参数,type:{},valueId:{}", type, valueId);

		int allCount = commentService.count(type, valueId, 0);
		int hasPicCount = commentService.count(type, valueId, 1);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("allCount", allCount);
		data.put("hasPicCount", hasPicCount);

		logger.info("【请求结束】获取评论数量,响应内容:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	/**
	 * 评论列表
	 *
	 * @param type
	 *            类型ID。 如果是0，则查询商品评论；如果是1，则查询专题评论。
	 * @param valueId
	 *            商品或专题ID。如果type是0，则是商品ID；如果type是1，则是专题ID。
	 * @param showType
	 *            显示类型。如果是0，则查询全部；如果是1，则查询有图片的评论。
	 * @param page
	 *            分页页数
	 * @param size
	 *            分页大小
	 * @return 评论列表
	 */
	@GetMapping("list")
	public Object list(@NotNull Byte type, @NotNull Integer valueId, @NotNull Integer showType,
			@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size) {
		logger.info("【请求开始】获取评论列表,请求参数,type:{},showType:{}", type, showType);

		List<DtsComment> commentList = commentService.query(type, valueId, showType, page, size);

		long count = PageInfo.of(commentList).getTotal();
		List<Map<String, Object>> commentVoList = new ArrayList<>(commentList.size());
		for (DtsComment comment : commentList) {
			Map<String, Object> commentVo = new HashMap<>();
			commentVo.put("addTime", comment.getAddTime());
			commentVo.put("content", comment.getContent());
			commentVo.put("picList", comment.getPicUrls());

			UserInfo userInfo = userInfoService.getInfo(comment.getUserId());
			commentVo.put("userInfo", userInfo);

			String reply = commentService.queryReply(comment.getId());
			commentVo.put("reply", reply);

			commentVoList.add(commentVo);
		}

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("data", commentVoList);
		data.put("count", count);
		data.put("currentPage", page);

		logger.info("【请求结束】获取评论列表,响应内容:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}
}