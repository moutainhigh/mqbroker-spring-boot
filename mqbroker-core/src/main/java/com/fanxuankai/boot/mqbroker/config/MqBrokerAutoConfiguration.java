package com.fanxuankai.boot.mqbroker.config;

import com.fanxuankai.boot.mqbroker.consume.AbstractMqConsumer;
import com.fanxuankai.boot.mqbroker.consume.EventListener;
import com.fanxuankai.boot.mqbroker.consume.EventListenerRegistry;
import com.fanxuankai.boot.mqbroker.consume.Listener;
import com.fanxuankai.boot.mqbroker.mapper.MsgSendMapper;
import com.fanxuankai.boot.mqbroker.model.ListenerMetadata;
import com.fanxuankai.boot.mqbroker.produce.MqProducer;
import com.fanxuankai.boot.mqbroker.service.MsgSendService;
import com.fanxuankai.boot.mqbroker.task.TaskConfigurer;
import com.fanxuankai.commons.util.concurrent.ThreadPoolService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author fanxuankai
 */
@Configuration
@EnableConfigurationProperties(MqBrokerProperties.class)
@MapperScan(basePackageClasses = MsgSendMapper.class)
@ComponentScan(basePackageClasses = {AbstractMqConsumer.class, MqProducer.class, MsgSendService.class,
        TaskConfigurer.class})
@EnableTransactionManagement
@EnableScheduling
public class MqBrokerAutoConfiguration implements ApplicationContextAware {

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        applicationContext.getBeansWithAnnotation(Listener.class).values()
                .forEach(o -> {
                    if (o instanceof EventListener) {
                        EventListener<?> eventListener = (EventListener<?>) o;
                        Listener listener = AnnotationUtils.findAnnotation(eventListener.getClass(), Listener.class);
                        assert listener != null;
                        String group = listener.group();
                        if (!StringUtils.hasText(group)) {
                            group = null;
                        }
                        EventListenerRegistry.addListener(new ListenerMetadata()
                                        .setGroup(group)
                                        .setTopic(listener.event()),
                                eventListener);
                    }
                });
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(4, new ThreadFactory() {
            private final AtomicLong count = new AtomicLong(0L);

            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("broker-scheduler-" + count.getAndIncrement());
                return thread;
            }
        });
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public ThreadPoolExecutor executorService() {
        return ThreadPoolService.getInstance();
    }
}
