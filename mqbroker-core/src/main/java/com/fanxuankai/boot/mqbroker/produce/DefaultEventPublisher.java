package com.fanxuankai.boot.mqbroker.produce;

import com.fanxuankai.boot.mqbroker.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author fanxuankai
 */
@Component
@Slf4j
public class DefaultEventPublisher extends AbstractEventPublisher {

    @Override
    public void publish(Event event) {
        publish(event, false);
    }

    @Override
    public void publish(List<Event> events) {
        publish(events, false);
    }

    @Override
    public void publish(Event event, boolean async) {
        persistence(event, async);
    }

    @Override
    public void publish(List<Event> events, boolean async) {
        persistence(events, async);
    }

}
