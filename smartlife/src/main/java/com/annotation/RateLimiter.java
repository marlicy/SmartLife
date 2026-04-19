package com.annotation;

import com.enums.RateLimitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {

    int maxRequests();

    int windowSeconds();

    RateLimitType type() default RateLimitType.IP;

    String key() default "";
}
