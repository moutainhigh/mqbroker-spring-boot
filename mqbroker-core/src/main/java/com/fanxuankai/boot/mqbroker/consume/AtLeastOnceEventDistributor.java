package com.fanxuankai.boot.mqbroker.consume;

import com.fanxuankai.boot.mqbroker.exception.EventHandleException;
import com.fanxuankai.boot.mqbroker.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author fanxuankai
 */
@Component
@Slf4j
public class AtLeastOnceEventDistributor extends AbstractEventDistributor {

    @Override
    protected void onEvent(Event event, List<EventListener> eventListeners) {
        boolean success = false;
        Throwable throwable = null;
        for (EventListener eventListener : eventListeners) {
            try {
                eventListener.onEvent(event);
                success = true;
            } catch (Exception e) {
                log.error("事件处理异常", e);
                throwable = e;
            }
        }
        if (success) {
            return;
        }
        throw new EventHandleException(throwable);
    }

    @Override
    public EventStrategy getEventListenerStrategy() {
        return EventStrategy.AT_LEAST_ONCE;
    }
}
