package com.nowcoder.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    //定义一个方法用来生成redis的key
    //某个实体的key
    public static String getEntityLikeKey(int entityType, int entityId){
        //格式为like:entity:entityType:entityId
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }
}
