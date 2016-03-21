package com.eastsoft.mqtt.cloud2.model;

/**
 * Created by Admin on 2015/12/26.
 */
public class MqttData {
    private String topic;
    private String payload;
    private String aid;

    public MqttData() {
    }

    public MqttData(String topic, String payload,String aid) {

        this.topic = topic;
        this.payload = payload;
        this.aid=aid;
    }

    public String getTopic() {

        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }
}
