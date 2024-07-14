package com.nowcoder.community.Config;

import com.nowcoder.community.Controller.interceptor.DataInterceptor;
import com.nowcoder.community.Controller.interceptor.LoginTicketInterceptor;
import com.nowcoder.community.Controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

//    @Autowired
//    private LoginRequiredInteceptor loginRequiredInteceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Autowired
    private DataInterceptor dataInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor).
                excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg", "/**/*.jpeg");

//        registry.addInterceptor(loginRequiredInteceptor).
//                excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg", "/**/*.jpeg");

        registry.addInterceptor(messageInterceptor).
                excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg", "/**/*.jpeg");

        registry.addInterceptor(dataInterceptor).
                excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg", "/**/*.jpeg");


    }
}
