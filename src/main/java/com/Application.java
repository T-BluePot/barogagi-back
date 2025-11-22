package com;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ServletComponentScan
@MapperScan(
        basePackages = {
                // MyBatis 인터페이스가 모여있는 패키지들만 적어줘
                "com.barogagi.**.mapper"
        },
        annotationClass = Mapper.class // @Mapper 붙은 것만 스캔
)
@EnableJpaRepositories(basePackages = {
        // JPA repository 패키지들만
        "com.barogagi.**.repository"
})
public class Application {
    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
}