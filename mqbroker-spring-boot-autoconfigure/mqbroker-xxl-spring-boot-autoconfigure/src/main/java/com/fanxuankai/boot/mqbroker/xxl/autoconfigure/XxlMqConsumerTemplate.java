package com.fanxuankai.boot.mqbroker.xxl.autoconfigure;

import com.fanxuankai.boot.mqbroker.mapper.MsgReceiveMapper;
import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.MqResult;

/**
 * @author fanxuankai
 */
public class XxlMqConsumerTemplate extends XxlMqConsumer implements IMqConsumer {

    public XxlMqConsumerTemplate(MsgReceiveMapper msgReceiveMapper) {
        super(msgReceiveMapper);
    }

    @Override
    public MqResult consume(String s) {
        accept(s);
        return MqResult.SUCCESS;
    }
}
