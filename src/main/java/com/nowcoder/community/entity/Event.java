package com.nowcoder.community.entity;

import java.util.HashMap;
import java.util.Map;

//创建一个类用来处理事件
public class Event {

    private String topic;   //主题、事件的类型
    private int userId;     //触发事件的人
    private int entityType; //事件发生实体
    private int entityId;
    private int entityUserID; //实体的作者
    private Map<String, Object> data = new HashMap<>(); // 用于后续的拓展

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserID() {
        return entityUserID;
    }

    public Event setEntityUserID(int entityUserID) {
        this.entityUserID = entityUserID;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
