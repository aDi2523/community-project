package com.nowcoder.community;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
class CommunityProjectApplicationTests {

    @Test
    void contextLoads() {
        String key = "asdasd:asdasdas:Asdasdsa";
        System.out.println(Arrays.toString(key.getBytes()));
    }

}
