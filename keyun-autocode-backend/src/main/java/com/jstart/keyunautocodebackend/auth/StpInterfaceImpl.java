package com.jstart.keyunautocodebackend.auth;

import cn.dev33.satoken.stp.StpInterface;
import com.jstart.keyunautocodebackend.exception.ThrowUtils;
import com.jstart.keyunautocodebackend.model.ResultEnum;
import com.jstart.keyunautocodebackend.model.entity.User;
import com.jstart.keyunautocodebackend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.jstart.keyunautocodebackend.auth.RoleEnum.ADMIN;
import static com.jstart.keyunautocodebackend.auth.RoleEnum.SUPER_ADMIN;

/**
 * 自定义权限加载接口实现类
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private UserService userService;

    /**
     * 返回一个账号所拥有的权限码集合 
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>(); // 本方法返回 null，表示不需要权限验证
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        long userId = Long.parseLong((String) loginId);
        User u = userService.getById(userId);
        ThrowUtils.throwIf(u == null, ResultEnum.NOT_FOUND_ERROR, "用户不存在");
        RoleEnum roleEnum = RoleEnum.getByKey(u.getRole());
        ThrowUtils.throwIf(roleEnum == null, ResultEnum.NOT_FOUND_ERROR, "异常角色");
        switch (roleEnum) {
            case SUPER_ADMIN:
                return List.of(SUPER_ADMIN.getValue(), ADMIN.getValue()); // 超级管理员拥有所有角色
            case ADMIN:
                return List.of(ADMIN.getValue()); // 管理员拥有 admin 角色
            case NORMAL_USER:
                return new ArrayList<>(); // 普通用户没有角色
            default:
                ThrowUtils.throwIf(true, ResultEnum.NOT_FOUND_ERROR, "异常角色");
        }
        return new ArrayList<>(); // 其他情况返回 空权限
    }

}
