package com.jstart.keyunautocodebackend.exception;


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
    //Preconditions参数校验产生的异常
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> businessExceptionHandler(IllegalArgumentException e) {
        log.error("IllegalArgumentException", e);
        return Result.error(ResultEnum.PARAMS_ERROR, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return Result.error(ResultEnum.SYSTEM_ERROR);
    }

}