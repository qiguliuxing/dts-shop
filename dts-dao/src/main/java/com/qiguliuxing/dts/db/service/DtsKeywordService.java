package com.qiguliuxing.dts.db.service;

import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsKeywordMapper;
import com.qiguliuxing.dts.db.domain.DtsKeyword;
import com.qiguliuxing.dts.db.domain.DtsKeywordExample;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DtsKeywordService {
	@Resource
	private DtsKeywordMapper keywordsMapper;

	public DtsKeyword queryDefault() {
		DtsKeywordExample example = new DtsKeywordExample();
		example.or().andIsDefaultEqualTo(true).andDeletedEqualTo(false);
		return keywordsMapper.selectOneByExample(example);
	}

	public List<DtsKeyword> queryHots() {
		DtsKeywordExample example = new DtsKeywordExample();
		example.or().andIsHotEqualTo(true).andDeletedEqualTo(false);
		return keywordsMapper.selectByExample(example);
	}

	public List<DtsKeyword> queryByKeyword(String keyword, Integer page, Integer size) {
		DtsKeywordExample example = new DtsKeywordExample();
		example.setDistinct(true);
		example.or().andKeywordLike("%" + keyword + "%").andDeletedEqualTo(false);
		PageHelper.startPage(page, size);
		return keywordsMapper.selectByExampleSelective(example, DtsKeyword.Column.keyword);
	}

	public List<DtsKeyword> querySelective(String keyword, String url, Integer page, Integer limit, String sort,
			String order) {
		DtsKeywordExample example = new DtsKeywordExample();
		DtsKeywordExample.Criteria criteria = example.createCriteria();

		if (!StringUtils.isEmpty(keyword)) {
			criteria.andKeywordLike("%" + keyword + "%");
		}
		if (!StringUtils.isEmpty(url)) {
			criteria.andUrlLike("%" + url + "%");
		}
		criteria.andDeletedEqualTo(false);

		if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
			example.setOrderByClause(sort + " " + order);
		}

		PageHelper.startPage(page, limit);
		return keywordsMapper.selectByExample(example);
	}

	public void add(DtsKeyword keywords) {
		keywords.setAddTime(LocalDateTime.now());
		keywords.setUpdateTime(LocalDateTime.now());
		keywordsMapper.insertSelective(keywords);
	}

	public DtsKeyword findById(Integer id) {
		return keywordsMapper.selectByPrimaryKey(id);
	}

	public int updateById(DtsKeyword keywords) {
		keywords.setUpdateTime(LocalDateTime.now());
		return keywordsMapper.updateByPrimaryKeySelective(keywords);
	}

	public void deleteById(Integer id) {
		keywordsMapper.logicalDeleteByPrimaryKey(id);
	}
}
