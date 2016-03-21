package com.eastsoft.mqtt.cloud2.model;

import org.eclipse.paho.client.mqttv3.MqttTopic;

/**
 * Created by Admin on 2015/11/11.
 */
public class DeviceInfo {
    private String deviceName;
    private boolean on=false;
    private MqttTopic mqttTopic;
    private String pubTopicString;
    private String subTopicString;

    public String getPubTopicString() {
        return pubTopicString;
    }

    public void setPubTopicString(String pubTopicString) {
        this.pubTopicString = pubTopicString;
    }

    public String getSubTopicString() {
        return subTopicString;
    }

    public void setSubTopicString(String subTopicString) {
        this.subTopicString = subTopicString;
    }

    public MqttTopic getMqttTopic() {
        return mqttTopic;
    }

    public void setMqttTopic(MqttTopic mqttTopic) {
        this.mqttTopic = mqttTopic;
    }
    public String getDeviceName() {
        return deviceName;

    }

    public DeviceInfo(String deviceName, boolean on, String pubTopicString, String subTopicString) {
        this.deviceName = deviceName;
        this.on = on;
        this.pubTopicString = pubTopicString;
        this.subTopicString = subTopicString;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }
}
