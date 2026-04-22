package com.paike.admin.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paike.admin.entity.User;
import com.paike.admin.mapper.UserMapper;
import com.paike.common.utils.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DemoAccountPasswordInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoAccountPasswordInitializer.class);
    private static final String DEFAULT_PASSWORD = "123456";
    private static final List<String> DEMO_USERNAMES = Arrays.asList(
            "admin",
            "teacher001",
            "teacher002",
            "teacher003",
            "teacher004",
            "teacher005",
            "student001",
            "student002"
    );

    private final UserMapper userMapper;

    public DemoAccountPasswordInitializer(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<User> demoUsers = userMapper.selectList(
                new LambdaQueryWrapper<User>().in(User::getUsername, DEMO_USERNAMES)
        );

        int updatedCount = 0;
        for (User user : demoUsers) {
            boolean passwordMatched = user.getPassword() != null
                    && PasswordUtils.matches(DEFAULT_PASSWORD, user.getPassword());
            if (passwordMatched && Integer.valueOf(1).equals(user.getStatus())) {
                continue;
            }

            user.setPassword(PasswordUtils.encode(DEFAULT_PASSWORD));
            user.setStatus(1);
            userMapper.updateById(user);
            updatedCount++;
        }

        if (updatedCount > 0) {
            log.info("已同步 {} 个演示账号的默认登录信息，默认密码为 {}", updatedCount, DEFAULT_PASSWORD);
        }
    }
}
