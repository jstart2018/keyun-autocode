package com.jstart.keyunautocodebackend.core.fileSaver;

import com.jstart.keyunautocodebackend.ai.model.HtmlCodeResult;
import com.jstart.keyunautocodebackend.ai.model.MultiFileCodeResult;
import com.jstart.keyunautocodebackend.enums.CodeGenTypeEnum;
import com.jstart.keyunautocodebackend.exception.BusinessException;
import com.jstart.keyunautocodebackend.model.ResultEnum;

import java.io.File;

/**
 * 文件保存执行器，负责根据代码生成类型选择合适的模板模板并执行保存操作
 * 主要职责：
 * 1. 根据代码生成类型创建对应的文件保存模板实例
 * 2. 调用模板方法执行代码保存
 */
public class FileSaveExecutor {
    private static final HtmlCodeFileSave htmlCodeFileSave = new HtmlCodeFileSave();

    private static final MultiCodeFileSave multiCodeFileSave = new MultiCodeFileSave();

    /**
     * 执行代码保存
     * @param codeResult 生成的代码内容，结构化后的封装对象
     * @param codeGenTypeEnum 代码生成类型枚举
     * @return 保存后的文件目录
     */
    public static File executeSave(Object codeResult, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeFileSave.codeSave((HtmlCodeResult) codeResult);
            case MULTI_FILE -> multiCodeFileSave.codeSave((MultiFileCodeResult) codeResult);
            default -> throw new BusinessException(ResultEnum.PARAMS_ERROR,"没有这种生成模式: " + codeGenTypeEnum);
        };

    }



}
