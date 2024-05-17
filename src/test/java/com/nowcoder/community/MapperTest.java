package com.nowcoder.community;

import com.nowcoder.community.Mapper.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class MapperTest {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0, 10);
        for(DiscussPost post: list){
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }
}
