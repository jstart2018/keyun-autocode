package com.jstart.keyunautocodebackend.config;


import cn.hutool.core.util.StrUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Setter
@Slf4j
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisChatMemoryStoreConfig {

    private String host;
    private int port;
    private String username;
    private String password;
    private Long ttl;

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        RedisChatMemoryStore.Builder builder = RedisChatMemoryStore.builder()
                .host(host)
                .port(port)
                .password(password)
                .ttl(ttl);
        if (StrUtil.isNotBlank(username)) {
            builder.user(username);
        }
        if (StrUtil.isNotBlank(password) && StrUtil.isBlank(username)) {
            builder.user("default");
        }
        log.info("正在配置RedisChatMemoryStore，host: {}, port: {}, username: {},password: {}, ttl: {}", host, port, username, password,ttl);
        return builder.build();
    }

}
