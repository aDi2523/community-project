package com.nowcoder.community.Controller;

import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import com.nowcoder.community.Service.LikeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class likeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();
        //判断用户是否登录
        if (user == null) {
            return CommunityUtil.getJSONString(1, "您还没有登录！请先登录");
        }
        //进行点赞操作
        likeService.like(user.getId(), entityType, entityId, entityUserId);

        //数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        //状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        //触发点赞事件后发送系统消息
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserID(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }

        //对帖子进行点赞后，计算分数并将帖子id存入redis中
        //只对帖子进行点赞才操作，对回复和评论点赞不进行算分操作
        if (likeStatus == 1 && entityType == ENTITY_TYPE_POST) {
            String postRedisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postRedisKey, entityId);
        }


        //返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);
        return CommunityUtil.getJSONString(0, null, map);
    }
}
