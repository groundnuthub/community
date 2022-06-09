package com.nowcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {
    //@PostConstruct复职管理bean的生命周期
    @PostConstruct
    public void init(){
    // 解决netty启动冲突问题
    // redis -> netty; es -> netty 当redis启动后es会检查netty，发现已设置则会不启动，因此报错
    // 从Netty4Utils.setAvailableProcessors()可发现
    System.setProperty("es.set.netty.runtime.available.processors","false");

    }

    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }

}
