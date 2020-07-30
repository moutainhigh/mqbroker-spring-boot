package com.fanxuankai.boot.mqbroker.task;

import com.fanxuankai.boot.mqbroker.domain.MsgSend;
import com.fanxuankai.boot.mqbroker.service.MsgSendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author fanxuankai
 */
@Slf4j
@Component
public class MsgSendTask implements Runnable {

    @Resource
    private MsgSendService msgSendService;

    @Override
    public void run() {
        while (true) {
            List<MsgSend> records = msgSendService.pullData();
            if (records.isEmpty()) {
                return;
            }
            for (MsgSend msg : records) {
                if (!msgSendService.lock(msg.getId())) {
                    continue;
                }
                msgSendService.produce(msg, true);
            }
        }
    }
}
