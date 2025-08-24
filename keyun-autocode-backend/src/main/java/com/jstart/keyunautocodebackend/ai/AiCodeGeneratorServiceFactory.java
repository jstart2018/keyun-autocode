package com.jstart.keyunautocodebackend.ai;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jstart.keyunautocodebackend.ai.guardrail.PromptSafetyInputGuardrail;
import com.jstart.keyunautocodebackend.ai.tools.FileWriteTool;
import com.jstart.keyunautocodebackend.ai.tools.ToolManager;
import com.jstart.keyunautocodebackend.enums.CodeGenTypeEnum;
import com.jstart.keyunautocodebackend.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource
    @Qualifier("openAiChatModel")
    private ChatModel chatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolManager toolManager;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，catchKey: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 兼容旧的代码
     * 根据 appId 获取服务（带缓存）
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        //适配旧的代码，因为这里html和多文件生成都使用了同一个服务，所以默认返回多文件生成的服务
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.MULTI_FILE);
    }

    /**
     * 根据 生成类型 获取AI服务实例（带缓存）
     * @param appId 应用id，因为缓存键跟应用id绑定了，所以每个应用都会有独立的AI服务实例（多例）
     * @param codeGenTypeEnum 代码生成类型
     * @return AI代码生成服务实例
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId,CodeGenTypeEnum codeGenTypeEnum) {
        String cacheKey = buildCacheKey(appId, codeGenTypeEnum);
        return serviceCache.get(cacheKey, key->createAiCodeGeneratorService(appId, codeGenTypeEnum));
    }

    /**
     * 创建新的 AI 服务实例
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenTypeEnum) {
        log.info("为 appId: {} 创建新的 AI 服务实例", appId);
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        // 当缓存中的AI Service过期时，需要重新创建，同时加载旧的对话记忆
        chatHistoryService.loadHistoryToRedis(appId, chatMemory, 20);

        // 每次创建新的 AiCodeGeneratorService 实例时，都创建新的StreamingChatModel实例，避免StreamingChatModel串行执行问题
        StreamingChatModel qwFlashStreamingModel = applicationContext.getBean("qwFlashStreamingModel", StreamingChatModel.class);
        StreamingChatModel chatStreamingModel = applicationContext.getBean("chatStreamingModel", StreamingChatModel.class);
        StreamingChatModel reasoningStreamingModel = applicationContext.getBean("reasoningStreamingModel", StreamingChatModel.class);

        return switch (codeGenTypeEnum) {
            case HTML -> AiServices.builder(AiCodeGeneratorService.class)
                    .streamingChatModel(qwFlashStreamingModel)
                    .chatMemory(chatMemory)
                    .inputGuardrails(new PromptSafetyInputGuardrail())
                    .build();
            case MULTI_FILE -> AiServices.builder(AiCodeGeneratorService.class)
                    .streamingChatModel(chatStreamingModel)
                    .chatMemory(chatMemory)
                    .inputGuardrails(new PromptSafetyInputGuardrail())
                    .build();
            case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)
                    .streamingChatModel(reasoningStreamingModel)
                    .chatMemoryProvider(memoryId -> chatMemory)
                    .inputGuardrails(new PromptSafetyInputGuardrail())
                    .tools(toolManager.getAllTools()) // 注册所有工具
                    .maxSequentialToolsInvocations(10) // 最大连续调用工具次数
                    // 处理工具调用幻觉问题
                    .hallucinatedToolNameStrategy(hallucinatedToolName ->
                            ToolExecutionResultMessage.from(hallucinatedToolName,
                                    "Error: there is no such tool: " + hallucinatedToolName.name()))
                    .build();
            default -> throw new IllegalArgumentException("不支持的代码生成类型: " + codeGenTypeEnum);
        };


    }


    // 创建AiCodeGeneratorService的 流式输出 实例
    @Bean
    @Deprecated
    public AiCodeGeneratorService aiCodeGeneratorServiceStream() {
        return getAiCodeGeneratorService(0L); // 使用0作为默认appId
    }


    // 创建AiCodeGeneratorService的实例
    //@Bean 使用框架自动整合的redis记忆能力
    @Deprecated
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .chatMemoryStore(redisChatMemoryStore)
                        .maxMessages(20)
                        .build())
                .build();
    }

    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenTypeEnum) {
        return  codeGenTypeEnum.getValue()+"_"+appId;
    }


}
