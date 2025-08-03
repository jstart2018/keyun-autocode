package com.jstart.keyunautocodebackend.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {


    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;


    @Test
    void chat() {
        String resp = aiCodeGeneratorService.chat("帮我生成一个个人网站，，数据使用静态数据，不超过50行代码");
        Assertions.assertNotNull(resp);
        System.out.println(resp);
    }
}