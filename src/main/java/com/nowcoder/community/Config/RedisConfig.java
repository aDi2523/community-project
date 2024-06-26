package com.nowcoder.community.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory){
        //实例化RedisTemplate<String, Object>类
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        //连接是由连接工厂创建的，所以注入了RedisConnectioFactory
        //注入了连接工厂才能访问数据库
        template.setConnectionFactory(factory);

        //设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        //设置value的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        //设置hash的key序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        //设置hash的value序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        //使配置生效
        template.afterPropertiesSet();
        return template;
    }
}
