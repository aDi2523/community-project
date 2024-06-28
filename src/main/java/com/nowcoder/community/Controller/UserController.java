package com.nowcoder.community.Controller;


import com.nowcoder.community.Service.CommunityConstant;
import com.nowcoder.community.Service.FollowService;
import com.nowcoder.community.Service.LikeService;
import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Map;


@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

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

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    //提供一个方法用以上传头像
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headImage, Model model) {
        //空值判断
        if (headImage == null) {
            model.addAttribute("error", "您还没有上传图片！");
            return "/site/setting";
        }

        //得到文件的原始名
        String fileName = headImage.getOriginalFilename();
        //分割得到文件的后缀名
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //判断文件是否有后缀名
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式有误！");
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
        String headUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headUrl);

        return "redirect:/index";
    }

    //提供一个外界获取头像的方法
    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        //文件存放位置
        fileName = uploadPath + "/" + fileName;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //在浏览器上响应图片
        response.setContentType("image/" + suffix);
        try (FileInputStream fis = new FileInputStream(fileName);
             OutputStream os = response.getOutputStream()) {
            int len;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            logger.error("读取头像失败！");
        }
    }


    //提供方法进行修改密码
    @PostMapping("/passwordModification")
    public String passwordModification(String oldPassword, String newPassword, String againPassword,
                                       @CookieValue("ticket") String ticket, Model model) {
        //对传入参数进行判断，不能为空
        if (StringUtils.isBlank(oldPassword)) {
            model.addAttribute("oldPasswordMsg", "请输入原始密码！");
            return "site/setting";
        }
        if (StringUtils.isBlank(newPassword)) {
            model.addAttribute("newPasswordMsg", "请输入新密码！");
            return "site/setting";
        }
        if (StringUtils.isBlank(againPassword)) {
            model.addAttribute("againPasswordMsg", "请输入确认密码！");
            return "site/setting";
        }
        //首先通过所持有的的用户对象获取当前用户
        User user = hostHolder.getUser();
        //判断用户输入的原密码是否与存储的原密码一致
        //首先对用户输入的原密码进行加密处理
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!oldPassword.equals(user.getPassword())) {
            model.addAttribute("oldPasswordMsg", "该密码与原密码不符!");
            return "site/setting";
        }
        //判断新输入密码与原密码是否一致
        //对新密码进行加密
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        if (newPassword.equals(user.getPassword())) {//判断
            model.addAttribute("newPasswordMsg", "新密码与原密码一致!");
            return "site/setting";
        }
        //对确认密码进行加密
        againPassword = CommunityUtil.md5(againPassword + user.getSalt());
        if (!newPassword.equals(againPassword)) {//判断
            model.addAttribute("againPasswordMsg", "两次密码不一致!");
            return "site/setting";
        }
        userService.updatePassword(user.getId(), newPassword);
        //修改密码后，用户需要重新登陆，所以在本次持有中释放用户
        userService.logout(ticket);
        return "redirect:/login";
    }

    //提供一个方法来查看主页
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        //防止传入的user为空，被恶意攻击
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);
        //以用户为key，查询点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        //统计关注数量、粉丝数量以及当前用户是否已经关注该实体
        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followerCount", followerCount);
        //当前用户是否已经关注该实体
        //细节：需要先判断当前有没有用户登录
        boolean hasFollowed = false;
        if(hostHolder.getUser() != null) {
            //已经登录了
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), userId, ENTITY_TYPE_USER);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }


}

