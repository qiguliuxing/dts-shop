package com.qiguliuxing.dts.admin.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
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
import com.qiguliuxing.dts.admin.annotation.RequiresPermissionsDesc;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.validator.Order;
import com.qiguliuxing.dts.core.validator.Sort;
import com.qiguliuxing.dts.db.domain.DtsGoods;
import com.qiguliuxing.dts.db.domain.DtsGroupon;
import com.qiguliuxing.dts.db.domain.DtsGrouponRules;
import com.qiguliuxing.dts.db.service.DtsGoodsService;
import com.qiguliuxing.dts.db.service.DtsGrouponRulesService;
import com.qiguliuxing.dts.db.service.DtsGrouponService;

@RestController
@RequestMapping("/admin/groupon")
@Validated
public class AdminGrouponController {
	private static final Logger logger = LoggerFactory.getLogger(AdminGrouponController.class);

	@Autowired
	private DtsGrouponRulesService rulesService;
	@Autowired
	private DtsGoodsService goodsService;
	@Autowired
	private DtsGrouponService grouponService;

	@RequiresPermissions("admin:groupon:read")
	@RequiresPermissionsDesc(menu = { "推广管理", "团购管理" }, button = "详情")
	@GetMapping("/listRecord")
	public Object listRecord(String grouponId, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer limit,
			@Sort @RequestParam(defaultValue = "add_time") String sort,
			@Order @RequestParam(defaultValue = "desc") String order) {
		logger.info("【请求开始】推广管理->团购管理->详情,请求参数:grouponId:{},page:{}", grouponId, page);

		List<DtsGroupon> grouponList = grouponService.querySelective(grouponId, page, limit, sort, order);
		long total = PageInfo.of(grouponList).getTotal();

		List<Map<String, Object>> records = new ArrayList<>();
		for (DtsGroupon groupon : grouponList) {
			try {
				Map<String, Object> RecordData = new HashMap<>();
				List<DtsGroupon> subGrouponList = grouponService.queryJoinRecord(groupon.getId());
				DtsGrouponRules rules = rulesService.queryById(groupon.getRulesId());
				DtsGoods goods = goodsService.findById(rules.getGoodsId());

				RecordData.put("groupon", groupon);
				RecordData.put("subGroupons", subGrouponList);
				RecordData.put("rules", rules);
				RecordData.put("goods", goods);

				records.add(RecordData);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Map<String, Object> data = new HashMap<>();
		data.put("total", total);
		data.put("items", records);

		logger.info("【请求结束】推广管理->团购管理->详情,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	@RequiresPermissions("admin:groupon:list")
	@RequiresPermissionsDesc(menu = { "推广管理", "团购管理" }, button = "查询")
	@GetMapping("/list")
	public Object list(String goodsId, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer limit,
			@Sort @RequestParam(defaultValue = "add_time") String sort,
			@Order @RequestParam(defaultValue = "desc") String order) {
		logger.info("【请求开始】推广管理->团购管理->查询,请求参数:goodsId:{},page:{}", goodsId, page);

		List<DtsGrouponRules> rulesList = rulesService.querySelective(goodsId, page, limit, sort, order);
		long total = PageInfo.of(rulesList).getTotal();
		Map<String, Object> data = new HashMap<>();
		data.put("total", total);
		data.put("items", rulesList);

		logger.info("【请求结束】推广管理->团购管理->查询,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	private Object validate(DtsGrouponRules grouponRules) {
		Integer goodsId = grouponRules.getGoodsId();
		if (goodsId == null) {
			return ResponseUtil.badArgument();
		}
		BigDecimal discount = grouponRules.getDiscount();
		if (discount == null) {
			return ResponseUtil.badArgument();
		}
		Integer discountMember = grouponRules.getDiscountMember();
		if (discountMember == null) {
			return ResponseUtil.badArgument();
		}
		LocalDateTime expireTime = grouponRules.getExpireTime();
		if (expireTime == null) {
			return ResponseUtil.badArgument();
		}

		return null;
	}

	@RequiresPermissions("admin:groupon:update")
	@RequiresPermissionsDesc(menu = { "推广管理", "团购管理" }, button = "编辑")
	@PostMapping("/update")
	public Object update(@RequestBody DtsGrouponRules grouponRules) {
		logger.info("【请求开始】推广管理->团购管理->编辑,请求参数:{}", JSONObject.toJSONString(grouponRules));

		Object error = validate(grouponRules);
		if (error != null) {
			return error;
		}

		Integer goodsId = grouponRules.getGoodsId();
		DtsGoods goods = goodsService.findById(goodsId);
		if (goods == null) {
			return ResponseUtil.badArgumentValue();
		}

		grouponRules.setGoodsName(goods.getName());
		grouponRules.setPicUrl(goods.getPicUrl());

		if (rulesService.updateById(grouponRules) == 0) {
			logger.info("推广管理->团购管理->编辑 失败：{}", "更新数据出错！");
			return ResponseUtil.updatedDataFailed();
		}

		logger.info("【请求结束】推广管理->团购管理->编辑,响应结果:{}", "成功！");
		return ResponseUtil.ok();
	}

	@RequiresPermissions("admin:groupon:create")
	@RequiresPermissionsDesc(menu = { "推广管理", "团购管理" }, button = "添加")
	@PostMapping("/create")
	public Object create(@RequestBody DtsGrouponRules grouponRules) {
		logger.info("【请求开始】推广管理->团购管理->添加,请求参数:{}", JSONObject.toJSONString(grouponRules));

		Object error = validate(grouponRules);
		if (error != null) {
			return error;
		}

		Integer goodsId = grouponRules.getGoodsId();
		DtsGoods goods = goodsService.findById(goodsId);
		if (goods == null) {
			return ResponseUtil.badArgumentValue();
		}

		grouponRules.setGoodsName(goods.getName());
		grouponRules.setPicUrl(goods.getPicUrl());

		rulesService.createRules(grouponRules);

		logger.info("【请求结束】推广管理->团购管理->添加,响应结果:{}", JSONObject.toJSONString(grouponRules));
		return ResponseUtil.ok(grouponRules);
	}

	@RequiresPermissions("admin:groupon:delete")
	@RequiresPermissionsDesc(menu = { "推广管理", "团购管理" }, button = "删除")
	@PostMapping("/delete")
	public Object delete(@RequestBody DtsGrouponRules grouponRules) {
		logger.info("【请求开始】推广管理->团购管理->删除,请求参数:{}", JSONObject.toJSONString(grouponRules));

		Integer id = grouponRules.getId();
		if (id == null) {
			return ResponseUtil.badArgument();
		}

		rulesService.delete(id);

		logger.info("【请求结束】推广管理->团购管理->删除,响应结果:{}", "成功！");
		return ResponseUtil.ok();
	}
}
