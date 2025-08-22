package com.jstart.keyunautocodebackend.ai;

import com.jstart.keyunautocodebackend.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * AI代码生成类型智能路由服务
 * 使用结构化输出直接返回枚举类型
 *
 * @author jstart
 */
public interface SimpleAiTaskService {

    /**
     * 路由：根据用户需求智能选择代码生成类型
     *
     * @param userPrompt 用户输入的需求描述
     * @return 推荐的代码生成类型（框架自动结构化输出为 CodeGenTypeEnum 枚举）
     */
    @SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userPrompt);

    /**
     * 生成appName:简化总结用户的提示词作为appName
     * @param userMessage 用户输入的消息
     * @return appName
     */
    @SystemMessage(fromResource = "prompt/codegen-genAppName-system-prompt.txt")
    String getInitialPrompt(String userMessage);

}
