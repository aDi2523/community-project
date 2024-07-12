package com.nowcoder.community.Mapper;


import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    //offset为每页起始行的行号
    //limit为每页最多显示的数据
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // @Param注解用于给参数取别名
    // 如果只有一个参数，并且在<if>里面使用，则必须加别名
    // 查询帖子行数
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int commentCount, int id);

    //更新类型
    int updateType(int id, int type);

    //更新状态
    int updateStatus(int id, int status);

}
