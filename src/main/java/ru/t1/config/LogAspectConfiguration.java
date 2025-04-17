package ru.t1.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.t1.aspect.LogAspect;
import ru.t1.prop.WebLogProperties;

@Configuration
@EnableConfigurationProperties(WebLogProperties.class)
public class LogAspectConfiguration {

    @Bean
    LogAspect logAspect(WebLogProperties webLogProperties) {
        return new LogAspect(webLogProperties);
    }
}
