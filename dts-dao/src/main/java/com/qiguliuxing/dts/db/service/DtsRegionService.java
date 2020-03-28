package com.qiguliuxing.dts.db.service;

import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsRegionMapper;
import com.qiguliuxing.dts.db.domain.DtsRegion;
import com.qiguliuxing.dts.db.domain.DtsRegionExample;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DtsRegionService {

	@Resource
	private DtsRegionMapper regionMapper;

	public List<DtsRegion> getAll() {
		DtsRegionExample example = new DtsRegionExample();
		byte b = 4;
		example.or().andTypeNotEqualTo(b);
		return regionMapper.selectByExample(example);
	}

	public List<DtsRegion> queryByPid(Integer parentId) {
		DtsRegionExample example = new DtsRegionExample();
		example.or().andPidEqualTo(parentId);
		return regionMapper.selectByExample(example);
	}

	public DtsRegion findById(Integer id) {
		return regionMapper.selectByPrimaryKey(id);
	}

	public List<DtsRegion> querySelective(String name, Integer code, Integer page, Integer size, String sort,
			String order) {
		DtsRegionExample example = new DtsRegionExample();
		DtsRegionExample.Criteria criteria = example.createCriteria();

		if (!StringUtils.isEmpty(name)) {
			criteria.andNameLike("%" + name + "%");
		}
		if (!StringUtils.isEmpty(code)) {
			criteria.andCodeEqualTo(code);
		}

		if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
			example.setOrderByClause(sort + " " + order);
		}

		PageHelper.startPage(page, size);
		return regionMapper.selectByExample(example);
	}
}
