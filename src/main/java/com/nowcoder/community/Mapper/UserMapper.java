package com.nowcoder.community.Mapper;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from community.user where id = #{id}")
    User selectById(int id);
}
