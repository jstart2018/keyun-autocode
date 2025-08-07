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
        String resp = aiCodeGeneratorService.chat("帮我生成一个登录页面，不超过50行代码");
        Assertions.assertNotNull(resp);
        resp = aiCodeGeneratorService.chat("修改一下，浓缩20行代码");
        Assertions.assertNotNull(resp);
        resp = aiCodeGeneratorService.chat("我刚刚都交代了你做什么？");
        Assertions.assertNotNull(resp);
        System.out.println(resp);
    }
}