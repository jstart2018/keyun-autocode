package com.jstart.keyunautocodebackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.keyunautocodebackend.auth.RoleEnum;
import com.jstart.keyunautocodebackend.constant.AppConstant;
import com.jstart.keyunautocodebackend.core.AiCodeGeneratorFacade;
import com.jstart.keyunautocodebackend.enums.CodeGenTypeEnum;
import com.jstart.keyunautocodebackend.exception.ThrowUtils;
import com.jstart.keyunautocodebackend.model.ResultEnum;
import com.jstart.keyunautocodebackend.model.dto.app.AppAdminUpdateRequest;
import com.jstart.keyunautocodebackend.model.dto.app.AppQueryRequest;
import com.jstart.keyunautocodebackend.model.entity.App;
import com.jstart.keyunautocodebackend.model.entity.User;
import com.jstart.keyunautocodebackend.model.vo.AppVO;
import com.jstart.keyunautocodebackend.model.vo.UserVO;
import com.jstart.keyunautocodebackend.service.AppService;
import com.jstart.keyunautocodebackend.mapper.AppMapper;
import com.jstart.keyunautocodebackend.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 28435
 * @description 针对表【app(应用)】的数据库操作Service实现
 * @createDate 2025-08-05 10:27:52
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App>
        implements AppService {

    @Resource
    private UserService userService;
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;


    /**
     * 生成应用代码（流式）
     * @param userMessage 用户提示词
     * @param appId 应用ID
     * @return 响应流
     */
    @Override
    public Flux<String> genAppCode(String userMessage, Long appId) {
        //校验参数
        ThrowUtils.throwIf(StrUtil.isBlank(userMessage), ResultEnum.PARAMS_ERROR, "用户消息不能为空");
        ThrowUtils.throwIf(appId == null || appId < 0, ResultEnum.PARAMS_ERROR, "用户消息不能为空");

        // 校验app是否存在
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ResultEnum.NOT_FOUND_ERROR, "应用不存在");

        // 校验权限（是否为应用的创建者）
        ThrowUtils.throwIf(!app.getUserId().equals(userService.getLoginUser().getId()),
                ResultEnum.NO_AUTH_ERROR, "无权限操作该应用");

        //校验代码生成类型
        CodeGenTypeEnum genTypeEnum = CodeGenTypeEnum.getByValue(app.getCodeGenType());
        ThrowUtils.throwIf(genTypeEnum == null, ResultEnum.PARAMS_ERROR, "不支持的代码生成类型");

        // 调用AI代码生成器（门面类）
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, genTypeEnum, appId);
    }

    /**
     * 获取查询条件
     *
     * @param appQueryRequest 查询请求参数
     * @return QueryWrapper<App> 查询条件包装器
     */
    @Override
    public QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest) {
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq(id != null, "id", id)
                .like(StrUtil.isNotBlank(appName), "app_name", appName)
                .like(StrUtil.isNotBlank(cover), "cover", cover)
                .like(StrUtil.isNotBlank(initPrompt), "init_prompt", initPrompt)
                .eq(StrUtil.isNotBlank(codeGenType), "code_gen_type", codeGenType)
                .eq(StrUtil.isNotBlank(deployKey), "deploy_key", deployKey)
                .eq(priority != null, "priority", priority)
                .eq(userId != null, "user_id", userId)
                .orderBy(StrUtil.isNotBlank(sortField),
                        sortOrder != null && sortOrder.equals("ascend"),
                        StrUtil.toUnderlineCase(sortField));


        return queryWrapper;
    }


    @Override
    public Long createApp(String initPrompt) {
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ResultEnum.PARAMS_ERROR, "应用初始化的 prompt 不能为空");
        App app = new App();
        app.setInitPrompt(initPrompt);
        app.setUserId(userService.getLoginUser().getId());
        //todo 应用名称暂时为 prompt 的前 12 位
        app.setAppName(initPrompt.length() <= 12 ? initPrompt : initPrompt.substring(0, 12));
        // todo 暂定应用模式多文件
        app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());


        ThrowUtils.throwIf(!this.save(app), ResultEnum.SYSTEM_ERROR, "应用创建失败");

        return app.getId();
    }

    @Override
    public void updateAppById(App app) {
        ThrowUtils.throwIf(app == null || app.getId() == null, ResultEnum.PARAMS_ERROR, "应用信息不能为空");

        // 校验应用是否存在
        App oldApp = this.getById(app.getId());
        ThrowUtils.throwIf(oldApp == null, ResultEnum.NOT_FOUND_ERROR, "应用不存在");
        // 校验是否为应用的创建者
        User loginUser = userService.getLoginUser();
        ThrowUtils.throwIf(!oldApp.getUserId().equals(loginUser.getId()), ResultEnum.NO_AUTH_ERROR, "无权限操作该应用");

        app.setEditTime(LocalDateTime.now());
        boolean result = this.updateById(app);
        if (!result) {
            throw new RuntimeException("应用信息更新失败");
        }

    }

    @Override
    public void removeAppById(Long appId) {

        ThrowUtils.throwIf(appId == null, ResultEnum.PARAMS_ERROR, "应用 id 不能为空");
        // 校验应用是否存在
        App oldApp = this.getById(appId);
        ThrowUtils.throwIf(oldApp == null, ResultEnum.NOT_FOUND_ERROR, "应用不存在");
        // 校验是否为应用的创建者
        User loginUser = userService.getLoginUser();
        ThrowUtils.throwIf(!oldApp.getUserId().equals(loginUser.getId()), ResultEnum.NO_AUTH_ERROR, "无权限操作该应用");

        boolean result = this.removeById(appId);
        if (!result) {
            throw new RuntimeException("应用删除失败");
        }

    }

    /**
     * 根据用户ID删除该用户的所有应用（内部没有鉴权，需要提前鉴权）
     *
     * @param userId 用户ID
     */
    @Override
    public boolean removeAppByUserId(Long userId) {
        LambdaUpdateWrapper<App> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(App::getUserId, userId);
        return this.remove(updateWrapper);
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        User user = userService.getById(app.getUserId());
        UserVO userVO = userService.getUserVO(user);

        AppVO appVO = new AppVO();
        BeanUtils.copyProperties(app, appVO);
        appVO.setUser(userVO);

        return appVO;
    }


    /**
     * 转换 appList 为 appVOList
     *
     * @param appList 应用列表
     * @return 脱敏后 的应用列表
     */
    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (appList == null || appList.isEmpty()) {
            return List.of();
        }
        // 获取所有用户的 ID 集合
        Set<Long> idSet = appList.stream().map(App::getUserId).collect(Collectors.toSet());
        List<User> userList = userService.listByIds(idSet);
        // 将用户列表分组，key位用户id，value是 用户id等于key的用户对象List
        Map<Long, List<User>> userMap =
                userList.stream().collect(Collectors.groupingBy(User::getId));

        return appList.stream().map(app -> {
            AppVO appVO = this.getAppVO(app);
            appVO.setUser(userService.getUserVO(userMap.get(app.getUserId()).getFirst()));
            return appVO;
        }).toList();
    }

    @Override
    public Page<App> listAppByPage(AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest.getPageSize() > 20, ResultEnum.OPERATION_ERROR, "每页最多只能查询20条数据");
        // 如果查询的不是精选应用，则要校验用户权限
        if (!appQueryRequest.getPriority().equals(AppConstant.GOOD_APP_PRIORITY)) {
            // 不是管理员，则只能查询自己的应用
            if (!StpUtil.hasRole(RoleEnum.ADMIN.getValue())) {
                if (appQueryRequest.getUserId() == null || !appQueryRequest.getUserId().equals(StpUtil.getLoginIdAsLong())) {
                    log.warn("非管理员用户 {} 查询了无权限用户：{} 的应用", StpUtil.getLoginIdAsLong(), appQueryRequest.getUserId());
                    appQueryRequest.setUserId(userService.getLoginUser().getId());
                }
            }
        }
        Page<App> page = new Page<>(appQueryRequest.getPageNum(), appQueryRequest.getPageSize());

        QueryWrapper<App> queryWrapper = this.getQueryWrapper(appQueryRequest);

        List<App> list = this.list(page, queryWrapper);

        page.setRecords(list);

        return page;
    }

    /**
     * 管理员更新应用信息
     *
     * @param appAdminUpdateRequest 更新请求参数
     */
    @Override
    public void updateAppByAdmin(AppAdminUpdateRequest appAdminUpdateRequest) {

        //判断应用是否存在
        App oldApp = this.getById(appAdminUpdateRequest.getId());
        ThrowUtils.throwIf(oldApp == null, ResultEnum.NOT_FOUND_ERROR, "应用不存在");

        App newApp = new App();
        BeanUtils.copyProperties(appAdminUpdateRequest, newApp);
        newApp.setEditTime(LocalDateTime.now());// 设置编辑时间为当前时间
        boolean result = this.updateById(newApp);
        ThrowUtils.throwIf(!result, ResultEnum.OPERATION_ERROR, "应用更新失败");

    }


}




