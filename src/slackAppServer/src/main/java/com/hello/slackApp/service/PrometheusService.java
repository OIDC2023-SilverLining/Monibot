package com.hello.slackApp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class PrometheusService {

    @Value("${PROMETHEUS_URL}")
    private String prometheusUrl;

    public String processQuery(String query) throws UnsupportedEncodingException {

        String promApiCall = "api/v1/query?query=";;

        try {
            Thread.sleep(200); // 200 milliseconds delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
        String requestUrl = prometheusUrl + "/" + promApiCall + encodedQuery;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create(requestUrl));

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            String responseBody = responseEntity.getBody();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                JsonNode valueNode = jsonNode
                        .path("data")
                        .path("result")
                        .get(0)
                        .path("value");

                String value = valueNode.get(1).asText();
                return value;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "Error";
    }
}
