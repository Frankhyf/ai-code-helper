package com.frank.aicodehelper.ratelimit.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private Integer redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private Integer redisDatabase;

    @Bean
    public RedissonClient redissonClient() {
        try {
            Config config = new Config();
            String address = "redis://" + redisHost + ":" + redisPort;
            log.info("正在初始化 Redisson 客户端，连接地址: {}", address);
            SingleServerConfig singleServerConfig = config.useSingleServer()
                    .setAddress(address)
                    .setDatabase(redisDatabase)
                    .setConnectionMinimumIdleSize(1)
                    .setConnectionPoolSize(10)
                    .setIdleConnectionTimeout(30000)
                    .setConnectTimeout(5000)
                    .setTimeout(3000)
                    .setRetryAttempts(3)
                    .setRetryInterval(1500);
            // 如果有密码则设置密码
            if (redisPassword != null && !redisPassword.isEmpty()) {
                singleServerConfig.setPassword(redisPassword);
            }
            RedissonClient client = Redisson.create(config);
            log.info("Redisson 客户端初始化成功");
            return client;
        } catch (Exception e) {
            log.error("Redisson 客户端初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("Redisson 客户端初始化失败，请检查 Redis 配置和连接", e);
        }
    }
}

