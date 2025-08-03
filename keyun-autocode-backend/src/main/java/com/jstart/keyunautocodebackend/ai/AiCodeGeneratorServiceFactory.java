package com.jstart.keyunautocodebackend.ai;


import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;


    @Resource
    private StreamingChatModel streamingChatModel;

    // 创建AiCodeGeneratorService的实例
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return AiServices.create(AiCodeGeneratorService.class,chatModel);
    }

    // 创建AiCodeGeneratorService的 流式输出 实例
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorServiceStream() {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }






}
