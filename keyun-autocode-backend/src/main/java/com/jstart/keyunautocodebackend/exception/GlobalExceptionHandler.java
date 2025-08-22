package com.jstart.keyunautocodebackend.exception;


import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenException;
import cn.hutool.json.JSONUtil;
import com.jstart.keyunautocodebackend.model.Result;
import com.jstart.keyunautocodebackend.model.ResultEnum;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Map;

@RestControllerAdvice
@Slf4j
@Hidden // 隐藏在Swagger文档中
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        // 尝试处理 SSE 请求
        if (handleSseError(e.getCode(), e.getMessage())) {
            return null;
        }
        // 对于普通请求，返回标准 JSON 响应
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public Result<?> saTokenExceptionHandler(NotLoginException nle) {
        log.error("SaTokenException", nle);
        return Result.error(ResultEnum.FORBIDDEN_ERROR,"用户未登录或登录状态已过期");
    }

    @ExceptionHandler(NotRoleException.class)
    public Result<?> saTokenExceptionHandler(NotRoleException nle) {
        log.error("SaTokenException", nle);
        return Result.error(ResultEnum.NO_AUTH_ERROR);
    }

    //Preconditions参数校验产生的异常
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> businessExceptionHandler(IllegalArgumentException e) {
        log.error("IllegalArgumentException", e);
        return Result.error(ResultEnum.PARAMS_ERROR, e.getMessage());
    }
    @ExceptionHandler(SaTokenException.class)
    public Result<?> saTokenExceptionHandler(SaTokenException ste) {
        log.error("SaTokenException", ste);
        return Result.error(ResultEnum.FORBIDDEN_ERROR,ste.getMessage());
    }
    @ExceptionHandler(RuntimeException.class)
    public Result<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        // 尝试处理 SSE 请求
        if (handleSseError(ResultEnum.SYSTEM_ERROR.getCode(), "系统错误")) {
            return null;
        }
        return Result.error(ResultEnum.SYSTEM_ERROR);
    }

    /**
     * 处理SSE请求的错误响应
     *
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     * @return true表示是SSE请求并已处理，false表示不是SSE请求
     */
    private boolean handleSseError(int errorCode, String errorMessage) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return false;
        }
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        // 判断是否是SSE请求（通过Accept头或URL路径）
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();
        if ((accept != null && accept.contains("text/event-stream")) ||
                uri.contains("/chat/gen/code")) {
            try {
                // 设置SSE响应头
                response.setContentType("text/event-stream");
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Cache-Control", "no-cache");
                response.setHeader("Connection", "keep-alive");
                // 构造错误消息的SSE格式
                Map<String, Object> errorData = Map.of(
                        "error", true,
                        "code", errorCode,
                        "message", errorMessage
                );
                String errorJson = JSONUtil.toJsonStr(errorData);
                // 发送业务错误事件（避免与标准error事件冲突）
                String sseData = "event: business-error\ndata: " + errorJson + "\n\n";
                response.getWriter().write(sseData);
                response.getWriter().flush();
                // 发送结束事件
                response.getWriter().write("event: done\ndata: {}\n\n");
                response.getWriter().flush();
                // 表示已处理SSE请求
                return true;
            } catch (IOException ioException) {
                log.error("Failed to write SSE error response", ioException);
                // 即使写入失败，也表示这是SSE请求
                return true;
            }
        }
        return false;
    }


}