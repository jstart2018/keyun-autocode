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
 * @author yupi
 */
@Slf4j
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 创建AI代码生成类型路由服务实例
     */
    @Bean
    @Scope("prototype")
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
        ChatModel openAiChatModelPrototype = applicationContext.getBean("openAiChatModelPrototype", ChatModel.class);
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(openAiChatModelPrototype)
                .build();
    }
}
