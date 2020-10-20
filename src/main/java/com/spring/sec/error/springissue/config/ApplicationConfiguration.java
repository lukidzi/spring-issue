package com.spring.sec.error.springissue.config;

import java.util.EnumSet;
import javax.servlet.DispatcherType;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter() {
        RequestLoggingFilter filter = new RequestLoggingFilter(LoggerFactory.getLogger("test"));

        FilterRegistrationBean<RequestLoggingFilter> registrationBean =
                new FilterRegistrationBean<>(filter);
        registrationBean.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        registrationBean.setOrder(filter.getOrder());
        return registrationBean;
    }
}
