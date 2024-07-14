package com.nowcoder.community.Controller.interceptor;

import com.nowcoder.community.Service.DataService;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class DataInterceptor implements HandlerInterceptor {

    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //每次发起请求都有可能需要记入UV或DAU，故在prehandle方法中进行统计
        //统计UV
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);

        //统计DAU
        User user = hostHolder.getUser();
        if(user != null){
            //登录的情况下才对用户进行统计
            dataService.recordDAU(user.getId());
        }

        return true;
    }
}
