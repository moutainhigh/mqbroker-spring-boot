package com.fanxuankai.boot.mqbroker.lite.xxl.autoconfigure;

import com.fanxuankai.boot.mqbroker.model.EventConfig;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author fanxuankai
 */
@Data
@Accessors(chain = true)
public class XxlEventConfig implements EventConfig {
    /**
     * 重试次数
     */
    private Integer retryCount;
    /**
     * 生效时间
     */
    private LocalDateTime effectTime;
}
