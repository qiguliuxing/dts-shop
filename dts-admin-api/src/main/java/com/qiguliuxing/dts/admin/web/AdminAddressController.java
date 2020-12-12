package com.qiguliuxing.dts.admin.web;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.qiguliuxing.dts.admin.annotation.RequiresPermissionsDesc;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.validator.Order;
import com.qiguliuxing.dts.core.validator.Sort;
import com.qiguliuxing.dts.db.domain.DtsAddress;
import com.qiguliuxing.dts.db.service.DtsAddressService;
import com.qiguliuxing.dts.db.service.DtsRegionService;

@RestController
@RequestMapping("/admin/address")
@Validated
public class AdminAddressController {
	private static final Logger logger = LoggerFactory.getLogger(AdminAddressController.class);

	@Autowired
	private DtsAddressService addressService;

	@Autowired
	private DtsRegionService regionService;

	private Map<String, Object> toVo(DtsAddress address) {
		Map<String, Object> addressVo = new HashMap<>();
		addressVo.put("id", address.getId());
		addressVo.put("userId", address.getUserId());
		addressVo.put("name", address.getName());
		addressVo.put("mobile", address.getMobile());
		addressVo.put("isDefault", address.getIsDefault());
		addressVo.put("provinceId", address.getProvinceId());
		addressVo.put("cityId", address.getCityId());
		addressVo.put("areaId", address.getAreaId());
		addressVo.put("address", address.getAddress());
		String province = regionService.findById(address.getProvinceId()).getName();
		String city = regionService.findById(address.getCityId()).getName();
		String area = regionService.findById(address.getAreaId()).getName();
		addressVo.put("province", province);
		addressVo.put("city", city);
		addressVo.put("area", area);
		return addressVo;
	}

	@RequiresPermissions("admin:address:list")
	@RequiresPermissionsDesc(menu = { "用户管理", "收货地址" }, button = "查询")
	@GetMapping("/list")
	public Object list(Integer userId, String name, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer limit,
			@Sort @RequestParam(defaultValue = "add_time") String sort,
			@Order @RequestParam(defaultValue = "desc") String order) {
		logger.info("【请求开始】用户管理->收货地址->查询,请求参数:name:{},userId:{},page:{}", name, userId, page);

		List<DtsAddress> addressList = addressService.querySelective(userId, name, page, limit, sort, order);
		long total = PageInfo.of(addressList).getTotal();

		List<Map<String, Object>> addressVoList = new ArrayList<>(addressList.size());
		for (DtsAddress address : addressList) {
			Map<String, Object> addressVo = toVo(address);
			addressVoList.add(addressVo);
		}

		Map<String, Object> data = new HashMap<>();
		data.put("total", total);
		data.put("items", addressVoList);

		logger.info("【请求结束】用户管理->收货地址->查询,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}
}
