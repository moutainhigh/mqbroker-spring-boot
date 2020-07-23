package com.fanxuankai.boot.mqbroker.produce;

import com.alibaba.fastjson.JSON;
import com.fanxuankai.boot.commons.util.GenericTypeUtils;
import com.fanxuankai.boot.mqbroker.domain.MsgSend;
import com.fanxuankai.boot.mqbroker.model.EmptyEventConfig;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.model.EventConfig;
import com.fanxuankai.boot.mqbroker.service.MsgSendService;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @param <E> the type of EventConfig
 * @author fanxuankai
 */
public abstract class AbstractMqProducer<E extends EventConfig> implements MqProducer<MsgSend>,
        Consumer<Event<String>> {

    @Resource
    private MsgSendService msgSendService;

    private final Class<E> eClass;

    protected AbstractMqProducer() {
        eClass = GenericTypeUtils.getGenericType(getClass(), AbstractMqProducer.class, 0);
    }

    @Override
    public void produce(MsgSend msg) {
        Event<String> event = new Event<String>()
                .setGroup(msg.getMsgGroup())
                .setName(msg.getTopic())
                .setKey(msg.getCode())
                .setData(msg.getData());
        Optional.ofNullable(msg.getMsgConfig())
                .map(s -> JSON.parseObject(msg.getMsgConfig(), eClass))
                .filter(e -> !(e instanceof EmptyEventConfig))
                .ifPresent(event::setEventConfig);
        accept(event);
        if (!isPublisherCallback()) {
            msgSendService.success(msg);
        }
    }

}
