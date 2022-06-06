package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Resource
    private UserMapper userMapper;

    @Resource
    private MailClient mailClient;

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private RedisTemplate redisTemplate;

   /* @Resource
    private LoginTicketMapper loginTicketMapper;*/

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUser(int id){
       /*return userMapper.selectById(id);*/
        User user = getCache(id);
        if(user==null){
            user = initCache(id);
        }
        return user;
    }

    //注册用户
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
            clearCache(userID);
            return ACTIVATION_SUCCESS;
        }else if(user.getStatus()==1){
            return ACTIVATION_REPEAT;
        }else {
            return ACTIVATION_FAILURE;
        }

    }

    //验证是否有输入的邮箱
    public User getVerification(String email){
        return userMapper.selectByEmail(email);
    }

    //忘记密码
    public Map<String,Object> getPassword(String email,String password){
        Map<String,Object> map=new HashMap<>();

        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
        }else if(StringUtils.isBlank(email)){
            map.put("emailMsg","邮箱不能为空");
        }
        User user=userMapper.selectByEmail(email);
        if(user==null){
            map.put("emailMsg","该邮箱未注册，请输入正确的邮箱（或前去注册新账号）");
        }else {
            String salt=CommunityUtil.generateUUID().substring(0,5);
            password=CommunityUtil.MD5(password+salt);
            userMapper.updatePassword(user.getId(),password,salt);
            map.put("user",user);
        }

        return map;
    }

    //修改账号信息
    public int updateHeader(int userId,String headerUrl){
        int rows = userMapper.updateHeader(userId,headerUrl);
        clearCache(userId);
        return rows;
    }

    //修改密码
    public Map<String, Object> updatePassword(User user,String newPassword,String oldPassword,String confirmPassword){
        Map<String, Object> map=new HashMap<>();

        String Password = CommunityUtil.MD5(oldPassword+user.getSalt());
        if(oldPassword == null){
            map.put("oldPasswordMsg","旧密码不能为空");
        }else if(!Password.equals(user.getPassword())){
            map.put("oldPasswordMsg","旧密码不正确，请重新输入！");
        }else if(newPassword == null){
            map.put("newPasswordMsg","新密码不能为空");
        }else if(confirmPassword == null){
            map.put("confirmPasswordMsg","确认密码不能为空，请输入新密码！");
        }else{
            user.setSalt(CommunityUtil.generateUUID().substring(0,5));
            String password=CommunityUtil.MD5(newPassword+user.getSalt());
            userMapper.updatePassword(user.getId(),password,user.getSalt());
        }

        return map;
    }


    //用户登录
    public Map<String,Object> login(String username,String password,long expiredSeconds) {
        Map<String, Object> map=new HashMap<>();

        User user=userMapper.selectByName(username);
        password = CommunityUtil.MD5(password+user.getSalt());

        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
        }else if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
        }else if(user==null){
            map.put("usernameMsg","该账号不存在！");
        }else if(user.getStatus()!=1){
            map.put("usernameMsg","该账号未激活!");
        }else if(!password.equals(user.getPassword())){
            map.put("passwordMsg","密码输入错误，请重新输入！");
        } else{
            LoginTicket loginTicket=new LoginTicket();
            loginTicket.setUserId(user.getId());
            loginTicket.setTicket(CommunityUtil.generateUUID());
            loginTicket.setStatus(0);
            loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
            //loginTicketMapper.insertLoginTicket(loginTicket);
            String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
            redisTemplate.opsForValue().set(redisKey,loginTicket);

            map.put("ticket",loginTicket.getTicket());
        }

        return map;
    }

    public void logOut(String ticket){
        /*loginTicketMapper.updateStatus(ticket,1);*/
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket= (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    public LoginTicket selectByTicket(String ticket){
        /*return loginTicketMapper.selectByTicket(ticket);*/
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    //1.优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    //2.取不到时初始化缓存数据
    private User initCache(int userId){
       User user = userMapper.selectById(userId);
       String redisKey = RedisKeyUtil.getUserKey(userId);
       redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
       return user;
    }

    //3.当数据变更时，清除缓存数据
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
}
