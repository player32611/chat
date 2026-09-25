package com.chat.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.chat.service.oss.OssProperties;
import com.chat.service.oss.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云 OSS 上传实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

	private final OssProperties properties;

	@Override
	public String upload(byte[] content, String suffix) {
		String objectKey = "chat/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
				+ "/" + UUID.randomUUID().toString().replace("-", "") + "." + suffix;
		OSS client = new OSSClientBuilder().build(
				properties.getEndpoint(), properties.getAccessKeyId(), properties.getAccessKeySecret());
		try {
			client.putObject(properties.getBucket(), objectKey, new ByteArrayInputStream(content));
		} finally {
			client.shutdown();
		}
		return "https://" + properties.getBucket() + "." + properties.getEndpoint() + "/" + objectKey;
	}
}
