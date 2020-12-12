package com.qiguliuxing.dts.db.service;

import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsAdminMapper;
import com.qiguliuxing.dts.db.domain.DtsAdmin;
import com.qiguliuxing.dts.db.domain.DtsAdminExample;
import com.qiguliuxing.dts.db.domain.DtsAdmin.Column;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DtsAdminService {
	private final Column[] result = new Column[] { Column.id, Column.username, Column.avatar, Column.roleIds };
	@Resource
	private DtsAdminMapper adminMapper;

	public List<DtsAdmin> findAdmin(String username) {
		DtsAdminExample example = new DtsAdminExample();
		example.or().andUsernameEqualTo(username).andDeletedEqualTo(false);
		return adminMapper.selectByExample(example);
	}

	public DtsAdmin findAdmin(Integer id) {
		return adminMapper.selectByPrimaryKey(id);
	}

	public List<DtsAdmin> querySelective(String username, Integer page, Integer limit, String sort, String order) {
		DtsAdminExample example = new DtsAdminExample();
		DtsAdminExample.Criteria criteria = example.createCriteria();

		if (!StringUtils.isEmpty(username)) {
			criteria.andUsernameLike("%" + username + "%");
		}
		criteria.andDeletedEqualTo(false);

		if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
			example.setOrderByClause(sort + " " + order);
		}

		PageHelper.startPage(page, limit);
		return adminMapper.selectByExampleSelective(example, result);
	}

	public int updateById(DtsAdmin admin) {
		admin.setUpdateTime(LocalDateTime.now());
		return adminMapper.updateByPrimaryKeySelective(admin);
	}

	public void deleteById(Integer id) {
		adminMapper.logicalDeleteByPrimaryKey(id);
	}

	public void add(DtsAdmin admin) {
		admin.setAddTime(LocalDateTime.now());
		admin.setUpdateTime(LocalDateTime.now());
		adminMapper.insertSelective(admin);
	}

	public DtsAdmin findById(Integer id) {
		return adminMapper.selectByPrimaryKeySelective(id, result);
	}
}
