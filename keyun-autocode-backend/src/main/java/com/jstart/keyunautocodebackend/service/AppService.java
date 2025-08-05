package com.jstart.keyunautocodebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.keyunautocodebackend.model.dto.app.AppAdminUpdateRequest;
import com.jstart.keyunautocodebackend.model.dto.app.AppQueryRequest;
import com.jstart.keyunautocodebackend.model.entity.App;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jstart.keyunautocodebackend.model.vo.AppVO;

import java.util.List;

/**
* @author 28435
* @description 针对表【app(应用)】的数据库操作Service
* @createDate 2025-08-05 10:27:52
*/
public interface AppService extends IService<App> {

    Long createApp(String initPrompt);

    void updateAppById(App app);

    void removeAppById(Long appId);

    AppVO getAppVO(App app);

    QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest);

    List<AppVO> getAppVOList(List<App> appList);


    Page<App> listAppByPage(AppQueryRequest appQueryRequest);

    void updateAppByAdmin(AppAdminUpdateRequest appAdminUpdateRequest);
}
