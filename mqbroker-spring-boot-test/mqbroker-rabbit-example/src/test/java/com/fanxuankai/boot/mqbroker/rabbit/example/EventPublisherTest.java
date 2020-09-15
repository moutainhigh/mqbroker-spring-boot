package com.fanxuankai.boot.mqbroker.rabbit.example;

import com.fanxuankai.boot.mqbroker.example.common.UserManager;
import com.fanxuankai.boot.mqbroker.example.common.domain.User;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.produce.EventPublisher;
import com.fanxuankai.boot.mqbroker.rabbit.autoconfigure.RabbitEventConfig;
import com.fanxuankai.commons.util.concurrent.Threads;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class EventPublisherTest {

    @Resource
    private EventPublisher<User> eventPublisher;

    @Test
    public void publish() {
        List<Event<User>> list = UserManager.mockData();
        list.forEach(userEvent -> userEvent.setEventConfig(new RabbitEventConfig()
                // 延迟消息
                .setEffectTime(LocalDateTime.now().plusSeconds(30))));
        eventPublisher.publish(list);
        Threads.sleep(30, TimeUnit.MINUTES);
    }
}
