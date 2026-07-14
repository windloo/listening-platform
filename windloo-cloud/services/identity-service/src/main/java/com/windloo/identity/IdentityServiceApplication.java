package com.windloo.identity;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.windloo.identity", "com.windloo.common"})
@EnableDiscoveryClient
@EnableAsync
@MapperScan("com.windloo.identity.mapper")
public class IdentityServiceApplication {
    public static void main(String[] args) { SpringApplication.run(IdentityServiceApplication.class, args); }
}