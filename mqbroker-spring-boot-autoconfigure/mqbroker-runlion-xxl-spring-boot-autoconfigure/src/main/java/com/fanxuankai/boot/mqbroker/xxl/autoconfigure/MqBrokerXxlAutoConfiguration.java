package com.fanxuankai.boot.mqbroker.xxl.autoconfigure;

import com.alibaba.fastjson.JSON;
import com.fanxuankai.boot.mqbroker.config.EventListenerRegistryHook;
import com.fanxuankai.boot.mqbroker.consume.EventListenerRegistry;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.produce.AbstractMqProducer;
import com.fanxuankai.boot.mqbroker.produce.MqProducer;
import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.MqConsumerRegistry;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.producer.XxlMqProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * @author fanxuankai
 */
@Configuration
public class MqBrokerXxlAutoConfiguration implements EventListenerRegistryHook {

    @Bean
    @ConditionalOnMissingBean(MqProducer.class)
    public AbstractMqProducer mqProducer() {
        return new AbstractMqProducer() {
            @Override
            public void accept(Event<String> event) {
                XxlMqMessage mqMessage = new XxlMqMessage();
                mqMessage.setTopic(event.getName());
                mqMessage.setGroup(event.getGroup());
                Optional.ofNullable(event.getRetryCount())
                        .ifPresent(mqMessage::setRetryCount);
                Optional.ofNullable(event.getEffectTime())
                        .map(localDateTime -> Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()))
                        .ifPresent(mqMessage::setEffectTime);
                mqMessage.setData(JSON.toJSONString(event));
                XxlMqProducer.produce(mqMessage);
            }

            @Override
            public boolean isPublisherCallback() {
                return false;
            }
        };
    }

    @Override
    public void execute() {
        EventListenerRegistry.getAllListenerMetadata()
                .parallelStream()
                .map(listenerMetadata -> {
                    try {
                        return (IMqConsumer) MqConsumerHelper.newClass(listenerMetadata)
                                .getConstructor()
                                .newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("IMqConsumer 实例化失败", e);
                    }
                })
                .forEach(MqConsumerRegistry::registerMqConsumer);
    }
}
