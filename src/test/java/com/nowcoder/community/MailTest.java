package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//用来加载main中的配置文件，让测试环境和正式环境相同
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Test
    public void testMail(){
        mailClient.sendMail("483296752@qq.com","text","welcome");

    }

    @Resource
    private TemplateEngine templateEngine;

    @Test
    public void testMailHtml(){
        Context context=new Context();
        context.setVariable("username","张三");

        String content=templateEngine.process("/mail/demo",context);
        System.out.println(content);

        mailClient.sendMail("483296752@qq.com","html",content);
    }
}
