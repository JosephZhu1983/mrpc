package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.demo.service.UserService;
import com.kongzhong.mrpc.metric.MetricsClient;
import com.kongzhong.mrpc.metric.MetricsInterceptor;
import com.kongzhong.mrpc.metric.MetricsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author biezhi
 *         2017/5/15
 */
@Slf4j
@SpringBootApplication
// metrics
@EnableConfigurationProperties(value = {MetricsProperties.class})
public class BootMetricsClientApplication {

    @Autowired
    private MetricsProperties metricsProperties;

    @Bean
    public MetricsClient metricsClient() {
        log.info("{}", metricsProperties);
        return new MetricsClient(metricsProperties);
    }

    @Bean
    public MetricsInterceptor metricInterceptor() {
        MetricsInterceptor metricsInterceptor = new MetricsInterceptor(metricsClient());
        return metricsInterceptor;
    }

    /*@Bean
    public Referers referers() {
        return new Referers().add(UserService.class);
    }*/

    public static void main(String[] args) {
        SpringApplication.run(BootMetricsClientApplication.class, args);
    }
}
