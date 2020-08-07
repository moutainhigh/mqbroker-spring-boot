package com.fanxuankai.boot.mqbroker.xxl.autoconfigure;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fanxuankai.boot.mqbroker.consume.AbstractMqConsumer;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.MqResult;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author fanxuankai
 */
public class XxlMqConsumer extends AbstractMqConsumer<String> implements IMqConsumer {

    public static final String DEFAULT_CONSUMER_NAME = "mqBrokerConsumer";

    public XxlMqConsumer(ThreadPoolExecutor threadPoolExecutor) {
        super(threadPoolExecutor);
    }

    @Override
    public Event<String> apply(String s) {
        return JSON.parseObject(s, new TypeReference<Event<String>>() {
        });
    }

    @Override
    public MqResult consume(String s) {
        accept(s);
        return MqResult.SUCCESS;
    }
}
