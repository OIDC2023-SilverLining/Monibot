package com.hello.slackApp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class GptCacheService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${CACHE_SERVER_URL}")
    private String cacheServerUrl;

    public String getCachedResponse(String query){
        String url = cacheServerUrl + "/?query={query}";
        log.info(url);
        try {
            ResponseEntity<String> result = restTemplate.getForEntity(url, String.class, query);
            if(result.getStatusCode() == HttpStatus.OK) {
                ObjectNode responseJson = objectMapper.readValue(result.getBody(), ObjectNode.class);
                boolean isValid = responseJson.get("valid").asBoolean();
                if(isValid){
                    String cacheResponse = responseJson.get("answer").asText();
                    log.info("cached: {}", cacheResponse);
                    return cacheResponse;
                }
            }else{
                log.info("gptCache GET request failed");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void setCachedResponse(String query, String gptResponse){

        ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
        jsonNode.put("query", query);
        jsonNode.put("answer", gptResponse);
        log.info("request: {}", jsonNode.toString());
        try {
            ResponseEntity<String> result = restTemplate.postForEntity(cacheServerUrl+"/", jsonNode, String.class);
            if(result.getStatusCode() == HttpStatus.OK) {
                ObjectNode responseJson = objectMapper.readValue(result.getBody(), ObjectNode.class);
                boolean isSuccess = responseJson.get("success").asBoolean();
                if(isSuccess){
                    log.info("setting query in cache success: query={}, answer={}", query, gptResponse);
                } else{
                    log.info("setting query in cache failed");
                }
            }else{
                log.info("gptCache POST request failed");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
