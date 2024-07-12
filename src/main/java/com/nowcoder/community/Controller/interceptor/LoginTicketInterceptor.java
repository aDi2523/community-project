package com.nowcoder.community.Controller.interceptor;

import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie中获取凭证
        String ticket = CookieUtil.getvalue(request, "ticket");

        if (ticket != null) {
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //检查凭证是否有效（是否空值，状态是否有效，是否到过期时间）
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                //凭证有效，获取user
                int userId = loginTicket.getUserId();
                User user = userService.findUserById(userId);
                //在本次请求中持有用户
                hostHolder.setUser(user);

                //获取用户的权限
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getPassword(), userService.getAuthorities(user.getId())
                );
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

                //手动保存SecurityContext到SecurityContextRepository
                securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);
            }
            if (loginTicket != null && !(loginTicket.getExpired().after(new Date()))) {
                //超过了有效时间，需要将登录凭证的状态改为无效
                userService.logout(ticket);
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //运行完controller方法执行
        User user = hostHolder.getUser();
        /*if(user != null && modelAndView != null){
            modelAndView.addObject("loginUser", user);
        }*/
        if (modelAndView != null) {
            if (user != null) {
                modelAndView.addObject("loginUser", user);
            } else {
                modelAndView.addObject("loginUser", null);
            }
        }

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //在模版渲染完毕之后对threadlocal进行清除，避免积累较多
        hostHolder.clean();
        SecurityContextHolder.clearContext();
    }
}
