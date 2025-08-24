package com.jstart.keyunautocodebackend.config;


import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;


/**
 * 简单Chat模型配置
 * 用于单HTML和多文件页面生成
 * 以及智能路由和根据提示词生成应用名称
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.dashscope")
@Data
public class DashScopeModelConfig {

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private boolean logRequests;

    private boolean logResponses;

    /**
     * 流式：用于单HTML页面生成
     */
    @Bean
    @Scope("prototype")
    public StreamingChatModel qwFlashStreamingModel() {
        final int maxTokens = 8192;
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }


    /**
     * 非流式：用于智能路由 和 根据提示词生成应用名称
     */
    @Bean
    @Scope("prototype")
    public OpenAiChatModel qwFlashModel() {
        final int maxTokens = 128;
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }
}
