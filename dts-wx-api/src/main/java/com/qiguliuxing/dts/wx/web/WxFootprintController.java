package com.qiguliuxing.dts.wx.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.qiguliuxing.dts.core.util.JacksonUtil;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.domain.DtsFootprint;
import com.qiguliuxing.dts.db.domain.DtsGoods;
import com.qiguliuxing.dts.db.service.DtsFootprintService;
import com.qiguliuxing.dts.db.service.DtsGoodsService;
import com.qiguliuxing.dts.wx.annotation.LoginUser;

/**
 * 用户访问足迹服务
 */
@RestController
@RequestMapping("/wx/footprint")
@Validated
public class WxFootprintController {
	private static final Logger logger = LoggerFactory.getLogger(WxFootprintController.class);

	@Autowired
	private DtsFootprintService footprintService;
	@Autowired
	private DtsGoodsService goodsService;

	/**
	 * 删除用户足迹
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            请求内容， { id: xxx }
	 * @return 删除操作结果
	 */
	@PostMapping("delete")
	public Object delete(@LoginUser Integer userId, @RequestBody String body) {
		logger.info("【请求开始】删除用户足迹,请求参数,userId:{},body:{}", userId, body);

		if (userId == null) {
			logger.error("删除用户足迹:用户未登录！！！");
			return ResponseUtil.unlogin();
		}
		if (body == null) {
			return ResponseUtil.badArgument();
		}

		Integer footprintId = JacksonUtil.parseInteger(body, "id");
		if (footprintId == null) {
			return ResponseUtil.badArgument();
		}
		DtsFootprint footprint = footprintService.findById(footprintId);

		if (footprint == null) {
			return ResponseUtil.badArgumentValue();
		}
		if (!footprint.getUserId().equals(userId)) {
			return ResponseUtil.badArgumentValue();
		}

		footprintService.deleteById(footprintId);

		logger.info("【请求结束】删除用户足迹成功!");
		return ResponseUtil.ok();
	}

	/**
	 * 用户足迹列表
	 *
	 * @param page
	 *            分页页数
	 * @param size
	 *            分页大小
	 * @return 用户足迹列表
	 */
	@GetMapping("list")
	public Object list(@LoginUser Integer userId, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer size) {
		logger.info("【请求开始】用户足迹列表查询,请求参数,userId:{}", userId);

		if (userId == null) {
			logger.error("删除用户足迹:用户未登录！！！");
			return ResponseUtil.unlogin();
		}

		List<DtsFootprint> footprintList = footprintService.queryByAddTime(userId, page, size);
		long count = PageInfo.of(footprintList).getTotal();
		int totalPages = (int) Math.ceil((double) count / size);

		List<Object> footprintVoList = new ArrayList<>(footprintList.size());
		for (DtsFootprint footprint : footprintList) {
			Map<String, Object> c = new HashMap<String, Object>();
			c.put("id", footprint.getId());
			c.put("goodsId", footprint.getGoodsId());
			c.put("addTime", footprint.getAddTime());

			DtsGoods goods = goodsService.findById(footprint.getGoodsId());
			c.put("name", goods.getName());
			c.put("brief", goods.getBrief());
			c.put("picUrl", goods.getPicUrl());
			c.put("retailPrice", goods.getRetailPrice());

			footprintVoList.add(c);
		}

		Map<String, Object> result = new HashMap<>();
		result.put("footprintList", footprintVoList);
		result.put("totalPages", totalPages);

		logger.info("【请求结束】添加意见反馈,响应结果:{}", JSONObject.toJSONString(result));
		return ResponseUtil.ok(result);
	}

}