package com.jstart.keyunautocodebackend.core;

import com.jstart.keyunautocodebackend.enums.CodeGenTypeEnum;
import com.jstart.keyunautocodebackend.model.entity.App;
import com.jstart.keyunautocodebackend.service.AppService;
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
    @Resource
    private AppService appService;



    @Test
    void generateAndSaveCodeStream(){
        App app = appService.getById(1L);
        // 测试流式代码生成
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream
                ("帮我生成一个个人网站，不超过150行代码", CodeGenTypeEnum.MULTI_FILE,app);
        List<String> result = codeStream.collectList().block();// 阻塞等待流处理完成
        Assertions.assertNotNull(result);

        String join = String.join("", result);
        Assertions.assertFalse(join.isEmpty(), "生成的代码流不应为空");

    }

    @Test
    void genVueProject(){
        App app = appService.getById(1L);
        // 测试流式代码生成
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream
                ("帮我生成一个个人网站，不超过300行代码", CodeGenTypeEnum.VUE_PROJECT,app);
        List<String> result = codeStream.collectList().block();// 阻塞等待流处理完成
        Assertions.assertNotNull(result);

        String join = String.join("", result);
        Assertions.assertFalse(join.isEmpty(), "生成的代码流不应为空");

    }
}