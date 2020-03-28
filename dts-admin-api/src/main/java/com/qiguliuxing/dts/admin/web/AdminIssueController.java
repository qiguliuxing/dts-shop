package com.qiguliuxing.dts.admin.web;

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
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.validator.Order;
import com.qiguliuxing.dts.core.validator.Sort;
import com.qiguliuxing.dts.db.domain.DtsIssue;
import com.qiguliuxing.dts.db.service.DtsIssueService;

@RestController
@RequestMapping("/admin/issue")
@Validated
public class AdminIssueController {
	private static final Logger logger = LoggerFactory.getLogger(AdminIssueController.class);

	@Autowired
	private DtsIssueService issueService;

	@RequiresPermissions("admin:issue:list")
	@RequiresPermissionsDesc(menu = { "商场管理", "通用问题" }, button = "查询")
	@GetMapping("/list")
	public Object list(String question, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer limit,
			@Sort @RequestParam(defaultValue = "add_time") String sort,
			@Order @RequestParam(defaultValue = "desc") String order) {
		logger.info("【请求开始】商场管理->通用问题->查询,请求参数:question:{},page:{}", question, page);

		List<DtsIssue> issueList = issueService.querySelective(question, page, limit, sort, order);
		long total = PageInfo.of(issueList).getTotal();
		Map<String, Object> data = new HashMap<>();
		data.put("total", total);
		data.put("items", issueList);

		logger.info("【请求结束】商场管理->通用问题->查询,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	private Object validate(DtsIssue issue) {
		String question = issue.getQuestion();
		if (StringUtils.isEmpty(question)) {
			return ResponseUtil.badArgument();
		}
		String answer = issue.getAnswer();
		if (StringUtils.isEmpty(answer)) {
			return ResponseUtil.badArgument();
		}
		return null;
	}

	@RequiresPermissions("admin:issue:create")
	@RequiresPermissionsDesc(menu = { "商场管理", "通用问题" }, button = "添加")
	@PostMapping("/create")
	public Object create(@RequestBody DtsIssue issue) {
		logger.info("【请求开始】商场管理->通用问题->添加,请求参数:question:{},page:{}", JSONObject.toJSONString(issue));

		Object error = validate(issue);
		if (error != null) {
			return error;
		}
		issueService.add(issue);

		logger.info("【请求结束】商场管理->通用问题->查询,响应结果:{}", JSONObject.toJSONString(issue));
		return ResponseUtil.ok(issue);
	}

	@RequiresPermissions("admin:issue:read")
	@GetMapping("/read")
	public Object read(@NotNull Integer id) {
		logger.info("【请求开始】商场管理->通用问题->详情,请求参数,id:{}", id);

		DtsIssue issue = issueService.findById(id);

		logger.info("【请求结束】商场管理->通用问题->详情,响应结果:{}", JSONObject.toJSONString(issue));
		return ResponseUtil.ok(issue);
	}

	@RequiresPermissions("admin:issue:update")
	@RequiresPermissionsDesc(menu = { "商场管理", "通用问题" }, button = "编辑")
	@PostMapping("/update")
	public Object update(@RequestBody DtsIssue issue) {
		logger.info("【请求开始】商场管理->通用问题->编辑,请求参数:{}", JSONObject.toJSONString(issue));

		Object error = validate(issue);
		if (error != null) {
			return error;
		}
		if (issueService.updateById(issue) == 0) {
			logger.error("商场管理->通用问题->编辑 失败:{}", "更新数据失败！");
			return ResponseUtil.updatedDataFailed();
		}

		logger.info("【请求结束】商场管理->通用问题->编辑,响应结果:{}", JSONObject.toJSONString(issue));
		return ResponseUtil.ok(issue);
	}

	@RequiresPermissions("admin:issue:delete")
	@RequiresPermissionsDesc(menu = { "商场管理", "通用问题" }, button = "删除")
	@PostMapping("/delete")
	public Object delete(@RequestBody DtsIssue issue) {
		logger.info("【请求开始】商场管理->通用问题->删除,请求参数:{}", JSONObject.toJSONString(issue));

		Integer id = issue.getId();
		if (id == null) {
			return ResponseUtil.badArgument();
		}
		issueService.deleteById(id);

		logger.info("【请求结束】商场管理->通用问题->删除,响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

}
