package com.nowcoder.community.Config;

import com.nowcoder.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class); //声明管理的job类
        factoryBean.setName("postScoreRefreshJob"); //给任务取名，不可重复
        factoryBean.setGroup("communityJobGroup"); //给任务取组名，多个任务可以在同一个组
        factoryBean.setDurability(true); //是否将任务持久保存
        factoryBean.setRequestsRecovery(true); //任务是否是可恢复的，即如果任务有问题的话是否是可以被恢复的
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail); //设置对哪个job的触发器，通过名字进行区分，优先将同名的进行注入
        factoryBean.setName("postScoreRefreshTrigger"); //给触发器取名
        factoryBean.setGroup("communityTriggerGroup"); //给触发器取组名
        factoryBean.setRepeatInterval(1000 * 60 * 5); //频率，即多长时间执行一次该任务，单位为毫秒
        factoryBean.setJobDataMap(new JobDataMap());  //用默认的JobDataMap对象来存储Job的一些状态
        return factoryBean;
    }
}
