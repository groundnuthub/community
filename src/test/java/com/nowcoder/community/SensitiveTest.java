package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//用来加载main中的配置文件，让测试环境和正式环境相同
public class SensitiveTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitive(){
        String text= "这里可以赌博，可以嫖娼，可以吸毒，可以开瓢,可以吸";
        String txt = sensitiveFilter.filter(text);
        System.out.println(txt);

        text= "这里可以☆☼赌☆☼博，可以☆☼嫖☆☼娼，可以☆☼吸☆☼毒，可以☆☼开☆☼瓢,可以吸";
        txt = sensitiveFilter.filter(text);
        System.out.println(txt);
    }

}
