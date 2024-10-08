package com.nowcoder.community.util;


public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";


    //定义一个方法用来生成redis的key
    //某个实体的key
    public static String getEntityLikeKey(int entityType, int entityId) {
        //格式为like:entity:entityType:entityId
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //定义一个方法来拼某个用户的赞所需要的key
    public static String getUserLikeKey(int userId) {
        //格式为like:user:userId
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //定义一个方法来拼某个用于关注的实体
    public static String getFolloweeKey(int userId, int entityType) {
        //followee:userId:entityType -> zset(entityId, now)
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //定义一个方法来拼某个实体拥有的粉丝
    public static String getFollowerKey(int entityType, int entityId) {
        //follower:entityType:entityId -> zset(userId , now)
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    //拼验证码的键
    public static String getKaptchaKey(String kaptchaOwner) {
        return PREFIX_KAPTCHA + SPLIT + kaptchaOwner;
    }

    //拼登录凭证的键
    public static String getTicketKey(String loginTicket) {
        return PREFIX_TICKET + SPLIT + loginTicket;
    }

    //拼用户的键
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    //拼单日uv的键
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    //拼区间uv的键
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    //拼单日活跃用户的键
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    //拼区间活跃用户的键
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    //拼帖子的键
    public static String getPostScoreKey() {
        //不需要传入参数，把所有修改的帖子放在一个set里面进行修改即可
        return PREFIX_POST + SPLIT + "score";
    }
}
