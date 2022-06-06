package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger= LoggerFactory.getLogger(LoginController.class);

    @Resource
    private UserService userService;

    @Resource
    private Producer kaptchaProducer;

    @Resource
    private MailClient mailClient;

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private RedisTemplate redisTemplate;

    @Value("server.servlet.context-path")
    private String contextPath;

    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    @RequestMapping(path = "/forget",method = RequestMethod.GET)
    public String getForPassword(){
        return "/site/forget";
    }

    @RequestMapping(path = "/registers",method = RequestMethod.POST)
    public String register(Model model, User user){

        Map<String,Object> map=userService.register(user);
        if(map ==null||map.isEmpty()){
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了激活邮件，请注意尽快激活！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }

    @RequestMapping(path = "/activation/{userID}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userID") int userID,@PathVariable("code") String code){
        int result = userService.activation(userID,code);
        if(result==ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功，请登录您的账号！");
            model.addAttribute("target","/login");
        }else if(result==ACTIVATION_REPEAT){
            model.addAttribute("msg","您已激活成功，请不要重复激活！");
            model.addAttribute("target","/login");
        }else{
            model.addAttribute("msg","激活成功，请登录您的账号！");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/){
        //生成验证码
       String text = kaptchaProducer.createText();
       BufferedImage image = kaptchaProducer.createImage(text);

       //将验证码存入session
        /*session.setAttribute("kaptcha",text);*/

        //验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //将验证码存入redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);

        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            //
            OutputStream os= response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("验证图片生成错误"+e.getMessage());
        }

    }

    //登录由此开始

    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username,String password,String code,boolean remember,Model model,
            /*HttpSession session,*/HttpServletResponse response,@CookieValue(value = "kaptchaOwner",required = false) String kaptchaOwner){

       /* String kaptcha=session.getAttribute("kaptcha").toString();*/
        String kaptcha =null;
        if(StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey=RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) ){
            model.addAttribute("codeMsg","验证码不能为空!");
            return "/site/login";
        }else if(!kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码输入错误!");
            return "/site/login";
        }

        int expiredSeconds =remember ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map= userService.login(username,password,expiredSeconds);
        if(map.containsKey("ticket")){
            Cookie cookie=new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            //从一个controller跳转到另一个控制器
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logOut(ticket);
        return "redirect:/login";
    }

    //发送验证码邮件
    @RequestMapping(path = "/forget/code",method = RequestMethod.GET)
    @ResponseBody
    public String getVerification(String email,HttpSession session){

        if(StringUtils.isBlank(email)){
            return CommunityUtil.getJSONString(1, "邮箱不能为空！");
        }else if(userService.getVerification(email)==null){
            return CommunityUtil.getJSONString(1, "该邮箱未注册，请输入正确的邮箱（或前去注册新账号）！");
        }else{
            //发送验证码邮件
            String verification = CommunityUtil.generateUUID().substring(0,6);
            Context context=new Context();
            context.setVariable("email",email);
            context.setVariable("Verification",verification);
            String content=templateEngine.process("/mail/forget",context);
            session.setAttribute(email+"_verification",verification);

            mailClient.sendMail(email,"找回密码",content);
            return CommunityUtil.getJSONString(0);
        }

    }

    //找回密码
    @RequestMapping(path = "/forget/password",method = RequestMethod.POST)
    public String getPassword(Model model,HttpSession session,String verifyCode,String email,String password){
        String verification= (String) session.getAttribute(email+"_verification");
        if(StringUtils.isBlank(verification)){
            model.addAttribute("verifyCodeMsg","验证码不能为空");
            return "/site/forget";
        }else if(!verification.equalsIgnoreCase(verifyCode)){
            model.addAttribute("verifyCodeMsg","验证码错误，请重新输入！");
            return "/site/forget";
        }

        Map<String, Object> map = userService.getPassword(email, password);
        if (map.containsKey("user")) {
            model.addAttribute("msg","修改密码成功，请重新登录");
            model.addAttribute("target","/login");
            return "/site/operate-result";
        } else {
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }

    }

}
