package com.fanxuankai.boot.mqbroker.rabbit.example;

import com.fanxuankai.boot.mqbroker.example.common.UserManager;
import com.fanxuankai.boot.mqbroker.example.common.domain.User;
import com.fanxuankai.boot.mqbroker.produce.EventPublisher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
@RunWith(SpringRunner.class)
public class EventPublisherTest {

    @Resource
    private EventPublisher<User> eventPublisher;

    @Test
    public void publish() {
        eventPublisher.publish(UserManager.mockData());
    }
}
