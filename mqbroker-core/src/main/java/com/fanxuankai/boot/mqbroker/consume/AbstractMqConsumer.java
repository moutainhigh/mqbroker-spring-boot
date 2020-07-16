package com.fanxuankai.boot.mqbroker.consume;

import com.fanxuankai.boot.mqbroker.domain.MsgReceive;
import com.fanxuankai.boot.mqbroker.enums.Status;
import com.fanxuankai.boot.mqbroker.mapper.MsgReceiveMapper;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.commons.util.ThrowableUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;
import java.util.function.Function;

/**
 * @author fanxuankai
 */
@Slf4j
public abstract class AbstractMqConsumer<T> implements MqConsumer<T>, Function<T, Event> {

    private final MsgReceiveMapper msgReceiveMapper;

    public AbstractMqConsumer(MsgReceiveMapper msgReceiveMapper) {
        this.msgReceiveMapper = msgReceiveMapper;
    }

    @Override
    public void accept(T t) {
        Event event = apply(t);
        MsgReceive message = new MsgReceive();
        message.setCode(event.getKey());
        message.setTopic(event.getName());
        message.setData(event.getData());
        message.setStatus(Status.CREATED.getCode());
        message.setRetry(0);
        Date now = new Date();
        message.setCreateDate(now);
        message.setLastModifiedDate(now);
        try {
            msgReceiveMapper.insert(message);
        } catch (Throwable throwable) {
            ThrowableUtils.checkException(throwable, DuplicateKeyException.class,
                    SQLIntegrityConstraintViolationException.class);
        }
    }

    @Override
    public Event apply(T t) {
        return (Event) t;
    }

}
