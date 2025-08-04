package com.jstart.keyunautocodebackend.core.codeParser;

import com.jstart.keyunautocodebackend.enums.CodeGenTypeEnum;
import com.jstart.keyunautocodebackend.exception.BusinessException;
import com.jstart.keyunautocodebackend.model.ResultEnum;

public class CodeParserExecutor {

    private static final HtmlCodeResultParser htmlCodeResultParser = new HtmlCodeResultParser();

    private static final MultiCodeResultParser multiCodeResultParser = new MultiCodeResultParser();


    public static Object executeParser(String content, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeResultParser.parseCode(content);
            case MULTI_FILE -> multiCodeResultParser.parseCode(content);
            default -> throw new BusinessException(ResultEnum.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenTypeEnum);
        };
    }





}
