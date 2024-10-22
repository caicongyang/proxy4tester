package com.caicongyang.proxy4tester;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableWebFlux
@MapperScan("com.caicongyang.proxy4tester.mapper")
public class Proxy4testerApplication {

    public static void main(String[] args) {
        SpringApplication.run(Proxy4testerApplication.class, args);
    }
}
