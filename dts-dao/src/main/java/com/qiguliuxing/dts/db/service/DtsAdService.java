package com.qiguliuxing.dts.db.service;

import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsAdMapper;
import com.qiguliuxing.dts.db.domain.DtsAd;
import com.qiguliuxing.dts.db.domain.DtsAdExample;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DtsAdService {
	@Resource
	private DtsAdMapper adMapper;

	public List<DtsAd> queryIndex() {
		DtsAdExample example = new DtsAdExample();
		example.or().andPositionEqualTo((byte) 1).andDeletedEqualTo(false).andEnabledEqualTo(true);
		return adMapper.selectByExample(example);
	}

	public List<DtsAd> querySelective(String name, String content, Integer page, Integer limit, String sort,
			String order) {
		DtsAdExample example = new DtsAdExample();
		DtsAdExample.Criteria criteria = example.createCriteria();

		if (!StringUtils.isEmpty(name)) {
			criteria.andNameLike("%" + name + "%");
		}
		if (!StringUtils.isEmpty(content)) {
			criteria.andContentLike("%" + content + "%");
		}
		criteria.andDeletedEqualTo(false);

		if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
			example.setOrderByClause(sort + " " + order);
		}

		PageHelper.startPage(page, limit);
		return adMapper.selectByExample(example);
	}

	public int updateById(DtsAd ad) {
		ad.setUpdateTime(LocalDateTime.now());
		return adMapper.updateByPrimaryKeySelective(ad);
	}

	public void deleteById(Integer id) {
		adMapper.logicalDeleteByPrimaryKey(id);
	}

	public void add(DtsAd ad) {
		ad.setAddTime(LocalDateTime.now());
		ad.setUpdateTime(LocalDateTime.now());
		adMapper.insertSelective(ad);
	}

	public DtsAd findById(Integer id) {
		return adMapper.selectByPrimaryKey(id);
	}
}
