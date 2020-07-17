package com.fanxuankai.boot.mqbroker.consume;

import com.fanxuankai.boot.commons.util.GenericTypeUtils;
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
    private static final Map<String, List<EventListener<?>>> EVENT_LISTENERS = new HashMap<>();
    private static final Map<String, Class<?>> DATA_TYPE = new HashMap<>();

    public static Set<String> allReceiveEvent() {
        return RECEIVE_EVENTS;
    }

    private static void registerReceiveEvent(String event) {
        RECEIVE_EVENTS.add(event);
    }

    public static void addListener(String event, EventListener<?> eventListener) {
        registerReceiveEvent(event);
        EVENT_LISTENERS.computeIfAbsent(event, s -> new ArrayList<>()).add(eventListener);
        DATA_TYPE.put(event, GenericTypeUtils.getGenericType(eventListener.getClass(), EventListener.class, 0));
    }

    public static List<EventListener<?>> getListeners(String event) {
        return EVENT_LISTENERS.get(event);
    }

    public static Class<?> getDataType(String event) {
        return DATA_TYPE.get(event);
    }
}
