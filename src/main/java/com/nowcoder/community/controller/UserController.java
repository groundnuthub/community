package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.*;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger= LoggerFactory.getLogger(UserController.class);

    @Value("${tengxunyun.sccess-key-id}")
    private String accessKey;
    @Value("${tengxunyun.sccess-key-secret}")
    private String secretKey;
    @Value("${tengxunyun.bucket}")
    private String bucket;
    @Value("${tengxunyun.bucketNname}")
    private String bucketName;
    @Value("${tengxunyun.path}")
    private String path;
    @Value("${tengxunyun.qianzui}")
    private String qianzui;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Resource
    private UserService userService;

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private CommentService commentService;

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

    /*//????????????
    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String updateHeader(MultipartFile headImage, Model model){
        if(headImage == null){
            model.addAttribute("error","???????????????????????????????????????");
            return "/site/setting";
        }

        //MultipartFile????????????getOriginalFilename???????????????,????????????????????????????????????
        String fileName = headImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","????????????????????????????????????");
            return "/site/setting";
        }else if(!suffix.equalsIgnoreCase("jpg")&&!suffix.equalsIgnoreCase("png")&&!suffix.equalsIgnoreCase("jpeg")){
            model.addAttribute("error","????????????????????????????????????");
            return "/site/setting";
        }
       fileName = CommunityUtil.generateUUID() +"."+ suffix;
        File dest=new File(uploadPath+"/"+fileName);
        try {
            //transferTo???headImage??????????????????dest
            headImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("??????????????????"+e.getMessage());

            throw new RuntimeException("??????????????????????????????????????????"+e);
        }
        //?????????????????????????????????
        User user = hostHolder.getUsers();
        String headerUrl=domain+contextPath+"/user/header/"+fileName;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }*/

    //????????????
    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String updateHeader(MultipartFile headImage, Model model){
        if(headImage == null){
            model.addAttribute("error","???????????????????????????????????????");
            return "/site/setting";
        }

        //MultipartFile????????????getOriginalFilename???????????????,????????????????????????????????????
        String oldFileName = headImage.getOriginalFilename();
        String suffix = oldFileName.substring(oldFileName.lastIndexOf(".")+1);
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","????????????????????????????????????");
            return "/site/setting";
        }else if(!suffix.equalsIgnoreCase("jpg")&&!suffix.equalsIgnoreCase("png")&&!suffix.equalsIgnoreCase("jpeg")){
            model.addAttribute("error","????????????????????????????????????");
            return "/site/setting";
        }
        String fileName = CommunityUtil.generateUUID() +"."+ suffix;
        // 1 ???????????????????????????(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(accessKey, secretKey);
        // 2 ??????bucket?????????, COS???????????????????????? https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(bucket));
        // 3 ??????cos?????????
        COSClient cosclient = new COSClient(cred, clientConfig);
        // bucket??????????????????{name}-{appid} ???????????????????????????????????????????????????
        String bucketName = this.bucketName;

        // ??????????????????, ???????????? 5 GB, ????????????????????????, ?????? 20 M ??????????????????????????????
        // ???????????????????????? API ???????????? API ??????
        //File dest=new File(uploadPath+"/"+fileName);
        File dest=null;
        User user = hostHolder.getUsers();
        try {
            //??????????????????
            dest = File.createTempFile(oldFileName, suffix);
            //transferTo???headImage??????????????????dest
            headImage.transferTo(dest);
            System.out.println(dest.length());
            // ?????????????????? COS ????????????
            String key ="/"+user.getId()+"/header/"+fileName;
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, dest);
            PutObjectResult putObjectResult = cosclient.putObject(putObjectRequest);
        } catch (IOException e) {
            logger.error("??????????????????"+e.getMessage());

            throw new RuntimeException("??????????????????????????????????????????"+e);
        }finally {
            // ???????????????(??????????????????)
            cosclient.shutdown();
        }
        //??????????????????
        dest.deleteOnExit();

        //?????????????????????????????????
        String headerUrl=path+"/"+ user.getId()+"/header/"+fileName;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }

    //????????????????????????????????????????????????
    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String filename, HttpServletResponse response){
        User user = hostHolder.getUsers();
        filename=path +"/"+ user.getId()+"/header/"+ filename;
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
            logger.error("??????????????????"+e.getMessage());
        }
    }

    //????????????
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

    //????????????
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String findUser(@PathVariable("userId") int userId,Model model){
        User user = userService.findUser(userId);
        if(user==null){
            throw new IllegalArgumentException("??????????????????!");
        }
        model.addAttribute("user",user);
        //????????????
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        //????????????
        long followCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followCount",followCount);

        //??????????????????
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);

        //?????????????????????????????????????????????
        boolean hasFollowed =false;
        if(hostHolder.getUsers()!=null){
            hasFollowed = followService.hasFollowed(hostHolder.getUsers().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }

    // ????????????
    @RequestMapping(path = "/mypost/{userId}", method = RequestMethod.GET)
    public String getMyPost(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUser(userId);
        if (user == null) {
            throw new RuntimeException("?????????????????????");
        }
        model.addAttribute("user", user);

        // ????????????
        page.setPath("/user/mypost/" + userId);
        page.setCount(discussPostService.findDiscussPostCount(userId));

        // ????????????
        List<DiscussPost> discussList = discussPostService
                .findDiscussPosts(userId, page.getBegin(), page.getLimit(),0);
        List<Map<String, Object>> discussVOList = new ArrayList<>();
        if (discussList != null) {
            for (DiscussPost post : discussList) {
                Map<String, Object> map = new HashMap<>();
                map.put("discussPost", post);
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                discussVOList.add(map);
            }
        }
        model.addAttribute("discussPosts", discussVOList);

        return "/site/my-post";
    }

    // ????????????
    @RequestMapping(path = "/myreply/{userId}", method = RequestMethod.GET)
    public String getMyReply(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUser(userId);
        if (user == null) {
            throw new RuntimeException("?????????????????????");
        }
        model.addAttribute("user", user);

        // ????????????
        page.setPath("/user/myreply/" + userId);
        page.setCount(commentService.findCountByUser(userId));

        // ????????????
        List<Comment> commentList = commentService.findCommentsByUser(userId, page.getBegin(), page.getLimit());
        List<Map<String, Object>> commentVOList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);
                DiscussPost post = discussPostService.findDiscussPost(comment.getEntityId());
                map.put("discussPost", post);
                commentVOList.add(map);
            }
        }
        model.addAttribute("comments", commentVOList);

        return "/site/my-reply";
    }


}
