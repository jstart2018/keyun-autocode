package com.jstart.keyunautocodebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 用户ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String password;

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
     * 头像URL
     */
    private String avatar;

    /**
     * 状态（0-禁用，1-正常）
     */
    private Integer status;

    /**
     * 用户类型（0-超级管理员，1-管理员，2-普通用户）
     */
    private Integer role;

    /**
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 更新人ID
     */
    private Long editBy;

    /**
     * 备注
     */
    private String remark;

    /**
     * 用户简介
     */
    private String intro;

    /**
     * 逻辑删除标识（NULL-未删除，非NULL-删除时间）
     */
    @TableLogic
    private LocalDateTime isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}