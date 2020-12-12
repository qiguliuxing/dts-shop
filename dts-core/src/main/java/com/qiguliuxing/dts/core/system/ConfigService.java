package com.qiguliuxing.dts.core.system;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qiguliuxing.dts.db.domain.DtsSystem;
import com.qiguliuxing.dts.db.service.DtsSystemConfigService;

/**
 * 该类用于自动初始化数据库配置到BaseConfig中，以便BaseConfig的子类调用
 */
@Component
class ConfigService {
	private static ConfigService systemConfigService;
	@Autowired
	private DtsSystemConfigService dtsSystemConfigService;

	// 不允许实例化
	private ConfigService() {

	}

	static ConfigService getSystemConfigService() {
		return systemConfigService;
	}

	@PostConstruct
	public void inist() {
		systemConfigService = this;
		systemConfigService.inistConfigs();
	}

	/**
	 * 根据 prefix 重置该 prefix 下所有设置
	 *
	 * @param prefix
	 */
	public void reloadConfig(String prefix) {
		List<DtsSystem> list = dtsSystemConfigService.queryAll();
		for (DtsSystem item : list) {
			// 符合条件，添加
			if (item.getKeyName().startsWith(prefix))
				BaseConfig.addConfig(item.getKeyName(), item.getKeyValue());
		}
	}

	/**
	 * 读取全部配置
	 */
	private void inistConfigs() {
		List<DtsSystem> list = dtsSystemConfigService.queryAll();
		for (DtsSystem item : list) {
			BaseConfig.addConfig(item.getKeyName(), item.getKeyValue());
		}
	}
}