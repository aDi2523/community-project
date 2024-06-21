package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SensitiveTest {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void filter(){
        String text = "这里可以吸毒，可以嫖娼，可以开票，可以赌博，哈哈哈！";
        System.out.println(sensitiveFilter.filter(text));

        String text2 = "这里可以.吸....毒.，可以.嫖...娼，可以.开..票.，可以.赌.博.，哈哈哈！";
        System.out.println(sensitiveFilter.filter(text2));

        String text3 = "这里可以a吸a毒a，可以a嫖a娼a，可以a开a票a，可以a赌a博a，哈哈哈！";
        System.out.println(sensitiveFilter.filter(text3));
    }
}
