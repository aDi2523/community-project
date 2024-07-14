package com.nowcoder.community;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void deleteRedis(){

        redisTemplate.delete("uv:20240717:20240726");
        redisTemplate.delete("dau:20240714");
        redisTemplate.delete("dau:20240714:20240714");
        redisTemplate.delete("dau:20240726:20240728");
        redisTemplate.delete("uv:20240714:20240714");
        redisTemplate.delete("dau:20240715:20240717");
        redisTemplate.delete("dau:20240704:20240704");
        redisTemplate.delete("uv:20240715:20240719");
        redisTemplate.delete("uv:20240709:20240714");
        redisTemplate.delete("dau:20240715:20240716");
        redisTemplate.delete("dau:20240715");
        redisTemplate.delete("uv:20240714:20240715");
        redisTemplate.delete("uv:20240714");
//        redisTemplate.delete();
    }

}
