package com.windloo.search;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.windloo.search", "com.windloo.common"},
    exclude = {DataSourceAutoConfiguration.class, FlywayAutoConfiguration.class, ElasticsearchRestClientAutoConfiguration.class})
@EnableDiscoveryClient
public class SearchServiceApplication {
    public static void main(String[] args) { SpringApplication.run(SearchServiceApplication.class, args); }
}