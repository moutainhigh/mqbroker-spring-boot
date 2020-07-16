package com.fanxuankai.boot.mqbroker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fanxuankai.boot.mqbroker.domain.MsgReceive;

import java.util.List;

/**
 * @author fanxuankai
 */
public interface MsgReceiveService extends IService<MsgReceive> {

    /**
     * 获取消息
     *
     * @return list
     */
    List<MsgReceive> pullData();

    /**
     * 锁定消息
     *
     * @param id 消息id
     * @return 是否成功
     */
    boolean lock(Long id);

    /**
     * 消费超时
     */
    void consumeTimeout();

    /**
     * 成功
     *
     * @param msg 消息
     */
    void consumed(MsgReceive msg);

    /**
     * nack
     *
     * @param msg   消息
     * @param cause 原因
     */
    void unconsumed(MsgReceive msg, String cause);

}
