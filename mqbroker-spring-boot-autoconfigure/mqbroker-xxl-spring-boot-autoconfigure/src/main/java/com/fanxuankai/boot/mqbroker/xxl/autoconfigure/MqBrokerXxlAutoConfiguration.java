package com.fanxuankai.boot.mqbroker.xxl.autoconfigure;

import com.fanxuankai.boot.commons.util.MqConsumerUtil;
import com.fanxuankai.boot.mqbroker.consume.EventListenerRegistry;
import com.fanxuankai.boot.mqbroker.mapper.MsgReceiveMapper;
import com.fanxuankai.boot.mqbroker.produce.MqProducer;
import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.factory.XxlMqClientFactory;
import com.xxl.mq.client.factory.impl.XxlMqSpringClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author fanxuankai
 */
@Slf4j
public class MqBrokerXxlAutoConfiguration {
    @Resource
    private XxlMqSpringClientFactory xxlMqSpringClientFactory;
    @Resource
    private MsgReceiveMapper msgReceiveMapper;

    @Bean
    @ConditionalOnMissingBean(MqProducer.class)
    public XxlMqProducer mqProducer() {
        return new XxlMqProducer();
    }

    @PostConstruct
    public void init() {
        List<IMqConsumer> consumers = EventListenerRegistry.allReceiveEvent()
                .parallelStream()
                .map(s -> MqConsumerUtil.newClass(s, XxlMqConsumerTemplate.class))
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
