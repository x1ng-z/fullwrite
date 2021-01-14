package hs.fullwrite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 15:56
 * @description: 异步并行任务配置类
 */

@Configuration
@EnableScheduling
@EnableAsync(
        order = Ordered.HIGHEST_PRECEDENCE
)
public class AsyncSchedulingConfiguration implements AsyncConfigurer, SchedulingConfigurer {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        //当前线程数
        scheduler.setPoolSize(100);
        //定时任务前缀
        scheduler.setThreadNamePrefix("scheduler-");
        //该方法用来设置线程池中 任务的等待时间，如果超过这个时间还没有销毁就 强制销毁，以确保应用最后能够被关闭，而不是阻塞住
        scheduler.setAwaitTerminationSeconds(20);
        // 该方法用来设置 线程池关闭 的时候 等待 所有任务都完成后，再继续 销毁 其他的 Bean，这样这些 异步任务 的 销毁 就会先于 数据库连接池对象 的销毁
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        return scheduler;
    }

    @Override
    public Executor getAsyncExecutor() {
        Executor executor = this.taskScheduler();
        return executor;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        TaskScheduler scheduler = this.taskScheduler();
        registrar.setTaskScheduler(scheduler);
    }
}
