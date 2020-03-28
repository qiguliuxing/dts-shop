package com.qiguliuxing.dts.db.service;

import org.springframework.stereotype.Service;

import com.qiguliuxing.dts.db.dao.DtsOrderGoodsMapper;
import com.qiguliuxing.dts.db.domain.DtsOrderGoods;
import com.qiguliuxing.dts.db.domain.DtsOrderGoodsExample;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DtsOrderGoodsService {
	@Resource
	private DtsOrderGoodsMapper orderGoodsMapper;

	public int add(DtsOrderGoods orderGoods) {
		orderGoods.setAddTime(LocalDateTime.now());
		orderGoods.setUpdateTime(LocalDateTime.now());
		return orderGoodsMapper.insertSelective(orderGoods);
	}

	public List<DtsOrderGoods> queryByOid(Integer orderId) {
		DtsOrderGoodsExample example = new DtsOrderGoodsExample();
		example.or().andOrderIdEqualTo(orderId).andDeletedEqualTo(false);
		return orderGoodsMapper.selectByExample(example);
	}

	public List<DtsOrderGoods> findByOidAndGid(Integer orderId, Integer goodsId) {
		DtsOrderGoodsExample example = new DtsOrderGoodsExample();
		example.or().andOrderIdEqualTo(orderId).andGoodsIdEqualTo(goodsId).andDeletedEqualTo(false);
		return orderGoodsMapper.selectByExample(example);
	}

	public DtsOrderGoods findById(Integer id) {
		return orderGoodsMapper.selectByPrimaryKey(id);
	}

	public void updateById(DtsOrderGoods orderGoods) {
		orderGoods.setUpdateTime(LocalDateTime.now());
		orderGoodsMapper.updateByPrimaryKeySelective(orderGoods);
	}

	public Short getComments(Integer orderId) {
		DtsOrderGoodsExample example = new DtsOrderGoodsExample();
		example.or().andOrderIdEqualTo(orderId).andDeletedEqualTo(false);
		long count = orderGoodsMapper.countByExample(example);
		return (short) count;
	}

	public boolean checkExist(Integer goodsId) {
		DtsOrderGoodsExample example = new DtsOrderGoodsExample();
		example.or().andGoodsIdEqualTo(goodsId).andDeletedEqualTo(false);
		return orderGoodsMapper.countByExample(example) != 0;
	}
}
