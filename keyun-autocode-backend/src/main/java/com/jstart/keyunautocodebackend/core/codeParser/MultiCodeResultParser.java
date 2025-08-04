package com.jstart.keyunautocodebackend.core.codeParser;

import com.jstart.keyunautocodebackend.ai.model.MultiFileCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiCodeResultParser implements CodeParser<MultiFileCodeResult> {

    // 正则表达式模式，用于匹配代码块
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);



    @Override
    public MultiFileCodeResult parseCode(String content) {
        MultiFileCodeResult multiFileCodeResult = new MultiFileCodeResult();
        String htmlCode = extractCodeByPattern(content, HTML_CODE_PATTERN);
        String cssCode = extractCodeByPattern(content, CSS_CODE_PATTERN);
        String jsCode = extractCodeByPattern(content, JS_CODE_PATTERN);

        multiFileCodeResult.setHtmlCode(htmlCode);
        multiFileCodeResult.setCssCode(cssCode);
        multiFileCodeResult.setJsCode(jsCode);

        return multiFileCodeResult;
    }

    private String extractCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
