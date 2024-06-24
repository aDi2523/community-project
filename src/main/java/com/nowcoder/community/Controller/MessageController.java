package com.nowcoder.community.Controller;

import com.nowcoder.community.Service.MessageService;
import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    //私信列表
    @GetMapping ("/letter/list")
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();

        //设置分页信息
        page.setLimit(5);
        page.setRows(messageService.findConversationCount(user.getId()));
        page.setPath("/letter/list");

        //最后一条信息的列表
        List<Message> conversationList = messageService.findConversations(
                user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message conversation : conversationList){
                Map<String, Object> map = new HashMap<>();
                //每个会话中的最后一条信息
                map.put("conversation", conversation);
                //总共的消息数量
                int letterCount = messageService.findLetterCount(conversation.getConversationId());
                map.put("letterCount", letterCount);
                //未读消息的数量
                int unreadCount = messageService.findLetterUnreadCount(
                        user.getId(), conversation.getConversationId());
                map.put("unreadCount", unreadCount);

                //发消息的用户
                int targetId = conversation.getToId() == user.getId() ? conversation.getFromId() : conversation.getToId();
                User target = userService.findUserById(targetId);
                map.put("target", target);

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        //获取未读消息的总数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/letter";
    }

    //私信详情
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Model model, Page page){
        //设置分页信息
        page.setLimit(5);
        page.setRows(messageService.findLetterCount(conversationId));
        page.setPath("/letter/detail/" + conversationId);

        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if(letterList != null){
            for(Message letter : letterList){
                Map<String, Object> map = new HashMap<>();
                map.put("letter", letter);
                User fromUser = userService.findUserById(letter.getFromId());
                map.put("fromUser", fromUser);
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        //将对话中对方用户的信息一并放入model中，即私信目标
        User target = getTarget(conversationId);
        model.addAttribute("target", target);

        return "/site/letter-detail";
    }

    //定义一个方法用来获取对话用户
    private User getTarget(String conversationId){
        String[] idStrArr = conversationId.split("_");
        int id0 = Integer.parseInt(idStrArr[0]);
        int id1 = Integer.parseInt(idStrArr[1]);
        if (id0 == hostHolder.getUser().getId()){
            return userService.findUserById(id1);
        }else{
            return userService.findUserById(id0);
        }
    }

}
