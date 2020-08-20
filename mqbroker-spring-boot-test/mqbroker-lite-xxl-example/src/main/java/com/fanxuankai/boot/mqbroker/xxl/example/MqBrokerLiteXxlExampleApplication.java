package com.fanxuankai.boot.mqbroker.xxl.example;

import com.xxl.mq.client.EnableXxlMqClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author fanxuankai
 */
@SpringBootApplication
@EnableXxlMqClient
public class MqBrokerLiteXxlExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(MqBrokerLiteXxlExampleApplication.class, args);
    }
}
