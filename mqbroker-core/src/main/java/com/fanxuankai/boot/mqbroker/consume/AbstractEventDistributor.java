package com.fanxuankai.boot.mqbroker.consume;

import com.alibaba.fastjson.JSON;
import com.fanxuankai.boot.mqbroker.domain.MsgReceive;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.model.ListenerMetadata;
import com.fanxuankai.boot.mqbroker.service.MsgReceiveService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author fanxuankai
 */
public abstract class AbstractEventDistributor implements EventDistributor, Consumer<MsgReceive> {

    @Resource
    private MsgReceiveService msgReceiveService;

    @Override
    public void distribute(Event<?> event) {
        List<EventListener<?>> eventListeners = EventListenerRegistry.getListeners(new ListenerMetadata()
                .setGroup(event.getGroup())
                .setTopic(event.getName()));
        if (CollectionUtils.isEmpty(eventListeners)) {
            return;
        }
        onEvent(event, eventListeners);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void accept(MsgReceive msg) {
        Event<Object> event = new Event<>()
                .setGroup(msg.getMsgGroup())
                .setName(msg.getTopic())
                .setKey(msg.getCode())
                .setData(JSON.parseObject(msg.getData(),
                        EventListenerRegistry.getDataType(new ListenerMetadata()
                                .setGroup(msg.getMsgGroup())
                                .setTopic(msg.getTopic())
                        )))
                .setRetryCount(msg.getRetryCount());
        Optional.ofNullable(msg.getRetryCount())
                .ifPresent(event::setRetryCount);
        distribute(event);
        msgReceiveService.success(msg);
    }

    /**
     * 消费实现(策略方法)
     *
     * @param event          事件
     * @param eventListeners 事件监听器
     */
    protected abstract void onEvent(Event<?> event, List<EventListener<?>> eventListeners);

    /**
     * 适用的事件监听策略
     *
     * @return 事件监听策略
     */
    public abstract EventStrategy getEventListenerStrategy();

}
