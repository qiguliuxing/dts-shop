package com.qiguliuxing.dts.admin.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
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
import com.qiguliuxing.dts.admin.annotation.RequiresPermissionsDesc;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.validator.Order;
import com.qiguliuxing.dts.core.validator.Sort;
import com.qiguliuxing.dts.db.domain.DtsFeedback;
import com.qiguliuxing.dts.db.service.DtsFeedbackService;

/**
 * @author CHENBO
 * @date 2018/8/26 01:11
 * @QQ 623659388
 */
@RestController
@RequestMapping("/admin/feedback")
@Validated
public class AdminFeedbackController {
	private static final Logger logger = LoggerFactory.getLogger(AdminFeedbackController.class);

	@Autowired
	private DtsFeedbackService feedbackService;

	@RequiresPermissions("admin:feedback:list")
	@RequiresPermissionsDesc(menu = { "用户管理", "意见反馈" }, button = "查询")
	@GetMapping("/list")
	public Object list(Integer userId, String username, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer limit,
			@Sort @RequestParam(defaultValue = "add_time") String sort,
			@Order @RequestParam(defaultValue = "desc") String order) {
		logger.info("【请求开始】用户管理->意见反馈->查询,请求参数:userId:{},username:{},page:{}", userId, username, page);

		List<DtsFeedback> feedbackList = feedbackService.querySelective(userId, username, page, limit, sort, order);
		long total = PageInfo.of(feedbackList).getTotal();
		Map<String, Object> data = new HashMap<>();
		data.put("total", total);
		data.put("items", feedbackList);

		logger.info("【请求结束】用户管理->意见反馈->查询,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}
}
