package com.fanxuankai.boot.mqbroker.produce;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fanxuankai.boot.mqbroker.domain.Msg;
import com.fanxuankai.boot.mqbroker.domain.MsgSend;
import com.fanxuankai.boot.mqbroker.enums.Status;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.service.MsgSendService;
import com.fanxuankai.commons.util.ThrowableUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author fanxuankai
 */
@Slf4j
public abstract class AbstractEventPublisher<T> implements EventPublisher<T> {

    @Resource
    protected MsgSendService msgSendService;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    private boolean exists(Event<T> event) {
        return msgSendService.count(Wrappers.lambdaQuery(MsgSend.class)
                .eq(StringUtils.hasText(event.getGroup()), Msg::getMsgGroup, event.getGroup())
                .isNull(!StringUtils.hasText(event.getGroup()), Msg::getMsgGroup)
                .eq(Msg::getTopic, event.getName())
                .eq(Msg::getCode, event.getKey())) > 0;
    }

    protected void persistence(Event<T> event, boolean async) {
        if (exists(event)) {
            log.info("防重生产: {}", event.getKey());
            return;
        }
        MsgSend msgSend = createMessageSend(event);
        if (async) {
            threadPoolExecutor.execute(() -> save(msgSend));
        } else {
            save(msgSend);
        }
    }

    protected void persistence(List<Event<T>> events, boolean async) {
        if (events.size() == 1) {
            persistence(events.get(0), async);
            return;
        }
        events = events.stream()
                .filter(event -> {
                    if (exists(event)) {
                        log.info("防重生产: {}", event.getKey());
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(events)) {
            return;
        }
        List<MsgSend> msgSends = events.stream()
                .map(this::createMessageSend)
                .collect(Collectors.toList());
        if (async) {
            threadPoolExecutor.execute(() -> save(msgSends));
        } else {
            save(msgSends);
        }
    }

    private MsgSend createMessageSend(Event<?> event) {
        MsgSend msg = new MsgSend();
        msg.setMsgGroup(event.getGroup());
        msg.setTopic(event.getName());
        msg.setCode(event.getKey());
        Object data = event.getData();
        if (data instanceof CharSequence) {
            msg.setData(data.toString());
        } else {
            msg.setData(JSON.toJSONString(data));
        }
        msg.setRetry(0);
        msg.setStatus(Status.RUNNING.getCode());
        Optional.ofNullable(event.getEventConfig())
                .map(JSON::toJSONString)
                .ifPresent(msg::setMsgConfig);
        Date now = new Date();
        msg.setCreateDate(now);
        msg.setLastModifiedDate(now);
        return msg;
    }

    private void save(MsgSend msgSend) {
        try {
            if (msgSendService.save(msgSend)) {
                produce(msgSend);
            }
        } catch (Throwable throwable) {
            ThrowableUtils.checkException(throwable, DuplicateKeyException.class,
                    SQLIntegrityConstraintViolationException.class);
        }
    }

    private void save(List<MsgSend> msgSends) {
        try {
            if (msgSendService.saveBatch(msgSends)) {
                msgSends.forEach(this::produce);
            }
        } catch (Throwable throwable) {
            ThrowableUtils.checkException(throwable, DuplicateKeyException.class,
                    SQLIntegrityConstraintViolationException.class);
        }
    }

    private void produce(MsgSend msg) {
        msgSendService.produce(msg, true);
    }
}
