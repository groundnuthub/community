package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DiscussPostService {

    @Resource
    private DiscussPostMapper discussPostMapper;

   public List<DiscussPost> findDiscussPosts(int userId, int begin, int limit){
       return discussPostMapper.selectDiscussPosts(userId,begin,limit);
   }

    //@Param注解用于给参数取别名
    //如果有且只有一个参数用于动态的sql查询语句，则该参数必须取别名
   public int findDiscussPostCount(@Param("userId") int userId){
       return discussPostMapper.selectDiscussPostCount(userId);
   }
}
