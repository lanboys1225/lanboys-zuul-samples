package com.bing.lan.zuul.config;

import com.bing.lan.zuul.filter.LogSimpleHostRoutingFilter;
import com.bing.lan.zuul.filter.UserFilter;

import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.route.SimpleHostRoutingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by lb on 2020/5/10.
 */
@Configuration
public class FilterConfig {

    @Bean
    public UserFilter userFilter() {
        return new UserFilter();
    }

    @Bean
    public SimpleHostRoutingFilter simpleHostRoutingFilter(ProxyRequestHelper helper,
            ZuulProperties zuulProperties,
            ApacheHttpClientConnectionManagerFactory connectionManagerFactory,
            ApacheHttpClientFactory httpClientFactory) {
        return new LogSimpleHostRoutingFilter(helper, zuulProperties,
                connectionManagerFactory, httpClientFactory);
    }
}
