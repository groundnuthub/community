package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Resource
    private UserMapper userMapper;

    @Resource
    private MailClient mailClient;

    @Resource
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUser(int id){
       return userMapper.selectById(id);
    }

    public Map<String,Object> register(User user){
        Map<String,Object> map=new HashMap<>();

        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        else if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
        }else if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
        }else if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
        }else if((userMapper.selectByName(user.getUsername())!=null)&&(userMapper.selectByEmail(user.getEmail())!=null)){
            map.put("usernameMsg","账号已存在");
            map.put("emailMsg","邮箱已被注册");
        }else if(userMapper.selectByEmail(user.getEmail())!=null){
            map.put("emailMsg","邮箱已被注册");
        }else if(userMapper.selectByName(user.getUsername())!=null){
            map.put("usernameMsg","账号已存在");
        }else{
            user.setSalt(CommunityUtil.generateUUID().substring(0,5));
            user.setPassword(CommunityUtil.MD5(user.getPassword()+user.getSalt()));
            user.setType(0);
            user.setStatus(0);
            user.setActivationCode(CommunityUtil.generateUUID());
            user.setHeaderUrl(String.format("http://imges.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
            user.setCreateTime(new Date());
            userMapper.insertUser(user);

            //发送激活邮件
            Context context=new Context();
            context.setVariable("email",user.getEmail());
            context.setVariable("url",domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode());
            String content=templateEngine.process("/mail/activation",context);

            mailClient.sendMail(user.getEmail(),"激活账号",content);
        }

        return map;
    }

    public int activation(int userID,String code){
        User user=userMapper.selectById(userID);
        if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userID,1);
            return ACTIVATION_SUCCESS;
        }else if(user.getStatus()==1){
            return ACTIVATION_REPEAT;
        }else {
            return ACTIVATION_FAILURE;
        }

    }

}
