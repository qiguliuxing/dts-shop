package com.qiguliuxing.dts.wx.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qiguliuxing.dts.db.domain.DtsRegion;
import com.qiguliuxing.dts.db.service.DtsRegionService;

/**
 * @author qiguliuxing
 * @since 1.0.0
 * @date 2017-04-11 11:07
 **/
@Component
public class GetRegionService {

	@Autowired
	private DtsRegionService regionService;

	private static List<DtsRegion> DtsRegions;

	protected List<DtsRegion> getDtsRegions() {
		if (DtsRegions == null) {
			createRegion();
		}
		return DtsRegions;
	}

	private synchronized void createRegion() {
		if (DtsRegions == null) {
			DtsRegions = regionService.getAll();
		}
	}
}
