package com.nowcoder.community.Mapper.elasticsearch;


import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {
    //继承的泛型中声明的两个值分别是被搜索的实体类型和主键类型
}
