package com.hello.slackApp.service;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import com.hello.slackApp.service.ChatgptService;
import com.hello.slackApp.repository.AlertLokiRepository;
import com.hello.slackApp.model.Loki;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class LogFetcher {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ChatgptService chatgptService;

    @Autowired
    private AlertLokiRepository alertLokiRepository;

    @Autowired
    private SlackAlertService slackAlertService;

    @Scheduled(fixedRate = 45000)
    public void fetchLogs() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String lokiPrompt = "Please explain the log above and explain how to solve it in Korean. Please explain it briefly in 5 sentences.";


        // Get all Loki labels from the database
        List<Loki> lokis = alertLokiRepository.findAll();
        for (Loki loki : lokis) {
            String appLabel = loki.getLabel();

            long now = Instant.now().toEpochMilli() * 1_000_000;
            long fifteenSecondsAgo = Instant.now().minus(15, ChronoUnit.SECONDS).toEpochMilli() * 1_000_000;

            URI uri = UriComponentsBuilder.fromHttpUrl("http://localhost:3100/loki/api/v1/query_range")
                    .queryParam("query", "{app=\"" + appLabel + "\"}") // Modified line
                    .queryParam("start", fifteenSecondsAgo)
                    .queryParam("end", now)
                    .build()
                    .encode()
                    .toUri();

            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) dataMap.get("result");

                for (Map<String, Object> result : resultList) {
                    List<List<String>> valuesList = (List<List<String>>) result.get("values");
                    for (List<String> values : valuesList) {
                        String logJson = values.get(1);
                        Map<String, String> logMap = objectMapper.readValue(logJson, Map.class);
                        if (logMap.get("log").contains("Error")) {
                            String lokiErrorLog = logMap.get("log");
                            String lokiQuery = lokiErrorLog + lokiPrompt;
                            String gpt_resp = chatgptService.processSearch(lokiQuery);
                            if (!gpt_resp.equals("failed")) {
                                slackAlertService.sendSlackNotificationLoki(gpt_resp, lokiErrorLog);
                            }
                        }
                    }
                }
            } else {
                System.out.println("Failed to fetch logs with status code: " + response.getStatusCode());
            }
        }
    }
}
