package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId,int begin,int limit);

    //@Param注解用于给参数取别名
    //如果有且只有一个参数用于动态的sql查询语句，则该参数必须取别名
    int selectDiscussPostCount(@Param("userId") int userId);

}
