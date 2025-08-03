package com.jstart.keyunautocodebackend.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.keyunautocodebackend.auth.RoleEnum;
import com.jstart.keyunautocodebackend.enums.UserStatusEnum;
import com.jstart.keyunautocodebackend.exception.BusinessException;
import com.jstart.keyunautocodebackend.exception.ThrowUtils;
import com.jstart.keyunautocodebackend.model.ResultEnum;
import com.jstart.keyunautocodebackend.model.dto.UserDTO;
import com.jstart.keyunautocodebackend.model.dto.UserQueryByAdmin;
import com.jstart.keyunautocodebackend.model.entity.User;
import com.jstart.keyunautocodebackend.model.vo.UserVO;
import com.jstart.keyunautocodebackend.service.UserService;
import com.jstart.keyunautocodebackend.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
* @author 28435
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-07-29 20:12:17
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    private static final String SALT = "qianyv";

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryByAdmin userQueryByAdmin) {
        Long id = userQueryByAdmin.getId();
        String userAccount = userQueryByAdmin.getUserAccount();
        String username = userQueryByAdmin.getUsername();
        String phone = userQueryByAdmin.getPhone();
        String email = userQueryByAdmin.getEmail();
        Integer sex = userQueryByAdmin.getSex();
        Integer status = userQueryByAdmin.getStatus();
        Integer role = userQueryByAdmin.getRole();
        Long deptId = userQueryByAdmin.getDeptId();
        Date lastLoginTime = userQueryByAdmin.getLastLoginTime();
        String lastLoginIp = userQueryByAdmin.getLastLoginIp();
        Date createTime = userQueryByAdmin.getCreateTime();
        Date editTime = userQueryByAdmin.getEditTime();
        Date updateTime = userQueryByAdmin.getUpdateTime();
        Long createBy = userQueryByAdmin.getCreateBy();
        Long editBy = userQueryByAdmin.getEditBy();
        String remark = userQueryByAdmin.getRemark();
        String intro = userQueryByAdmin.getIntro();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(id != null, User::getId, id)
                .like(StringUtils.isNotBlank(userAccount), User::getUserAccount, userAccount)
                .like(StringUtils.isNotBlank(username), User::getUsername, username)
                .like(StringUtils.isNotBlank(phone), User::getPhone, phone)
                .like(StringUtils.isNotBlank(email), User::getEmail, email)
                .eq(sex != null, User::getSex, sex)
                .eq(status != null, User::getStatus, status)
                .eq(role != null, User::getRole, role)
                .eq(deptId != null, User::getDeptId, deptId)
                .ge(lastLoginTime != null, User::getLastLoginTime, lastLoginTime)
                .like(StringUtils.isNotBlank(lastLoginIp), User::getLastLoginIp, lastLoginIp)
                .ge(createTime != null, User::getCreateTime, createTime)
                .ge(editTime != null, User::getEditTime, editTime)
                .ge(updateTime != null, User::getUpdateTime, updateTime)
                .ge(updateTime != null, User::getUpdateTime, updateTime)
                .eq(createBy != null, User::getCreateBy, createBy)
                .eq(editBy != null, User::getEditBy, editBy)
                .like(StringUtils.isNotBlank(remark), User::getRemark, remark)
                .like(StringUtils.isNotBlank(intro), User::getIntro, intro);



        return queryWrapper;
    }

    @Override
    public Long doLogin(String userAccount, String password) {
        User u = this.getOne(new QueryWrapper<User>().lambda()
                .eq(User::getUserAccount, userAccount)
                .eq(User::getPassword, encrypt(password)));
        if (u == null) {
            throw new BusinessException(ResultEnum.OPERATION_ERROR, "账号或密码错误");
        }
        StpUtil.login(u.getId());

        return u.getId();
    }

    @Override
    public Long register(UserDTO userDTO) {
        checkUserInfo(userDTO);//校验参数
        User user = new User();
        user.setUserAccount(userDTO.getUserAccount());
        user.setPassword(encrypt(userDTO.getPassword()));//加密
        user.setUsername(String.format("可云%s", RandomUtil.randomNumbers(5)));
        //todo: 补充操作数据库的原子性，防止重复注册
        User one = this.getOne(new QueryWrapper<User>().lambda().eq(User::getUserAccount, user.getUserAccount()));
        ThrowUtils.throwIf(one != null, ResultEnum.PARAMS_ERROR, "账号已存在");
        boolean save = this.save(user);
        ThrowUtils.throwIf(!save, ResultEnum.OPERATION_ERROR, "注册失败");

        return user.getId();
    }



    @Override
    public UserVO getLoginUser() {
        Long loginId = Long.parseLong(StpUtil.getLoginId().toString());
        ThrowUtils.throwIf(loginId == null, ResultEnum.NOT_LOGIN_ERROR, "用户未登录");
        User user = this.getOne(new QueryWrapper<User>().lambda().eq(User::getId, loginId));
        return userToUserVO(user);
    }


    @Override
    public Long addUser(UserDTO userDTO) {
        checkUserInfo(userDTO);//校验参数
        RoleEnum roleEnum = RoleEnum.getByKey(userDTO.getRole());
        ThrowUtils.throwIf(roleEnum == null, ResultEnum.PARAMS_ERROR, "角色不存在");
        if (roleEnum.getValue().equals(RoleEnum.SUPER_ADMIN.getValue())){
            StpUtil.checkRole(RoleEnum.SUPER_ADMIN.getValue());
        }
        User user = new User();
        user.setUserAccount(userDTO.getUserAccount());
        user.setRole(roleEnum.getKey());
        user.setPassword(encrypt(userDTO.getPassword()));//加密
        user.setUsername(String.format("账号%s", RandomUtil.randomNumbers(5)));
        //todo: 补充操作数据库的原子性，防止重复注册
        User one = this.getOne(new QueryWrapper<User>().lambda().eq(User::getUserAccount, user.getUserAccount()));
        ThrowUtils.throwIf(one != null, ResultEnum.PARAMS_ERROR, "账号已存在");
        ThrowUtils.throwIf(!this.save(user), ResultEnum.OPERATION_ERROR, "添加失败");

        return user.getId();
    }

    @Override
    public UserVO editUser(UserDTO userDTO) {
        User u = new User();
        BeanUtils.copyProperties(userDTO, u);
        u.setPassword(encrypt(userDTO.getPassword()));//加密
        u.setRole(RoleEnum.NORMAL_USER.getKey());
        u.setStatus(UserStatusEnum.DISABLE.getKey());
        u.setEditBy(Long.parseLong(StpUtil.getLoginId().toString()));

        ThrowUtils.throwIf(save(u), ResultEnum.OPERATION_ERROR, "修改失败");
        UserVO uv = new UserVO();
        BeanUtils.copyProperties(u, uv);
        return uv;
    }

    /**
     * 校验参数
     * @param userDTO
     */
    @Override
    public void checkUserInfo(UserDTO userDTO) {
        ThrowUtils.throwIf(StringUtils.isBlank(userDTO.getUserAccount()), ResultEnum.PARAMS_ERROR, "账号不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(userDTO.getPassword()), ResultEnum.PARAMS_ERROR, "密码不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(userDTO.getCheckPassword()), ResultEnum.PARAMS_ERROR, "确认密码不能为空");
        ThrowUtils.throwIf(!userDTO.getPassword().equals(userDTO.getCheckPassword()), ResultEnum.PARAMS_ERROR, "两次输入的密码不一致");
        if (userDTO.getUserAccount().length()<6 || userDTO.getUserAccount().length()>16) {
            throw new BusinessException(ResultEnum.PARAMS_ERROR, "账号长度必须在6-16之间");
        }
        if (userDTO.getPassword().length()<6 || userDTO.getPassword().length()>20) {
            throw new BusinessException(ResultEnum.PARAMS_ERROR, "密码长度必须在6-20之间");
        }
    }


    private String encrypt(String text) {
        ThrowUtils.throwIf(text.length()<6,ResultEnum.PARAMS_ERROR,"明文过小");
        String replace = text.replace(text.substring(1, 3), SALT);
        return SaSecureUtil.sha1(replace);
    }

    public UserVO userToUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO uservo = new UserVO();
        BeanUtils.copyProperties(user,uservo);
        return uservo;
    }


}




