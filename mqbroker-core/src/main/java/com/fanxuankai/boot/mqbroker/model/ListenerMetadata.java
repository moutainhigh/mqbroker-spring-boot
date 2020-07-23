package com.fanxuankai.boot.mqbroker.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * @author fanxuankai
 */
@Data
@Accessors(chain = true)
public class ListenerMetadata {
    private String group;
    private String topic;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ListenerMetadata that = (ListenerMetadata) o;
        return group.equals(that.group) &&
                topic.equals(that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, topic);
    }
}