package com.jstart.keyunautocodebackend.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jstart.keyunautocodebackend.exception.ThrowUtils;
import com.jstart.keyunautocodebackend.model.Result;
import com.jstart.keyunautocodebackend.model.ResultEnum;
import com.jstart.keyunautocodebackend.model.dto.UserDTO;
import com.jstart.keyunautocodebackend.model.dto.UserQueryByAdmin;
import com.jstart.keyunautocodebackend.model.entity.User;
import com.jstart.keyunautocodebackend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private UserService userService;

    /**
     * 管理员查询用户列表
     *
     * @param userQueryByAdmin
     * @return
     */
    @PostMapping("/listUser")
    public Result<List<User>> listUser(@RequestBody UserQueryByAdmin userQueryByAdmin) {
        ThrowUtils.throwIf(userQueryByAdmin == null, ResultEnum.PARAMS_ERROR, "用户信息不能为空");

        QueryWrapper<User> queryWrapper = userService.getQueryWrapper(userQueryByAdmin);

        List<User> userList = userService.list(queryWrapper);
        ThrowUtils.throwIf(userList == null, ResultEnum.OPERATION_ERROR, "用户列表查询失败");
        userList.stream().map(user -> {
            user.setPassword(null); // 不返回密码
            return user;
        });

        return Result.success(userList);

    }


    @DeleteMapping("/deleteUser/{id}")
    public Result<?> deleteById(@PathVariable(value="id") Long id) {
        ThrowUtils.throwIf(id == null, ResultEnum.PARAMS_ERROR, "用户ID不能为空");
        ThrowUtils.throwIf(userService.getById(id) == null, ResultEnum.NOT_FOUND_ERROR, "用户不存在");
        ThrowUtils.throwIf(!userService.removeById(id), ResultEnum.NOT_FOUND_ERROR, "删除失败");
        return Result.success();
    }

    @PostMapping("/addUser")
    public Result addUser(@RequestBody UserDTO userDTO){
        ThrowUtils.throwIf(userDTO == null, ResultEnum.PARAMS_ERROR, "用户信息不能为空");
        Long id = userService.addUser(userDTO);

        return Result.success(id);


    }


}
