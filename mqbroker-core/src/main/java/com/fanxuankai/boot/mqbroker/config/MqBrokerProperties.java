package com.fanxuankai.boot.mqbroker.config;

import com.fanxuankai.boot.mqbroker.consume.EventStrategy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;

/**
 * @author fanxuankai
 */
@Configuration
@ConfigurationProperties(prefix = MqBrokerProperties.PREFIX)
@Data
public class MqBrokerProperties {
    public static final String PREFIX = "mq-broker";
    /**
     * 最大并发数
     */
    private int maxConcurrent = Runtime.getRuntime().availableProcessors();
    /**
     * 拉取消息的数量, 大于 500 时需要设置 mybatis-plus 分页 limit 为-1
     */
    private int msgSize = 1_000;
    /**
     * 最大重试次数
     */
    private int maxRetry = 3;
    /**
     * 拉取数据的间隔 ms
     */
    private long intervalMillis = 300_000;
    /**
     * 发布回调超时 ms
     */
    private long publisherCallbackTimeout = 300_000;
    /**
     * 消费超时 ms
     */
    private long consumeTimeout = 300_000;
    /**
     * 手动签收
     */
    private boolean manualAcknowledge;
    /**
     * key: 事件名 value: EventStrategy
     */
    private Map<String, EventStrategy> eventStrategy = Collections.emptyMap();
}
