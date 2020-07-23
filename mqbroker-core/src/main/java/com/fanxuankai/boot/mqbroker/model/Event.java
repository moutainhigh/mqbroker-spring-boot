package com.fanxuankai.boot.mqbroker.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 事件
 *
 * @author fanxuankai
 */
@Data
@Accessors(chain = true)
public class Event<T> implements Serializable {
    /**
     * 分组, 可选
     */
    private String group;
    /**
     * 事件名
     */
    private String name;
    /**
     * key
     */
    private String key;
    /**
     * 数据
     */
    private T data;
    /**
     * 事件配置, 可选
     */
    private EventConfig eventConfig;
}
