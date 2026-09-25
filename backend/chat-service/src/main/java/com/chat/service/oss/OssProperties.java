package com.chat.service.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置项。
 */
@Data
@Component
@ConfigurationProperties(prefix = "chat.aliyun.oss")
public class OssProperties {

	private String endpoint;

	private String accessKeyId;

	private String accessKeySecret;

	private String bucket;

	private String domain;
}
