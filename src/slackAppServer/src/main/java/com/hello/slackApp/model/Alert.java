package com.hello.slackApp.model;

public class Alert {
    private String type;
    private String metric;
    private String threshold;
    private String condition;
    private String duration;
    private int queryValue; // queryValue field
    private int count; // count field

    // All-arguments constructor
    public Alert(String type, String metric, String threshold, String condition, String duration) {
        this.type = type;
        this.metric = metric;
        this.threshold = threshold;
        this.condition = condition;
        this.duration = duration;
        this.queryValue = Integer.parseInt(threshold); // initialize queryValue
        this.count = 0; // initialize count
    }

    public int getQueryValue() {
        return queryValue;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        count++;
    }

    public void resetCount() {
        count = 0;
    }
    
    // getters and setters
    public String getType() {
         return type;
         }
    public void setType(String type) {
         this.type = type;
         }
    public String getMetric() {
         return metric;
         }
    public void setMetric(String metric) {
         this.metric = metric;
         }
    public String getCondition() {
         return condition;
         }
    public void setCondition(String condition) {
         this.condition = condition;
         }
    public String getDuration() {
         return duration;
         }
    public void setDuration(String duration) {
         this.duration = duration;
         }

    public String getThreshold() {
        return threshold;
    }

}
