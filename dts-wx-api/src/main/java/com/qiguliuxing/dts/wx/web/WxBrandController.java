package com.qiguliuxing.dts.wx.web;

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
import com.qiguliuxing.dts.db.domain.DtsBrand;
import com.qiguliuxing.dts.db.service.DtsBrandService;

/**
 * 品牌供应商
 */
@RestController
@RequestMapping("/wx/brand")
@Validated
public class WxBrandController {
	private static final Logger logger = LoggerFactory.getLogger(WxBrandController.class);

	@Autowired
	private DtsBrandService brandService;

	/**
	 * 品牌列表
	 *
	 * @param page
	 *            分页页数
	 * @param size
	 *            分页大小
	 * @return 品牌列表
	 */
	@GetMapping("list")
	public Object list(@RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer size) {
		logger.info("【请求开始】品牌列表,请求参数,page:{},size:{}", page, size);
		List<DtsBrand> brandList = brandService.queryVO(page, size);
		int total = brandService.queryTotalCount();
		int totalPages = (int) Math.ceil((double) total / size);

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("brandList", brandList);
		data.put("totalPages", totalPages);

		logger.info("【请求结束】品牌列表,响应结果：{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	/**
	 * 品牌详情
	 *
	 * @param id
	 *            品牌ID
	 * @return 品牌详情
	 */
	@GetMapping("detail")
	public Object detail(@NotNull Integer id) {
		logger.info("【请求开始】品牌详情,请求参数,id:{}", id);
		DtsBrand entity = brandService.findById(id);
		if (entity == null) {
			logger.error("品牌商获取失败,id:{}", id);
			return ResponseUtil.badArgumentValue();
		}

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("brand", entity);

		logger.info("【请求结束】品牌详情,响应结果：{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}
}