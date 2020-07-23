package com.fanxuankai.boot.mqbroker.kafka.autoconfigure;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fanxuankai.boot.mqbroker.consume.AbstractMqConsumer;
import com.fanxuankai.boot.mqbroker.consume.EventListenerRegistry;
import com.fanxuankai.boot.mqbroker.consume.MqConsumer;
import com.fanxuankai.boot.mqbroker.mapper.MsgReceiveMapper;
import com.fanxuankai.boot.mqbroker.model.EmptyEventConfig;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.produce.AbstractMqProducer;
import com.fanxuankai.boot.mqbroker.produce.MqProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;

/**
 * @author fanxuankai
 */
@Slf4j
public class MqBrokerKafkaAutoConfiguration {

    @Bean
    public KafkaMessageListenerContainer<String, String> messageListenerContainer(ConsumerFactory<String, Object> consumerFactory
            , AbstractMqConsumer<String> mqConsumer) {
        String[] topics = EventListenerRegistry.getAllListenerMetadata().toArray(new String[0]);
        ContainerProperties properties = new ContainerProperties(topics);
        properties.setGroupId("mq-broker-group");
        properties.setMessageListener((MessageListener<String, String>) data -> mqConsumer.accept(data.value()));
        return new KafkaMessageListenerContainer<>(consumerFactory, properties);
    }

    @Bean
    @ConditionalOnMissingBean(MqProducer.class)
    public AbstractMqProducer<EmptyEventConfig> mqProducer(KafkaTemplate<String, String> kafkaTemplate) {
        return new AbstractMqProducer<EmptyEventConfig>() {
            @Override
            public boolean isPublisherCallback() {
                return false;
            }

            @Override
            public void accept(Event<String> event) {
                kafkaTemplate.send(event.getName(), JSON.toJSONString(event));
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(MqConsumer.class)
    public AbstractMqConsumer<String> mqConsumer(MsgReceiveMapper msgReceiveMapper) {
        return new AbstractMqConsumer<String>(msgReceiveMapper) {
            @Override
            public Event<String> apply(String s) {
                return JSON.parseObject(s, new TypeReference<Event<String>>() {
                });
            }
        };
    }
}
