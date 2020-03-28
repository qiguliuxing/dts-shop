package com.qiguliuxing.dts.wx.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.qiguliuxing.dts.core.consts.CommConsts;

/**
 * 简单缓存的数据
 */
public class HomeCacheManager {
	public static final boolean ENABLE = true;// 默认启动缓存

	public static final String INDEX = "index";
	public static final String CATALOG = "catalog";
	public static final String GOODS = "goods";

	private static Map<String, Map<String, Object>> cacheDataList = new HashMap<>();

	/**
	 * 缓存首页数据
	 *
	 * @param data
	 */
	public static void loadData(String cacheKey, Map<String, Object> data) {
		Map<String, Object> cacheData = cacheDataList.get(cacheKey);
		// 有记录，则先丢弃
		if (cacheData != null) {
			cacheData.remove(cacheKey);
		}
		cacheData = new HashMap<>();
		// 深拷贝
		cacheData.putAll(data);
		if (INDEX.equals(cacheKey)) {// 拷贝后去掉可能含用户独有的信息数据
			cacheData.remove("couponList");
		}
		cacheData.put("isCache", "true");

		// 设置缓存有效期单位 ： 分钟
		cacheData.put("expireTime", LocalDateTime.now().plusMinutes(CommConsts.CACHE_EXPIRE_MINUTES));
		cacheDataList.put(cacheKey, cacheData);
	}

	public static Map<String, Object> getCacheData(String cacheKey) {
		return cacheDataList.get(cacheKey);
	}

	/**
	 * 判断缓存中是否有数据
	 *
	 * @return
	 */
	public static boolean hasData(String cacheKey) {
		if (!ENABLE) {
			return false;
		}

		Map<String, Object> cacheData = cacheDataList.get(cacheKey);
		if (cacheData == null) {
			return false;
		} else {// 存在数据还需判断有效期是否已经过了
			LocalDateTime expire = (LocalDateTime) cacheData.get("expireTime");
			if (expire.isBefore(LocalDateTime.now())) {
				return false;
			} else {
				return true;
			}
		}
	}

	/**
	 * 清除所有缓存
	 */
	public static void clearAll() {
		cacheDataList = new HashMap<>();
	}

	/**
	 * 清除缓存数据
	 */
	public static void clear(String cacheKey) {
		Map<String, Object> cacheData = cacheDataList.get(cacheKey);
		if (cacheData != null) {
			cacheDataList.remove(cacheKey);
		}
	}
}
