package com.jstart.keyunautocodebackend.core;

import com.jstart.keyunautocodebackend.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

@SpringBootTest
class AiCodeGeneratorFacadeTest {


    @Resource
    AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveHtmlCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个简单的登录页面，不超过20行代码", CodeGenTypeEnum.HTML);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateAndSaveMultiFileCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个简单的登录页面，不超过50行代码", CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateAndSaveCodeStream(){
        // 测试流式代码生成
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream
                ("帮我生成一个个人网站，不超过150行代码", CodeGenTypeEnum.MULTI_FILE);
        List<String> result = codeStream.collectList().block();// 阻塞等待流处理完成
        Assertions.assertNotNull(result);

        String join = String.join("", result);
        Assertions.assertFalse(join.isEmpty(), "生成的代码流不应为空");

    }
}