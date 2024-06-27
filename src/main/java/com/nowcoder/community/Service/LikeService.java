package com.nowcoder.community.Service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate<String, Object> template;

    //实现点赞功能
    public void like(int userId, int entityType, int entityId){
        //需要判断当前用户有没有对实体进行点赞，没有就进行点赞操作，有就进行取消操作
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        boolean isLike = template.opsForSet().isMember(entityLikeKey, userId);
        if (isLike){
            //如果已经点赞了，进行取消
            template.opsForSet().remove(entityLikeKey, userId);
        }else{
            //如果没有点赞，进行点赞操作
            template.opsForSet().add(entityLikeKey, userId);
        }
    }

    //查询某实体点赞的数量
    public long findEntityLikeCount(int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return template.opsForSet().size(entityLikeKey);
    }

    //查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return template.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }
}
