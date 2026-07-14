package com.windloo.encoder;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.windloo.encoder", "com.windloo.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.windloo.model.feign")
@EnableScheduling
@MapperScan("com.windloo.encoder.mapper")
public class MediaEncoderServiceApplication {
    public static void main(String[] args) { SpringApplication.run(MediaEncoderServiceApplication.class, args); }
}