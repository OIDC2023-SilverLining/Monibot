package com.hello.slackApp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.hello.slackApp.model.Alert;
import com.hello.slackApp.model.Loki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SlackAlertService {

    @Value("${SLACK_WEBHOOK_URL}")
    private String webhookUrl;
                
    public void sendSlackNotification(String[] alertQuery) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> payload = new HashMap<>();
        List<Map<String, Object>> blocks = new ArrayList<>();

        // Header Section with Emoji
        Map<String, Object> headerSection = new HashMap<>();
        headerSection.put("type", "header");
        Map<String, Object> headerText = new HashMap<>();
        headerText.put("type", "plain_text");
        headerText.put("text", ":bell: Alert Notification");
        headerSection.put("text", headerText);
        blocks.add(headerSection);

        // Divider
        Map<String, Object> divider = new HashMap<>();
        divider.put("type", "divider");
        blocks.add(divider);

        // Alert Title Section with Emoji
        Map<String, Object> section1 = new HashMap<>();
        section1.put("type", "section");
        Map<String, Object> text1 = new HashMap<>();
        text1.put("type", "mrkdwn");
        text1.put("text", ":dart: *Metric Name:* " + alertQuery[1] + "\n:mag: *Metric Value:* " + alertQuery[3]);
        section1.put("text", text1);
        blocks.add(section1);

        // Alert Content Section
        Map<String, Object> section2 = new HashMap<>();
        section2.put("type", "section");
        Map<String, Object> text2 = new HashMap<>();
        text2.put("type", "mrkdwn");
        text2.put("text", ":exclamation: 현재 값 " + alertQuery[3] + "이 설정 값 " + alertQuery[4] + alertQuery[2] + " 입니다");
        section2.put("text", text2);
        blocks.add(section2);

        payload.put("blocks", blocks);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity(webhookUrl, request, String.class);
    }
    public void sendSlackNotificationLoki(String lokiResponse, String originErrorLog) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> payload = new HashMap<>();
        List<Map<String, Object>> blocks = new ArrayList<>();

        // Title Section
        Map<String, Object> titleSection = new HashMap<>();
        titleSection.put("type", "header");
        Map<String, Object> titleText = new HashMap<>();
        titleText.put("type", "plain_text");
        titleText.put("text", ":no_entry: Error Log Notification");
        titleText.put("emoji", true);
        titleSection.put("text", titleText);
        blocks.add(titleSection);

        // Divider
        Map<String, Object> divider = new HashMap<>();
        divider.put("type", "divider");
        blocks.add(divider);

        // Error Log Section with warning emoji
        Map<String, Object> errorLogSection = new HashMap<>();
        errorLogSection.put("type", "section");
        Map<String, Object> errorLogText = new HashMap<>();
        errorLogText.put("type", "mrkdwn");
        errorLogText.put("text", ":warning: *발생한 Error Log* \n" + originErrorLog);
        errorLogSection.put("text", errorLogText);
        blocks.add(errorLogSection);

        // GPT Answer Section with robot emoji
        Map<String, Object> gptAnswerSection = new HashMap<>();
        gptAnswerSection.put("type", "section");
        Map<String, Object> gptAnswerText = new HashMap<>();
        gptAnswerText.put("type", "mrkdwn");
        gptAnswerText.put("text", ":robot_face: *GPT Answer* : \n" + lokiResponse);
        gptAnswerSection.put("text", gptAnswerText);
        blocks.add(gptAnswerSection);

        payload.put("blocks", blocks);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity(webhookUrl, request, String.class);
    }

    public void sendSlackNotificationMonitor(String userId, String query, String gptResponse, String metricResult, String dashboardUrl){
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> payload = new HashMap<>();
        List<Map<String, Object>> blocks = new ArrayList<>();

        // Title Section
        Map<String, Object> titleSection = new HashMap<>();
        titleSection.put("type", "header");
        Map<String, Object> titleText = new HashMap<>();
        titleText.put("type", "plain_text");
        titleText.put("text", ":mega: Monitor Notification");
        titleText.put("emoji", true);
        titleSection.put("text", titleText);
        blocks.add(titleSection);

        // Divider
        Map<String, Object> divider = new HashMap<>();
        divider.put("type", "divider");
        blocks.add(divider);

        // Content Section
        Map<String, Object> contentSection = new HashMap<>();
        contentSection.put("type", "section");
        Map<String, Object> contentText = new HashMap<>();
        contentText.put("type", "mrkdwn");
        contentText.put("text", ":question: " + userId + "님의 질문 : \n" + query + "\n" +
                ":robot_face: *gpt answer* : \n" + gptResponse + "\n" +
                ":mag: *Metric:* \n" + metricResult +"\n"+
                ":bar_chart: *Dashboard:* \n" + dashboardUrl);
        contentSection.put("text", contentText);
        blocks.add(contentSection);

        payload.put("blocks", blocks);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity(webhookUrl, request, String.class);

    }

    public void sendSlackNotificationShow(List<Alert> alerts) {
    RestTemplate restTemplate = new RestTemplate();
    Map<String, Object> payload = new HashMap<>();
    List<Map<String, Object>> blocks = new ArrayList<>();

    // Header
    Map<String, Object> headerSection = new HashMap<>();
    headerSection.put("type", "header");
    Map<String, Object> headerText = new HashMap<>();
    headerText.put("type", "plain_text");
    headerText.put("text", ":mag: Alert Metrics List");
    headerSection.put("text", headerText);
    blocks.add(headerSection);

    // Header Divider
    Map<String, Object> headerDivider = new HashMap<>();
    headerDivider.put("type", "divider");
    blocks.add(headerDivider);

    // Alerts
    for (Alert alert : alerts) {
        Map<String, Object> section = new HashMap<>();
        section.put("type", "section");
        Map<String, Object> text = new HashMap<>();
        text.put("type", "mrkdwn");
        text.put("text", ":bell: *Metric:* " + alert.getMetric() + "\n:bar_chart: *Threshold:* " + alert.getThreshold() + "\n:bulb: *Condition:* " + alert.getCondition() + "\n:hourglass: *Duration:* " + alert.getDuration());
        section.put("text", text);
        blocks.add(section);

        // Divider for each alert (optional)
        Map<String, Object> divider = new HashMap<>();
        divider.put("type", "divider");
        blocks.add(divider);
    }

    payload.put("blocks", blocks);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
    restTemplate.postForEntity(webhookUrl, request, String.class);
    }

    public void sendSlackNotificationLokiLabels(List<Loki> lokis) {
    RestTemplate restTemplate = new RestTemplate();
    Map<String, Object> payload = new HashMap<>();
    List<Map<String, Object>> blocks = new ArrayList<>();

    // Header
    Map<String, Object> headerSection = new HashMap<>();
    headerSection.put("type", "header");
    Map<String, Object> headerText = new HashMap<>();
    headerText.put("type", "plain_text");
    headerText.put("text", ":scroll: Loki Labels List");
    headerSection.put("text", headerText);
    blocks.add(headerSection);

    // Header Divider
    Map<String, Object> headerDivider = new HashMap<>();
    headerDivider.put("type", "divider");
    blocks.add(headerDivider);

    // Loki Labels
    for (Loki loki : lokis) {
        Map<String, Object> section = new HashMap<>();
        section.put("type", "section");
        Map<String, Object> text = new HashMap<>();
        text.put("type", "mrkdwn");
        text.put("text", ":label: *Label:* " + loki.getLabel());
        section.put("text", text);
        blocks.add(section);

        // Divider for each label (optional)
        Map<String, Object> divider = new HashMap<>();
        divider.put("type", "divider");
        blocks.add(divider);
    }

    payload.put("blocks", blocks);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
    restTemplate.postForEntity(webhookUrl, request, String.class);
    }


}
