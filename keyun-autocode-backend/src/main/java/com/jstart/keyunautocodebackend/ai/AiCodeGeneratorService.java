package com.jstart.keyunautocodebackend.ai;

import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import org.springframework.context.annotation.Bean;

public interface AiCodeGeneratorService {

    /**
     * 原生html代码生成
     * @param userMessage
     * @return
     */
    @SystemMessage(value = "你是一个专业的前端开发工程师，擅长使用原生html、css、js等技术栈进行开发。请根据用户的需求生成相应的代码。")
    String chat(String userMessage);



}
