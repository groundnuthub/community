package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private UserService userService;

    @Resource
    private CommentService commentService;

    @Resource
    private LikeService likeService;

    @LoginRequired
    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscuss(String title,String content){
        User user = hostHolder.getUsers();
        if(user == null){
            return CommunityUtil.getJSONString(403,"您还未登录，请先登录！");
        }
        DiscussPost discussPost=new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPostCount(discussPost);

        //报错的情况·，另外处理
        return CommunityUtil.getJSONString(0,"发布成功！");
    }

    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String selectDiscuss(@PathVariable("discussPostId") int discussPostId, Model model, Page page){

        DiscussPost discussPost = discussPostService.findDiscussPost(discussPostId);
        User user = userService.findUser(discussPost.getUserId());
        model.addAttribute("post",discussPost);
        model.addAttribute("user",user);
        //点赞信息
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeCount",likeCount);
        //点赞状态
        int likeStatus = hostHolder.getUsers() ==null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUsers().getId(),ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeStatus",likeStatus);

       //对评论进行分页
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setCount(discussPost.getCommentCount());
        //查寻对应的评论
        //评论：给帖子的评论
        //回复：给评论进行的回复
        List<Comment> commentList = commentService.selectCommentsByEntity(
                ENTITY_TYPE_POST,discussPost.getId(),page.getBegin(),page.getLimit());
        //评论的ov列表
        List<Map<String,Object>> commentVoList = new ArrayList<>();

        if(commentList != null){
            for (Comment comment:commentList) {
                Map<String,Object> commentVo=new HashMap<>();
                //单条评论的详细信息，包括它的作者的信息
                commentVo.put("comment",comment);
                commentVo.put("user",userService.findUser(comment.getUserId()));
                //点赞信息
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",likeCount);
                //点赞状态
                likeStatus = hostHolder.getUsers() ==null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUsers().getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeStatus",likeStatus);

                //帖子评论的回复
                List<Comment> replyList = commentService.selectCommentsByEntity(
                        ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE);
                //回复的vo列表
                List<Map<String,Object>> replyVoList =new ArrayList<>();

                if(replyList != null){
                    for (Comment reply:replyList) {
                        Map<String,Object> replyVo=new HashMap<>();
                        replyVo.put("reply",reply);
                        replyVo.put("user",userService.findUser(reply.getUserId()));
                        //回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUser(reply.getTargetId());
                        replyVo.put("target",target);
                        //点赞信息
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeCount",likeCount);
                        //点赞状态
                        likeStatus = hostHolder.getUsers() ==null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUsers().getId(),ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeStatus",likeStatus);

                        replyVoList.add(replyVo);
                    }
                }

                commentVo.put("replys",replyVoList);

                //回复的数量
                 int replyCount = commentService.selectCountByEntity(ENTITY_TYPE_COMMENT,comment.getId());
                 commentVo.put("replyCount",replyCount);

                 commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);

        return "/site/discuss-detail";
    }

}
