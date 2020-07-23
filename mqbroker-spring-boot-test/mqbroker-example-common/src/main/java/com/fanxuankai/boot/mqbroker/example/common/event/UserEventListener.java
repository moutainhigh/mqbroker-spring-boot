package com.fanxuankai.boot.mqbroker.example.common.event;

import com.alibaba.fastjson.JSON;
import com.fanxuankai.boot.mqbroker.consume.EventListener;
import com.fanxuankai.boot.mqbroker.consume.Listener;
import com.fanxuankai.boot.mqbroker.example.common.domain.User;
import com.fanxuankai.boot.mqbroker.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author fanxuankai
 */
@Slf4j
@Service
@Listener(event = "user", group = "mqbroker-user")
public class UserEventListener implements EventListener<User> {

    @Override
    public void onEvent(Event<User> event) {
        log.info(JSON.toJSONString(event));
    }
}
