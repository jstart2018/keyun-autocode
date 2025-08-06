package com.jstart.keyunautocodebackend.core.fileSaver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.jstart.keyunautocodebackend.enums.CodeGenTypeEnum;
import com.jstart.keyunautocodebackend.exception.BusinessException;
import com.jstart.keyunautocodebackend.model.ResultEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Slf4j
public abstract class FileSaveTemplate<T> {
    //文件保存的根目录
    private static final String SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";


    /**
     * 代码保存模板方法
     * @param codeResult 生成的代码内容，结构化后的封装对象
     * @param appId 应用ID，用于构建唯一文件目录
     * @return 保存后的 文件目录
     */
    public final File codeSave(T codeResult,Long appId) {

        // 校验输入
        checkInput(codeResult);

        //构建文件保存路径
        CodeGenTypeEnum codeGenType = getCodeGenType();
        String uploadPath = buildFilePath(codeGenType.getValue(),appId);

        //保存代码（子类实现具体保存逻辑）
        return writeCodeToFile(codeResult, uploadPath);

    }



    /**
     * 标识子类的代码生成类型
     */
    protected abstract CodeGenTypeEnum getCodeGenType();

    protected abstract File writeCodeToFile(T codeResult, String uploadPath);


    //模板专用 构建文件的唯一路径并创建该文件夹（tmp/code_output/{bizType}_appId）
    private static String buildFilePath(String bizType,Long appId) {
        String uniqueDirName = String.format("%s_%s", bizType, appId);
        String uploadDir = SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(uploadDir); //创建目录
        return uploadDir;

    }

    /**
     * 保存单个文件到指定目录，可供子类使用，但不能被重写
     */
    protected final void saveOneFile(String dirPath, String fileName, String content) {
        //构建文件路径
        String filePath = dirPath + File.separator + fileName; //File.separator是获取系统默认的文件分隔符
        //保存文件
        if (content != null && !content.isEmpty()) {
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        } else {
            log.error("尝试保存空内容到文件: {}, 路径: {}", fileName, filePath);
        }
    }

    /**
     * 校验输入，可由子类重写自定义实现
     * @param codeResult 代码内容
     */
    protected void checkInput(Object codeResult) {
        if (codeResult == null) {
            throw new BusinessException(ResultEnum.PARAMS_ERROR, "代码生成结果或类型不能为空");
        }
    }

}
