package com.paike.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {ValidationAutoConfiguration.class})
@ComponentScan(basePackages = "com.paike")
@EnableAsync
public class CourseSchedulingAdminApplication {

    public static void main(String[] args) {
        System.setProperty("spring.classformat.ignore", "true");
        SpringApplication.run(CourseSchedulingAdminApplication.class, args);
    }
}
