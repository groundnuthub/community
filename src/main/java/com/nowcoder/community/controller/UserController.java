package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger= LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Resource
    private UserService userService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private LikeService likeService;

    @Resource
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    //上传头像
    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String updateHeader(MultipartFile headImage, Model model){
        if(headImage == null){
            model.addAttribute("error","您还没有选择要上传的图片！");
            return "/site/setting";
        }

        //MultipartFile中有名为getOriginalFilename的内置方法,可以获取用户原始的文件名
        String fileName = headImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","您上传的图片格式不正确！");
            return "/site/setting";
        }else if(!suffix.equalsIgnoreCase("jpg")&&!suffix.equalsIgnoreCase("png")&&!suffix.equalsIgnoreCase("jpeg")){
            model.addAttribute("error","您上传的图片格式不正确！");
            return "/site/setting";
        }
       fileName = CommunityUtil.generateUUID() +"."+ suffix;
        File dest=new File(uploadPath+"/"+fileName);
        try {
            //transferTo将headImage中的内容传给dest
            headImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("图片上传失败"+e.getMessage());

            throw new RuntimeException("文件上传失败，服务器出现异常"+e);
        }
        //更新当前用户头像的路径
        User user = hostHolder.getUsers();
        String headerUrl=domain+contextPath+"/user/header/"+fileName;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }

    //获取头像路径并对头像进行写入硬盘
    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String filename, HttpServletResponse response){
        filename=uploadPath + "/" + filename;
        String suffix=filename.substring(filename.lastIndexOf(".")+1);
        response.setContentType("image/"+suffix);

        try (
                FileInputStream fis = new FileInputStream(filename);
                OutputStream os=response.getOutputStream();
                ){
            byte[] buffer = new byte[1024];
            int b;
            while ((b = fis.read(buffer)) != -1){
                os.write(buffer,0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败"+e.getMessage());
        }
    }

    //修改密码
    @LoginRequired
    @RequestMapping(path = "/password",method = RequestMethod.POST)
    public String updatePassword(String oldPassword,String newPassword,String confirmPassword, Model model) {
        User user = hostHolder.getUsers();
        Map<String,Object> map = userService.updatePassword(user,newPassword,oldPassword,confirmPassword);
        if (map.isEmpty()){
            return "redirect:/logout";
        }else {
            model.addAttribute("oldPasswordMsg",map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg",map.get("newPasswordMsg"));
            model.addAttribute("confirmPasswordMsg",map.get("confirmPasswordMsg"));
        }
            return "/site/setting";
    }

    //个人主页
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String findUser(@PathVariable("userId") int userId,Model model){
        User user = userService.findUser(userId);
        if(user==null){
            throw new IllegalArgumentException("该用户不存在!");
        }
        model.addAttribute("user",user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        //关注数量
        long followCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followCount",followCount);

        //查询粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);

        //当前的登录用户是否关注目标实体
        boolean hasFollowed =false;
        if(hostHolder.getUsers()!=null){
            hasFollowed = followService.hasFollowed(hostHolder.getUsers().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }
}
