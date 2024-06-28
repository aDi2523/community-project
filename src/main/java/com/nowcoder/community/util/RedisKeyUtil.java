package com.nowcoder.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_FOLLOWEE = "followee";


    //定义一个方法用来生成redis的key
    //某个实体的key
    public static String getEntityLikeKey(int entityType, int entityId){
        //格式为like:entity:entityType:entityId
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //定义一个方法来拼某个用户的赞所需要的key
    public static String getUserLikeKey(int userId){
        //格式为like:user:userId
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //定义一个方法来拼某个用于关注的实体
    public static String getFolloweeKey(int userId, int entityType){
        //followee:userId:entityType -> zset(entityId, now)
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //定义一个方法来拼某个实体拥有的粉丝
    public static String getFollowerKey(int entityType, int entityId){
        //follower:entityType:entityId -> zset(userId , now)
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }
}
