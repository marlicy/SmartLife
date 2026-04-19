package com.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dto.LoginFormDto;
import com.dto.Result;
import com.entity.User;
import jakarta.servlet.http.HttpSession;

public interface UserService extends IService<User> {

    Result sendcode(String phone, HttpSession session);

    Result login(LoginFormDto loginForm, HttpSession session);
}
