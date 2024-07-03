package com.nowcoder.community.Service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate<String, Object> template;

    //实现点赞功能
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        template.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                //这里要获取的是被查看人的userid，不是当前登录用户的userid
                //获取一个用户的总点赞数
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                //查询操作不要放在事务内，否则返回结果为空
                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                //启用事务
                operations.multi();

                if (isMember) {
                    //如果已经点赞了，进行取消
                    operations.opsForSet().remove(entityLikeKey, userId);
                    //取消则总点赞数减1
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    //如果没有点赞，进行点赞操作
                    operations.opsForSet().add(entityLikeKey, userId);
                    //点赞则总点赞数加1
                    operations.opsForValue().increment(userLikeKey);
                }

                return operations.exec();
            }
        });

    }

    //查询某实体点赞的数量
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return template.opsForSet().size(entityLikeKey);
    }

    //查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return template.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    //查询某个用户获得的赞
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) template.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }
}
