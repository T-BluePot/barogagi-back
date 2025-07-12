package com;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@MapperScan(basePackages = {
        "com.barogagi.approval",
        "com.barogagi.member",
        "com.barogagi.plan.query.mapper",
        "com.barogagi.region.query.mapper",
        "com.barogagi.schedule.query.mapper",
        "com.barogagi.tag.query.mapper",
        "com.barogagi.terms"
})
public class Application {
    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
}