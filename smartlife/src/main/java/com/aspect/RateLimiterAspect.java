package com.aspect;

import com.annotation.RateLimiter;
import com.dto.UserDto;
import com.enums.RateLimitType;
import com.exception.RateLimitException;
import com.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.UUID;

import static com.utils.RedisConstants.RATE_LIMIT_KEY;

@Aspect
@Component
public class RateLimiterAspect {

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
        RATE_LIMIT_SCRIPT.setLocation(new ClassPathResource("rate-limit.lua"));
        RATE_LIMIT_SCRIPT.setResultType(Long.class);
    }

    private final StringRedisTemplate stringRedisTemplate;

    public RateLimiterAspect(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Around("@annotation(com.annotation.RateLimiter)")
    public Object doRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RateLimiter rateLimiter = method.getAnnotation(RateLimiter.class);
        String limitKey = buildLimitKey(rateLimiter, method);
        long now = System.currentTimeMillis();
        String member = now + "-" + UUID.randomUUID();
        Long passed = stringRedisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                Collections.singletonList(limitKey),
                String.valueOf(now),
                String.valueOf(rateLimiter.windowSeconds()),
                String.valueOf(rateLimiter.maxRequests()),
                member
        );
        if (passed == null || passed == 0L) {
            throw new RateLimitException("请求过于频繁，请稍后再试");
        }
        return joinPoint.proceed();
    }

    private String buildLimitKey(RateLimiter rateLimiter, Method method) {
        String suffix;
        if (rateLimiter.type() == RateLimitType.GLOBAL) {
            suffix = method.getDeclaringClass().getSimpleName() + ":" + method.getName();
        } else if (rateLimiter.type() == RateLimitType.USER) {
            UserDto user = UserHolder.getUser();
            suffix = user == null ? "anonymous" : String.valueOf(user.getId());
        } else {
            suffix = resolveClientIp();
        }
        if (!rateLimiter.key().isBlank()) {
            suffix = rateLimiter.key() + ":" + suffix;
        }
        return RATE_LIMIT_KEY + suffix;
    }

    private String resolveClientIp() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return "unknown";
        }
        HttpServletRequest request = servletAttributes.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
