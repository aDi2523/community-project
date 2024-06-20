package com.nowcoder.community.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieUtil {

    public static String getvalue(HttpServletRequest request, String name){
        //如果传入的是空（不合法的值），要进行判断并抛出异常
        if(request == null || name == null){
            throw new IllegalArgumentException("参数为空！");
        }
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals(name)){
                    //遍历到键为ticket的话，就返回ticket的具体value
                    return cookie.getValue();
                }
            }
        }

        //否则就返回null
        return null;
    }

}
