package com.eastsoft.mqtt.cloud2.model;

/**
 * Created by Admin on 2015/11/28.
 */
public class MqttMessage {
    private boolean mutable = true;
    private byte[] payload;
    private int qos = 1;
    private boolean retained = false;
    private boolean dup = false;

    public static void validateQos(int qos) {
        if(qos < 0 || qos > 2) {
            throw new IllegalArgumentException();
        }
    }

    public MqttMessage() {
        this.setPayload(new byte[0]);
    }

    public MqttMessage(byte[] payload) {
        this.setPayload(payload);
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public void clearPayload() {
        this.checkMutable();
        this.payload = new byte[0];
    }

    public void setPayload(byte[] payload) {
        this.checkMutable();
        if(payload == null) {
            throw new NullPointerException();
        } else {
            this.payload = payload;
        }
    }

    public boolean isRetained() {
        return this.retained;
    }

    public void setRetained(boolean retained) {
        this.checkMutable();
        this.retained = retained;
    }

    public int getQos() {
        return this.qos;
    }

    public void setQos(int qos) {
        this.checkMutable();
        validateQos(qos);
        this.qos = qos;
    }

    public String toString() {
        return new String(this.payload);
    }

    protected void setMutable(boolean mutable) {
        this.mutable = mutable;
    }

    protected void checkMutable() throws IllegalStateException {
        if(!this.mutable) {
            throw new IllegalStateException();
        }
    }

    protected void setDuplicate(boolean dup) {
        this.dup = dup;
    }

    public boolean isDuplicate() {
        return this.dup;
    }
}
