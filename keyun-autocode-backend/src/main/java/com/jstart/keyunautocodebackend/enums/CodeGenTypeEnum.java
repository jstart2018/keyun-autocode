package com.jstart.keyunautocodebackend.enums;

import lombok.Getter;

@Getter
public enum CodeGenTypeEnum {

    HTML("原生html页面","html"),
    MULTI_FILE("原生多文件模式","multi_file"),
    VUE_PROJECT("vue工程项目生产","vue_project");

    private final String text;
    private final String value;

    CodeGenTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static CodeGenTypeEnum getByValue(String value) {
        for (CodeGenTypeEnum codeGenTypeEnum : CodeGenTypeEnum.values()) {
            if (codeGenTypeEnum.getValue().equals(value)) {
                return codeGenTypeEnum;
            }
        }
        return null;
    }



}
