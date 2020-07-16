package com.fanxuankai.boot.mqbroker.task;

import com.fanxuankai.boot.mqbroker.config.MqBrokerProperties;
import com.fanxuankai.boot.mqbroker.consume.EventDistributorFactory;
import com.fanxuankai.boot.mqbroker.service.MsgReceiveService;
import com.fanxuankai.commons.util.ThrowableUtils;
import com.fanxuankai.boot.mqbroker.domain.MsgReceive;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author fanxuankai
 */
@Slf4j
@Component
public class MsgReceiveTask implements Runnable {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private MsgReceiveService msgReceiveService;
    @Resource
    private EventDistributorFactory eventDistributorFactory;
    @Resource
    private MqBrokerProperties mqBrokerProperties;

    @Override
    public void run() {
        while (true) {
            List<MsgReceive> records = msgReceiveService.pullData();
            if (records.isEmpty()) {
                return;
            }
            int size = records.size() / mqBrokerProperties.getMaxConcurrent();
            size = size == 0 ? records.size() : size;
            List<List<MsgReceive>> partition = Lists.partition(records, size);
            CountDownLatch countDownLatch = new CountDownLatch(partition.size());
            partition.forEach(list -> threadPoolExecutor.execute(() -> {
                try {
                    for (MsgReceive msg : list) {
                        if (!msgReceiveService.lock(msg.getId())) {
                            continue;
                        }
                        try {
                            eventDistributorFactory.get(msg).accept(msg);
                        } catch (Exception e) {
                            log.error("distribute error", e);
                            msgReceiveService.unconsumed(msg, ThrowableUtils.getStackTrace(e));
                        }
                    }
                } finally {
                    countDownLatch.countDown();
                }
            }));
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error("计数器被中断", e);
            }
        }
    }
}
