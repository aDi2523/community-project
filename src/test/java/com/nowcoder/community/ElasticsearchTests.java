package com.nowcoder.community;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.util.ObjectBuilder;
import com.nowcoder.community.Mapper.DiscussPostMapper;
import com.nowcoder.community.Mapper.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


@SpringBootTest
public class ElasticsearchTests {
    @Autowired
    private DiscussPostRepository repository;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;


    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Test
    public void testInsert(){
        System.out.println(elasticsearchTemplate.save(discussPostMapper.selectDiscussPostById(241)));
        System.out.println(repository.save(discussPostMapper.selectDiscussPostById(241)));
    }

    @Test
    public void testInsertList(){
        repository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100));
        repository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100));
        repository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100));
        repository.saveAll(discussPostMapper.selectDiscussPosts(111,0,100));
        repository.saveAll(discussPostMapper.selectDiscussPosts(112,0,100));
        repository.saveAll(discussPostMapper.selectDiscussPosts(131,0,100));
        repository.saveAll(discussPostMapper.selectDiscussPosts(132,0,100));
        repository.saveAll(discussPostMapper.selectDiscussPosts(133,0,100));
        repository.saveAll(discussPostMapper.selectDiscussPosts(134,0,100));
        repository.saveAll(discussPostMapper.selectDiscussPosts(152,0,100));
    }

    @Test
    public void testUpdate(){
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("我是新人，基尼太美。");
        repository.save(post);
    }

    @Test
    public void testDeleteById(){
        repository.deleteById(231);
    }

    @Test
    public void testDeleteAll(){
        repository.deleteAll();
    }

    @Test
    public void findByQuery(){

    }

    @Test
    public void testSearchByTemplate() throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("discusspost")
                .query(q -> q
                        .multiMatch(m -> m
                                .fields("title", "content")
                                .query("互联网寒冬")
                        )
                )
                .sort(s1 -> s1.field(f -> f.field("type").order(SortOrder.Desc)))
                .sort(s2 -> s2.field(f -> f.field("score").order(SortOrder.Desc)))
                .sort(s3 -> s3.field(f -> f.field("createTime").order(SortOrder.Desc)))
                .highlight(h -> h
                        .fields("title", f -> f.preTags("<em>").postTags("</em>"))
                        .fields("content", f -> f.preTags("<em>").postTags("</em>"))
                )
                .from(0)
                .size(10)
                .build();

        SearchResponse<DiscussPost> response = elasticsearchClient.search(searchRequest, DiscussPost.class);

        List<DiscussPost> list = new ArrayList<>();
        for (Hit<DiscussPost> hit : response.hits().hits()) {
            DiscussPost post = hit.source();
            if (post == null) {
                continue;
            }
            Map<String, List<String>> highlightFields = hit.highlight();

            if (highlightFields != null && highlightFields.containsKey("title")) {
                post.setTitle(String.join(" ", highlightFields.get("title")));
            }
            if (highlightFields != null && highlightFields.containsKey("content")) {
                post.setContent(String.join(" ", highlightFields.get("content")));
            }

            list.add(post);
        }

        List<DiscussPost> list2 = new ArrayList<>();
        for (Hit<DiscussPost> hit : response.hits().hits()) {
            DiscussPost post = hit.source();
            if (post == null) {
                continue;
            }
            Map<String, List<String>> highlightFields = hit.highlight();

            if (highlightFields != null && highlightFields.containsKey("title")) {
                post.setTitle(String.join(" ", highlightFields.get("title")));
            }
            if (highlightFields != null && highlightFields.containsKey("content")) {
                post.setContent(String.join(" ", highlightFields.get("content")));
            }

            list2.add(post);
        }

        System.out.println("搜索结果总数: " + response.hits().total().value());
        list.forEach(System.out::println);
        list2.forEach(System.out::println);
    }

}
