package com.fanxuankai.boot.mqbroker.service;

import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.fanxuankai.boot.mqbroker.config.MqBrokerProperties;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author fanxuankai
 */
@Slf4j
public class MqBrokerDingTalkClientHelper {

    @Resource
    private MqBrokerProperties mqBrokerProperties;
    private final DingTalkClient dingTalkClient;

    public MqBrokerDingTalkClientHelper(DingTalkClient dingTalkClient) {
        this.dingTalkClient = dingTalkClient;
    }

    public void push(String title, String topic, String code, int retry, String ip) {
        if (dingTalkClient == null ||
                !Objects.equals(mqBrokerProperties.getDingTalk().getEnabled(), Boolean.TRUE)) {
            return;
        }
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("markdown");
        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
        markdown.setTitle(title);
        markdown.setText("#### " + title + "\n" +
                "> 主题: " + topic + "\n\n" +
                "> 代码: " + code + "\n\n" +
                "> 重试次数: " + retry + "\n\n" +
                "> 服务器 IP: " + ip + "\n\n" +
                "> 服务器环境: " + mqBrokerProperties.getDingTalk().getEnv() + "\n\n"
        );
        request.setMarkdown(markdown);
        try {
            dingTalkClient.execute(request);
        } catch (ApiException e) {
            log.error("钉钉推送异常", e);
        }
    }

}
