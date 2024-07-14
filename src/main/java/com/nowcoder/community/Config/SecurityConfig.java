package com.nowcoder.community.Config;

import com.nowcoder.community.Service.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig implements CommunityConstant {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return web -> web.ignoring().requestMatchers("/resources/**");
    }

    // 在SecurityConfig中增加配置SecurityContextRepository
    // 便于手动保存SecurityContext到SecurityContextRepository中，进行持久化认证
    @Bean
    public SecurityContextRepository securityContextRepository(){
        return new HttpSessionSecurityContextRepository();
    }

    // 用以在自定义的logout功能里调用LogoutHandler彻底地清理授权信息
    @Bean
    public SecurityContextLogoutHandler securityContextLogoutHandler() {
        return new SecurityContextLogoutHandler();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //授权
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                //授权路径
                                "user/setting",
                                "/user/setting",
                                "/user/upload",
                                "/discuss/add",
                                "/comment/add/**",
                                "/letter/**",
                                "/notice/**",
                                "/like",
                                "/follow",
                                "/unfollow"
                        )
                        .hasAnyAuthority(
                                //授权类型
                                AUTHORITY_USER,
                                AUTHORITY_ADMIN,
                                AUTHORITY_MODERATOR
                        )
                        .requestMatchers(
                                //置顶和加精
                                "/discuss/top",
                                "/discuss/wonderful"
                        )
                        .hasAnyAuthority(
                                //仅版主可以访问
                                AUTHORITY_MODERATOR
                        )
                        .requestMatchers(
                                //删帖
                                "/discuss/delete"
                        )
                        .hasAnyAuthority(
                                //仅管理员和发帖人可以访问
                                AUTHORITY_ADMIN,
                                AUTHORITY_USER
                        ).requestMatchers(
                                "/data/**"
                        ).hasAnyAuthority(
                                //统计网站UV和DAU，只有管理员可以访问
                                AUTHORITY_ADMIN
                        )
                        .anyRequest().permitAll()//对于其他路径都可以放行
                )
                .csrf(csrf -> csrf.disable());//关闭防csrf攻击

        //权限不够时的处理
        http.exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    //对于没有登录的情况
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            //异步请求
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "您还没有登录!"));
                        } else {
                            //同步请求
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            //异步请求
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "您没有访问此功能的权限!"));
                        } else {
                            //同步请求
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                })
        );

        // Security底层默认会拦截/logout请求,进行退出处理.
        // 覆盖它默认的逻辑，执行自己的写的代码
        http.logout(logout -> logout.logoutUrl("/SecurityLogout"));
        return http.build();
    }

}
