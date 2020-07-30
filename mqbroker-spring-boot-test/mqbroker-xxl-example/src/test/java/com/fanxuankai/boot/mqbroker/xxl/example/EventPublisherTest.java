package com.fanxuankai.boot.mqbroker.xxl.example;

import com.fanxuankai.boot.mqbroker.example.common.UserManager;
import com.fanxuankai.boot.mqbroker.example.common.domain.User;
import com.fanxuankai.boot.mqbroker.model.Event;
import com.fanxuankai.boot.mqbroker.produce.EventPublisher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class EventPublisherTest {

    @Resource
    private EventPublisher<User> eventPublisher;

    @Test
    public void publish() {
        List<Event<User>> list = UserManager.mockData();
//        list.forEach(userEvent ->
//                userEvent.setGroup("mqbroker-user")
//                        .setEventConfig(new XxlEventConfig()
//                                .setRetryCount(2)
//                                // 5分钟后生效
//                                .setEffectTime(LocalDateTime.now().plusSeconds(5))));
        eventPublisher.publish(list);
    }
}
