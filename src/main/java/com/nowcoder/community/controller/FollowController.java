package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.FollowService;
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
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Resource
    private FollowService followService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private UserService userService;

    @Resource
    private EventProducer eventProducer;

    @LoginRequired
    @RequestMapping(path = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = hostHolder.getUsers();

        followService.follow(user.getId(),entityType,entityId);

        //触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"已关注");
    }

    @LoginRequired
    @RequestMapping(path = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user = hostHolder.getUsers();

        followService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"已取消关注");
    }

    //查询某个人的关注列表
    @RequestMapping(path = "/followee/{userId}",method = RequestMethod.GET)
    public String followee(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUser(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followee/"+userId);
        page.setCount((int) followService.findFolloweeCount(userId,ENTITY_TYPE_USER));

        List<Map<String,Object>> userList = followService.findFollowee(userId,page.getBegin(),page.getLimit());
        if(userList!=null){
            for (Map<String,Object> map:userList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);

        return "/site/followee";
    }

    //查询某个人的被关注列表
    @RequestMapping(path = "/follower/{userId}",method = RequestMethod.GET)
    public String follower(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUser(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/follower/"+userId);
        page.setCount((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        List<Map<String,Object>> userList = followService.findFollower(userId,page.getBegin(),page.getLimit());
        if(userList!=null){
            for (Map<String,Object> map:userList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);

        return "/site/follower";
    }

    public boolean hasFollowed(int userId){
        User user = hostHolder.getUsers();
        if(user==null){
            return false;
        }
        return followService.hasFollowed(user.getId(),ENTITY_TYPE_USER,userId);
    }

}
