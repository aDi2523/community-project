package com.nowcoder.community.Mapper;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    //通过selectCommentsByEntity方法，返回的一页评论用List集合装起来
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    //通过selectCountByEntity方法，返回条目数
    int selectCountByEntity(int entityType, int entityId);

    //增加评论数据
    int insertComment(Comment comment);
}
