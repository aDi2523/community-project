package com.nowcoder.community.Controller;


import com.nowcoder.community.Service.*;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403,"你还没有登录哦！");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        //报错的情况统一处理
        return CommunityUtil.getJSONString(0,"发布成功！");
    }

    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //帖子
        DiscussPost post = discussPostService.selectDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        //作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeCount", likeCount);
        //点赞状态
        //细节：判断当前用户是否已经登录了，没有登录要直接返回0，表示没有点赞
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeStatus", likeStatus);

        //评论分页信息
        page.setLimit(5);
        page.setRows(post.getCommentCount());
        page.setPath("/discuss/detail/" + discussPostId);

        //- 评论：给帖子的评论
        //- 回复：给评论的评论
        // 每个帖子要包含带有的具体每条评论，而且每条评论下也要带有具体的回复
        // 具体包括评论，作者，每条评论的回复列表，每条评论的回复数
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        //Vo指View Object，即显示的对象
        //评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if(commentList != null){
            for(Comment comment : commentList){
                //评论vo
                Map<String, Object> commentVo = new HashMap<>();
                //评论
                commentVo.put("comment", comment);
                //作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                //点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                //点赞状态
                //细节：判断当前用户是否已经登录了，没有登录要直接返回0，表示没有点赞
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                //每条评论的回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复vo列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if(replyList != null){
                    for(Comment reply: replyList){
                        Map<String, Object> replyVo = new HashMap<>();
                        //回复
                        replyVo.put("reply", reply);
                        //回复的作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        //点赞状态
                        //细节：判断当前用户是否已经登录了，没有登录要直接返回0，表示没有点赞
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        //回复的目标
                        //需要先判断是否有回复的目标
                        User taget = reply.getTargetId() == 0? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", taget);
                        replyVoList.add(replyVo);
                    }
                }

                commentVo.put("replys", replyVoList);

                //每条评论的回复数量
                int replyCount = commentService.findCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);


                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);

        return"/site/discuss-detail";
    }
}
