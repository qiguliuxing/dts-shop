package com.qiguliuxing.dts.admin.dao;

import com.qiguliuxing.dts.db.domain.DtsGoods;
import com.qiguliuxing.dts.db.domain.DtsGoodsAttribute;
import com.qiguliuxing.dts.db.domain.DtsGoodsProduct;
import com.qiguliuxing.dts.db.domain.DtsGoodsSpecification;

public class GoodsAllinone {
	DtsGoods goods;
	DtsGoodsSpecification[] specifications;
	DtsGoodsAttribute[] attributes;
	// 这里采用 Product 再转换到 DtsGoodsProduct
	DtsGoodsProduct[] products;

	public DtsGoods getGoods() {
		return goods;
	}

	public void setGoods(DtsGoods goods) {
		this.goods = goods;
	}

	public DtsGoodsProduct[] getProducts() {
		return products;
	}

	public void setProducts(DtsGoodsProduct[] products) {
		this.products = products;
	}

	public DtsGoodsSpecification[] getSpecifications() {
		return specifications;
	}

	public void setSpecifications(DtsGoodsSpecification[] specifications) {
		this.specifications = specifications;
	}

	public DtsGoodsAttribute[] getAttributes() {
		return attributes;
	}

	public void setAttributes(DtsGoodsAttribute[] attributes) {
		this.attributes = attributes;
	}

}
