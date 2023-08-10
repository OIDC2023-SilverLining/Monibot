package com.hello.slackApp.service;

import com.hello.slackApp.model.Alert;
import com.hello.slackApp.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SchedulerService {
    private final Map<String, Integer> alertCounts = new HashMap<>();

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private SlackAlertService slackAlertService;

    @Autowired
    private PrometheusService prometheusService;

    public List<Alert> findAll() {
        return alertRepository.findAll();
    }

    @Scheduled(fixedRate = 5000) // 5 seconds
    public void scheduledAlerts() {
        List<Alert> alerts = alertRepository.findAll();
        for (Alert alert : alerts) {
            processAlert(alert);
        }
    }

    private void processAlert(Alert alert) {
        try {
            String metricResult = prometheusService.processQuery(alert.getMetric());
            double metricResultDouble = Double.parseDouble(metricResult);
            int queryValue = alert.getQueryValue();
    
    
            boolean conditionMet = (alert.getCondition().equals("up") && queryValue <= metricResultDouble)
                    || (alert.getCondition().equals("down") && queryValue >= metricResultDouble);
    
            int duration = Integer.parseInt(alert.getDuration());
    
    
            if (conditionMet) {
                int count = alertCounts.getOrDefault(alert.getMetric(), 0) + 1;
                alertCounts.put(alert.getMetric(), count);
    
    
                if (count >= duration / 5) {
                    sendAlert(alert, metricResultDouble);
                }
            } else {
                alertCounts.put(alert.getMetric(), 0); 
            }
    
        } catch (NumberFormatException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void sendAlert(Alert alert, double metricResultValue) {
        String conditionTmp = "";
        if (alert.getCondition().equals("up")) {
            conditionTmp = "이상";
        }
        else {
            conditionTmp = "이하";
        }
        String[] alertQuery = { alert.getType(), alert.getMetric(), conditionTmp,
                Double.toString(metricResultValue), alert.getThreshold() };
        slackAlertService.sendSlackNotification(alertQuery);
    }

    public Alert createAlertFromInput(String[] alertInput) {
        String type = alertInput[0];
        String metric = alertInput[1];
        String threshold = alertInput[2];
        String condition = alertInput[3];
        String duration = alertInput[4];

        return new Alert(type, metric, threshold, condition, duration);
    }

    public void addToDatabase(String[] alertInput) {
        Alert alert = createAlertFromInput(alertInput);
        alertRepository.save(alert);
    }

    public void removeFromDatabase(String[] alertInput) {
        String metric = alertInput[1];
        String threshold = alertInput[2];
        String condition = alertInput[3];
        String duration = alertInput[4];

        alertRepository.delete(metric, threshold, condition, duration);
    }

}
