package com.nowcoder.community.Service;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant{
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    //定义方法来进行关注
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();
                //关注操作
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    //定义方法来进行取关操作
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();
               //取关操作
                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);

                return operations.exec();
            }
        });
    }

    //定义方法来查询关注实体的数量
    public long findFolloweeCount(int userId, int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //定义方法来查询实体的粉丝的数量
    public long findFollowerCount(int entityId, int entityType){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //定义方法来查询当前用户是否已关注该实体
    public boolean hasFollowed(int userId, int entityId, int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    //定义方法来查询用户关注的人,支持分页
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit){
        List<Map<String, Object>> list = new ArrayList<>();
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        //从redis数据库中获取到当前用户关注的人的id
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        //做非空判断
        if(targetIds == null){
            return null;
        }
        //对每个id都要获得实体类user并存入到map集合中
        for(Integer targetId : targetIds){
            Map<String, Object> map = new HashMap<>();
            //获取关注的用户实体类
            User target = userService.findUserById(targetId);
            map.put("user", target);
            //获取关注的时间
            Double followTime = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime", new Date(followTime.longValue()));
            list.add(map);
        }

        return list;
    }

    //定义方法来查询用户的粉丝
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit){
        List<Map<String, Object>> list = new ArrayList<>();
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        //从redis数据库中获取到当前用户的粉丝id
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        //做非空判断
        if(targetIds == null){
            return null;
        }
        //对每个id都要获得实体类user并存入到map集合中
        for(Integer targetId : targetIds){
            Map<String, Object> map = new HashMap<>();
            //获取关注的用户实体类
            User target = userService.findUserById(targetId);
            map.put("user", target);
            //获取关注的时间
            Double followTime = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(followTime.longValue()));
            list.add(map);
        }

        return list;
    }


}
