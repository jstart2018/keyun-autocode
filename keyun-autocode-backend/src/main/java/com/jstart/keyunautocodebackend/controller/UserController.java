package com.jstart.keyunautocodebackend.controller;


import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.keyunautocodebackend.auth.RoleEnum;
import com.jstart.keyunautocodebackend.exception.BusinessException;
import com.jstart.keyunautocodebackend.exception.ThrowUtils;
import com.jstart.keyunautocodebackend.model.Result;
import com.jstart.keyunautocodebackend.model.ResultEnum;
import com.jstart.keyunautocodebackend.model.dto.DeleteRequest;
import com.jstart.keyunautocodebackend.model.dto.user.*;
import com.jstart.keyunautocodebackend.model.entity.User;
import com.jstart.keyunautocodebackend.model.vo.UserVO;
import com.jstart.keyunautocodebackend.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Result<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ResultEnum.PARAMS_ERROR, "用户信息不能为空");
        Long id = userService.register(userRegisterRequest.getUserAccount(),
                userRegisterRequest.getPassword(),
                userRegisterRequest.getCheckPassword());

        return Result.success(id);
    }

    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody UserLoginRequest userLoginRequest) {
        ThrowUtils.throwIf(StringUtils.isBlank(userLoginRequest.getUserAccount()), ResultEnum.PARAMS_ERROR, "账号不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(userLoginRequest.getUserPassword()), ResultEnum.PARAMS_ERROR, "密码不能为空");
        UserVO userVO = userService.doLogin(userLoginRequest.getUserAccount(), userLoginRequest.getUserPassword());

        return Result.success(userVO);
    }


    @GetMapping("/get/login")
    public Result<UserVO> getLoginUser() {
        User loginUser = userService.getLoginUser();

        UserVO userVO = userService.getUserVO(loginUser);

        return Result.success(userVO);
    }


    @PostMapping("/logout")
    public Result<Boolean> logout() {
        userService.getLoginUser(); // 确保用户已登录
        StpUtil.logout();
        return Result.success(true);
    }

    /**
     * 创建用户（仅 超级管理员）
     * @param userAddRequest 用户信息
     * @return 新增的用户ID
     */
    @SaCheckRole("super_admin")
    @PostMapping("/add")
    public Result<Long> addUser(@RequestBody UserAddRequest userAddRequest){
        ThrowUtils.throwIf(userAddRequest == null, ResultEnum.PARAMS_ERROR, "用户信息不能为空");
        Long id = userService.addUser(userAddRequest);

        return Result.success(id);

    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @SaCheckRole("admin")
    @GetMapping("/get")
    public Result<User> getUserByIdByAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ResultEnum.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ResultEnum.NOT_FOUND_ERROR);
        return Result.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public Result<UserVO> getUserVOById(long id) {
        Result<User> response = getUserByIdByAdmin(id);
        User user = response.getData();
        return Result.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ResultEnum.PARAMS_ERROR);
        }
        userService.deleteUserById(deleteRequest.getId());

        return Result.success(true);
    }



    @PostMapping("/update")
    public Result<Boolean> editUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null, ResultEnum.PARAMS_ERROR, "用户信息不能为空");
        userService.updateUser(userUpdateRequest);

        return Result.success(true);
    }


    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @SaCheckRole("admin")
    @PostMapping("/list/page/vo")
    public Result<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ResultEnum.PARAMS_ERROR);

        Page<UserVO> userVOPage = userService.getUserVOByPage(userQueryRequest);

        return Result.success(userVOPage);
    }


}
