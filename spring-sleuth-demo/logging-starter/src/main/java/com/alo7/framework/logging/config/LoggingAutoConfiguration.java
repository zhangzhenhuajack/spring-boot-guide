package com.alo7.framework.logging.config;

import com.alo7.framework.logging.filter.LoggingFilter;
import com.alo7.framework.logging.interceptor.RequestLoggingInterceptor;
import io.sentry.servlet.SentryServletRequestListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import javax.servlet.Filter;

@Configuration
@Import(SleuthHealthIndicatorAutoConfiguration.class)
public class LoggingAutoConfiguration {
    /**
     * pre request simple logging with out response body
     * <p>
     * can approve: exclude(URI)
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setIncludeHeaders(true);
        return loggingFilter;
    }

    /***
     * pre request detail logging with response body
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public RequestLoggingInterceptor requestLoggingInterceptor() {
        return new RequestLoggingInterceptor();
    }

    @Bean
    @Order(1)
    public FilterRegistrationBean requestDumperFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        Filter defaultFilter = new LoggingFilter();
        registration.setFilter(defaultFilter);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggingConfigurer loggingConfigurer() {
        return new LoggingConfigurer();
    }

    /**
     * Sentry listener,pre execption add http header
     *
     * @return
     */
    @Bean
    public SentryServletRequestListener sentryServletRequestListener() {
        return new SentryServletRequestListener();
    }
}
