package com.eastsoft.mqtt.cloud2;

import android.app.Application;

/**
 * Created by Administrator on 2016/1/4 0004.
 */
public class MyApplication extends Application{
   public  MqttManeger mqttManeger;
    @Override
    public void onCreate() {
        super.onCreate();
        mqttManeger=new MqttManeger();
        mqttManeger.connect(this);
    }


}
