package com.jstart.keyunautocodebackend.model.dto.user;

import com.jstart.keyunautocodebackend.model.dto.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 简介
     */
    private String intro;

    /**
     * 用户角色：user/admin/ban
     */
    private String role;

    private static final long serialVersionUID = 1L;
}