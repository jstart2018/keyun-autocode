package com.jstart.keyunautocodebackend.core;


import com.jstart.keyunautocodebackend.ai.AiCodeGeneratorService;
import com.jstart.keyunautocodebackend.ai.model.HtmlCodeResult;
import com.jstart.keyunautocodebackend.ai.model.MultiFileCodeResult;
import com.jstart.keyunautocodebackend.enums.CodeGenTypeEnum;
import com.jstart.keyunautocodebackend.exception.BusinessException;
import com.jstart.keyunautocodebackend.model.ResultEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * AI代码生成器 门面类，组合代码生成和保存功能
 */
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据用户输入的消息和代码生成类型，生成并保存代码文件
     * @param userMessage 用户提示词
     * @param codeGenTypeEnum 代码生成类型
     * @return 文件保存目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ResultEnum.PARAMS_ERROR, "代码生成类型不能为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileeCode(userMessage);
            default ->throw new BusinessException(ResultEnum.PARAMS_ERROR, "不支持的代码生成类型");
        };
    }


    /**
     * 生成并保存HTML代码
     * @param userMessage 用户提示词
     * @return HTML文件目录
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCode(htmlCodeResult);
    }

    /**
     * 生成并保存多文件代码
     * @param userMessage 用户提示词
     * @return 多文件代码目录
     */
    private File generateAndSaveMultiFileeCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCode(multiFileCodeResult);
    }

}
