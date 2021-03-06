package com.nowcoder.community.util;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {

    //生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("_","");
    }

    //MD5
    //MD5加密方式为：hello-->abc123edf456是一种固定的加密方式，并不完全安全
    //但是在加密前给密码加上随机数则可以保障一定的安全系数
    //StringUtils.isBlank(key)是commons中的方法，用于判断传入的字符串是否是空或空格
    public static String MD5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        //DigestUtils.md5DigestAsHex是spring自带方法，用于将传入的值转换成MD5加密
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJSONString(int code, String msg, Map<String, Object> map){
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if(map != null){
            for(String key : map.keySet()){
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg){
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code){
        return getJSONString(code, null, null);
    }

}
