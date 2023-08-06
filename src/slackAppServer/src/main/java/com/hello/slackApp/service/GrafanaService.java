package com.hello.slackApp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
@Slf4j
public class GrafanaService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private JacksonJsonParser jsonParser;

    @Value("${GRAFANA_URL}")
    private String grafana_url;

    @Value("${PROMETHEUS_URL}")
    private String prometheus_url;

    @Value("${GRAFANA_TOKEN}")
    private String grafana_token;

    private String datasourceUid;
    private String dashboardUid;
    private int dashboardVersion;

    public GrafanaService(){
        this.datasourceUid = null;
        this.dashboardUid = null;
        this.dashboardVersion = 0;
        this.jsonParser = new JacksonJsonParser();
    }
    public String getDashboardUrl(String expr) throws IOException {

        if(datasourceUid==null){
            setDataSource();
            createDashboard();
        }

        getDashboardVersion();
        return grafana_url + updateDashboard(expr);
    }

    private void setDataSource() {
        try {
            HttpHeaders headers = getHttpHeadersWithToken();

            String url = grafana_url + "/api/datasources";
            ObjectNode dataSourceJson = createDataSourceJson();
            HttpEntity<JsonNode> requestEntity = new HttpEntity<>(dataSourceJson, headers);
            ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            if(result.getStatusCode() == HttpStatus.OK){
                Map<String, Object> jsonObjectMap = jsonParser.parseMap(result.getBody());
                Map<String, Object> datasource = (Map) jsonObjectMap.get("datasource");
                datasourceUid = (String) datasource.get("uid");
            }else{
                log.info("datasource setting request failed");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @NotNull
    private HttpHeaders getHttpHeadersWithToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + grafana_token);
        return headers;
    }

    @NotNull
    private ObjectNode createDataSourceJson() {
        ObjectNode jsonNodes = JsonNodeFactory.instance.objectNode();
        jsonNodes.put("name", "Prometheus");
        jsonNodes.put("type", "prometheus");
        jsonNodes.put("url", prometheus_url);
        jsonNodes.put("access", "proxy");
        jsonNodes.put("basicAuth", false);
        return jsonNodes;
    }

    private void getDashboardVersion() throws JsonProcessingException {
        HttpHeaders headers = getHttpHeadersWithToken();
        HttpEntity<MultiValueMap<String, String>> requestHeader = new HttpEntity(headers);

        String url = grafana_url + "/api/dashboards/uid/" + dashboardUid;
        try {
            ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, requestHeader, String.class);
            if(result.getStatusCode() == HttpStatus.OK) {
                ObjectNode jsonNode = objectMapper.readValue(result.getBody(), ObjectNode.class);
                ObjectNode dashboardNode = (ObjectNode) jsonNode.get("dashboard");
                dashboardVersion = dashboardNode.get("version").asInt();
            }else{
                log.info("dashboard GET request failed");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private String updateDashboard(String requestedExpr) throws IOException {

        String updatedDashboardUrl = "";
        HttpHeaders headers = getHttpHeadersWithToken();

        JsonNode updateDashBoardJson = createUpdateDashBoardJson(requestedExpr);
        String updatedJsonData = objectMapper.writeValueAsString(updateDashBoardJson);
        HttpEntity<String> requestEntity = new HttpEntity<>(updatedJsonData, headers);

        String url = grafana_url + "/api/dashboards/db";
        try {
            ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            ObjectNode responseNode = objectMapper.readValue(result.getBody(), ObjectNode.class);
            dashboardVersion = responseNode.get("version").asInt();
            updatedDashboardUrl = responseNode.get("url").asText();
        }catch (Exception e){
            e.printStackTrace();
        }
        return updatedDashboardUrl;
    }

    @NotNull
    private JsonNode createUpdateDashBoardJson(String requestedExpr) throws IOException {
        ClassPathResource resource = new ClassPathResource("static/updateTemplate.json");
        InputStream inputStream = resource.getInputStream();
        byte[] data = inputStream.readAllBytes();
        String jsonData = new String(data);
        JsonNode jsonNode = objectMapper.readTree(jsonData);

        JsonNode datasourceUidNode = jsonNode.at("/dashboard/panels/0/datasource/uid");
        if (datasourceUidNode.isNull() || datasourceUidNode.asText().isEmpty()) {
            ((ObjectNode) jsonNode.at("/dashboard/panels/0/datasource")).put("uid", datasourceUid);
        }

        JsonNode dashboardUidNode = jsonNode.at("/dashboard/uid");
        if (dashboardUidNode.isNull() || dashboardUidNode.asText().isEmpty()) {
            ((ObjectNode) jsonNode.at("/dashboard")).put("uid", dashboardUid);
        }

        ((ObjectNode) jsonNode.at("/dashboard")).put("version", dashboardVersion);

        JsonNode panelsNode = jsonNode.at("/dashboard/panels");
        if (panelsNode.isArray()) {
            panelsNode.elements().forEachRemaining(panelNode -> {
                JsonNode targetsNode = panelNode.at("/targets");
                if (targetsNode.isArray()) {
                    targetsNode.elements().forEachRemaining(targetNode -> {
                        JsonNode targetDatasourceUidNode = targetNode.at("/datasource/uid");
                        if (targetDatasourceUidNode.isNull() || targetDatasourceUidNode.asText().isEmpty()) {
                            ((ObjectNode) targetNode.at("/datasource")).put("uid", datasourceUid);
                        }

                        ((ObjectNode) targetNode).put("expr", requestedExpr);
                    });
                }
            });
        }
        return jsonNode;
    }

    private void createDashboard() throws JsonProcessingException {

        HttpHeaders headers = getHttpHeadersWithToken();

        String url = grafana_url + "/api/dashboards/db";

        ObjectNode dashBoardJson = createDashBoardJson();
        try {
            HttpEntity<JsonNode> requestEntity = new HttpEntity<>(dashBoardJson, headers);
            ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            if (result.getStatusCode() == HttpStatus.OK) {
                log.info("dashboard create request success");
                ObjectNode jsonNode = objectMapper.readValue(result.getBody(), ObjectNode.class);
                dashboardUid = jsonNode.get("uid").asText();
            } else {
                log.info("dashboard create request failed");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @NotNull
    private static ObjectNode createDashBoardJson() {
        ObjectNode jsonNodes = JsonNodeFactory.instance.objectNode();
        jsonNodes.put("folderId", 0);
        jsonNodes.put("message", "create Monibot dashboard");
        jsonNodes.put("overwrite", true);

        ObjectNode dashboardNode = JsonNodeFactory.instance.objectNode();
        dashboardNode.put("id", (String) null);
        dashboardNode.put("uid", (String) null);
        dashboardNode.put("title", "Monibot Dashboard");
        dashboardNode.putArray("tags").add("prometheus");
        dashboardNode.put("timezone", "browser");
        dashboardNode.put("schemaVersion", 16);
        dashboardNode.put("version", 0);
        dashboardNode.put("refresh", "25s");

        jsonNodes.set("dashboard", dashboardNode);
        return jsonNodes;
    }

}
