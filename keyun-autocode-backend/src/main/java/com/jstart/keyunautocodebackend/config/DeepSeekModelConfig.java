package com.jstart.keyunautocodebackend.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;


/**
 * 创建新的StreamingChatModel，切换模型用于更复杂的vue项目生成
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
@Data
public class DeepSeekModelConfig {

    private String baseUrl;

    private String apiKey;

    /**
     * 推理流式：用于 Vue 项目生成，带工具调用
     */
    @Bean
    @Scope("prototype")
    public StreamingChatModel chatStreamingModel() {
        // 为了测试方便临时修改
        final String modelName = "deepseek-chat";
        final int maxTokens = 8192;
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * 推理流式：用于 Vue 项目生成，带工具调用
     */
    @Bean
    @Scope("prototype")
    public StreamingChatModel reasoningStreamingModel() {
        // todo 生产环境使用r1模型，临时测试使用v3：
//        final String modelName = "deepseek-chat";
//        final int maxTokens = 8192;
         final String modelName = "deepseek-reasoner";
         final int maxTokens = 32768;
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(true)
                .logResponses(true)
                .build();
    }


}
