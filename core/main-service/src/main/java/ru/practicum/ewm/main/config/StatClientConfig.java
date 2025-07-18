package ru.practicum.ewm.main.config;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.client.StatClient;

@Configuration
public class StatClientConfig {

    @Bean
    public StatClient statClient(DiscoveryClient discoveryClient) {
        return new StatClient(discoveryClient);
    }
}