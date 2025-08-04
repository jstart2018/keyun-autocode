package com.jstart.keyunautocodebackend.core.fileSaver;

import com.jstart.keyunautocodebackend.ai.model.MultiFileCodeResult;
import com.jstart.keyunautocodebackend.enums.CodeGenTypeEnum;

import java.io.File;

public class MultiCodeFileSave extends FileSaveTemplate<MultiFileCodeResult>{

    @Override
    protected CodeGenTypeEnum getCodeGenType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    /**
     * 将多文件代码结果写入指定目录
     * @param codeResult 代码结果对象，包含HTML、CSS和JS代码
     * @param uploadPath 上传路径
     * @return 保存后的文件目录
     */
    @Override
    protected File writeCodeToFile(MultiFileCodeResult codeResult, String uploadPath) {

        saveOneFile(uploadPath, "index.html", codeResult.getHtmlCode());
        saveOneFile(uploadPath, "style.css", codeResult.getCssCode());
        saveOneFile(uploadPath, "script.js", codeResult.getJsCode());

        return new File(uploadPath);
    }
}
