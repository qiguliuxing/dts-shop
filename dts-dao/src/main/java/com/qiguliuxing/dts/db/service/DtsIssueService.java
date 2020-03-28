package com.qiguliuxing.dts.db.service;

import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsIssueMapper;
import com.qiguliuxing.dts.db.domain.DtsIssue;
import com.qiguliuxing.dts.db.domain.DtsIssueExample;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DtsIssueService {
	@Resource
	private DtsIssueMapper issueMapper;

	public List<DtsIssue> query() {
		DtsIssueExample example = new DtsIssueExample();
		example.or().andDeletedEqualTo(false);
		return issueMapper.selectByExample(example);
	}

	public void deleteById(Integer id) {
		issueMapper.logicalDeleteByPrimaryKey(id);
	}

	public void add(DtsIssue issue) {
		issue.setAddTime(LocalDateTime.now());
		issue.setUpdateTime(LocalDateTime.now());
		issueMapper.insertSelective(issue);
	}

	public List<DtsIssue> querySelective(String question, Integer page, Integer size, String sort, String order) {
		DtsIssueExample example = new DtsIssueExample();
		DtsIssueExample.Criteria criteria = example.createCriteria();

		if (!StringUtils.isEmpty(question)) {
			criteria.andQuestionLike("%" + question + "%");
		}
		criteria.andDeletedEqualTo(false);

		if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
			example.setOrderByClause(sort + " " + order);
		}

		PageHelper.startPage(page, size);
		return issueMapper.selectByExample(example);
	}

	public int updateById(DtsIssue issue) {
		issue.setUpdateTime(LocalDateTime.now());
		return issueMapper.updateByPrimaryKeySelective(issue);
	}

	public DtsIssue findById(Integer id) {
		return issueMapper.selectByPrimaryKey(id);
	}
}
