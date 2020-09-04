package com.fanxuankai.boot.mqbroker.xxl.autoconfigure;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.fanxuankai.boot.mqbroker.consume.EventListenerRegistry;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.produce.AbstractMqProducer;
import com.fanxuankai.boot.mqbroker.produce.MqProducer;
import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.MqConsumerRegistry;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.producer.XxlMqProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * @author fanxuankai
 */
@Slf4j
@Configuration
public class MqBrokerXxlAutoConfiguration implements ApplicationContextAware {
    private final SimplePropertyPreFilter filter;

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
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        EventListenerRegistry.getAllListenerMetadata()
                .parallelStream()
                .map(listenerMetadata -> {
                    try {
                        return (IMqConsumer) MqConsumerHelper.newClass(listenerMetadata, XxlMqConsumer.class)
                                .getConstructor()
                                .newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("IMqConsumer 实例化失败", e);
                    }
                })
                .forEach(MqConsumerRegistry::registerMqConsumer);
    }
}
