package com.qiguliuxing.dts.wx.web;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.domain.DtsRegion;
import com.qiguliuxing.dts.db.service.DtsRegionService;

/**
 * 区域服务
 */
@RestController
@RequestMapping("/wx/region")
@Validated
public class WxRegionController {
	private static final Logger logger = LoggerFactory.getLogger(WxRegionController.class);

	@Autowired
	private DtsRegionService regionService;

	/**
	 * 区域数据
	 * <p>
	 * 根据父区域ID，返回子区域数据。 如果父区域ID是0，则返回省级区域数据；
	 *
	 * @param pid
	 *            父区域ID
	 * @return 区域数据
	 */
	@GetMapping("list")
	public Object list(@NotNull Integer pid) {
		logger.info("【请求开始】根据pid获取子区域数据,请求参数,pid:{}", pid);
		List<DtsRegion> regionList = regionService.queryByPid(pid);

		logger.info("【请求结束】根据pid获取子区域数据成功!");
		return ResponseUtil.ok(regionList);
	}
}