package com.nowcoder.community.Controller;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.nowcoder.community.Service.CommunityConstant;
import com.nowcoder.community.Service.ElasticsearchService;
import com.nowcoder.community.Service.LikeService;
import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;


    //search?keyword=xxx
    @GetMapping("/search")
    public String search(Model model, Page page, String keyword) throws IOException {
        //定义方法来处理搜索请求
        Map<String, Object> searchResult = elasticsearchService.searchDiscussPost(keyword, page.getOffset(), page.getLimit());
        //通过聚合数据的方式对查询到的帖子进行返回
        List<DiscussPost> postList = (List<DiscussPost>) searchResult.get("list");
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (postList != null) {
            for (DiscussPost post : postList) {
                //需要展现的内容是帖子具体内容、帖子的作者以及帖子的点赞数量
                Map<String, Object> map = new HashMap<>();
                //帖子
                map.put("post", post);
                //作者
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                //点赞数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }

            model.addAttribute("discussPosts", discussPosts);
            model.addAttribute("keyword", keyword);

            //分页信息
            page.setPath("/search?keyword=" + keyword);
            page.setRows(postList == null ? 0 : (int) ((long) searchResult.get("rows")));
        }

        return "/site/search";
    }
}
