package com.fanxuankai.boot.mqbroker.produce;

import com.fanxuankai.boot.mqbroker.domain.MsgSend;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.service.MsgSendService;

import javax.annotation.Resource;
import java.util.function.Consumer;

/**
 * @author fanxuankai
 */
public abstract class AbstractMqProducer implements MqProducer<MsgSend>, Consumer<Event<String>> {

    @Resource
    private MsgSendService msgSendService;

    @Override
    public void produce(MsgSend msg) {
        accept(new Event<String>().setName(msg.getTopic()).setKey(msg.getCode()).setData(msg.getData()));
        if (!isPublisherCallback()) {
            msgSendService.success(msg);
        }
    }

}
