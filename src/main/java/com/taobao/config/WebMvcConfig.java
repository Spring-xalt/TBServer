package com.taobao.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 @auther:Jimi
 @description: 显式配置静态资源映射，覆盖可能的隐性修改导致的无法访问静态资源
 */
@Configuration
public class WebMvcConfig  implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        System.out.println("===== 静态资源映射配置生效 =====");

        // 配置：所有以 "/" 开头的请求，都映射到 classpath:/static/ 目录
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                // 设置缓存时间（可选，避免浏览器缓存旧资源）
                .setCachePeriod(3600);
    }
}
