package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class DiscussPostService {

    @Resource
    private DiscussPostMapper discussPostMapper;

    @Resource
    private SensitiveFilter sensitiveFilter;

   public List<DiscussPost> findDiscussPosts(int userId, int begin, int limit,int orderMode){
       return discussPostMapper.selectDiscussPosts(userId,begin,limit,orderMode);
   }

    //@Param注解用于给参数取别名
    //如果有且只有一个参数用于动态的sql查询语句，则该参数必须取别名
   public int findDiscussPostCount(@Param("userId") int userId){
       return discussPostMapper.selectDiscussPostCount(userId);
   }

    public int addDiscussPostCount(DiscussPost discussPost){
       if(discussPost == null){
           throw new IllegalArgumentException("参数不能为空");
       }
       //将页面传到服务器的数据转化成存文本格式使用HtmlUtils的htmlEscape方法
       discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
       discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

       //使用过滤器过滤敏感词
       discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
       discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

       return discussPostMapper.insertDiscussPosts(discussPost);
    }

    public DiscussPost findDiscussPost(int id){
        return discussPostMapper.selectDiscussPost(id);
    }

    public int updateCommentCount(int id,int commentCount){
       return discussPostMapper.updateCommentCount(id,commentCount);
    }

    public int updateType(int id,int type){
       return discussPostMapper.updateType(id,type);
    }

    public int updateStatus(int id,int status){
       return discussPostMapper.updateStatus(id,status);
    }

    public int updateScore(int id,double score){
       return discussPostMapper.updateScore(id,score);
    }
}
