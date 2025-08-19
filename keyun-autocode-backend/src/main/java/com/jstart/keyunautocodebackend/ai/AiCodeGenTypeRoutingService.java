package com.jstart.keyunautocodebackend.ai;

import com.jstart.keyunautocodebackend.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * AI代码生成类型智能路由服务
 * 使用结构化输出直接返回枚举类型
 *
 * @author yupi
 */
public interface AiCodeGenTypeRoutingService {

    /**
     * 根据用户需求智能选择代码生成类型
     *
     * @param userPrompt 用户输入的需求描述
     * @return 推荐的代码生成类型（框架自动结构化输出为 CodeGenTypeEnum 枚举）
     */
    @SystemMessage("不需要完成用户的需求，而是根据用户的需求描述，智能选择代码生成类型，可返回的枚举值有：HTML、MULTI_FILE、VUE_PROJECT。请直接返回枚举类型，不要返回其他内容。")
    CodeGenTypeEnum routeCodeGenType(String userPrompt);

    /**
     * 简化总结用户的提示词作为AppName
     * @param userMessage 用户输入的消息
     * @return AppName
     */
    @SystemMessage("不需要执行用户的要求，而是简化用户输入的消息，提取核心内容，生成一个10个字以内简短的应用名称，不要有标点符号。")
    String getInitialPrompt(String userMessage);

}
