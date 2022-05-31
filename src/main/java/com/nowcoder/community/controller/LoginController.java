package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger= LoggerFactory.getLogger(LoginController.class);

    @Resource
    private UserService userService;

    @Resource
    private Producer kaptchaProducer;

    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
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
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //生成验证码
       String text = kaptchaProducer.createText();
       BufferedImage image = kaptchaProducer.createImage(text);

       //将验证码存入session
        session.setAttribute("kaptcha",text);

        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            //
            OutputStream os= response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("验证图片生成错误",e.getMessage());
        }

    }

}
