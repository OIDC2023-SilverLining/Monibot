// src/main/java/com/hello/slackApp/repository/AlertRepository.java

package com.hello.slackApp.repository;

import com.hello.slackApp.model.Loki;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Collections;
import java.util.List;

@Repository
public class AlertLokiRepository {

    private final JdbcTemplate jdbcTemplate;

    public AlertLokiRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Loki> findAll() {
        try {
            return jdbcTemplate.query(
                    "SELECT * FROM loki",
                    (rs, rowNum) -> new Loki(rs.getString("label")));
        } catch (DataAccessException e) {
            return Collections.emptyList();
        }
    }

    public void save(Loki loki) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO loki(label) VALUES (?)",
                    loki.getLabel());
        } catch (DataAccessException e) {
                e.printStackTrace();
        }
    }

    public Loki findByMetric(String label) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM loki WHERE label = ?",
                    new Object[]{label},
                    (rs, rowNum) -> new Loki(rs.getString("label")));
        } catch (DataAccessException e) {
            return null;
        }
    }

    public void delete(String label) {
        try {
            jdbcTemplate.update(
                    "DELETE FROM loki WHERE label = ?",
                    label);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }
}
