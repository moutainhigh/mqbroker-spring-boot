package com.fanxuankai.boot.mqbroker.config;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.fanxuankai.boot.mqbroker.consume.AbstractMqConsumer;
import com.fanxuankai.boot.mqbroker.consume.EventListener;
import com.fanxuankai.boot.mqbroker.consume.EventListenerRegistry;
import com.fanxuankai.boot.mqbroker.consume.Listener;
import com.fanxuankai.boot.mqbroker.mapper.MsgSendMapper;
import com.fanxuankai.boot.mqbroker.model.ListenerMetadata;
import com.fanxuankai.boot.mqbroker.produce.MqProducer;
import com.fanxuankai.boot.mqbroker.service.MsgSendService;
import com.fanxuankai.boot.mqbroker.task.TaskConfigurer;
import com.fanxuankai.commons.util.concurrent.ThreadPoolService;
import org.apache.commons.codec.binary.Base64;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author fanxuankai
 */
@Configuration
@EnableConfigurationProperties(MqBrokerProperties.class)
@MapperScan(basePackageClasses = MsgSendMapper.class)
@ComponentScan(basePackageClasses = {AbstractMqConsumer.class, MqProducer.class, MsgSendService.class,
        TaskConfigurer.class})
@EnableTransactionManagement
@EnableScheduling
public class MqBrokerAutoConfiguration implements ApplicationContextAware {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(4, new ThreadFactory() {
            private final AtomicLong count = new AtomicLong(0L);

            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("broker-scheduler-" + count.getAndIncrement());
                return thread;
            }
        });
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public ThreadPoolExecutor threadPoolExecutor() {
        return ThreadPoolService.getInstance();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        applicationContext.getBeansOfType(EventListener.class)
                .values()
                .forEach(eventListener -> {
                    Listener listener = AnnotationUtils.findAnnotation(eventListener.getClass(), Listener.class);
                    assert listener != null;
                    String group = Optional.of(listener.group())
                            .filter(StringUtils::hasText)
                            .orElse(null);
                    EventListenerRegistry.addListener(new ListenerMetadata()
                            .setGroup(group)
                            .setTopic(listener.event())
                            .setName(listener.name()), eventListener);
                });
    }

    @Bean
    @ConditionalOnMissingBean(DingTalkClient.class)
    @ConditionalOnProperty(prefix = MqBrokerProperties.DING_TALK_PREFIX, value = "enabled", havingValue = "true")
    public DingTalkClient dingTalkClient(MqBrokerProperties mqBrokerProperties) throws NoSuchAlgorithmException,
            InvalidKeyException, UnsupportedEncodingException {
        MqBrokerProperties.DingTalk dingTalk = mqBrokerProperties.getDingTalk();
        String serviceUrl = dingTalk.getUrl();
        serviceUrl += "?access_token=" + dingTalk.getAccessToken();
        if (StringUtils.hasText(dingTalk.getSecret())) {
            Long timestamp = System.currentTimeMillis();
            String secret = dingTalk.getSecret();
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
            serviceUrl += "&timestamp=" + timestamp;
            serviceUrl += "&sign=" + sign;
        }
        return new DefaultDingTalkClient(serviceUrl);
    }
}
