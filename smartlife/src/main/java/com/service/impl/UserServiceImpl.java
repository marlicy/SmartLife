package com.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dto.LoginFormDto;
import com.dto.Result;
import com.dto.UserDto;
import com.entity.User;
import com.mapper.UserMapper;
import com.service.UserService;
import com.utils.RegexUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.utils.RedisConstants.LOGIN_CODE_KEY;
import static com.utils.RedisConstants.LOGIN_CODE_TTL;
import static com.utils.RedisConstants.LOGIN_USER_KEY;
import static com.utils.RedisConstants.LOGIN_USER_TTL;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final StringRedisTemplate stringRedisTemplate;

    public UserServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Result sendcode(String phone, HttpSession session) {
        if (RegexUtil.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        log.info("验证码发送成功，phone={}, code={}", phone, code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDto loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        if (RegexUtil.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        if (cacheCode == null || !cacheCode.equals(loginForm.getCode())) {
            return Result.fail("验证码错误");
        }

        User user = lambdaQuery().eq(User::getPhone, phone).one();
        if (user == null) {
            user = createWithPhone(phone);
        }

        String token = UUID.randomUUID().toString(true);
        UserDto userDto = BeanUtil.copyProperties(user, UserDto.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(
                userDto,
                new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue == null ? null : fieldValue.toString())
        );
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, userMap);
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);
        stringRedisTemplate.delete(LOGIN_CODE_KEY + phone);
        return Result.ok(token);
    }

    private User createWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName("user_" + RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
