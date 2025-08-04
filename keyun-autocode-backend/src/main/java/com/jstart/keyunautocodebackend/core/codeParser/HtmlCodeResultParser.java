package com.jstart.keyunautocodebackend.core.codeParser;

import com.jstart.keyunautocodebackend.ai.model.HtmlCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlCodeResultParser implements CodeParser<HtmlCodeResult> {

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);


    @Override
    public HtmlCodeResult parseCode(String content) {
        HtmlCodeResult htmlCodeResult = new HtmlCodeResult();
        String htmlCode = extractCodeByPattern(content);
        htmlCodeResult.setHtmlCode(htmlCode);
        return htmlCodeResult;
    }


    private static String extractCodeByPattern(String content) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

}
