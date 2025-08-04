package com.jstart.keyunautocodebackend.core.fileSaver;

import com.jstart.keyunautocodebackend.ai.model.HtmlCodeResult;
import com.jstart.keyunautocodebackend.enums.CodeGenTypeEnum;

import java.io.File;

public class HtmlCodeFileSave extends FileSaveTemplate<HtmlCodeResult>{
    @Override
    protected CodeGenTypeEnum getCodeGenType() {
        return CodeGenTypeEnum.HTML;
    }

    /**
     * 将HTML代码结果写入指定目录
     * @param codeResult 代码结果对象，包含HTML代码
     * @param uploadPath 上传路径
     * @return 保存后的文件目录
     */
    @Override
    protected File writeCodeToFile(HtmlCodeResult codeResult, String uploadPath) {
        saveOneFile(uploadPath, "index.html", codeResult.getHtmlCode());
        return new File(uploadPath);
    }
}
