package com.fanxuankai.boot.mqbroker.xxl.autoconfigure;

import com.alibaba.fastjson.JSON;
import com.fanxuankai.boot.mqbroker.consume.AbstractMqConsumer;
import com.fanxuankai.boot.mqbroker.mapper.MsgReceiveMapper;
import com.fanxuankai.boot.mqbroker.model.Event;

/**
 * @author fanxuankai
 */
public class XxlMqConsumer extends AbstractMqConsumer<String> {

    public XxlMqConsumer(MsgReceiveMapper msgReceiveMapper) {
        super(msgReceiveMapper);
    }

    @Override
    public Event apply(String s) {
        return JSON.parseObject(s, Event.class);
    }

}
