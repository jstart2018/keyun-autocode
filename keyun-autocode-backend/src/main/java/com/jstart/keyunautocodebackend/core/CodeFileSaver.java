package com.jstart.keyunautocodebackend.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.jstart.keyunautocodebackend.ai.model.HtmlCodeResult;
import com.jstart.keyunautocodebackend.ai.model.MultiFileCodeResult;
import com.jstart.keyunautocodebackend.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 代码文件保存器
 * 用于将生成的代码保存到本地文件系统
 */
@Deprecated
public class CodeFileSaver {
    //文件保存的根目录
    private static final String SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    //保存html网页代码
    public static File saveHtmlCode(HtmlCodeResult htmlCodeResult){
        //构建文件的保存路径
        String uploadDir = buildFilePath(CodeGenTypeEnum.HTML.getValue());
        //保存HTML代码
        saveFile(uploadDir, "index.html", htmlCodeResult.getHtmlCode());

        return new File(uploadDir); //返回目录对象，包含生成的HTML文件
    }

    //保存多文件代码
    public static File saveMultiFileCode(MultiFileCodeResult multiFileCodeResult){
        //构建文件的保存路径
        String uploadDir = buildFilePath(CodeGenTypeEnum.MULTI_FILE.getValue());
        //保存HTML代码
         saveFile(uploadDir, "index.html", multiFileCodeResult.getHtmlCode());
         saveFile(uploadDir, "style.css", multiFileCodeResult.getCssCode());
         saveFile(uploadDir, "script.js", multiFileCodeResult.getJsCode());

         return new File(uploadDir); //返回目录对象，包含所有生成的文件

    }


    //构建文件的唯一路径并创建该文件夹（tmp/code_output/bizType_雪花id）
    private static String buildFilePath(String bizType) {
        String uniqueDirName = String.format("%s_%s",bizType,IdUtil.getSnowflakeNextIdStr());
        String uploadDir =  SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(uploadDir); //创建目录
        return uploadDir;

    }

    //保存单个文件
    private static void saveFile(String dirPath, String fileName, String content) {
        //构建文件路径
        String filePath = dirPath + File.separator + fileName;//File.separator是获取系统默认的文件分隔符
        //保存文件
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);

    }




}
