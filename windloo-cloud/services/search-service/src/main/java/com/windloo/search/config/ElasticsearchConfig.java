package com.windloo.search.config;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {
    @Value("${elasticsearch.host:localhost}")
    private String host;
    @Value("${elasticsearch.port:9200}")
    private int port;
    @Value("${elasticsearch.username:elastic}")
    private String username;
    @Value("${elasticsearch.password:}")
    private String password;

    @Bean
    public RestClient restClient() {
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        return RestClient.builder(new HttpHost(host, port, "http"))
                .setHttpClientConfigCallback(builder -> builder.setDefaultCredentialsProvider(credsProvider))
                .build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        return new ElasticsearchClient(new RestClientTransport(restClient, new JacksonJsonpMapper()));
    }
}