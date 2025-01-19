// src/test/java/com/TestRestTemplateConfig.java
package com;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
public class TestRestTemplateConfig {

    @Bean
    public TestRestTemplate testRestTemplate(RestTemplateBuilder builder) {
        // Create the RequestConfig with Timeout instances
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(60)) // 60 seconds connection request timeout
                .setConnectTimeout(Timeout.ofSeconds(60))           // 60 seconds connect timeout
                .setResponseTimeout(Timeout.ofSeconds(60))          // 60 seconds response timeout
                .build();

        // Create the HTTP client with the RequestConfig
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        // Create the TestRestTemplate with the custom HTTP client
        return new TestRestTemplate(
                builder.requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
        );
    }
}
