package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LikeService {

    @Resource
    private RedisTemplate redisTemplate;

    //点赞
    public void like(int userId,int entityType,int entityId,int entityUserId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKye = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                String userLikeKye = RedisKeyUtil.getUserLikKey(entityUserId);
                boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKye,userId);

                //开启事务
                operations.multi();
                if(isMember){
                    operations.opsForSet().remove(entityLikeKye,userId);
                    operations.opsForValue().decrement(userLikeKye);
                }else {
                    operations.opsForSet().add(entityLikeKye,userId);
                    operations.opsForValue().increment(userLikeKye);
                }

                return operations.exec();
            }
        });
    }

    //查询实体点赞数量
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKye = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKye);
    }

    //查询你是否已点赞
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKye = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKye,userId) ? 1 : 0;
    }

    //查询
    public int findUserLikeCount(int userId){
        String userLikeKye = RedisKeyUtil.getUserLikKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKye);
        return count ==null ? 0 :  count.intValue();
    }

}
