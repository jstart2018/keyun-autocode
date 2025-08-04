package com.jstart.keyunautocodebackend.core.codeParser;

import com.jstart.keyunautocodebackend.ai.model.HtmlCodeResult;

import java.util.regex.Pattern;

public interface CodeParser<T> {

    /**
     * 解析代码内容
     * @param resultContent Ai响应内容
     * @return 解析后的结果对象
     */
    T parseCode(String resultContent);


}
