package com.qiguliuxing.dts.db.service;

import org.springframework.stereotype.Service;

import com.qiguliuxing.dts.db.dao.DtsUserFormidMapper;
import com.qiguliuxing.dts.db.domain.DtsUserFormid;
import com.qiguliuxing.dts.db.domain.DtsUserFormidExample;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class DtsUserFormIdService {
	@Resource
	private DtsUserFormidMapper formidMapper;

	/**
	 * 查找是否有可用的FormId
	 *
	 * @param openId
	 * @return
	 */
	public DtsUserFormid queryByOpenId(String openId) {
		DtsUserFormidExample example = new DtsUserFormidExample();
		// 符合找到该用户记录，且可用次数大于1，且还未过期
		example.or().andOpenidEqualTo(openId).andExpireTimeGreaterThan(LocalDateTime.now());
		return formidMapper.selectOneByExample(example);
	}

	/**
	 * 更新或删除FormId
	 *
	 * @param userFormid
	 */
	public int updateUserFormId(DtsUserFormid userFormid) {
		// 更新或者删除缓存
		if (userFormid.getIsprepay() && userFormid.getUseamount() > 1) {
			userFormid.setUseamount(userFormid.getUseamount() - 1);
			userFormid.setUpdateTime(LocalDateTime.now());
			return formidMapper.updateByPrimaryKey(userFormid);
		} else {
			return formidMapper.deleteByPrimaryKey(userFormid.getId());
		}
	}

	/**
	 * 添加一个 FormId
	 *
	 * @param userFormid
	 */
	public void addUserFormid(DtsUserFormid userFormid) {
		userFormid.setAddTime(LocalDateTime.now());
		userFormid.setUpdateTime(LocalDateTime.now());
		formidMapper.insertSelective(userFormid);
	}
}
