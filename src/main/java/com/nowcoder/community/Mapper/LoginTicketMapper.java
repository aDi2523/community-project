package com.nowcoder.community.Mapper;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketMapper {

    @Insert("insert into community.login_ticket (user_id, ticket, status, expired) " +
            "values (#{userId}, #{ticket}, #{status}, #{expired})")
    //开启自增主键开关，令id字段为自增主键
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select("select id,user_id,ticket,status,expired from community.login_ticket " +
            "where ticket=#{ticket}")
    LoginTicket selectByTicket(String ticket);

    @Update("update community.login_ticket set status=#{status} where ticket=#{ticket}")
    int updateStatus(String ticket, int status);




}
