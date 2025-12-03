package com.frank.aicodehelper.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisChatMemoryStoreConfig {

    private String host;

    private int port;

    private String password;

    private long ttl;

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        // 只有在密码不为空时才设置密码
        if (password != null && !password.isEmpty()) {
            return RedisChatMemoryStore.builder()
                    .host(host)
                    .port(port)
                    .password(password)
                    .ttl(ttl)
                    .build();
        } else {
            return RedisChatMemoryStore.builder()
                    .host(host)
                    .port(port)
                    .ttl(ttl)
                    .build();
        }
    }
}

