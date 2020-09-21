package com.fanxuankai.boot.mqbroker.xxl.autoconfigure;

import com.alibaba.fastjson.JSON;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.produce.AbstractMqProducer;
import com.fanxuankai.boot.mqbroker.produce.MqProducer;
import com.xxl.mq.client.XxlMqClientProperties;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.producer.XxlMqProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * @author fanxuankai
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({XxlMqClientProperties.class})
public class MqBrokerXxlAutoConfiguration {

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

    @Bean
    @ConditionalOnMissingBean(MqBrokerLiteXxlMqSpringClientFactory.class)
    public MqBrokerLiteXxlMqSpringClientFactory mqBrokerLiteXxlMqSpringClientFactory(XxlMqClientProperties clientProperties) {
        log.info(">>>>>>>>>>> xxl-mq client config init.");

        MqBrokerLiteXxlMqSpringClientFactory xxlMqSpringClientFactory = new MqBrokerLiteXxlMqSpringClientFactory();
        xxlMqSpringClientFactory.setAdminAddress(clientProperties.getAdminAddress());
        xxlMqSpringClientFactory.setAccessToken(clientProperties.getAccessToken());
        xxlMqSpringClientFactory.setConsumerConfig(clientProperties.getConsumerConfig());

        return xxlMqSpringClientFactory;
    }

}
