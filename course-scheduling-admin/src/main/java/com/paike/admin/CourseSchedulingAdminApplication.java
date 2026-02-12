package com.paike.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.paike.**.mapper")
public class CourseSchedulingAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseSchedulingAdminApplication.class, args);
    }
}
