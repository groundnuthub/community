package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {

    @Resource
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");


    //将指定的IP记入UV
    public void recordUV(String ip){
        String redisKey= RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);
    }

    //统计指定日期范围内的uv
    public long calculateUV(Date start,Date end){
        if(start == null || end == null || start.after(end)){
            throw new IllegalArgumentException("参数不能为空!");
        }
        //整理该日期范围内的key
        List<String> keyList =new ArrayList<>();
        //Calendar.getInstance()不仅能获取当前的时间，还能指定需要获取的时间点.可以做到定时的作用
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE,1);
        }

        //合并日期范围内的数据
        String redisKey =RedisKeyUtil.getUVKey(df.format(start),df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey,keyList.toArray());

        //返回统计结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    //将指定的用户记入DAU
    public void recordDAU(int userId){
        String redisKey= RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey,userId,true);
    }

    //统计指定日期范围内的DAU
    public long calculateDAU(Date start,Date end){
        if(start == null || end == null || start.after(end)){
            throw new IllegalArgumentException("参数不能为空!");
        }
        //整理该日期范围内的key
        //因为key中存储的是二维的数据：每个userid对应一个状态，所以在遍历此集合获取key中的数据时所需的是一个二维的byte的数组
        List<byte[]> keyList =new ArrayList<>();
        //Calendar.getInstance()不仅能获取当前的时间，还能指定需要获取的时间点.可以做到定时的作用
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE,1);
        }

        //进行or运算
        String redisKey =RedisKeyUtil.getDAUKey(df.format(start),df.format(end));
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                redisKey.getBytes(), keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });

    }

}
