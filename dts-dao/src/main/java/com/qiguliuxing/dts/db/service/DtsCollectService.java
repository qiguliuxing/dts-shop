package com.qiguliuxing.dts.db.service;

import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsCollectMapper;
import com.qiguliuxing.dts.db.domain.DtsCollect;
import com.qiguliuxing.dts.db.domain.DtsCollectExample;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DtsCollectService {
	@Resource
	private DtsCollectMapper collectMapper;

	public int count(int uid, Integer gid) {
		DtsCollectExample example = new DtsCollectExample();
		example.or().andUserIdEqualTo(uid).andValueIdEqualTo(gid).andDeletedEqualTo(false);
		return (int) collectMapper.countByExample(example);
	}

	public List<DtsCollect> queryByType(Integer userId, Byte type, Integer page, Integer size) {
		DtsCollectExample example = new DtsCollectExample();
		example.or().andUserIdEqualTo(userId).andTypeEqualTo(type).andDeletedEqualTo(false);
		example.setOrderByClause(DtsCollect.Column.addTime.desc());
		PageHelper.startPage(page, size);
		return collectMapper.selectByExample(example);
	}

	public int countByType(Integer userId, Byte type) {
		DtsCollectExample example = new DtsCollectExample();
		example.or().andUserIdEqualTo(userId).andTypeEqualTo(type).andDeletedEqualTo(false);
		return (int) collectMapper.countByExample(example);
	}

	public DtsCollect queryByTypeAndValue(Integer userId, Byte type, Integer valueId) {
		DtsCollectExample example = new DtsCollectExample();
		example.or().andUserIdEqualTo(userId).andValueIdEqualTo(valueId).andTypeEqualTo(type).andDeletedEqualTo(false);
		return collectMapper.selectOneByExample(example);
	}

	public void deleteById(Integer id) {
		collectMapper.logicalDeleteByPrimaryKey(id);
	}

	public int add(DtsCollect collect) {
		collect.setAddTime(LocalDateTime.now());
		collect.setUpdateTime(LocalDateTime.now());
		return collectMapper.insertSelective(collect);
	}

	public List<DtsCollect> querySelective(String userId, String valueId, Integer page, Integer size, String sort,
			String order) {
		DtsCollectExample example = new DtsCollectExample();
		DtsCollectExample.Criteria criteria = example.createCriteria();

		if (!StringUtils.isEmpty(userId)) {
			criteria.andUserIdEqualTo(Integer.valueOf(userId));
		}
		if (!StringUtils.isEmpty(valueId)) {
			criteria.andValueIdEqualTo(Integer.valueOf(valueId));
		}
		criteria.andDeletedEqualTo(false);

		if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
			example.setOrderByClause(sort + " " + order);
		}

		PageHelper.startPage(page, size);
		return collectMapper.selectByExample(example);
	}
}
