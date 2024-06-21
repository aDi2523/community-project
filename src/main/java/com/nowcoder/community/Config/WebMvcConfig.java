package com.nowcoder.community.Config;

import com.nowcoder.community.Controller.interceptor.LoginRequiredInteceptor;
import com.nowcoder.community.Controller.interceptor.LoginTicketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private LoginRequiredInteceptor loginRequiredInteceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor).
                excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg","/**/*.jpeg");

        registry.addInterceptor(loginRequiredInteceptor).
                excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg","/**/*.jpeg");
    }
}
