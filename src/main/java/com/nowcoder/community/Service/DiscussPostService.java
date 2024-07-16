package com.nowcoder.community.Service;


import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.Mapper.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import jakarta.annotation.PostConstruct;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    //声明帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    //声明帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    //定义方法对帖子列表和总数进行初始化
    @PostConstruct
    public void init() {
        //初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误！");
                        }

                        String[] keyStrArr = key.split(":");
                        if (keyStrArr == null || keyStrArr.length != 2) {
                            throw new IllegalArgumentException("参数错误！");
                        }

                        int offset = Integer.parseInt(keyStrArr[0]);
                        int limit = Integer.parseInt(keyStrArr[1]);

                        logger.info("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });

        //出事阿虎帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(Integer key) throws Exception {
                        logger.info("load post rows from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
        if (userId == 0 && orderMode == 1) {
            //仅在首页查询以及为最热帖子的时候在本地缓存中获取帖子列表
            return postListCache.get(offset + ":" + limit);
        }

        logger.info("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    public Integer findDiscussPostRows(int userId) {
        if(userId == 0){
            //仅在首页查询的时候在本地缓存中获取帖子的总行数
            return postRowsCache.get(userId);
        }

        logger.info("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        //对标签的处理
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        //对敏感词的处理
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }

    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int commentCount, int id) {
        return discussPostMapper.updateCommentCount(commentCount, id);
    }

    //更新类型
    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    //更新状态
    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    //更新帖子分数
    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }


}
