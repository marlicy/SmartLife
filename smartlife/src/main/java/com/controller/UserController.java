package com.controller;

import com.annotation.RateLimiter;
import com.dto.LoginFormDto;
import com.dto.Result;
import com.enums.RateLimitType;
import com.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/code")
    @RateLimiter(maxRequests = 5, windowSeconds = 60, type = RateLimitType.IP, key = "sendCode")
    public Result sendcode(@RequestParam("phone") String phone, HttpSession session) {
        return userService.sendcode(phone, session);
    }

    @PostMapping("/login")
    @RateLimiter(maxRequests = 10, windowSeconds = 60, type = RateLimitType.IP, key = "login")
    public Result login(@RequestBody LoginFormDto loginForm, HttpSession session) {
        return userService.login(loginForm, session);
    }
}
