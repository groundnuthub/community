package com.nowcoder.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Resource
    private DiscussPostMapper discussPostMapper;

    @Resource
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //caffeine核心接口：Cache,LoadingCache,AsyncLoadingCache
    //LoadingCache同步缓存：在缓存中没有数据的情况下，他会启用一个线程从数据库中获取数据，其他线程在此期间等待响应
    //AsyncLoadingCache异步缓存：可以多个线程并发的同时去数据

    //帖子列表的缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    //缓存帖子总数
    private LoadingCache<Integer, Integer> postCountCache;

    @PostConstruct
    public void init(){
        //初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                //CacheLoader:在缓存中没有数据时，用于声明查询数据方法的接口
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception {
                        if(StringUtils.isBlank(key)){
                            throw new IllegalArgumentException("参数错误!");
                        }

                        String[] params = key.split(":");
                        if(params == null || params.length!=2){
                            throw new IllegalArgumentException("参数错误!");
                        }

                        int begin = Integer.parseInt(params[0]);
                        int limit = Integer.parseInt(params[1]);

                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0,begin,limit,1);
                    }
                });

        //初始化帖子总数缓存
        postCountCache= Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                //CacheLoader:在缓存中没有数据时，用于声明查询数据方法的接口
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception {

                        logger.debug("load post count from DB.");
                        return discussPostMapper.selectDiscussPostCount(key);
                    }
                });

    }

   public List<DiscussPost> findDiscussPosts(int userId, int begin, int limit,int orderMode){
        if(userId == 0 && orderMode == 1){
            return postListCache.get(begin  + ":"  + limit);
        }

        logger.debug("load post list from DB.");
       return discussPostMapper.selectDiscussPosts(userId,begin,limit,orderMode);
   }

    //@Param注解用于给参数取别名
    //如果有且只有一个参数用于动态的sql查询语句，则该参数必须取别名
   public int findDiscussPostCount(@Param("userId") int userId){
        if(userId == 0){
            return postCountCache.get(userId);
        }

       logger.debug("load post count from DB.");
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
