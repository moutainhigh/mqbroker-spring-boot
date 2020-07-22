package com.fanxuankai.boot.mqbroker.xxl.autoconfigure;

import com.alibaba.fastjson.JSON;
import com.fanxuankai.boot.commons.util.MqConsumerUtil;
import com.fanxuankai.boot.mqbroker.consume.EventListenerRegistry;
import com.fanxuankai.boot.mqbroker.mapper.MsgReceiveMapper;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.produce.AbstractMqProducer;
import com.fanxuankai.boot.mqbroker.produce.MqProducer;
import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.factory.XxlMqClientFactory;
import com.xxl.mq.client.factory.impl.XxlMqSpringClientFactory;
import com.xxl.mq.client.message.XxlMqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author fanxuankai
 */
@Slf4j
public class MqBrokerXxlAutoConfiguration implements ApplicationRunner {
    @Resource
    private XxlMqSpringClientFactory xxlMqSpringClientFactory;
    @Resource
    private MsgReceiveMapper msgReceiveMapper;

    @Bean
    @ConditionalOnMissingBean(MqProducer.class)
    public AbstractMqProducer mqProducer() {
        return new AbstractMqProducer() {
            @Override
            public void accept(Event<String> event) {
                com.xxl.mq.client.producer.XxlMqProducer.produce(new XxlMqMessage(event.getName(),
                        JSON.toJSONString(event)));
            }

            @Override
            public boolean isPublisherCallback() {
                return false;
            }
        };
    }

    @Override
    public void run(ApplicationArguments args) {
        List<IMqConsumer> consumers = EventListenerRegistry.allReceiveEvent()
                .parallelStream()
                .map(s -> MqConsumerUtil.newClass(s, XxlMqConsumer.class))
                .map(aClass -> {
                    try {
                        return (IMqConsumer) aClass.getConstructor(MsgReceiveMapper.class).newInstance(msgReceiveMapper);
                    } catch (Exception ignored) {

                    }
                    return null;
                })
                .filter(Objects::nonNull)
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
