package com.points.PS_Backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.Filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<Filter> jwtFilter() {

        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();

        bean.setFilter(new JwtFilter());

        bean.addUrlPatterns("/api/*");

        bean.setOrder(1);

        return bean;
    }
}