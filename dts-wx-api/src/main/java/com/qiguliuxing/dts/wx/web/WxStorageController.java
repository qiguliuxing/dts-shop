package com.qiguliuxing.dts.wx.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.qiguliuxing.dts.core.storage.StorageService;
import com.qiguliuxing.dts.core.util.CharUtil;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.domain.DtsStorage;
import com.qiguliuxing.dts.db.service.DtsStorageService;

/**
 * 对象存储服务
 */
@RestController
@RequestMapping("/wx/storage")
@Validated
public class WxStorageController {
	private static final Logger logger = LoggerFactory.getLogger(WxStorageController.class);

	@Autowired
	private StorageService storageService;

	@Autowired
	private DtsStorageService DtsStorageService;

	@SuppressWarnings("unused")
	private String generateKey(String originalFilename) {
		int index = originalFilename.lastIndexOf('.');
		String suffix = originalFilename.substring(index);

		String key = null;
		DtsStorage storageInfo = null;

		do {
			key = CharUtil.getRandomString(20) + suffix;
			storageInfo = DtsStorageService.findByKey(key);
		} while (storageInfo != null);

		return key;
	}

	/**
	 * 上传文件
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/upload")
	public Object upload(@RequestParam("file") MultipartFile file) throws IOException {
		logger.info("【请求开始】上传文件,请求参数,file:{}", file.getOriginalFilename());

		String originalFilename = file.getOriginalFilename();
		String url = storageService.store(file.getInputStream(), file.getSize(), file.getContentType(),
				originalFilename);

		Map<String, Object> data = new HashMap<>();
		data.put("url", url);

		logger.info("【请求结束】上传文件,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	/**
	 * 访问存储对象
	 *
	 * @param key
	 *            存储对象key
	 * @return
	 */
	@GetMapping("/fetch/{key:.+}")
	public ResponseEntity<Resource> fetch(@PathVariable String key) {
		// logger.info("【请求开始】访问存储对象,请求参数,key:{}", key);

		DtsStorage DtsStorage = DtsStorageService.findByKey(key);
		if (key == null) {
			return ResponseEntity.notFound().build();
		}
		if (key.contains("../")) {
			return ResponseEntity.badRequest().build();
		}
		String type = DtsStorage.getType();
		MediaType mediaType = MediaType.parseMediaType(type);

		Resource file = storageService.loadAsResource(key);
		if (file == null) {
			return ResponseEntity.notFound().build();
		}
		// logger.info("【请求结束】访问存储对象,响应结果:{}","成功");
		return ResponseEntity.ok().contentType(mediaType).body(file);
	}

	/**
	 * 访问存储对象
	 *
	 * @param key
	 *            存储对象key
	 * @return
	 */
	@GetMapping("/download/{key:.+}")
	public ResponseEntity<Resource> download(@PathVariable String key) {
		// logger.info("【请求开始】访问存储对象,请求参数,key:{}", key);
		DtsStorage DtsStorage = DtsStorageService.findByKey(key);
		if (key == null) {
			return ResponseEntity.notFound().build();
		}
		if (key.contains("../")) {
			return ResponseEntity.badRequest().build();
		}

		String type = DtsStorage.getType();
		MediaType mediaType = MediaType.parseMediaType(type);

		Resource file = storageService.loadAsResource(key);
		if (file == null) {
			return ResponseEntity.notFound().build();
		}
		// logger.info("【请求结束】访问存储对象,响应结果:{}","成功");
		return ResponseEntity.ok().contentType(mediaType)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

}
