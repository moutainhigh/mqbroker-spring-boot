package com.fanxuankai.boot.mqbroker.consume;

import com.fanxuankai.boot.commons.util.ApplicationContexts;
import com.fanxuankai.boot.mqbroker.domain.MsgReceive;
import com.fanxuankai.boot.mqbroker.enums.Status;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.service.MsgReceiveService;
import com.fanxuankai.commons.util.ThrowableUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

/**
 * @author fanxuankai
 */
@Slf4j
public abstract class AbstractMqConsumer<T> implements MqConsumer<T>, Function<T, Event<String>> {

    @Override
    public void accept(T t) {
        Event<String> event = apply(t);
        MsgReceive msg = new MsgReceive();
        msg.setMsgGroup(event.getGroup());
        msg.setTopic(event.getName());
        msg.setCode(event.getKey());
        msg.setData(event.getData());
        msg.setStatus(Status.RUNNING.getCode());
        msg.setRetry(0);
        Date now = new Date();
        msg.setCreateDate(now);
        msg.setLastModifiedDate(now);
        try {
            MsgReceiveService msgReceiveService = getBean(null, MsgReceiveService.class);
            msgReceiveService.save(msg);
            getBean("executorService", ThreadPoolExecutor.class).execute(() ->
                    getBean(null, MsgReceiveService.class).consume(msg, false));
        } catch (Throwable throwable) {
            ThrowableUtils.checkException(throwable, DuplicateKeyException.class,
                    SQLIntegrityConstraintViolationException.class);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Event<String> apply(T t) {
        return (Event<String>) t;
    }

    private final Map<Class<?>, Object> cache = new HashMap<>();

    @SuppressWarnings("unchecked")
    private <B> B getBean(String name, Class<B> type) {
        B b = (B) cache.get(type);
        if (b == null) {
            if (name == null) {
                b = ApplicationContexts.getBean(type);
            } else {
                b = ApplicationContexts.getBean(name, type);
            }
            cache.put(type, b);
        }
        return b;
    }

}
