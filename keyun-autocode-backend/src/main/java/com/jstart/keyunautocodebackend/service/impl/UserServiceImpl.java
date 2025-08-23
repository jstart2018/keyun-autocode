package com.jstart.keyunautocodebackend.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.keyunautocodebackend.auth.RoleEnum;
import com.jstart.keyunautocodebackend.exception.BusinessException;
import com.jstart.keyunautocodebackend.exception.ThrowUtils;
import com.jstart.keyunautocodebackend.model.ResultEnum;
import com.jstart.keyunautocodebackend.model.dto.user.UserAddRequest;
import com.jstart.keyunautocodebackend.model.dto.user.UserQueryRequest;
import com.jstart.keyunautocodebackend.model.dto.user.UserUpdateRequest;
import com.jstart.keyunautocodebackend.model.entity.User;
import com.jstart.keyunautocodebackend.model.vo.UserVO;
import com.jstart.keyunautocodebackend.service.AppService;
import com.jstart.keyunautocodebackend.service.UserService;
import com.jstart.keyunautocodebackend.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 28435
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-07-29 20:12:17
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    private static final String SALT = "qianyv";

    @Resource
    @Lazy
    private AppService appService;

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        Long id = userQueryRequest.getId();
        String username = userQueryRequest.getUsername();
        String userAccount = userQueryRequest.getUserAccount();
        String intro = userQueryRequest.getIntro();
        String role = userQueryRequest.getRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        QueryWrapper<User> qw = new QueryWrapper<>();

        qw.eq(id!=null,"id", id)
          .like(StringUtils.isNotBlank(username), "username", username)
          .like(StringUtils.isNotBlank(userAccount), "user_account", userAccount)
          .like(StringUtils.isNotBlank(intro), "intro", intro)
          .eq(StringUtils.isNotBlank(role), "role", role)
          .orderBy( StringUtils.isNotBlank(sortField),
                        "ascend".equals(sortOrder), sortField);
        return qw;
    }

    @Override
    public UserVO doLogin(String userAccount, String password) {
        User u = this.getOne(new QueryWrapper<User>().lambda()
                .eq(User::getUserAccount, userAccount)
                .eq(User::getPassword, encrypt(password)));
        if (u == null) {
            throw new BusinessException(ResultEnum.OPERATION_ERROR, "账号或密码错误");
        }
        StpUtil.login(u.getId());
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(u, userVO);

        return userVO;
    }

    @Override
    public Long register(String userAccount, String password, String checkPassword) {
        RegisterInfoCheck(userAccount, password, checkPassword);//校验参数
        User user = new User();
        user.setUserAccount(userAccount);
        user.setPassword(encrypt(password));//加密
        user.setUsername(String.format("可云%s", RandomUtil.randomNumbers(5)));
        //todo: 补充操作数据库的原子性，防止重复注册
        User one = this.getOne(new QueryWrapper<User>().lambda().eq(User::getUserAccount, user.getUserAccount()));
        ThrowUtils.throwIf(one != null, ResultEnum.PARAMS_ERROR, "账号已存在");
        boolean save = this.save(user);
        ThrowUtils.throwIf(!save, ResultEnum.OPERATION_ERROR, "注册失败");

        return user.getId();
    }


    @Override
    public User getLoginUser() {
        Long loginId = Long.parseLong(StpUtil.getLoginId().toString());
        ThrowUtils.throwIf(loginId == null, ResultEnum.NOT_LOGIN_ERROR, "用户未登录");
        User u = this.getOne(new QueryWrapper<User>().lambda().eq(User::getId, loginId));
        if (u == null) {
            StpUtil.logout();
            throw new BusinessException(ResultEnum.NOT_LOGIN_ERROR, "用户已不存在");
        }
        return u;
    }


    @Override
    public Long addUser(UserAddRequest userAddRequest) {
        RegisterInfoCheck(userAddRequest.getUserAccount(),userAddRequest.getPassword(), userAddRequest.getPassword());//校验参数
        RoleEnum roleEnum = RoleEnum.getByValue(userAddRequest.getRole()== null ? RoleEnum.NORMAL_USER.getValue() : userAddRequest.getRole());
        ThrowUtils.throwIf(roleEnum == null, ResultEnum.PARAMS_ERROR, "角色不存在");

        User user = new User();
        user.setCreateBy(Long.parseLong(StpUtil.getLoginId().toString()));
        user.setUserAccount(userAddRequest.getUserAccount());
        user.setRole(roleEnum.getKey());
        user.setPassword(encrypt(userAddRequest.getPassword()));//加密
        user.setUsername(String.format("创建账号%s", RandomUtil.randomNumbers(5)));
        //todo: 补充操作数据库的原子性，防止重复注册
        User one = this.getOne(new QueryWrapper<User>().lambda().eq(User::getUserAccount, user.getUserAccount()));
        ThrowUtils.throwIf(one != null, ResultEnum.PARAMS_ERROR, "账号已存在");
        ThrowUtils.throwIf(!this.save(user), ResultEnum.OPERATION_ERROR, "添加失败");

        return user.getId();
    }

    @Override
    public void updateUser(UserUpdateRequest userUpdateRequest) {
        Long id = userUpdateRequest.getId();
        ThrowUtils.throwIf(id == null, ResultEnum.PARAMS_ERROR, "用户 id 不能为空");
        //只有自己能修改自己的信息，超级管理员可以修改所有用户信息
        if (!StpUtil.hasRole(RoleEnum.ADMIN.getValue())) {
            Long loginIdAsLong = StpUtil.getLoginIdAsLong();
            ThrowUtils.throwIf(!loginIdAsLong.equals(id), ResultEnum.OPERATION_ERROR, "只能修改自己的信息");
        }

        User user = getById(id);// 校验用户是否存在
        ThrowUtils.throwIf(user == null, ResultEnum.NOT_FOUND_ERROR, "用户不存在");

        User u = new User();
        BeanUtils.copyProperties(userUpdateRequest, u);

        if (StringUtils.isNotBlank(userUpdateRequest.getRole())) {
            RoleEnum roleEnum = RoleEnum.getByValue(userUpdateRequest.getRole());
            ThrowUtils.throwIf(roleEnum == null, ResultEnum.PARAMS_ERROR, "角色不存在");
            u.setRole(roleEnum.getKey());
        }
        u.setEditBy(Long.parseLong(StpUtil.getLoginId().toString()));

        ThrowUtils.throwIf(!updateById(u), ResultEnum.OPERATION_ERROR, "修改失败");

    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO uservo = new UserVO();
        BeanUtils.copyProperties(user, uservo);
        RoleEnum roleEnum = RoleEnum.getByKey(user.getRole());
        uservo.setRole(roleEnum != null ? roleEnum.getValue() : null);
        return uservo;
    }

    /**
     * 校验参数
     */
    @Override
    public void RegisterInfoCheck(String userAccount, String userPassword, String checkPassword) {
        ThrowUtils.throwIf(StringUtils.isBlank(userAccount), ResultEnum.PARAMS_ERROR, "账号不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(userPassword), ResultEnum.PARAMS_ERROR, "密码不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(checkPassword), ResultEnum.PARAMS_ERROR, "确认密码不能为空");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ResultEnum.PARAMS_ERROR, "两次输入的密码不一致");
        if (userAccount.length() < 6 || userAccount.length() > 16) {
            throw new BusinessException(ResultEnum.PARAMS_ERROR, "账号长度必须在6-16之间");
        }
        if (userPassword.length() < 6 || userPassword.length() > 20) {
            throw new BusinessException(ResultEnum.PARAMS_ERROR, "密码长度必须在6-20之间");
        }
    }

    @Override
    public Page<UserVO> getUserVOByPage(UserQueryRequest userQueryRequest) {

        int pageNum = userQueryRequest.getPageNum();
        int pageSize = userQueryRequest.getPageSize();

        Page<User> userPage = this.page(new Page<>(pageNum, pageSize),
                this.getQueryWrapper(userQueryRequest));

        List<User> userList = this.list(userPage);

        List<UserVO> userVOList = userList.stream().map(this::getUserVO).toList();

        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotal());
        userVOPage.setRecords(userVOList);

        return userVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserById(Long id) {
        if (!StpUtil.hasRole(RoleEnum.ADMIN.getValue())){
            User loginUser = this.getLoginUser();
            ThrowUtils.throwIf(!loginUser.getId().equals(id), ResultEnum.OPERATION_ERROR, "只能删除自己的账号");
        }
        boolean result = appService.removeAppByUserId(id);// 删除用户的应用
        StpUtil.logout(id);
        ThrowUtils.throwIf(!this.removeById(id) || !result,ResultEnum.SYSTEM_ERROR, "系统错误，删除失败");

    }


    private String encrypt(String text) {
        ThrowUtils.throwIf(text.length() < 6, ResultEnum.PARAMS_ERROR, "不符合密码长度要求");
        String replace = text.replace(text.substring(1, 3), SALT);
        return SaSecureUtil.sha1(replace);
    }


}




