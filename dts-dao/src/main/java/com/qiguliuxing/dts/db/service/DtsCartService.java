package com.qiguliuxing.dts.db.service;

import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsCartMapper;
import com.qiguliuxing.dts.db.domain.DtsCart;
import com.qiguliuxing.dts.db.domain.DtsCartExample;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DtsCartService {
	@Resource
	private DtsCartMapper cartMapper;

	public DtsCart queryExist(Integer goodsId, Integer productId, Integer userId) {
		DtsCartExample example = new DtsCartExample();
		example.or().andGoodsIdEqualTo(goodsId).andProductIdEqualTo(productId).andUserIdEqualTo(userId)
				.andDeletedEqualTo(false);
		return cartMapper.selectOneByExample(example);
	}

	public void add(DtsCart cart) {
		cart.setAddTime(LocalDateTime.now());
		cart.setUpdateTime(LocalDateTime.now());
		cartMapper.insertSelective(cart);
	}

	public int updateById(DtsCart cart) {
		cart.setUpdateTime(LocalDateTime.now());
		return cartMapper.updateByPrimaryKeySelective(cart);
	}

	public List<DtsCart> queryByUid(int userId) {
		DtsCartExample example = new DtsCartExample();
		example.or().andUserIdEqualTo(userId).andDeletedEqualTo(false);
		return cartMapper.selectByExample(example);
	}

	public List<DtsCart> queryByUidAndChecked(Integer userId) {
		DtsCartExample example = new DtsCartExample();
		example.or().andUserIdEqualTo(userId).andCheckedEqualTo(true).andDeletedEqualTo(false);
		return cartMapper.selectByExample(example);
	}

	public int delete(List<Integer> productIdList, int userId) {
		DtsCartExample example = new DtsCartExample();
		example.or().andUserIdEqualTo(userId).andProductIdIn(productIdList);
		return cartMapper.logicalDeleteByExample(example);
	}

	public DtsCart findById(Integer id) {
		return cartMapper.selectByPrimaryKey(id);
	}

	public int updateCheck(Integer userId, List<Integer> idsList, Boolean checked) {
		DtsCartExample example = new DtsCartExample();
		example.or().andUserIdEqualTo(userId).andProductIdIn(idsList).andDeletedEqualTo(false);
		DtsCart cart = new DtsCart();
		cart.setChecked(checked);
		cart.setUpdateTime(LocalDateTime.now());
		return cartMapper.updateByExampleSelective(cart, example);
	}

	public void clearGoods(Integer userId) {
		DtsCartExample example = new DtsCartExample();
		example.or().andUserIdEqualTo(userId).andCheckedEqualTo(true);
		DtsCart cart = new DtsCart();
		cart.setDeleted(true);
		cartMapper.updateByExampleSelective(cart, example);
	}

	public List<DtsCart> querySelective(Integer userId, Integer goodsId, Integer page, Integer limit, String sort,
			String order) {
		DtsCartExample example = new DtsCartExample();
		DtsCartExample.Criteria criteria = example.createCriteria();

		if (!StringUtils.isEmpty(userId)) {
			criteria.andUserIdEqualTo(userId);
		}
		if (!StringUtils.isEmpty(goodsId)) {
			criteria.andGoodsIdEqualTo(goodsId);
		}
		criteria.andDeletedEqualTo(false);

		if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
			example.setOrderByClause(sort + " " + order);
		}

		PageHelper.startPage(page, limit);
		return cartMapper.selectByExample(example);
	}

	public void deleteById(Integer id) {
		cartMapper.logicalDeleteByPrimaryKey(id);
	}

	public boolean checkExist(Integer goodsId) {
		DtsCartExample example = new DtsCartExample();
		example.or().andGoodsIdEqualTo(goodsId).andCheckedEqualTo(true).andDeletedEqualTo(false);
		return cartMapper.countByExample(example) != 0;
	}
}
