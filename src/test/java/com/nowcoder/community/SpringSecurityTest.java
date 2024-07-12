package com.nowcoder.community;

import com.nowcoder.community.Service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@SpringBootTest
public class SpringSecurityTest {

    @Autowired
    private UserService userService;

    @Test
    public void testGetAuthorities(){
        Collection<? extends GrantedAuthority> authorities = userService.getAuthorities(152);
        System.out.println(authorities.toString());
    }
}
