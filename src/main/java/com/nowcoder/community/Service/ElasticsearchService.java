package com.nowcoder.community.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.nowcoder.community.Mapper.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository repository;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    //将帖子保存到ES服务器中
    public void saveDiscussPost(DiscussPost discussPost){
        repository.save(discussPost);
    }

    //从ES服务器中删除帖子
    public void deleteDiscussPost(int postId){
        repository.deleteById(postId);
    }

    //从ES服务器中搜索帖子
    public Map<String, Object> searchDiscussPost(String keyword, int offset, int limit) throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("discusspost")
                .query(q -> q
                        .multiMatch(m -> m
                                .fields("title", "content")
                                .query(keyword)
                        )
                )
                .sort(s1 -> s1.field(f -> f.field("type").order(SortOrder.Desc)))
                .sort(s2 -> s2.field(f -> f.field("score").order(SortOrder.Desc)))
                .sort(s3 -> s3.field(f -> f.field("createTime").order(SortOrder.Desc)))
                .highlight(h -> h
                        .fields("title", f -> f.preTags("<em>").postTags("</em>"))
                        .fields("content", f -> f.preTags("<em>").postTags("</em>"))
                )
                .from(offset)
                .size(limit)
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

        Map<String, Object> map = new HashMap<>();
        map.put("rows", response.hits().total().value());
        map.put("list", list);
        return map;
//        System.out.println("搜索结果总数: " + response.hits().total().value());
//        list.forEach(System.out::println);
    }
}
