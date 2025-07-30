package com.jstart.keyunautocodebackend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户
 */
@Data
public class UserDTO implements Serializable {
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String password;

    /**
     * 确认密码
     */
    private String checkPassword;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别（0-未知，1-男，2-女）
     */
    private Integer sex;

    /**
     * 状态（0-禁用，1-正常）
     */
    private Integer status;

    /**
     * 用户类型（0-超级管理员，1-管理员，2-普通用户）
     */
    private Integer role;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 用户简介
     */
    private String intro;


    private static final long serialVersionUID = 1L;
}