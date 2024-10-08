package com.nowcoder.community.Controller;

import com.nowcoder.community.Service.CommentService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.Service.DiscussPostService;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        //传入的comment只包含3个值，还不完整，需要手动进行补充
        //传入的值有content, entity_type, entity_id
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        //extra：这里不加targetid是因为如果点击具体的人进行回复的时候，前端界面传入的值就会多一个targetid
        commentService.addComment(comment);

        //触发评论事件后通过kafka发送系统通知
        Event event = new Event();
        event.setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);//额外数据是需要跳转到帖子详情页
        //根据是评论还是回复，传入不同的entityUserId
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int userId = discussPostService.findDiscussPostById(comment.getEntityId()).getUserId();
            event.setEntityUserID(userId);
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            //这里不能直接用comment.getTargetId
            //因为直接对评论进行评论的情况下是没有targetId的
            //只有对评论的评论进行回复的时候才会有targetId
            int userId = commentService.findCommentById(comment.getEntityId()).getId();
            event.setEntityUserID(userId);
        }
        eventProducer.fireEvent(event);

        //触发评论事件后，通过kafka存入es服务器
        //先判断是否是对帖子进行评论，对评论进行回复的不用更改帖子的回复数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);

            //对帖子进行评论，计算分数并将帖子id存入redis中
            String postRedisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postRedisKey, discussPostId);
        }


        return "redirect:/discuss/detail/" + discussPostId;
    }
}
