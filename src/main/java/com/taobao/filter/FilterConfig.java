package com.taobao.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */
@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<LoginFilter> loginFilter() {
        //注册容器
        FilterRegistrationBean<LoginFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new LoginFilter());
        //所有路径都要经过这个过滤器
        bean.addUrlPatterns("/*");
        bean.setOrder(1);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<RoleFilter> roleFilter() {
        FilterRegistrationBean<RoleFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new RoleFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(2);
        return bean;
    }
}
