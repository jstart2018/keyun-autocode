package com.jstart.keyunautocodebackend;

import cn.dev33.satoken.SaManager;
import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.jstart.keyunautocodebackend.mapper") // 扫描 Mapper 接口
public class KeyunAutocodeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeyunAutocodeBackendApplication.class, args);
    }

}
