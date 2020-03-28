package com.qiguliuxing.dts.wx.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.validator.Order;
import com.qiguliuxing.dts.core.validator.Sort;
import com.qiguliuxing.dts.db.domain.DtsGoods;
import com.qiguliuxing.dts.db.domain.DtsTopic;
import com.qiguliuxing.dts.db.service.DtsGoodsService;
import com.qiguliuxing.dts.db.service.DtsTopicService;

/**
 * 专题服务
 */
@RestController
@RequestMapping("/wx/topic")
@Validated
public class WxTopicController {
	private static final Logger logger = LoggerFactory.getLogger(WxTopicController.class);

	@Autowired
	private DtsTopicService topicService;
	@Autowired
	private DtsGoodsService goodsService;

	/**
	 * 专题列表
	 *
	 * @param page
	 *            分页页数
	 * @param size
	 *            分页大小
	 * @return 专题列表
	 */
	@GetMapping("list")
	public Object list(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size,
			@Sort @RequestParam(defaultValue = "add_time") String sort,
			@Order @RequestParam(defaultValue = "desc") String order) {
		logger.info("【请求开始】获取专题列表,请求参数,page:{},size:{}", page, size);

		List<DtsTopic> topicList = topicService.queryList(page, size, sort, order);
		int total = topicService.queryTotal();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("data", topicList);
		data.put("count", total);

		logger.info("【请求结束】获取专题列表,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	/**
	 * 专题详情
	 *
	 * @param id
	 *            专题ID
	 * @return 专题详情
	 */
	@GetMapping("detail")
	public Object detail(@NotNull Integer id) {
		logger.info("【请求开始】获取专题详情,请求参数,id:{}", id);

		Map<String, Object> data = new HashMap<>();
		DtsTopic topic = topicService.findById(id);
		data.put("topic", topic);
		List<DtsGoods> goods = new ArrayList<>();
		for (Integer i : topic.getGoods()) {
			DtsGoods good = goodsService.findByIdVO(i);
			if (null != good)
				goods.add(good);
		}
		data.put("goods", goods);

		logger.info("【请求结束】获取专题详情,响应结果:{}", "成功");
		return ResponseUtil.ok(data);
	}

	/**
	 * 相关专题
	 *
	 * @param id
	 *            专题ID
	 * @return 相关专题
	 */
	@GetMapping("related")
	public Object related(@NotNull Integer id) {
		logger.info("【请求开始】相关专题列表,请求参数,id:{}", id);

		List<DtsTopic> topicRelatedList = topicService.queryRelatedList(id, 0, 6);

		logger.info("【请求结束】相关专题列表,响应结果:相关专题数{}", topicRelatedList == null ? 0 : topicRelatedList.size());
		return ResponseUtil.ok(topicRelatedList);
	}
}