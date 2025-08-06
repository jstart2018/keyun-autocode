package com.jstart.keyunautocodebackend.controller;


import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.keyunautocodebackend.constant.AppConstant;
import com.jstart.keyunautocodebackend.exception.BusinessException;
import com.jstart.keyunautocodebackend.exception.ThrowUtils;
import com.jstart.keyunautocodebackend.model.Result;
import com.jstart.keyunautocodebackend.model.ResultEnum;
import com.jstart.keyunautocodebackend.model.dto.DeleteRequest;
import com.jstart.keyunautocodebackend.model.dto.app.AppAddRequest;
import com.jstart.keyunautocodebackend.model.dto.app.AppAdminUpdateRequest;
import com.jstart.keyunautocodebackend.model.dto.app.AppQueryRequest;
import com.jstart.keyunautocodebackend.model.dto.app.AppUpdateRequest;
import com.jstart.keyunautocodebackend.model.entity.App;
import com.jstart.keyunautocodebackend.model.vo.AppVO;
import com.jstart.keyunautocodebackend.service.AppService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/app")
@Slf4j
public class AppController {

    @Resource
    private AppService appService;

    @PostMapping("/add")
    public Result<Long> createApp(@RequestBody AppAddRequest appAddRequest) {
        ThrowUtils.throwIf(appAddRequest == null, ResultEnum.PARAMS_ERROR, "请求参数不能为空");
        Long appId = appService.createApp(appAddRequest.getInitPrompt());
        return Result.success(appId);
    }


    /**
     * 生成应用代码（流式）
     *
     * @param appId   应用ID
     * @param message 用户消息
     * @return 响应流
     */
    @GetMapping("/chat/get/code")
    public Flux<ServerSentEvent<String>> genAppCode(@RequestParam Long appId, @RequestParam String message) {
        ThrowUtils.throwIf(appId == null || appId < 0, ResultEnum.PARAMS_ERROR, "请输入正确的应用 id");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ResultEnum.PARAMS_ERROR, "用户消息不能为空");

        return appService.genAppCode(message, appId)
                .map(content -> {
                    //将内容封装成 json格式
                    Map<String, String> data = Map.of("d", content);
                    //转成 json 字符串
                    String dataJson = JSONUtil.toJsonStr(data);
                    return ServerSentEvent.<String>builder()
                            .data(dataJson)
                            .build();
                })
                .concatWith(Mono
                        .just(ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()));
    }


    @PostMapping("/update")
    public Result<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest) {
        ThrowUtils.throwIf(appUpdateRequest == null, ResultEnum.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(appUpdateRequest.getId() == null, ResultEnum.PARAMS_ERROR, "应用 id 不能为空");

        App app = new App();
        BeanUtils.copyProperties(appUpdateRequest, app);

        appService.updateAppById(app);

        return Result.success(true);
    }


    @PostMapping("/delete")
    public Result<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, ResultEnum.PARAMS_ERROR, "请求参数不能为空");

        appService.removeAppById(deleteRequest.getId());

        return Result.success(true);
    }

    /**
     * 根据应用id获取应用信息
     *
     * @param id 应用id
     * @return 应用信息
     */
    @GetMapping("/get/vo")
    public Result<AppVO> getAppVO(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null, ResultEnum.PARAMS_ERROR, "应用 id 不能为空");

        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ResultEnum.NOT_FOUND_ERROR, "应用不存在");

        return Result.success(appService.getAppVO(app));
    }

    /**
     * 分页获取当前用户的应用列表
     *
     * @param appQueryRequest 应用查询请求参数
     * @return 应用列表
     */
    @PostMapping("/my/list/list/page/vo")
    public Result<Page<AppVO>> getMyAppList(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ResultEnum.PARAMS_ERROR, "请求参数不能为空");

        // 设置查询条件为当前用户的应用
        appQueryRequest.setUserId(StpUtil.getLoginIdAsLong()); // 设置查询条件为当前用户的应用
        Page<App> appPage = appService.listAppByPage(appQueryRequest);
        //Page<App> 转成 Page<AppVO>
        Page<AppVO> appVOPage = new Page<>(appPage.getCurrent(), appPage.getSize(), appPage.getTotal());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords()); // List<APP> 转成 List<AppVO>

        appVOPage.setRecords(appVOList);
        return Result.success(appVOPage);
    }

    /**
     * 分页获取精选应用列表
     *
     * @param appQueryRequest 查询请求
     * @return 精选应用列表
     */
    @PostMapping("/good/list/page/vo")
    public Result<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ResultEnum.PARAMS_ERROR, "请求参数不能为空");
        // 设置查询条件为精选应用
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY); // 设置查询条件为精选应用
        Page<App> appPage = appService.listAppByPage(appQueryRequest);
        //Page<App> 转成 Page<AppVO>
        Page<AppVO> appVOPage = new Page<>(appPage.getCurrent(), appPage.getSize(), appPage.getTotal());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords()); // List<APP> 转成 List<AppVO>

        appVOPage.setRecords(appVOList);
        return Result.success(appVOPage);

    }

    /**
     * 管理员删除应用
     *
     * @param deleteRequest 删除请求
     * @return 删除结果
     */
    @SaCheckRole("admin")
    @PostMapping("/admin/delete")
    public Result<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ResultEnum.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ResultEnum.NOT_FOUND_ERROR);
        boolean result = appService.removeById(id);
        return Result.success(result);
    }


    /**
     * 管理员更新应用
     *
     * @param appAdminUpdateRequest 更新请求
     * @return 更新结果
     */
    @SaCheckRole("admin")
    @PostMapping("/admin/update")
    public Result<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        if (appAdminUpdateRequest == null || appAdminUpdateRequest.getId() <= 0) {
            throw new BusinessException(ResultEnum.PARAMS_ERROR);
        }

        appService.updateAppByAdmin(appAdminUpdateRequest);

        return Result.success(true);
    }

    /**
     * 管理员分页获取应用列表
     * （可选鉴权，因为在service已经鉴权了，这里其实和用户分页查询的逻辑代码一样的，只是url不同）
     *
     * @param appQueryRequest 查询请求
     * @return 应用列表
     */
    @PostMapping("/admin/list/page/vo")
    @SaCheckRole("admin")
    public Result<Page<AppVO>> listAppVOByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ResultEnum.PARAMS_ERROR, "请求参数不能为空");

        Page<App> appPage = appService.listAppByPage(appQueryRequest);
        //Page<App> 转成 Page<AppVO>
        Page<AppVO> appVOPage = new Page<>(appPage.getCurrent(), appPage.getSize(), appPage.getTotal());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords()); // List<APP> 转成 List<AppVO>

        appVOPage.setRecords(appVOList);
        return Result.success(appVOPage);
    }

    /**
     * 管理员根据 id 获取应用详情
     *
     * @param id 应用 id
     * @return 应用详情
     */
    @SaCheckRole("admin")
    @GetMapping("/admin/get/vo")
    public Result<AppVO> getAppVOByIdByAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ResultEnum.PARAMS_ERROR, "应用 id 不能为空");

        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ResultEnum.NOT_FOUND_ERROR, "应用不存在");

        return Result.success(appService.getAppVO(app));
    }


}
