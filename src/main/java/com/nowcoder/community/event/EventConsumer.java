package com.nowcoder.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.Service.CommunityConstant;
import com.nowcoder.community.Service.DiscussPostService;
import com.nowcoder.community.Service.ElasticsearchService;
import com.nowcoder.community.Service.MessageService;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handle(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }

        //发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserID());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        //页面显示效果为：用户xxx关注/点赞/评论了你/你的帖子
        //要将其中的信息进行处理存入
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        if (event.getData() != null) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    //增加一个方法handlePublishMessage，消费帖子发布事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }

        //从数据库中调用帖子并将该帖子传入到es服务器中即可
        DiscussPost discussPost = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(discussPost);

    }

    //增加一个方法handleDeleteMessage，消费帖子删除事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }

        //从数据库中调用帖子并将该帖子传入到es服务器中即可
        elasticsearchService.deleteDiscussPost(event.getEntityId());

    }


}
