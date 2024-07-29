package com.nowcoder.community.quartz;

import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.Service.DiscussPostService;
import com.nowcoder.community.Service.ElasticsearchService;
import com.nowcoder.community.Service.LikeService;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    //记录日志
    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    //纪元，即创建时间
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("纪元时间初始化失败！" + e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //处理set集合
        String postRedisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(postRedisKey);
        if (operations.size() == 0) {
            logger.info("【任务开始】 没有需要刷新的帖子！");
            return;
        }

        logger.info("【任务开始】 正在刷新帖子分数：" + operations.size());
        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }
        logger.info("【任务结束】 帖子分数刷新完毕！");

    }

    //定义一个方法来计算分数
    //分数 = log10(帖子权重) + 距离天数
    private void refresh(Integer postId) {
        //先对权重进行计算
        //权重包括加精，评论和点赞数
        //如果加精了就加75分，再加上评论数*10，点赞数*2
        DiscussPost discussPost = discussPostService.findDiscussPostById(postId);
        if (discussPost == null) {
            logger.error("该帖子不存在：id = " + postId);
            return;
        }
        double weight = discussPost.getStatus() == 1 ? 75 : 0 +
                discussPost.getCommentCount() * 10 + likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId) * 2;
        //再计算距离天数
        //getTime方法得到的是毫秒
        double days = (discussPost.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        //计算分数
        double score = Math.log10(Math.max(weight, 1)) + days;

        //更新帖子分数
        discussPostService.updateScore(postId, score);
        //同步搜索数据
        discussPost.setScore(score);
        elasticsearchService.saveDiscussPost(discussPost);
    }
}
