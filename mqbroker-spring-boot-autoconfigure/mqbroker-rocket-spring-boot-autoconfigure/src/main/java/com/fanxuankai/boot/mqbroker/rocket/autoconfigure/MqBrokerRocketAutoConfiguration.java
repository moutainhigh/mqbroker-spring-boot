package com.fanxuankai.boot.mqbroker.rocket.autoconfigure;

import com.alibaba.fastjson.JSON;
import com.fanxuankai.boot.mqbroker.consume.AbstractMqConsumer;
import com.fanxuankai.boot.mqbroker.consume.EventListenerRegistry;
import com.fanxuankai.boot.mqbroker.consume.MqConsumer;
import com.fanxuankai.boot.mqbroker.mapper.MsgReceiveMapper;
import com.fanxuankai.boot.mqbroker.model.EmptyEventConfig;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.produce.AbstractMqProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author fanxuankai
 */
@Slf4j
public class MqBrokerRocketAutoConfiguration {

    @Bean
    public AbstractMqProducer<EmptyEventConfig> mqProducer(RocketMQTemplate template) {
        return new AbstractMqProducer<EmptyEventConfig>() {
            @Override
            public boolean isPublisherCallback() {
                return false;
            }

            @Override
            public void accept(Event<String> event) {
                template.convertAndSend(event.getName(), event);
            }
        };
    }

    @Bean(initMethod = "start")
    public DefaultMQPushConsumer pushConsumer(RocketMQProperties properties, AbstractMqConsumer<Event<?>> mqConsumer) {
        DefaultMQPushConsumer consumer =
                new DefaultMQPushConsumer(properties.getProducer().getGroup());
        consumer.setNamesrvAddr(properties.getNameServer());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.setMessageModel(MessageModel.BROADCASTING);
        consumer.registerMessageListener((MessageListenerConcurrently) (list, consumeConcurrentlyContext) -> {
            list.forEach(messageExt -> mqConsumer.accept(JSON.parseObject(new String(messageExt.getBody()),
                    Event.class)));
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        EventListenerRegistry.getAllListenerMetadata()
                .forEach(s -> {
                    try {
                        consumer.subscribe(s.getTopic(), "*");
                    } catch (MQClientException e) {
                        log.error("订阅失败", e);
                    }
                });
        return consumer;
    }

    @Bean
    @ConditionalOnMissingBean(MqConsumer.class)
    public AbstractMqConsumer<Event<String>> mqConsumer(MsgReceiveMapper msgReceiveMapper) {
        return new AbstractMqConsumer<Event<String>>(msgReceiveMapper) {
        };
    }
}
