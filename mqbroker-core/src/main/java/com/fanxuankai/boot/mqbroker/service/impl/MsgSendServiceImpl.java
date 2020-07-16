package com.fanxuankai.boot.mqbroker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fanxuankai.boot.mqbroker.config.MqBrokerProperties;
import com.fanxuankai.boot.mqbroker.enums.Status;
import com.fanxuankai.boot.mqbroker.service.MsgSendService;
import com.fanxuankai.commons.util.AddressUtils;
import com.fanxuankai.boot.mqbroker.domain.Msg;
import com.fanxuankai.boot.mqbroker.domain.MsgSend;
import com.fanxuankai.boot.mqbroker.mapper.MsgSendMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author fanxuankai
 */
@Component
public class MsgSendServiceImpl extends ServiceImpl<MsgSendMapper, MsgSend>
        implements MsgSendService {

    @Resource
    private MqBrokerProperties mqBrokerProperties;

    @Override
    public List<MsgSend> pullData() {
        return page(new Page<>(1, mqBrokerProperties.getMsgSize()),
                new QueryWrapper<MsgSend>().lambda()
                        .eq(Msg::getStatus, Status.CREATED.getCode())
                        .orderByAsc(Msg::getId)
                        .lt(Msg::getRetry, mqBrokerProperties.getMaxRetry())).getRecords();
    }

    @Override
    public boolean lock(Long id) {
        MsgSend entity = new MsgSend();
        entity.setStatus(Status.RUNNING.getCode());
        entity.setLastModifiedDate(new Date());
        entity.setHostAddress(AddressUtils.getHostAddress());
        return update(entity, new UpdateWrapper<MsgSend>()
                .lambda()
                .eq(Msg::getStatus, Status.CREATED.getCode())
                .eq(Msg::getId, id));
    }

    @Override
    public void publisherCallbackTimeout() {
        Date timeout = Date.from(LocalDateTime.now().plus(-mqBrokerProperties.getPublisherCallbackTimeout(),
                ChronoUnit.MILLIS).atZone(ZoneId.systemDefault()).toInstant());
        MsgSend entity = new MsgSend();
        entity.setCause("回调超时");
        entity.setLastModifiedDate(new Date());
        Supplier<LambdaUpdateWrapper<MsgSend>> lambdaSupplier = () -> new UpdateWrapper<MsgSend>()
                .lambda()
                .setSql("retry = retry + 1")
                .eq(Msg::getStatus, Status.RUNNING.getCode())
                .lt(Msg::getLastModifiedDate, timeout);
        int lastChance = mqBrokerProperties.getMaxRetry() - 1;
        entity.setStatus(Status.CREATED.getCode());
        update(entity, lambdaSupplier.get().lt(Msg::getRetry, lastChance));
        entity.setStatus(Status.FAILURE.getCode());
        update(entity, lambdaSupplier.get().ge(Msg::getRetry, lastChance));
    }

    @Override
    public void success(MsgSend msg) {
        MsgSend entity = new MsgSend();
        entity.setLastModifiedDate(new Date());
        entity.setStatus(Status.SUCCESS.getCode());
        update(entity, new UpdateWrapper<MsgSend>()
                .lambda()
                .eq(Msg::getId, msg.getId())
                .eq(Msg::getStatus, Status.RUNNING.getCode()));
    }

    @Override
    public void success(String topic, String code) {
        MsgSend entity = new MsgSend();
        entity.setLastModifiedDate(new Date());
        entity.setStatus(Status.SUCCESS.getCode());
        update(entity, new UpdateWrapper<MsgSend>()
                .lambda()
                .eq(Msg::getTopic, topic)
                .eq(Msg::getCode, code)
                .eq(Msg::getStatus, Status.RUNNING.getCode()));
    }

    @Override
    public void failure(String topic, String code, String cause) {
        MsgSend msg = getOne(new QueryWrapper<MsgSend>()
                .lambda()
                .eq(Msg::getTopic, topic)
                .eq(Msg::getCode, code));
        if (msg == null) {
            return;
        }
        failure(msg, cause);
    }

    @Override
    public void failure(MsgSend msg, String cause) {
        String increaseRetry = "retry = retry + 1";
        MsgSend entity = new MsgSend();
        entity.setCause(cause);
        entity.setLastModifiedDate(new Date());
        LambdaUpdateWrapper<MsgSend> lambda = new UpdateWrapper<MsgSend>().lambda()
                .setSql(increaseRetry)
                .eq(Msg::getRetry, msg.getRetry())
                .eq(Msg::getStatus, Status.RUNNING.getCode());
        int lastChance = mqBrokerProperties.getMaxRetry() - 1;
        if (msg.getRetry() < lastChance) {
            entity.setStatus(Status.CREATED.getCode());
            lambda.lt(Msg::getRetry, lastChance);
        } else {
            entity.setStatus(Status.FAILURE.getCode());
            lambda.ge(Msg::getRetry, lastChance);
        }
        update(entity, lambda);
    }

}
