package com.jstart.keyunautocodebackend;

import cn.dev33.satoken.SaManager;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.jstart.keyunautocodebackend.mapper") // 扫描 Mapper 接口
public class KeyunAutocodeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeyunAutocodeBackendApplication.class, args);
    }

}
