package com.chat.web.controller;

import com.chat.common.constant.CommonConstant;
import com.chat.common.exception.BusinessException;
import com.chat.common.result.Result;
import com.chat.service.oss.OssService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

/**
 * 文件上传接口。
 */
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

	private static final Set<String> ALLOWED_SUFFIX = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

	private final OssService ossService;

	@PostMapping("/upload")
	public Result<String> upload(@RequestParam("file") MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException(CommonConstant.CODE_BAD_REQUEST, "请选择文件");
		}
		byte[] bytes;
		try {
			bytes = file.getBytes();
		} catch (IOException e) {
			throw new BusinessException("读取文件失败");
		}
		return Result.success(ossService.upload(bytes, resolveSuffix(file.getOriginalFilename())));
	}

	private String resolveSuffix(String filename) {
		if (filename == null || !filename.contains(".")) {
			return "jpg";
		}
		String suffix = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
		return ALLOWED_SUFFIX.contains(suffix) ? suffix : "jpg";
	}
}
