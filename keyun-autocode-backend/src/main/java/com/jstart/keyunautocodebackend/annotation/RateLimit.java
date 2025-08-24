package com.jstart.keyunautocodebackend.annotation;

import com.jstart.keyunautocodebackend.enums.RateLimitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RateLimit {


    /**
     * 限流key前缀
     */
    String key() default "";

    /**
     * 每个时间窗口允许的请求次数
     */
    int rate() default 5;

    /**
     * 时间窗口，单位秒
     */
    int rateInterval() default 60;

    /**
     * 限流类型
     */
    RateLimitType limitType() default RateLimitType.USER;

    /**
     * 限流提示信息
     */
    String message() default "请求过于频繁，请稍后再试";

}
