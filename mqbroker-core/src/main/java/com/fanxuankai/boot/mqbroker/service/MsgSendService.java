package com.fanxuankai.boot.mqbroker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fanxuankai.boot.mqbroker.domain.MsgSend;

import java.util.List;

/**
 * @author fanxuankai
 */
public interface MsgSendService extends IService<MsgSend> {

    /**
     * 获取消息
     *
     * @return list
     */
    List<MsgSend> pullData();

    /**
     * 锁定消息
     *
     * @param id 消息id
     * @return 是否成功
     */
    boolean lock(Long id);

    /**
     * 回调超时
     */
    void publisherCallbackTimeout();

    /**
     * success
     *
     * @param msg 消息
     */
    void success(MsgSend msg);

    /**
     * success
     *
     * @param topic 主题
     * @param code  code
     */
    void success(String topic, String code);

    /**
     * failure
     *
     * @param topic 主题
     * @param code  code
     * @param cause 原因
     */
    void failure(String topic, String code, String cause);

    /**
     * failure
     *
     * @param msg   消息
     * @param cause 原因
     */
    void failure(MsgSend msg, String cause);

}
