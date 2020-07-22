package com.fanxuankai.boot.mqbroker.produce;

import com.alibaba.fastjson.JSON;
import com.fanxuankai.boot.mqbroker.domain.MsgSend;
import com.fanxuankai.boot.mqbroker.enums.Status;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.service.MsgSendService;
import com.fanxuankai.commons.util.ThrowableUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;

import javax.annotation.Resource;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;
import java.util.List;
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

    protected void persistence(Event<T> event, boolean async) {
        MsgSend msgSend = createMessageSend(event);
        if (async) {
            threadPoolExecutor.execute(() -> save(msgSend));
        } else {
            save(msgSend);
        }
    }

    private void save(MsgSend msgSend) {
        try {
            msgSendService.save(msgSend);
        } catch (Throwable throwable) {
            ThrowableUtils.checkException(throwable, DuplicateKeyException.class,
                    SQLIntegrityConstraintViolationException.class);
        }
    }

    private void save(List<MsgSend> msgSends) {
        try {
            msgSendService.saveBatch(msgSends);
        } catch (Throwable throwable) {
            ThrowableUtils.checkException(throwable, DuplicateKeyException.class,
                    SQLIntegrityConstraintViolationException.class);
        }
    }

    protected void persistence(List<Event<T>> events, boolean async) {
        if (events.size() == 1) {
            persistence(events.get(0), async);
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

    protected MsgSend createMessageSend(Event<?> event) {
        MsgSend message = new MsgSend();
        message.setTopic(event.getName());
        message.setCode(event.getKey());
        Object data = event.getData();
        if (data instanceof CharSequence) {
            message.setData(data.toString());
        } else {
            message.setData(JSON.toJSONString(data));
        }
        message.setStatus(Status.CREATED.getCode());
        message.setRetry(0);
        Date now = new Date();
        message.setCreateDate(now);
        message.setLastModifiedDate(now);
        return message;
    }

}
