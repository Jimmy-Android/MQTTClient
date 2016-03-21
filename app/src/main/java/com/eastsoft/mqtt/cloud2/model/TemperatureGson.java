package com.eastsoft.mqtt.cloud2.model;

/**
 * Created by Admin on 2015/11/10.
 */
public class TemperatureGson {
    private int  current_temperature;
    private boolean on_off;


    public int getTemperature() {
        return current_temperature;
    }

    public void setTemperature(int current_temperature) {
        this.current_temperature = current_temperature;
    }


    public boolean isOn() {
        return on_off;
    }

    public void setOn(boolean on) {
        this.on_off = on;
    }
}
