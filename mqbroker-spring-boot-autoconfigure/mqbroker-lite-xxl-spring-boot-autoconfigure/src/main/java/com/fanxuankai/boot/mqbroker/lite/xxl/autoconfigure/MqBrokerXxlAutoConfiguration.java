package com.fanxuankai.boot.mqbroker.lite.xxl.autoconfigure;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.fanxuankai.boot.mqbroker.consume.EventListenerRegistry;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.produce.AbstractMqProducer;
import com.fanxuankai.boot.mqbroker.produce.MqProducer;
import com.xxl.mq.client.EnableXxlMqClient;
import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.factory.XxlMqClientFactory;
import com.xxl.mq.client.factory.impl.XxlMqSpringClientFactory;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.producer.XxlMqProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author fanxuankai
 */
@Slf4j
@EnableXxlMqClient
public class MqBrokerXxlAutoConfiguration implements ApplicationRunner {
    private final SimplePropertyPreFilter filter;
    @Resource
    private XxlMqSpringClientFactory xxlMqSpringClientFactory;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    public MqBrokerXxlAutoConfiguration() {
        filter = new SimplePropertyPreFilter();
        filter.getExcludes().add("eventConfig");
    }

    @Bean
    @ConditionalOnMissingBean(MqProducer.class)
    public AbstractMqProducer<XxlEventConfig> mqProducer() {
        return new AbstractMqProducer<XxlEventConfig>() {
            @Override
            public void accept(Event<String> event) {
                XxlMqMessage mqMessage = new XxlMqMessage();
                mqMessage.setTopic(event.getName());
                mqMessage.setGroup(event.getGroup());
                Optional.ofNullable(event.getEventConfig())
                        .map(eventConfig -> (XxlEventConfig) eventConfig)
                        .ifPresent(eventConfig -> {
                            Optional.ofNullable(eventConfig.getRetryCount())
                                    .ifPresent(mqMessage::setRetryCount);
                            Optional.ofNullable(eventConfig.getEffectTime())
                                    .map(localDateTime -> Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()))
                                    .ifPresent(mqMessage::setEffectTime);
                        });
                mqMessage.setData(JSON.toJSONString(event, filter));
                XxlMqProducer.produce(mqMessage);
            }

            @Override
            public boolean isPublisherCallback() {
                return false;
            }
        };
    }

    @Override
    public void run(ApplicationArguments args) {
        List<IMqConsumer> consumers = EventListenerRegistry.getAllListenerMetadata()
                .parallelStream()
                .map(s -> MqConsumerHelper.newClass(XxlMqConsumer.DEFAULT_CONSUMER_NAME, s.getGroup(), s.getTopic(),
                        XxlMqConsumer.class))
                .map(aClass -> {
                    try {
                        return (IMqConsumer) aClass.getConstructor(ThreadPoolExecutor.class).newInstance(threadPoolExecutor);
                    } catch (Exception e) {
                        throw new RuntimeException("消费者实例化失败", e);
                    }
                })
                .collect(Collectors.toList());
        try {
            Field field = xxlMqSpringClientFactory.getClass().getDeclaredField("xxlMqClientFactory");
            field.setAccessible(true);
            XxlMqClientFactory clientFactory = (XxlMqClientFactory) field.get(xxlMqSpringClientFactory);
            clientFactory.setConsumerList(consumers);
            clientFactory.init();
        } catch (Exception e) {
            log.error("获取 XxlMqClientFactory 异常", e);
        }
    }
}
