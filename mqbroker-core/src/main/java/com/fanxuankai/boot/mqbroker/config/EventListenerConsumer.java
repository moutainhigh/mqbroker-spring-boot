package com.fanxuankai.boot.mqbroker.config;

import com.fanxuankai.boot.mqbroker.model.ListenerMetadata;

import java.util.Set;
import java.util.function.Consumer;

/**
 * @author fanxuankai
 */
public interface EventListenerConsumer extends Consumer<Set<ListenerMetadata>> {

}
