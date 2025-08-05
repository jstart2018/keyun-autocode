package com.jstart.keyunautocodebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.keyunautocodebackend.model.dto.user.UserAddRequest;
import com.jstart.keyunautocodebackend.model.dto.user.UserQueryRequest;
import com.jstart.keyunautocodebackend.model.dto.user.UserUpdateRequest;
import com.jstart.keyunautocodebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jstart.keyunautocodebackend.model.vo.UserVO;

/**
* @author 28435
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-07-29 20:12:17
*/
public interface UserService extends IService<User> {

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    UserVO doLogin(String userAccount, String password);

    Long register(String userAccount, String password, String checkPassword);

    User getLoginUser();

    void updateUser(UserUpdateRequest userUpdateRequest);

    UserVO getUserVO(User user);

    Long addUser(UserAddRequest userAddRequest);

    void RegisterInfoCheck(String userAccount, String userPassword, String checkPassword);

    Page<UserVO> getUserVOByPage(UserQueryRequest userQueryRequest);

    void deleteUserById(Long id);
}
