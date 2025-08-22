package com.jstart.keyunautocodebackend.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private Integer redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Value("${spring.data.redis.database}")
    private Integer redisDatabase;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = "redis://" + redisHost + ":" + redisPort;
        SingleServerConfig singleServerConfig = config.useSingleServer()//使用单节点 Redis 服务器模式
                .setAddress(address)
                .setDatabase(redisDatabase)
                .setConnectionMinimumIdleSize(1)//最小空闲连接数
                .setConnectionPoolSize(10)      //最大连接数
                .setIdleConnectionTimeout(30000)//连接空闲超过此时间将被自动关闭释放(30秒)
                .setConnectTimeout(5000)        //连接redis的超时时间
                .setTimeout(3000)               //redis执行命令超时时间
                .setRetryAttempts(3)            //命令执行失败后的重试次数
                .setRetryInterval(1500);        //重试间隔时间(毫秒)
        // 如果有密码则设置密码
        if (redisPassword != null && !redisPassword.isEmpty()) {
            singleServerConfig.setPassword(redisPassword);
        }
        return Redisson.create(config);
    }
}
