package hs.fullwrite.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/10/8 11:28
 */
@Configuration
public class AppThreadconfig {
    @Bean
    public ExecutorService threadpool(){
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("exe-pool-%d").setDaemon(true).build();

        /**
         *new ThreadPoolExecutor.CallerRunsPolicy()是一个RejectedExecutorHandler
         *RejectedExecutionHandler：当线程池不能执行提交的线程任务时使用的策略
         * -DiscardOldestPolicy：丢弃最先提交到线程池的任务
         *-AbortPolicy： 中断此次提交，并抛出异常
         *-CallerRunsPolicy： 主线程自己执行此次任务
         *-DiscardPolicy： 直接丢弃此次任务，不抛出异常
         * */
        ExecutorService pool = new ThreadPoolExecutor(10/* 线程池维护的线程数量，即使其中有闲置线程*/, 100/*线程池能容纳的最大线程数量*/,
                60L/*当前线程数量超出CORE_POOL_SIZE时，过量线程在开始任务前的等待时间，超时将被关闭*/, TimeUnit.MILLISECONDS,/*KEEP_ALIVE_TIME的单位*/
                new LinkedBlockingQueue<Runnable>(1), namedThreadFactory,  new ThreadPoolExecutor.CallerRunsPolicy()/*当执行被阻塞时要使用的处理程序,因为达到了线程界限和队列容量*/);

        return pool;
    }
}
