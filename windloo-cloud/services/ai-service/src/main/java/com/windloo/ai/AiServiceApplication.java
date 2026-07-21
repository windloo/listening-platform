package com.windloo.ai;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.windloo.ai", "com.windloo.common"})
@EnableDiscoveryClient
@MapperScan("com.windloo.ai.mapper")
public class AiServiceApplication {
    public static void main(String[] args) { SpringApplication.run(AiServiceApplication.class, args); }
}