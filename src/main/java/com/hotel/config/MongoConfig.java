// src/main/java/com/hotel/config/MongoConfig.java
package com.hotel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {
    // 不需要额外配置，使用默认设置
}