package com.jstart.keyunautocodebackend.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 用户表
 * @TableName user
 */
@Data
public class UserVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 用户类型（0-超级管理员，1-管理员，2-普通用户）
     */
    private String role;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 编辑时间
     */
    private LocalDateTime updateTime;


    /**
     * 用户简介
     */
    private String intro;

    private static final long serialVersionUID = 1L;
}