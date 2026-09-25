package com.chat.web.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 公共字段自动填充。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

	@Override
	public void insertFill(MetaObject metaObject) {
		LocalDateTime now = LocalDateTime.now();
		this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
		this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
		this.strictInsertFill(metaObject, "joinedAt", LocalDateTime.class, now);
	}

	@Override
	public void updateFill(MetaObject metaObject) {
		this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
	}
}
