package com.jstart.keyunautocodebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户简介
     */
    private String intro;

    /**
     * 用户角色: user, admin
     */
    private String role;

    private static final long serialVersionUID = 1L;
}