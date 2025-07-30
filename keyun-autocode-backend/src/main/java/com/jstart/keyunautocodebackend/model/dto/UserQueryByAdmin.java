package com.jstart.keyunautocodebackend.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 */
@Data
public class UserQueryByAdmin implements Serializable {
    /**
     * 用户ID（主键）
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


    private static final long serialVersionUID = 1L;
}