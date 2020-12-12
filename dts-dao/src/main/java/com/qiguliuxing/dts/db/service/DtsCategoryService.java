package com.qiguliuxing.dts.db.service;

import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsCategoryMapper;
import com.qiguliuxing.dts.db.domain.DtsCategory;
import com.qiguliuxing.dts.db.domain.DtsCategoryExample;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DtsCategoryService {
	@Resource
	private DtsCategoryMapper categoryMapper;
	private DtsCategory.Column[] CHANNEL = { DtsCategory.Column.id, DtsCategory.Column.name,
			DtsCategory.Column.iconUrl };

	public List<DtsCategory> queryL1WithoutRecommend(int offset, int limit) {
		DtsCategoryExample example = new DtsCategoryExample();
		example.or().andLevelEqualTo("L1").andNameNotEqualTo("推荐").andDeletedEqualTo(false);
		PageHelper.startPage(offset, limit);
		return categoryMapper.selectByExample(example);
	}

	public List<DtsCategory> queryL1(int offset, int limit) {
		DtsCategoryExample example = new DtsCategoryExample();
		example.or().andLevelEqualTo("L1").andDeletedEqualTo(false);
		PageHelper.startPage(offset, limit);
		return categoryMapper.selectByExample(example);
	}

	public List<DtsCategory> queryL1() {
		DtsCategoryExample example = new DtsCategoryExample();
		example.or().andLevelEqualTo("L1").andDeletedEqualTo(false);
		return categoryMapper.selectByExample(example);
	}

	public List<DtsCategory> queryByPid(Integer pid) {
		DtsCategoryExample example = new DtsCategoryExample();
		example.or().andPidEqualTo(pid).andDeletedEqualTo(false);
		return categoryMapper.selectByExample(example);
	}

	public List<DtsCategory> queryL2ByIds(List<Integer> ids) {
		DtsCategoryExample example = new DtsCategoryExample();
		example.or().andIdIn(ids).andLevelEqualTo("L2").andDeletedEqualTo(false);
		return categoryMapper.selectByExample(example);
	}

	public DtsCategory findById(Integer id) {
		return categoryMapper.selectByPrimaryKey(id);
	}

	public List<DtsCategory> querySelective(String id, String name, Integer page, Integer size, String sort,
			String order) {
		DtsCategoryExample example = new DtsCategoryExample();
		DtsCategoryExample.Criteria criteria = example.createCriteria();

		if (!StringUtils.isEmpty(id)) {
			criteria.andIdEqualTo(Integer.valueOf(id));
		}
		if (!StringUtils.isEmpty(name)) {
			criteria.andNameLike("%" + name + "%");
		}
		criteria.andDeletedEqualTo(false);

		if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
			example.setOrderByClause(sort + " " + order);
		}

		PageHelper.startPage(page, size);
		return categoryMapper.selectByExample(example);
	}

	public int updateById(DtsCategory category) {
		category.setUpdateTime(LocalDateTime.now());
		return categoryMapper.updateByPrimaryKeySelective(category);
	}

	public void deleteById(Integer id) {
		categoryMapper.logicalDeleteByPrimaryKey(id);
	}

	public void add(DtsCategory category) {
		category.setAddTime(LocalDateTime.now());
		category.setUpdateTime(LocalDateTime.now());
		categoryMapper.insertSelective(category);
	}

	public List<DtsCategory> queryChannel() {
		DtsCategoryExample example = new DtsCategoryExample();
		example.or().andLevelEqualTo("L1").andDeletedEqualTo(false);
		PageHelper.startPage(1, 9);// 设置分页10
		return categoryMapper.selectByExampleSelective(example, CHANNEL);
	}
}
