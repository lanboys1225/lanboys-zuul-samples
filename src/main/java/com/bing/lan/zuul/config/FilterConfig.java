package com.bing.lan.zuul.config;

import com.bing.lan.zuul.filter.LogRibbonRoutingFilter;
import com.bing.lan.zuul.filter.LogSimpleHostRoutingFilter;
import com.bing.lan.zuul.filter.UserFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.netflix.ribbon.support.RibbonRequestCustomizer;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandFactory;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonRoutingFilter;
import org.springframework.cloud.netflix.zuul.filters.route.SimpleHostRoutingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * Created by lb on 2020/5/10.
 */
@Configuration
public class FilterConfig {

    @Autowired(required = false)
    private List<RibbonRequestCustomizer> requestCustomizers = Collections.emptyList();

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

    @Bean
    public RibbonRoutingFilter ribbonRoutingFilter(ProxyRequestHelper helper,
            RibbonCommandFactory<?> ribbonCommandFactory) {
        RibbonRoutingFilter filter = new LogRibbonRoutingFilter(helper, ribbonCommandFactory,
                this.requestCustomizers);
        return filter;
    }
}
