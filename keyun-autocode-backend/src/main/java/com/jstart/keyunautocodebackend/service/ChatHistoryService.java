package com.jstart.keyunautocodebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.keyunautocodebackend.model.dto.ChatHistory.ChatHistoryQueryRequest;
import com.jstart.keyunautocodebackend.model.entity.ChatHistory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jstart.keyunautocodebackend.model.entity.User;

import java.time.LocalDateTime;

/**
* @author 28435
* @description 针对表【chat_history(对话历史)】的数据库操作Service
* @createDate 2025-08-07 10:35:12
*/
public interface ChatHistoryService extends IService<ChatHistory> {

    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    boolean deleteByAppId(Long appId);

    QueryWrapper<ChatHistory> getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,LocalDateTime lastCreateTime);
}
