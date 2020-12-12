package com.qiguliuxing.dts.db.service;

import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsSearchHistoryMapper;
import com.qiguliuxing.dts.db.domain.DtsSearchHistory;
import com.qiguliuxing.dts.db.domain.DtsSearchHistoryExample;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DtsSearchHistoryService {
	@Resource
	private DtsSearchHistoryMapper searchHistoryMapper;

	public void save(DtsSearchHistory searchHistory) {
		searchHistory.setAddTime(LocalDateTime.now());
		searchHistory.setUpdateTime(LocalDateTime.now());
		searchHistoryMapper.insertSelective(searchHistory);
	}

	public List<DtsSearchHistory> queryByUid(int uid) {
		DtsSearchHistoryExample example = new DtsSearchHistoryExample();
		example.or().andUserIdEqualTo(uid).andDeletedEqualTo(false);
		example.setDistinct(true);
		return searchHistoryMapper.selectByExampleSelective(example, DtsSearchHistory.Column.keyword);
	}

	public void deleteByUid(int uid) {
		DtsSearchHistoryExample example = new DtsSearchHistoryExample();
		example.or().andUserIdEqualTo(uid);
		searchHistoryMapper.logicalDeleteByExample(example);
	}

	public List<DtsSearchHistory> querySelective(String userId, String keyword, Integer page, Integer size, String sort,
			String order) {
		DtsSearchHistoryExample example = new DtsSearchHistoryExample();
		DtsSearchHistoryExample.Criteria criteria = example.createCriteria();

		if (!StringUtils.isEmpty(userId)) {
			criteria.andUserIdEqualTo(Integer.valueOf(userId));
		}
		if (!StringUtils.isEmpty(keyword)) {
			criteria.andKeywordLike("%" + keyword + "%");
		}
		criteria.andDeletedEqualTo(false);

		if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
			example.setOrderByClause(sort + " " + order);
		}

		PageHelper.startPage(page, size);
		return searchHistoryMapper.selectByExample(example);
	}
}
