package com.jstart.keyunautocodebackend.exception;


import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenException;
import com.jstart.keyunautocodebackend.model.Result;
import com.jstart.keyunautocodebackend.model.ResultEnum;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@Hidden // 隐藏在Swagger文档中
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
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
        return Result.error(ResultEnum.SYSTEM_ERROR);
    }



}