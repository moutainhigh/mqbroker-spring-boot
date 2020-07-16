package com.fanxuankai.boot.mqbroker.consume;

import com.fanxuankai.boot.mqbroker.domain.MsgReceive;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.service.MsgReceiveService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author fanxuankai
 */
public abstract class AbstractEventDistributor implements EventDistributor, Consumer<MsgReceive> {

    @Resource
    private MsgReceiveService msgReceiveService;

    @Override
    public void distribute(Event event) {
        List<EventListener> eventListeners = EventListenerRegistry.getListeners(event.getName());
        if (CollectionUtils.isEmpty(eventListeners)) {
            return;
        }
        onEvent(event, eventListeners);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void accept(MsgReceive message) {
        distribute(new Event().setName(message.getTopic()).setKey(message.getCode()).setData(message.getData()));
        msgReceiveService.consumed(message);
    }

    /**
     * 消费实现(策略方法)
     *
     * @param event          事件
     * @param eventListeners 事件监听器
     */
    protected abstract void onEvent(Event event, List<EventListener> eventListeners);

    /**
     * 适用的事件监听策略
     *
     * @return 事件监听策略
     */
    public abstract EventStrategy getEventListenerStrategy();

}
