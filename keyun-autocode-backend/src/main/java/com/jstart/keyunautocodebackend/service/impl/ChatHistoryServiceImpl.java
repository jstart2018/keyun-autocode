package com.jstart.keyunautocodebackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.keyunautocodebackend.auth.RoleEnum;
import com.jstart.keyunautocodebackend.enums.ChatHistoryMessageTypeEnum;
import com.jstart.keyunautocodebackend.exception.ThrowUtils;
import com.jstart.keyunautocodebackend.model.ResultEnum;
import com.jstart.keyunautocodebackend.model.dto.ChatHistory.ChatHistoryQueryRequest;
import com.jstart.keyunautocodebackend.model.entity.App;
import com.jstart.keyunautocodebackend.model.entity.ChatHistory;
import com.jstart.keyunautocodebackend.service.AppService;
import com.jstart.keyunautocodebackend.service.ChatHistoryService;
import com.jstart.keyunautocodebackend.mapper.ChatHistoryMapper;
import com.jstart.keyunautocodebackend.service.UserService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author 28435
* @description 针对表【chat_history(对话历史)】的数据库操作Service实现
* @createDate 2025-08-07 10:35:12
*/
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>
    implements ChatHistoryService{
    
    @Resource
    @Lazy
    private AppService appService;
    @Resource
    private UserService userService;


    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ResultEnum.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ResultEnum.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ResultEnum.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ResultEnum.PARAMS_ERROR, "用户ID不能为空");
        // 验证消息类型是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ResultEnum.PARAMS_ERROR, "不支持的消息类型: " + messageType);
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();
        return this.save(chatHistory);
    }

    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ResultEnum.PARAMS_ERROR, "应用ID不能为空");
        QueryWrapper<ChatHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", appId);
        return this.remove(queryWrapper);
    }


    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest 查询请求对象
     * @return 查询请求
     */
    @Override
    public QueryWrapper<ChatHistory> getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper<ChatHistory> queryWrapper = new QueryWrapper<>();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(id != null,"id", id)
                .like(StrUtil.isNotBlank(message),"message", message)
                .eq(StrUtil.isNotBlank(messageType),"message_type", messageType)
                .eq(appId != null,"app_id", appId)
                .eq(userId != null,"user_id", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("create_time", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(true, "ascend".equals(sortOrder),sortField);
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy(true,false,"create_time");
        }
        return queryWrapper;
    }



    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ResultEnum.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ResultEnum.PARAMS_ERROR, "页面大小必须在1-50之间");
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ResultEnum.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = StpUtil.hasRole(RoleEnum.ADMIN.getValue());
        boolean isCreator = userService.getLoginUser().getId().equals(app.getUserId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ResultEnum.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper<ChatHistory> queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }


    /**
     * 用于给redis对话记忆过期后，加载历史记录
     * @param appId 应用ID
     * @param chatMemory 对话记忆实例
     * @param maxMessages 最大消息数
     * @return 加载的消息数量
     */
    @Override
    public int loadHistoryToRedis(Long appId, ChatMemory chatMemory, int maxMessages) {
        int loadedCount = 0;
        try {
            // 验证权限：只有应用创建者和管理员可以加载历史记录
            App app = appService.getById(appId);
            if (app == null) {
                log.debug("查找了不存在的应用对话历史，appId: {}", appId.toString());
                return 0;
            }
            // 查询对话历史
            QueryWrapper<ChatHistory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("app_id", appId)
                    .orderByDesc("create_time")
                    .last("LIMIT " + "1" + maxMessages);
            List<ChatHistory> histories = this.list(queryWrapper);
            // 验证是否有历史记录
            if (histories == null || histories.isEmpty()) {
                return 0;
            }
            //反转历史消息，最早的放在前面
            histories = histories.reversed();
            //防止重复消息，先清除旧的脏数据
            chatMemory.clear();

            loadedCount = 0;
            for (ChatHistory history : histories) {
                if (ChatHistoryMessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                } else if (ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                } else {
                    log.warn("不支持加载到 记忆 的消息类型: {}", history.getMessageType());
                    continue; // 跳过不支持的消息类型
                }
                loadedCount++;
            }
        } catch (Exception e) {
            log.error("加载对话历史到 Redis 记忆失败，appId: {}, 错误: {}", appId, e.getMessage(), e);
            // 可以选择抛出异常或返回0表示加载失败
            return loadedCount;
        }
        return loadedCount;

    }

}




