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
}
