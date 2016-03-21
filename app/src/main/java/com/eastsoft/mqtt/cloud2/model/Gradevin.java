package com.eastsoft.mqtt.cloud2.model;

/**
 * Created by Admin on 2015/12/28.
 */
public class Gradevin {

    public Gradevin(String cmd, int dt) {
        this.cmd = cmd;
        this.dt = dt;
    }

    private String cmd;
    private int dt;

    public String getCmd() {
        return cmd;
    }


    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public int getDt() {
        return dt;
    }

    public void setDt(int dt) {
        this.dt = dt;
    }
}
