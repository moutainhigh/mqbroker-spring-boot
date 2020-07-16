## 简介
增强消息队列可靠性，消息一定发送成功且只发送一次，消息一定接收成功且只消费一次。

![](http://processon.com/chart_image/5ec55550f346fb6907090118.png)

## 发送端的可靠性
本地创建发送消息表并具有唯一编号，利用本地事务机制持久化，待事务提交成功，将未发送的消息发送至消息队列，失败则重试到一定次数，成功则修改状态为已发送或者删除。
## 接收端的可靠性
本地创建接收消息表并具有唯一编号，接收到消息队列的消息后利用本地事务机制持久化，待事务提交成功，将未消费的消息分配给对应的消费者，失败则重试到一定次数，成功则修改状态为已消费或者删除。

## 支持的消息队列
- RabbitMQ
- XXL-MQ
- RocketMQ
- Kafka

## Getting started
- 建表
```
CREATE TABLE `mq_broker_msg_send` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `topic` varchar(255) NOT NULL COMMENT '队列名',
  `code` varchar(255) NOT NULL COMMENT '代码',
  `data` text NOT NULL COMMENT '内容',
  `status` int(11) NOT NULL COMMENT '状态 0:已创建 1:运行中 2:成功 3:失败',
  `host_address` varchar(255) DEFAULT NULL COMMENT '主机地址',
  `retry` int(11) NOT NULL DEFAULT '0' COMMENT '重试次数',
  `cause` text DEFAULT NULL COMMENT '失败原因',
  `create_date` datetime NOT NULL COMMENT '创建日期',
  `last_modified_date` datetime NOT NULL COMMENT '修改日期',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_topic_code` (`topic`,`code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='发送消息表';

CREATE TABLE `mq_broker_msg_receive` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `topic` varchar(255) NOT NULL COMMENT '队列名',
  `code` varchar(255) NOT NULL COMMENT '代码',
  `data` text NOT NULL COMMENT '内容',
  `status` int(11) NOT NULL COMMENT '状态 0:已创建 1:运行中 2:成功 3:失败',
  `host_address` varchar(255) DEFAULT NULL COMMENT '主机地址',
  `retry` int(11) NOT NULL DEFAULT '0' COMMENT '重试次数',
  `cause` text DEFAULT NULL COMMENT '失败原因',
  `create_date` datetime NOT NULL COMMENT '创建日期',
  `last_modified_date` datetime NOT NULL COMMENT '修改日期',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_topic_code` (`topic`,`code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='接收消息表';
```
- 添加 maven 依赖
```xml
<dependencies>
    <dependency>
        <groupId>com.fanxuankai.boot</groupId>
        <artifactId>mqbroker-rabbit-spring-boot-starter</artifactId>
        <version>${mqbroker.spring.boot.version}</version>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
</dependencies>
```
- 配置
```
## application.yml
spring:
  rabbitmq:
    publisher-confirm-type: correlated
    publisher-returns: true
    username: guest
    password: guest
    host: localhost
    port: 5672
  datasource:
    url: jdbc:mysql://localhost:3306/canal_client_example?autoReconnect=true&useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: HzB!OPxxE$5CwJIZ
    driver-class-name: com.mysql.cj.jdbc.Driver
mq-broker:
  # 最大并发数
  #max-concurrent:
  # 拉取消息的数量
  #msg-size: 1000
  # 最大重试次数
  #max-retry: 3
  # 拉取数据的间隔 ms
  #interval-millis: 1000
  # 发布回调超时
  #publisher-callback-timeout: 20000
  # 消费超时
  #consume-timeout: 20000
  # 手动确认
  #manual-acknowledge: false
  # 事件策略
  #event-strategy:
    # key: 消息队列 value: EventStrategy
    #user: DEFAULT
```
- 监听事件
```
@Service
@Listener(event = "user")
public class UserEventListener implements EventListener {

    @Override
    public void onEvent(Event event) {
        // do something
    }

}
```
- Usage
```
@Resource
private EventPublisher eventPublisher;

eventPublisher.publish(IntStream.range(0, 100)
                .mapToObj(value -> new Event()
                        .setName("user")
                        .setKey(UUID.randomUUID().toString())
                        .setData("fanxuankai"))
                .collect(Collectors.toList()));
```