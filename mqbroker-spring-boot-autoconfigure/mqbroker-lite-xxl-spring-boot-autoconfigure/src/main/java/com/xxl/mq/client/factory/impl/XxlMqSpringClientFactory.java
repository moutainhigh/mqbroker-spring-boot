package com.xxl.mq.client.factory.impl;

import com.fanxuankai.boot.mqbroker.consume.EventListener;
import com.fanxuankai.boot.mqbroker.consume.Listener;
import com.fanxuankai.boot.mqbroker.xxl.autoconfigure.MqConsumerHelper;
import com.fanxuankai.boot.mqbroker.xxl.autoconfigure.XxlMqConsumer;
import com.xxl.mq.client.XxlMqClientProperties;
import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.annotation.MqConsumer;
import com.xxl.mq.client.factory.XxlMqClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author xuxueli 2018-11-18 21:18:10
 */
@Slf4j
public class XxlMqSpringClientFactory implements ApplicationContextAware, DisposableBean {

    // ---------------------- param  ----------------------

    private String adminAddress;
    private String accessToken;
    /**
     * 消费者配置
     */
    private Map<String, XxlMqClientProperties.ConsumerConfig> consumerConfig;
    private XxlMqClientFactory xxlMqClientFactory;

    public void setAdminAddress(String adminAddress) {
        this.adminAddress = adminAddress;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setConsumerConfig(Map<String, XxlMqClientProperties.ConsumerConfig> consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        // load consumer from spring
        List<IMqConsumer> consumerList = new ArrayList<>();

        Map<String, Object> serviceMap = applicationContext.getBeansWithAnnotation(MqConsumer.class);
        if (!CollectionUtils.isEmpty(serviceMap)) {
            for (Object serviceBean : serviceMap.values()) {
                if (serviceBean instanceof IMqConsumer) {
                    consumerList.add((IMqConsumer) serviceBean);
                }
            }
        }

        applicationContext.getBeansOfType(EventListener.class)
                .values()
                .forEach(eventListener -> {
                    Listener listener = AnnotationUtils.findAnnotation(eventListener.getClass(), Listener.class);
                    assert listener != null;
                    String group = Optional.of(listener.group())
                            .filter(StringUtils::hasText)
                            .orElse(null);
                    try {
                        IMqConsumer mqConsumer =
                                (IMqConsumer) MqConsumerHelper.newClass(Optional.of(listener.name())
                                                .filter(StringUtils::hasText)
                                                .orElse("default"), group, listener.event(),
                                        XxlMqConsumer.class)
                                        .getConstructor()
                                        .newInstance();
                        consumerList.add(mqConsumer);
                    } catch (Exception e) {
                        log.error("IMqConsumer 实例化失败", e);
                    }
                });

        // init
        xxlMqClientFactory = new XxlMqClientFactory();

        xxlMqClientFactory.setAdminAddress(adminAddress);
        xxlMqClientFactory.setAccessToken(accessToken);
        xxlMqClientFactory.setConsumerList(consumerList);
        xxlMqClientFactory.setConsumerConfig(consumerConfig);

        xxlMqClientFactory.init();
    }

    @Override
    public void destroy() throws Exception {
        xxlMqClientFactory.destroy();
    }

}
