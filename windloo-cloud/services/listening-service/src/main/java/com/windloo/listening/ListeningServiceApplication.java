package com.windloo.listening;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.windloo.listening", "com.windloo.common"})
@EnableDiscoveryClient
@EnableAsync
@EnableFeignClients(basePackages = "com.windloo.model.feign")
@MapperScan("com.windloo.listening.mapper")
public class ListeningServiceApplication {
    public static void main(String[] args) { SpringApplication.run(ListeningServiceApplication.class, args); }
}