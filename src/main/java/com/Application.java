package com;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
//@MapperScan("com.barogagi")
@MapperScan(basePackages = "com.barogagi.**.mapper")  // MyBatis 매퍼만 있는 경로로 제한
public class Application {
    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
}