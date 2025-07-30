package com.jstart.keyunautocodebackend.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.jstart.keyunautocodebackend.exception.ThrowUtils;
import com.jstart.keyunautocodebackend.model.Result;
import com.jstart.keyunautocodebackend.model.ResultEnum;
import com.jstart.keyunautocodebackend.model.dto.UserDTO;
import com.jstart.keyunautocodebackend.model.vo.UserVO;
import com.jstart.keyunautocodebackend.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Result<Long> login(String userAccount, String password) {
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), ResultEnum.PARAMS_ERROR, "账号不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(password), ResultEnum.PARAMS_ERROR, "密码不能为空");
        Long userId = userService.doLogin(userAccount, password);

        return Result.success(userId);
    }

    @PostMapping("/register")
    public Result<Long> register(@RequestBody UserDTO userDTO) {
        ThrowUtils.throwIf(userDTO == null, ResultEnum.PARAMS_ERROR, "用户信息不能为空");
        Long id = userService.register(userDTO);

        return Result.success(id);
    }

    @GetMapping("/getLoginUser")
    public Result<UserVO> getLoginUser() {
        UserVO userVO = userService.getLoginUser();

        return Result.success(userVO);
    }


    @GetMapping("/logout")
    public Result<?> logout() {
        StpUtil.getLoginId();
        StpUtil.logout();
        return Result.success("退出成功");
    }

    @PostMapping("/editUser")
    public Result<UserVO> editUser(@RequestBody UserDTO userDTO) {
        ThrowUtils.throwIf(userDTO == null, ResultEnum.PARAMS_ERROR, "用户信息不能为空");
        UserVO userVO = userService.editUser(userDTO);

        return Result.success(userVO);
    }




}
