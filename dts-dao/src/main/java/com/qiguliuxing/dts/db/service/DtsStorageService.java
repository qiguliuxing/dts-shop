package com.qiguliuxing.dts.db.service;

import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsStorageMapper;
import com.qiguliuxing.dts.db.domain.DtsStorage;
import com.qiguliuxing.dts.db.domain.DtsStorageExample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DtsStorageService {
	@Autowired
	private DtsStorageMapper storageMapper;

	public void deleteByKey(String key) {
		DtsStorageExample example = new DtsStorageExample();
		example.or().andKeyEqualTo(key);
		storageMapper.logicalDeleteByExample(example);
	}

	public void add(DtsStorage storageInfo) {
		storageInfo.setAddTime(LocalDateTime.now());
		storageInfo.setUpdateTime(LocalDateTime.now());
		storageMapper.insertSelective(storageInfo);
	}

	public DtsStorage findByKey(String key) {
		DtsStorageExample example = new DtsStorageExample();
		example.or().andKeyEqualTo(key).andDeletedEqualTo(false);
		return storageMapper.selectOneByExample(example);
	}

	public int update(DtsStorage storageInfo) {
		storageInfo.setUpdateTime(LocalDateTime.now());
		return storageMapper.updateByPrimaryKeySelective(storageInfo);
	}

	public DtsStorage findById(Integer id) {
		return storageMapper.selectByPrimaryKey(id);
	}

	public List<DtsStorage> querySelective(String key, String name, Integer page, Integer limit, String sort,
			String order) {
		DtsStorageExample example = new DtsStorageExample();
		DtsStorageExample.Criteria criteria = example.createCriteria();

		if (!StringUtils.isEmpty(key)) {
			criteria.andKeyEqualTo(key);
		}
		if (!StringUtils.isEmpty(name)) {
			criteria.andNameLike("%" + name + "%");
		}
		criteria.andDeletedEqualTo(false);

		if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
			example.setOrderByClause(sort + " " + order);
		}

		PageHelper.startPage(page, limit);
		return storageMapper.selectByExample(example);
	}
}
