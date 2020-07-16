package com.fanxuankai.boot.mqbroker.consume;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 事件注册
 *
 * @author fanxuankai
 */
@Slf4j
public class EventListenerRegistry {

    private static final Set<String> RECEIVE_EVENTS = new HashSet<>();
    private static final Map<String, List<com.fanxuankai.boot.mqbroker.consume.EventListener>> EVENT_LISTENERS = new HashMap<>();

    public static Set<String> allReceiveEvent() {
        return RECEIVE_EVENTS;
    }

    private static void registerReceiveEvent(String event) {
        RECEIVE_EVENTS.add(event);
    }

    public static void addListener(String event, com.fanxuankai.boot.mqbroker.consume.EventListener eventListener) {
        registerReceiveEvent(event);
        EVENT_LISTENERS.computeIfAbsent(event, s -> new ArrayList<>()).add(eventListener);
    }

    public static List<EventListener> getListeners(String event) {
        return EVENT_LISTENERS.get(event);
    }

}
