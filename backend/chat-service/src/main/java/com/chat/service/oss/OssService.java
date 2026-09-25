package com.chat.service.oss;

/**
 * 对象存储上传服务。
 */
public interface OssService {

	/**
	 * 上传文件内容，返回可访问 URL。
	 *
	 * @param content 文件字节
	 * @param suffix  文件后缀（不含点）
	 */
	String upload(byte[] content, String suffix);
}
