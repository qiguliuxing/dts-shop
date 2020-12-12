package com.qiguliuxing.dts.db.service;

import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsCommentMapper;
import com.qiguliuxing.dts.db.domain.DtsComment;
import com.qiguliuxing.dts.db.domain.DtsCommentExample;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DtsCommentService {
	@Resource
	private DtsCommentMapper commentMapper;

	public List<DtsComment> queryGoodsByGid(Integer id, int offset, int limit) {
		DtsCommentExample example = new DtsCommentExample();
		example.setOrderByClause(DtsComment.Column.addTime.desc());
		example.or().andValueIdEqualTo(id).andTypeEqualTo((byte) 0).andDeletedEqualTo(false);
		PageHelper.startPage(offset, limit);
		return commentMapper.selectByExample(example);
	}

	public List<DtsComment> query(Byte type, Integer valueId, Integer showType, Integer offset, Integer limit) {
		DtsCommentExample example = new DtsCommentExample();
		example.setOrderByClause(DtsComment.Column.addTime.desc());
		if (showType == 0) {
			example.or().andValueIdEqualTo(valueId).andTypeEqualTo(type).andDeletedEqualTo(false);
		} else if (showType == 1) {
			example.or().andValueIdEqualTo(valueId).andTypeEqualTo(type).andHasPictureEqualTo(true)
					.andDeletedEqualTo(false);
		} else {
			throw new RuntimeException("showType不支持");
		}
		PageHelper.startPage(offset, limit);
		return commentMapper.selectByExample(example);
	}

	public int count(Byte type, Integer valueId, Integer showType) {
		DtsCommentExample example = new DtsCommentExample();
		if (showType == 0) {
			example.or().andValueIdEqualTo(valueId).andTypeEqualTo(type).andDeletedEqualTo(false);
		} else if (showType == 1) {
			example.or().andValueIdEqualTo(valueId).andTypeEqualTo(type).andHasPictureEqualTo(true)
					.andDeletedEqualTo(false);
		} else {
			throw new RuntimeException("showType不支持");
		}
		return (int) commentMapper.countByExample(example);
	}

	public int save(DtsComment comment) {
		comment.setAddTime(LocalDateTime.now());
		comment.setUpdateTime(LocalDateTime.now());
		return commentMapper.insertSelective(comment);
	}

	public List<DtsComment> querySelective(String userId, String valueId, Integer page, Integer size, String sort,
			String order) {
		DtsCommentExample example = new DtsCommentExample();
		DtsCommentExample.Criteria criteria = example.createCriteria();

		// type=2 是订单商品回复，这里过滤
		criteria.andTypeNotEqualTo((byte) 2);

		if (!StringUtils.isEmpty(userId)) {
			criteria.andUserIdEqualTo(Integer.valueOf(userId));
		}
		if (!StringUtils.isEmpty(valueId)) {
			criteria.andValueIdEqualTo(Integer.valueOf(valueId)).andTypeEqualTo((byte) 0);
		}
		criteria.andDeletedEqualTo(false);

		if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
			example.setOrderByClause(sort + " " + order);
		}

		PageHelper.startPage(page, size);
		return commentMapper.selectByExample(example);
	}

	public void deleteById(Integer id) {
		commentMapper.logicalDeleteByPrimaryKey(id);
	}

	public String queryReply(Integer id) {
		DtsCommentExample example = new DtsCommentExample();
		example.or().andTypeEqualTo((byte) 2).andValueIdEqualTo(id);
		List<DtsComment> commentReply = commentMapper.selectByExampleSelective(example, DtsComment.Column.content);
		// 目前业务只支持回复一次
		if (commentReply.size() == 1) {
			return commentReply.get(0).getContent();
		}
		return null;
	}

	public DtsComment findById(Integer id) {
		return commentMapper.selectByPrimaryKey(id);
	}
}
