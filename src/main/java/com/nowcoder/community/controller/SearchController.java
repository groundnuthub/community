package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.SearchResult;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Resource
    private ElasticsearchService elasticsearchService;

    @Resource
    private UserService userService;

    @Resource
    private LikeService likeService;

    @RequestMapping(path = "/search",method = RequestMethod.GET)
    public String search(String keyword, Page page,Model model) throws IOException {
        //搜索帖子
        SearchResult searchResult = elasticsearchService.searchDiscussPost(keyword,(page.getCurrent()-1)*10,page.getLimit());
        long total = searchResult.getTotal();
        List<DiscussPost> posts =searchResult.getList();

        List<Map<String,Object>> discussPosts =new ArrayList<>();
        if(posts != null){
            for(DiscussPost post : posts){
                Map<String,Object> map =new HashMap<>();
                //帖子的详细数据
                map.put("post",post);
                //作者的详细数据
                map.put("user",userService.findUser(post.getUserId()));
                //帖子的点赞数量
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);
        //设置分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setCount(searchResult.getTotal() == 0 ? 0 : (int) searchResult.getTotal());

        return "/site/search";
    }

}
