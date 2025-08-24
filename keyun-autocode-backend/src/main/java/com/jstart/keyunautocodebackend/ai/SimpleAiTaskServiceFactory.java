package com.jstart.keyunautocodebackend.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * AI代码生成类型路由服务工厂
 *
 * @author jstart
 */
@Slf4j
@Configuration
public class SimpleAiTaskServiceFactory {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 创建AI代码生成类型路由服务实例
     */
    @Bean
    @Scope("prototype")
    public SimpleAiTaskService aiCodeGenTypeRoutingService() {
        ChatModel qwFlashModel = applicationContext.getBean("qwFlashModel", ChatModel.class);
        return AiServices.builder(SimpleAiTaskService.class)
                .chatModel(qwFlashModel)
                .build();
    }
}
