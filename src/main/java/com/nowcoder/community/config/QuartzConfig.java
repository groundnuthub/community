package com.nowcoder.community.config;

import com.nowcoder.community.quartz.AlphaJob;
import com.nowcoder.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
public class QuartzConfig {

    //FactoryBean是spring底层为了帮我们简化bean的实例化过程
    //1.spring通过FactoryBean封装了某些bean的实例化过程
    //2.可以将FactoryBean装配进容器中,注入给其他的bean。那么其他的bean得到的是FactoryBean所管理的对象的实例

    //配置jobDetail
    //@Bean
    public JobDetailFactoryBean alphaJobDetail(){
        JobDetailFactoryBean factoryBean =new JobDetailFactoryBean();
        //声明你管理的是哪一个bean，它的类型是什么
        factoryBean.setJobClass(AlphaJob.class);
        //给这个任务取一个名字
        factoryBean.setName("alphaJob");
        //给这个任务设置一个组
        factoryBean.setGroup("alphaJobGroup");
        //这个任务是长久的保存,即使任务已经不再执行也不会进行删除
        factoryBean.setDurability(true);
        //这个任务是否可恢复,在应用程序出现问题且已经恢复后也进行恢复
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    //配置Trigger(SimpleTriggerFactoryBean,CronTriggerFactoryBean)
    //SimpleTriggerFactoryBean只能用来定时做一件事
    //CronTriggerFactoryBean可以用来设置每个月或者每个星期的某一天的某一个时间开始做某件事
    //@Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        //对哪一个job做触发
        factoryBean.setJobDetail(alphaJobDetail);
        factoryBean.setName("alphaTrigger");
        factoryBean.setGroup("alphaTriggerGroup");
        //多长时间执行一次这个任务
        factoryBean.setRepeatInterval(3000);
        //Trigger的底层需要一个对象存储job的状态,你需要指定对象的类型. new JobDataMap()这是一个默认的类型
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

    //刷新帖子分数的任务
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean =new JobDetailFactoryBean();
        //声明你管理的是哪一个bean，它的类型是什么
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        //给这个任务取一个名字
        factoryBean.setName("postScoreRefreshJob");
        //给这个任务设置一个组
        factoryBean.setGroup("communityJobGroup");
        //这个任务是长久的保存,即使任务已经不再执行也不会进行删除
        factoryBean.setDurability(true);
        //这个任务是否可恢复,在应用程序出现问题且已经恢复后也进行恢复
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        //对哪一个job做触发
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        //多长时间执行一次这个任务
        factoryBean.setRepeatInterval(1000 * 60 *5);
        //Trigger的底层需要一个对象存储job的状态,你需要指定对象的类型. new JobDataMap()这是一个默认的类型
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

}
