package com.hello.slackApp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class SlackAlertService {

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    public void sendSlackNotification(String[] alertQuery) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> payload = new HashMap<>();
        payload.put("text", "Metric : " + alertQuery[0] + ", Value : " + alertQuery[1] + " /// 현재 값 " + alertQuery[3] + "이 설정 값 " + alertQuery[1] + alertQuery[2] + " 입니다");
        restTemplate.postForEntity(webhookUrl, payload, String.class);
    }

    public void sendSlackNotificationLoki(String lokiResponse, String originErrorLog) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> payload = new HashMap<>();
        payload.put("text", " :warning:*[ 발생한 Error Log ]* \n \n " + originErrorLog + "\n\n :robot_face:*[gpt answer]* : \n \n" + lokiResponse);
        restTemplate.postForEntity(webhookUrl, payload, String.class);
    }
}
