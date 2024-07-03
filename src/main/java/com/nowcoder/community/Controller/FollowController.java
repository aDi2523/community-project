package com.nowcoder.community.Controller;

import com.nowcoder.community.Service.CommunityConstant;
import com.nowcoder.community.Service.FollowService;
import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;


    //处理关注的请求，且为异步请求
    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType, int entityId){
        User user = hostHolder.getUser();

        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件后发送系统消息
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserID(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "已关注！");
    }

    //处理取关的请求，且为异步请求
    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType, int entityId){
        User user = hostHolder.getUser();

        followService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0, "已取消关注！");
    }

    //处理查询关注的人
    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        //设置分页信息
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));


        // 需要得到关注的人的列表
        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if(userList != null){
            for(Map<String, Object> target : userList){
                User u = (User) target.get("user");
                //判断当前用户有没有关注目标用户
                boolean hasFollowed = hasFollowed(u.getId());
                target.put("hasFollowed", hasFollowed);
            }
        }
        model.addAttribute("users", userList);

        return "/site/followee";
    }

    //处理查询粉丝
    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        //设置分页信息
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(userId, ENTITY_TYPE_USER));

        //两个需求：
        // 需要得到关注的人的列表
        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if(userList != null){
            for(Map<String, Object> target : userList){
                User u = (User) target.get("user");
                //如果点击界面上的已关注，则要对关注的人进行取关
                boolean hasFollowed = hasFollowed(u.getId());
                target.put("hasFollowed", hasFollowed);
            }
        }
        model.addAttribute("users", userList);

        return "/site/follower";
    }


    //定义一个方法来判断当前用户是否已经关注目标用户
    public boolean hasFollowed(int userId){
        if(hostHolder.getUser() == null){
            //没有登录即没有关注
            return false;
        }

        return followService.hasFollowed(hostHolder.getUser().getId(), userId, ENTITY_TYPE_USER);

    }
}
