package com.qiguliuxing.dts.db.dao.ex;

import org.apache.ibatis.annotations.Param;

public interface GoodsProductMapper {
	int addStock(@Param("id") Integer id, @Param("num") Short num);

	int reduceStock(@Param("id") Integer id, @Param("num") Short num);

	int addBrowse(@Param("id") Integer id, @Param("num") Short num);

	int addSales(@Param("id") Integer id, @Param("num") Short num);
}