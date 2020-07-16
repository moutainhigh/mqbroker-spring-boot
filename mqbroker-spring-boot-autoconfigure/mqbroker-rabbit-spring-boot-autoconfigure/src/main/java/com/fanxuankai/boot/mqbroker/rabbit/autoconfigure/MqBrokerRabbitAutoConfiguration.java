package com.fanxuankai.boot.mqbroker.rabbit.autoconfigure;

import com.alibaba.fastjson.JSON;
import com.fanxuankai.boot.mqbroker.config.MqBrokerProperties;
import com.fanxuankai.boot.mqbroker.consume.AbstractMqConsumer;
import com.fanxuankai.boot.mqbroker.consume.EventListenerRegistry;
import com.fanxuankai.boot.mqbroker.consume.MqConsumer;
import com.fanxuankai.boot.mqbroker.mapper.MsgReceiveMapper;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.produce.AbstractMqProducer;
import com.fanxuankai.boot.mqbroker.produce.MqProducer;
import com.fanxuankai.boot.mqbroker.service.MsgSendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory.ConfirmType;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fanxuankai
 */
@Slf4j
public class MqBrokerRabbitAutoConfiguration {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public MessageListenerContainer simpleMessageQueueLister(ConnectionFactory connectionFactory,
                                                             AbstractMqConsumer<Event> mqConsumer,
                                                             AmqpAdmin amqpAdmin,
                                                             MqBrokerProperties mqBrokerProperties) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        // 监听的队列
        EventListenerRegistry.allReceiveEvent()
                .stream()
                .map(o -> new Queue(o, true))
                .forEach(queue -> {
                    amqpAdmin.declareQueue(queue);
                    container.addQueues(queue);
                });
        // 是否重回队列
        container.setDefaultRequeueRejected(false);
        container.setErrorHandler(throwable -> log.error("消费异常", throwable));
        if (mqBrokerProperties.isManualAcknowledge()) {
            // 手动确认
            container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
            container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
                mqConsumer.accept(JSON.parseObject(new String(message.getBody()), Event.class));
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            });
        } else {
            // 自动确认
            container.setAcknowledgeMode(AcknowledgeMode.AUTO);
            container.setMessageListener(message -> mqConsumer.accept(JSON.parseObject(new String(message.getBody()),
                    Event.class)));
        }
        return container;
    }

    @Bean
    @ConditionalOnMissingBean(MqProducer.class)
    public AbstractMqProducer mqProducer(AmqpAdmin amqpAdmin,
                                         RabbitTemplate rabbitTemplate,
                                         RabbitProperties rabbitProperties,
                                         MsgSendService msgSendService) {
        String correlationDataRegex = ",";
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            assert correlationData != null;
            String[] split = Objects.requireNonNull(correlationData.getId()).split(correlationDataRegex);
            String topic = split[0];
            String code = split[1];
            if (ack) {
                msgSendService.success(topic, code);
            } else {
                msgSendService.failure(topic, code, Optional.ofNullable(cause).orElse("nack"));
            }
        });
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            Event event = JSON.parseObject(new String(message.getBody()), Event.class);
            String cause = "replyCode: " + replyCode + ", replyText: " + replyText + ", exchange: " + exchange;
            msgSendService.failure(routingKey, event.getKey(), cause);
        });
        return new AbstractMqProducer() {
            private final Map<String, Object> queueCache = new ConcurrentHashMap<>(16);

            @Override
            public boolean isPublisherCallback() {
                ConfirmType publisherConfirmType = rabbitProperties.getPublisherConfirmType();
                return publisherConfirmType != null
                        && publisherConfirmType != ConfirmType.NONE
                        && rabbitProperties.isPublisherReturns();
            }

            @Override
            public void accept(Event event) {
                if (!queueCache.containsKey(event.getName())) {
                    amqpAdmin.declareQueue(new Queue(event.getName()));
                    queueCache.put(event.getName(), Boolean.TRUE);
                }
                rabbitTemplate.convertAndSend(event.getName(), event,
                        new CorrelationData(event.getName() + correlationDataRegex + event.getKey()));
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(MqConsumer.class)
    public AbstractMqConsumer<Event> mqConsumer(MsgReceiveMapper msgReceiveMapper) {
        return new AbstractMqConsumer<Event>(msgReceiveMapper) {
        };
    }
}
