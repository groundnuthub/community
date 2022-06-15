package com.nowcoder.community;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//用来加载main中的配置文件，让测试环境和正式环境相同
public class CaffeineTests {

    @Resource
    private DiscussPostService discussPostService;

    @Test
    public void initDataForTest() {
        for (int i = 0; i <300000 ; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("111的互联网求职计划！");
            post.setContent("今年的就业形式不容乐观，过了个年仿佛没有公司还要人一班。令人绝望！");
            post.setCreateTime(new Date());
            post.setScore(Math.random() * 2000);
            discussPostService.addDiscussPostCount(post);
        }
    }

    @Test
    public void test(){
        System.out.println(discussPostService.findDiscussPosts(0,0,10,1));
        System.out.println(discussPostService.findDiscussPosts(0,0,10,1));
        System.out.println(discussPostService.findDiscussPosts(0,0,10,1));
        System.out.println(discussPostService.findDiscussPosts(0,0,10,0));
    }

}
