package com.jstart.keyunautocodebackend.core;


import com.jstart.keyunautocodebackend.ai.AiCodeGeneratorService;
import com.jstart.keyunautocodebackend.ai.AiCodeGeneratorServiceFactory;
import com.jstart.keyunautocodebackend.ai.model.HtmlCodeResult;
import com.jstart.keyunautocodebackend.ai.model.MultiFileCodeResult;
import com.jstart.keyunautocodebackend.core.codeParser.CodeParserExecutor;
import com.jstart.keyunautocodebackend.core.fileSaver.FileSaveExecutor;
import com.jstart.keyunautocodebackend.enums.CodeGenTypeEnum;
import com.jstart.keyunautocodebackend.exception.BusinessException;
import com.jstart.keyunautocodebackend.model.ResultEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI代码生成器 门面类，组合代码生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;


    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ResultEnum.SYSTEM_ERROR, "生成类型为空");
        }
        //获取 AI 代码生成服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);

        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield parserAndSaveResult(result, codeGenTypeEnum, appId);
            }
            case MULTI_FILE -> {
                Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield parserAndSaveResult(result, codeGenTypeEnum, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ResultEnum.SYSTEM_ERROR, errorMessage);
            }
        };
    }


    private static Flux<String> parserAndSaveResult(Flux<String> result, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 当流式返回生成代码完成后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        // 实时收集代码片段
        return result
                .doOnNext(codeBuilder::append) // 实时收集代码片段
                .doOnComplete(() -> {
                    // 流式返回完成后保存代码
                    try {
                        String completeMultiFileCode = codeBuilder.toString();
                        // 使用解析器解析AI结果到 对象中
                        Object multiFileResult = CodeParserExecutor.executeParser(completeMultiFileCode, codeGenTypeEnum);
                        // 使用解析器 保存代码到文件
                        File savedDir = FileSaveExecutor.executeSave(multiFileResult, codeGenTypeEnum, appId);
                        log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败: {}", e.getMessage());
                    }
                });
    }

}
