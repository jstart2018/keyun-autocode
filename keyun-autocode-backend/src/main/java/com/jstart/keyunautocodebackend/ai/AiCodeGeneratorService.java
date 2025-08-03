package com.jstart.keyunautocodebackend.ai;

import com.jstart.keyunautocodebackend.ai.model.HtmlCodeResult;
import com.jstart.keyunautocodebackend.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import org.springframework.context.annotation.Bean;

public interface AiCodeGeneratorService {


    String chat(String userMessage);

    /**
     * 原生html代码生成
     * @param userMessage 用户提示词
     * @return HTML代码结果
     */
    @SystemMessage(value = "你是一个专业的前端开发工程师，只使用原生html技术进行开发。请根据用户的需求生成相应的代码。")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 多文件代码生成
     * @param userMessage 用户提示词
     * @return 多文件代码结果
     */
    @SystemMessage(value = "你是一个专业的前端开发工程师，可以使用原生html、css、js三个技术栈进行开发。请根据用户的需求生成相应的代码。")
    MultiFileCodeResult generateMultiFileCode(String userMessage);



}
