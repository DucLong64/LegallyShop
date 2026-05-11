package com.legallyshop.legallyshop.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // Bật @CreatedDate và @LastModifiedDate trên entity
    // Khai báo ở đây để tập trung cấu hình JPA
}