package com.nowcoder.community.Controller.interceptor;

import com.nowcoder.community.Service.MessageService;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            //获取未读消息的总数量
            int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
            //获取未读通知的总数量
            int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);

            int allUnreadCount = letterUnreadCount + noticeUnreadCount;
            modelAndView.addObject("allUnreadCount", allUnreadCount);
        }


    }
}
