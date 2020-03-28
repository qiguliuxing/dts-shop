package com.qiguliuxing.dts.wx.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.qiguliuxing.dts.db.domain.DtsBrand;
import com.qiguliuxing.dts.db.domain.DtsCart;

/**
 * 用于存储 品牌入驻商购物车商品的对象
 * 
 * @author CHENBO
 * @QQ 623659388
 * @since 1.0.0
 */
public class BrandCartGoods implements Serializable {

	private static final long serialVersionUID = -7908381028314100456L;

	private static final Integer DEFAULT_BRAND_ID = 1001000;

	private static final String DEFAULT_BRAND_COMMPANY = "长沙聚惠星自营店";

	private static final String DEFAULT_BRAND_NAME = "长沙聚惠星自营店";

	private Integer brandId;

	private String brandName;

	private String brandCommpany;

	private List<DtsCart> cartList;

	private BigDecimal bandGoodsTotalPrice;

	private BigDecimal bandFreightPrice;

	public Integer getBrandId() {
		return brandId;
	}

	public void setBrandId(Integer brandId) {
		this.brandId = brandId;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public String getBrandCommpany() {
		return brandCommpany;
	}

	public void setBrandCommpany(String brandCommpany) {
		this.brandCommpany = brandCommpany;
	}

	public List<DtsCart> getCartList() {
		return cartList;
	}

	public void setCartList(List<DtsCart> cartList) {
		this.cartList = cartList;
	}

	public BigDecimal getBandGoodsTotalPrice() {
		return bandGoodsTotalPrice;
	}

	public void setBandGoodsTotalPrice(BigDecimal bandGoodsTotalPrice) {
		this.bandGoodsTotalPrice = bandGoodsTotalPrice;
	}

	public BigDecimal getBandFreightPrice() {
		return bandFreightPrice;
	}

	public void setBandFreightPrice(BigDecimal bandFreightPrice) {
		this.bandFreightPrice = bandFreightPrice;
	}

	public static BrandCartGoods init(DtsBrand dtsBrand) {
		BrandCartGoods bcg = new BrandCartGoods();
		if (dtsBrand != null) {
			bcg.setBrandId(dtsBrand.getId());
			bcg.setBrandCommpany(dtsBrand.getCommpany());
			bcg.setBrandName(dtsBrand.getName());
		} else {
			bcg.setBrandId(DEFAULT_BRAND_ID);
			bcg.setBrandCommpany(DEFAULT_BRAND_COMMPANY);
			bcg.setBrandName(DEFAULT_BRAND_NAME);
		}
		List<DtsCart> dtsCartList = new ArrayList<DtsCart>();
		bcg.setCartList(dtsCartList);
		return bcg;
	}

}
