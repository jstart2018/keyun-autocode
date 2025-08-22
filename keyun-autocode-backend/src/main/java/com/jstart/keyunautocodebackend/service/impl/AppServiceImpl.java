package com.jstart.keyunautocodebackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.keyunautocodebackend.ai.SimpleAiTaskService;
import com.jstart.keyunautocodebackend.auth.RoleEnum;
import com.jstart.keyunautocodebackend.constant.AppConstant;
import com.jstart.keyunautocodebackend.core.AiCodeGeneratorFacade;
import com.jstart.keyunautocodebackend.core.projectBuilder.VueProjectBuilder;
import com.jstart.keyunautocodebackend.core.resultStreamHandler.StreamHandlerExecutor;
import com.jstart.keyunautocodebackend.enums.ChatHistoryMessageTypeEnum;
import com.jstart.keyunautocodebackend.enums.CodeGenTypeEnum;
import com.jstart.keyunautocodebackend.exception.BusinessException;
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
import com.jstart.keyunautocodebackend.service.ScreenshotService;
import com.jstart.keyunautocodebackend.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
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
    @Resource
    private ChatHistoryServiceImpl chatHistoryService;
    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;
    @Resource
    private VueProjectBuilder vueProjectBuilder;
    @Resource
    private ScreenshotService screenshotService;
    @Resource
    private SimpleAiTaskService simpleAiTaskService;


    /**
     * 生成应用代码（流式）
     *
     * @param userMessage 用户提示词
     * @param appId       应用ID
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
        User loginUser = userService.getLoginUser();
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()),
                ResultEnum.NO_AUTH_ERROR, "无权限操作该应用");

        //校验代码生成类型
        CodeGenTypeEnum genTypeEnum = CodeGenTypeEnum.getByValue(app.getCodeGenType());
        ThrowUtils.throwIf(genTypeEnum == null, ResultEnum.PARAMS_ERROR, "不支持的代码生成类型");

        // 持久化用户的提问
        boolean saveUserMsgResult = chatHistoryService.addChatMessage
                (appId, userMessage, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());

        // 调用AI代码生成器（门面类）
        Flux<String> useAiResult = aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, genTypeEnum, app);
        // 持久化AI的回答
        return streamHandlerExecutor.doExecute(useAiResult, chatHistoryService, appId, loginUser, genTypeEnum);

        /* 已被上面的适配器替代
        StringBuilder sb = new StringBuilder();
        return useAiResult.map(chunk -> {
            sb.append(chunk);
            return chunk;//使用map要返回值，如果使用的是doOnNext则不需要返回值
        }).doOnComplete(() -> {
            String string = sb.toString();
            if (StrUtil.isNotBlank(string)) {
                chatHistoryService.addChatMessage(appId, string,
                        ChatHistoryMessageTypeEnum.AI.getValue(), loginUserId);
            }
        }).doOnError(error -> {
            //如果生成代码失败，也要持久化AI的回答
            chatHistoryService.addChatMessage(appId, "生成代码失败，原因：" + error.getMessage(),
                    ChatHistoryMessageTypeEnum.AI.getValue(), loginUserId);
        });*/

    }

    /**
     * 部署应用
     *
     * @param appId 应用ID
     * @return 可访问的URL
     */
    @Override
    public String deployApp(Long appId) {
        //1、校验参数
        ThrowUtils.throwIf(appId == null || appId < 0, ResultEnum.PARAMS_ERROR, "请输入正确的应用 id");
        //2、校验app是否存在
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ResultEnum.NOT_FOUND_ERROR, "应用不存在");

        //3、校验权限（是否为应用的创建者）
        ThrowUtils.throwIf(!app.getUserId().equals(userService.getLoginUser().getId()),
                ResultEnum.NO_AUTH_ERROR, "无权限操作该应用");

        //4、检查deployKey是否存在
        String deployKey = app.getDeployKey();
        if (StrUtil.isNotBlank(deployKey)) {
            //如果deployKey已存在，直接返回可访问的URL
            return String.format("%s/%s", AppConstant.CODE_DEPLOY_HOST, deployKey);
        }
        deployKey = RandomUtil.randomStringWithoutStr(6, "0oO");

        //5、获取代码模式类型、获取原文件路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourcePath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;

        //6、检查该目录是否存在
        File sourceDir = new File(sourcePath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(),
                ResultEnum.OPERATION_ERROR, "应用代码不存在，请先生成代码");

        // 7. Vue 项目特殊处理：执行构建
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourcePath);
            ThrowUtils.throwIf(!buildSuccess, ResultEnum.SYSTEM_ERROR, "Vue 项目构建失败，请检查代码和依赖");
            // 检查 dist 目录是否存在
            File distDir = new File(sourcePath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ResultEnum.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 将 dist 目录作为部署源
            sourceDir = distDir;
            log.info("Vue 项目构建成功，将部署 dist 目录: {}", distDir.getAbsolutePath());
        }
        // 8. 复制文件到部署目录
        String deployPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;

        try {
            FileUtil.copyContent(sourceDir, new File(deployPath), true);
        } catch (IORuntimeException e) {
            throw new BusinessException(ResultEnum.SYSTEM_ERROR, "应用部署失败，请重试");
        }

        //7、更新数据库（部署时间、部署key）
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ResultEnum.SYSTEM_ERROR, "更新应用部署信息失败");

        //8、生成可访问的URL
        String appDeployUrl = String.format("%s/%s", AppConstant.CODE_DEPLOY_HOST, deployKey);

        //9、异步生成截图并上传到COS
        //todo 待测试
        Thread.startVirtualThread(() -> {
            try {
                // 生成并上传截图
                String imgUrl = screenshotService.generateAndUploadScreenshot(appDeployUrl);
                // 更新应用的封面图片URL
                if (StrUtil.isNotBlank(imgUrl)) {
                    this.update(new LambdaUpdateWrapper<App>()
                            .eq(App::getId, appId)
                            .set(App::getCover, imgUrl));
                }
            } catch (Exception e) {
                log.error("生成应用部署截图失败，应用ID：{}，错误信息：{}", appId, e.getMessage());
            }
        });

        return appDeployUrl;

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
        //让AI总结用户提示词，智能获取App名字
        app.setAppName(simpleAiTaskService.getInitialPrompt(initPrompt));
        // 让AI智能选择代码生成类型
        app.setCodeGenType(simpleAiTaskService.routeCodeGenType(initPrompt).getValue());

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

    /**
     * 重写removeById方法，增加鉴权和关联删除该app的对话记忆
     *
     * @param appId 应用ID
     * @return 是否删除成功
     */
    @Override
    public boolean removeById(Serializable appId) {

        ThrowUtils.throwIf(appId == null, ResultEnum.PARAMS_ERROR, "应用 id 不能为空");
        // 1、校验应用是否存在
        App oldApp = this.getById(appId);
        ThrowUtils.throwIf(oldApp == null, ResultEnum.NOT_FOUND_ERROR, "应用不存在");
        // 2、校验是否为应用的创建者
        User loginUser = userService.getLoginUser();
        ThrowUtils.throwIf(!oldApp.getUserId().equals(loginUser.getId()), ResultEnum.NO_AUTH_ERROR, "无权限操作该应用");

        //3、删除该应用的对话记忆
        boolean delChatHistoryResult = chatHistoryService.deleteByAppId(oldApp.getId());

        try {
            //4、删除应用代码文件
            String sourceDirName = oldApp.getCodeGenType() + "_" + oldApp.getId();
            String sourcePath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
            FileUtil.del(sourcePath);

            //5、删除部署的代码文件
            String deployKey = oldApp.getDeployKey();
            if (StrUtil.isNotBlank(deployKey)) {
                String deployPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
                FileUtil.del(deployPath);
            }
        } catch (IORuntimeException e) {
            log.error("删除应用文件失败，应用ID：{}，错误信息：{}", oldApp.getId(), e.getMessage());
            throw new BusinessException(ResultEnum.SYSTEM_ERROR, "应用文件删除失败!");
        }

        //6、删除应用
        boolean delAppResult = super.removeById(oldApp.getId());
        if (!(delAppResult && delChatHistoryResult)) {
            throw new RuntimeException("应用删除失败");
        }

        return true;
    }

    /**
     * 根据用户ID删除该用户的所有应用（内部没有鉴权，需要提前鉴权）
     *
     * @param userId 用户ID
     */
    @Override
    public boolean removeAppByUserId(Long userId) {
        QueryWrapper<App> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        //关联删除该用户的所有应用
        this.list(qw).forEach(app -> {
            this.removeById(app.getId());
        });

        return true;
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




