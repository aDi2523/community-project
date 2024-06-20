package com.nowcoder.community.Controller;


import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;


@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @GetMapping("/setting")
    public String getSettingPage(){
        return "/site/setting";
    }

    //提供一个方法用以上传头像
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headImage, Model model){
        //空值判断
        if(headImage == null){
            model.addAttribute("error", "您还没有上传图片！");
            return "/site/setting";
        }

        //得到文件的原始名
        String fileName = headImage.getOriginalFilename();
        //分割得到文件的后缀名
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //判断文件是否有后缀名
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式有误！");
            return "/site/setting";
        }

        //用随机字符串对文件名进行更改，避免不同用户上传同名的文件进行覆盖
        fileName = CommunityUtil.generateUUID() + suffix;
        //存放文件
        //MultipartFile的transferTo方法就可以直接将图片存放到指定位置
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败" + e.getMessage());
           throw new RuntimeException("上传文件失败，服务器发生异常", e);
        }

        // 更新当前用户的头像的路径(web访问路径)
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headUrl = domain + contextPath +"/user/header/" + fileName;
        userService.updateHeader(user.getId(), headUrl);

        return "redirect:/index";
    }

    //提供一个外界获取头像的方法
    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //文件存放位置
        fileName = uploadPath + "/" + fileName;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //在浏览器上响应图片
        response.setContentType("image/" + suffix);
        try (FileInputStream fis = new FileInputStream(fileName);
             OutputStream os = response.getOutputStream()) {
            int len;
            byte[] buffer = new byte[1024];
            while((len = fis.read(buffer)) != -1){
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            logger.error("读取头像失败！");
        }

    }

}
