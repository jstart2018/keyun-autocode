package com.jstart.keyunautocodebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jstart.keyunautocodebackend.model.dto.UserDTO;
import com.jstart.keyunautocodebackend.model.dto.UserQueryByAdmin;
import com.jstart.keyunautocodebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jstart.keyunautocodebackend.model.vo.UserVO;

/**
* @author 28435
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-07-29 20:12:17
*/
public interface UserService extends IService<User> {

    QueryWrapper<User> getQueryWrapper(UserQueryByAdmin userQueryByAdmin);

    Long doLogin(String userAccount, String password);

    Long register(UserDTO userDTO);

    UserVO getLoginUser();

    Long addUser(UserDTO userDTO);

    UserVO editUser(UserDTO userDTO);

    void checkUserInfo(UserDTO userDTO);
}
