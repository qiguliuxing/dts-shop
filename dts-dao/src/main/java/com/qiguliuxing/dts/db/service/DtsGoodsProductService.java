package com.qiguliuxing.dts.db.service;

import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.qiguliuxing.dts.db.dao.ex.GoodsProductMapper;
import com.qiguliuxing.dts.db.dao.DtsGoodsProductMapper;
import com.qiguliuxing.dts.db.domain.DtsGoodsProduct;
import com.qiguliuxing.dts.db.domain.DtsGoodsProductExample;

@Service
public class DtsGoodsProductService {
	@Resource
	private DtsGoodsProductMapper dtsGoodsProductMapper;
	@Resource
	private GoodsProductMapper goodsProductMapper;

	public List<DtsGoodsProduct> queryByGid(Integer gid) {
		DtsGoodsProductExample example = new DtsGoodsProductExample();
		example.or().andGoodsIdEqualTo(gid).andDeletedEqualTo(false);
		return dtsGoodsProductMapper.selectByExample(example);
	}

	public DtsGoodsProduct findById(Integer id) {
		return dtsGoodsProductMapper.selectByPrimaryKey(id);
	}

	public void deleteById(Integer id) {
		dtsGoodsProductMapper.logicalDeleteByPrimaryKey(id);
	}

	public void add(DtsGoodsProduct goodsProduct) {
		goodsProduct.setAddTime(LocalDateTime.now());
		goodsProduct.setUpdateTime(LocalDateTime.now());
		dtsGoodsProductMapper.insertSelective(goodsProduct);
	}

	public int count() {
		DtsGoodsProductExample example = new DtsGoodsProductExample();
		example.or().andDeletedEqualTo(false);
		return (int) dtsGoodsProductMapper.countByExample(example);
	}

	public void deleteByGid(Integer gid) {
		DtsGoodsProductExample example = new DtsGoodsProductExample();
		example.or().andGoodsIdEqualTo(gid);
		dtsGoodsProductMapper.logicalDeleteByExample(example);
	}

	public int addStock(Integer id, Short num) {
		return goodsProductMapper.addStock(id, num);
	}

	public int addBrowse(Integer id, Short num) {
		return goodsProductMapper.addBrowse(id, num);// 新增商品流量量
	}

	public int reduceStock(Integer id, Integer goodsId, Short num) {
		goodsProductMapper.addSales(goodsId, num);// 每次需将商品的销售量加下
		return goodsProductMapper.reduceStock(id, num);
	}
}