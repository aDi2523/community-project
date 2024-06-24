package com.nowcoder.community.Controller;

import com.nowcoder.community.Service.CommentService;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
        //传入的comment只包含3个值，还不完整，需要手动进行补充
        //传入的值有content, entity_type, entity_id
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        //extra：这里不加targetid是因为如果点击具体的人进行回复的时候，前端界面传入的值就会多一个targetid
        commentService.addComment(comment);

        return "redirect:/discuss/detail/" + discussPostId;
    }
}
