package com.jstart.keyunautocodebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.keyunautocodebackend.model.dto.ChatHistory.ChatHistoryQueryRequest;
import com.jstart.keyunautocodebackend.model.entity.ChatHistory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jstart.keyunautocodebackend.model.entity.User;
import dev.langchain4j.memory.ChatMemory;

import java.time.LocalDateTime;

/**
* @author 28435
* @description 针对表【chat_history(对话历史)】的数据库操作Service
* @createDate 2025-08-07 10:35:12
*/
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话消息到指定应用的对话历史
     * @param appId 应用ID
     * @param message 消息内容
     * @param messageType 消息类型
     * @param userId 用户ID
     * @return 是否添加成功
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 删除指定应用的所有对话历史
     * @param appId 应用ID
     * @return 是否删除成功
     */
    boolean deleteByAppId(Long appId);

    QueryWrapper<ChatHistory> getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 分页查询应用的对话历史
     * @param appId 应用ID
     * @param pageSize 每页大小
     * @param lastCreateTime 上一页最后一条记录的创建时间
     * @return 分页结果
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,LocalDateTime lastCreateTime);

    /**
     * 用于给redis对话记忆过期后，加载历史记录
     * @param appId 应用ID
     * @param chatMemory 对话记忆实例
     * @param maxMessages 最大消息数
     * @return 加载的消息数量
     */
    int loadHistoryToRedis(Long appId, ChatMemory chatMemory, int maxMessages);
}
