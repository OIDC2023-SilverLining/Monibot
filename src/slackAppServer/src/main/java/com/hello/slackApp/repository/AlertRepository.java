// src/main/java/com/hello/slackApp/repository/AlertRepository.java

package com.hello.slackApp.repository;

import com.hello.slackApp.model.Alert;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class AlertRepository {

    private final JdbcTemplate jdbcTemplate;

    public AlertRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Alert> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM alerts",
                (rs, rowNum) -> new Alert(rs.getString("type"),
                        rs.getString("metric"),
                        rs.getString("threshold"),
                        rs.getString("condition"),
                        rs.getString("duration")));
    }

    public void save(Alert alert) {
        jdbcTemplate.update(
                "INSERT INTO alerts(type, metric, threshold, condition, duration) VALUES (?, ?, ?, ?, ?)",
                alert.getType(),
                alert.getMetric(),
                alert.getThreshold(),
                alert.getCondition(),
                alert.getDuration());
    }
    
    public Alert findByMetric(String metric) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM alerts WHERE metric = ?",
                new Object[]{metric},
                (rs, rowNum) -> new Alert(rs.getString("type"),
                        rs.getString("metric"),
                        rs.getString("threshold"),
                        rs.getString("condition"),
                        rs.getString("duration")));
    }    

    public void delete(String metric, String threshold, String condition, String duration) {
    jdbcTemplate.update(
            "DELETE FROM alerts WHERE metric = ? AND threshold = ? AND condition = ? AND duration = ?",
            metric, threshold, condition, duration);
    }

 
}
