package com.fanxuankai.boot.mqbroker.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * 消息
 *
 * @author fanxuankai
 */
@Data
public class Msg {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 分组
     */
    private String msgGroup;
    /**
     * 主题
     */
    private String topic;
    /**
     * code
     */
    private String code;
    /**
     * 内容
     */
    private String data;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 主机地址
     */
    private String hostAddress;
    /**
     * 重试
     */
    private Integer retry;
    /**
     * 失败原因
     */
    private String cause;
    /**
     * 创建日期
     */
    private Date createDate;
    /**
     * 修改日期
     */
    private Date lastModifiedDate;
}
