package com.fanxuankai.boot.mqbroker.kafka.example;

import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.produce.EventPublisher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest
@RunWith(SpringRunner.class)
public class EventPublisherTest {

    @Resource
    private EventPublisher eventPublisher;

    @Test
    public void publish() {
        eventPublisher.publish(IntStream.range(0, 10)
                .mapToObj(value -> new Event()
                        .setName("user")
                        .setKey(UUID.randomUUID().toString())
                        .setData("fanxuankai"))
                .collect(Collectors.toList()));
    }
}
