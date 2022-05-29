package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private UserService userService;

    @RequestMapping(path = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        //方法调用前，springMVC会自动实例化model和page，并将page注入给model，所以在thymeleaf中可以直接访问page中的数据
        page.setCount(discussPostService.findDiscussPostCount(0));
        page.setPath("/index");

       List<DiscussPost> list=discussPostService.findDiscussPosts(0,page.getBegin(),page.getLimit());
       List<Map<String,Object>> discussPosts=new ArrayList<>();
       if(list !=null){
           for(DiscussPost post:list){
               Map<String,Object> map=new HashMap<>();
               map.put("post",post);
               User user=userService.findUser(post.getUserId());
               map.put("user",user);
               discussPosts.add(map);
           }
       }
       model.addAttribute("discussPosts",discussPosts);
        return "/index";
    }

}
