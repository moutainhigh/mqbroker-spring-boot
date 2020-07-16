package com.fanxuankai.boot.mqbroker.consume;

import com.fanxuankai.boot.mqbroker.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author fanxuankai
 */
@Component
@Slf4j
public class AtMostManyEventDistributor extends AbstractEventDistributor {

    @Override
    protected void onEvent(Event event, List<EventListener> eventListeners) {
        eventListeners.forEach(eventListener -> {
            try {
                eventListener.onEvent(event);
            } catch (Exception e) {
                log.error("事件处理异常", e);
            }
        });
    }

    @Override
    public EventStrategy getEventListenerStrategy() {
        return EventStrategy.AT_MOST_MANY;
    }
}
