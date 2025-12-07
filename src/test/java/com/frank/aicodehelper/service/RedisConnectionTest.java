package com.frank.aicodehelper.service;

import redis.clients.jedis.JedisPooled;

public class RedisConnectionTest {
    public static void main(String[] args) {
        // 测试连接6380端口的Redis Stack
        try (JedisPooled jedis = new JedisPooled("localhost", 6380)) {
            System.out.println("Redis连接成功：" + jedis.ping());
            // 测试FT._LIST命令
            System.out.println("FT._LIST执行结果：" + jedis.ftList());
        } catch (Exception e) {
            System.err.println("Redis连接失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}