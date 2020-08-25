package com.fanxuankai.boot.mqbroker.consume;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fanxuankai.boot.commons.util.ApplicationContexts;
import com.fanxuankai.boot.mqbroker.domain.Msg;
import com.fanxuankai.boot.mqbroker.domain.MsgReceive;
import com.fanxuankai.boot.mqbroker.enums.Status;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.service.MsgReceiveService;
import com.fanxuankai.commons.util.ThrowableUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;

import javax.annotation.Resource;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;
import java.util.function.Function;

/**
 * @author fanxuankai
 */
@Slf4j
public abstract class AbstractMqConsumer<T> implements MqConsumer<T>, Function<T, Event<String>> {
    @Resource
    private MsgReceiveService msgReceiveService;

    private MsgReceiveService getMsgReceiveService() {
        if (msgReceiveService == null) {
            msgReceiveService = ApplicationContexts.getBean(MsgReceiveService.class);
        }
        return msgReceiveService;
    }

    @Override
    public void accept(T t) {
        Event<String> event = apply(t);
        MsgReceiveService msgReceiveService = getMsgReceiveService();
        if (msgReceiveService.count(Wrappers.lambdaQuery(MsgReceive.class)
                .eq(Msg::getCode, event.getKey())) > 0) {
            log.info("防重消费: {}", event.getKey());
            return;
        }
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
            msgReceiveService.save(msg);
            msgReceiveService.consume(msg, false, true);
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
}
