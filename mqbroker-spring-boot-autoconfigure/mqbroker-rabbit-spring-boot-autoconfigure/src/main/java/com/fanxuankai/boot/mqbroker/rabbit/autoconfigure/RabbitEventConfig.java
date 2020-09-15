package com.fanxuankai.boot.mqbroker.rabbit.autoconfigure;

import com.fanxuankai.boot.mqbroker.model.EventConfig;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author fanxuankai
 */
@Data
@Accessors(chain = true)
public class RabbitEventConfig implements EventConfig {
    /**
     * 生效时间
     */
    private LocalDateTime effectTime;
}
