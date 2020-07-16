package com.fanxuankai.boot.mqbroker.xxl.example.event;

import com.fanxuankai.boot.mqbroker.consume.EventListener;
import com.fanxuankai.boot.mqbroker.consume.Listener;
import com.fanxuankai.boot.mqbroker.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author fanxuankai
 */
@Slf4j
@Service
@Listener(event = "user")
public class UserEventListener implements EventListener {

    @Override
    public void onEvent(Event event) {
        log.info(event.getName());
    }
}
