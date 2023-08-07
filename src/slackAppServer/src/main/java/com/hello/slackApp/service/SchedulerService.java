package com.hello.slackApp.service;

import java.io.UnsupportedEncodingException;
import com.hello.slackApp.model.Alert;
import com.hello.slackApp.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchedulerService {
    public int cnt = 0;
    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private SlackAlertService slackAlertService;

    @Autowired
    private PrometheusService prometheusService;

    @Scheduled(fixedRate = 15000) // 5 seconds
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
            int queryValue = alert.getQueryValue(); // No need for Integer.parseInt()

            if ((alert.getCondition().equals("up") && queryValue <= metricResultDouble)
                    || (alert.getCondition().equals("down") && queryValue >= metricResultDouble)) {
                alert.incrementCount();

                int duration = Integer.parseInt(alert.getDuration());
                if (alert.getCount() >= duration / 5) {
                    sendAlert(alert, metricResultDouble);
                    alert.resetCount();
                }
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

    public void removeFromDatabase(String metric) {
        Alert alert = alertRepository.findByMetric(metric);
        if (alert != null) {
            alertRepository.delete(alert.getMetric());
        }
    }
}
