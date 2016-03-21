package com.eastsoft.mqtt.cloud2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.eastsoft.mqtt.cloud2.model.MqttData;
import com.eastsoft.mqtt.cloud2.util.ActionUtil;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by Admin on 2015/12/26.
 */
public class MqttManeger {
    //    private String host = "tcp://test.mosquitto.org:1883";
    private String host = "tcp://iot.eastsoft.com.cn:1883";
    private String userName = "d6fa49b0-ebd9-46c8-ad5a-56438c7571ef";
    private String passWord = "gMd0d1mQ";
    public MqttClient client = null;
    //private static String TOPIC_HEAD = "eastsoft/things/";
    //private static String CMD = "/cmd";
    //private String mac;
    private static int QOS = 1;

    public void connect(final Context context) {
        initMqttClient(context);
        if (!client.isConnected()) {
            con(context);
        }
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction(ActionUtil.ACTION_CONNECT_RESULT);
        BroadcastReceiver r1 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                Boolean result = bundle.getBoolean(ActionUtil.DATA);
                if (result) {
                    Toast.makeText(context, "连接成功", Toast.LENGTH_SHORT).show();
                    try {
                        client.subscribe("5OHR236W3/receive/test01", QOS);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(context, "连接失败，正在重连", Toast.LENGTH_SHORT).show();
                    con(context);
                }
            }
        };
        context.registerReceiver(r1, intentFilter1);


    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void con(final Context context) {
        new Thread(new Runnable() {

            public void run() {
                try {
                    client.connect(options);
                    Intent intent = new Intent(ActionUtil.ACTION_CONNECT_RESULT);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(ActionUtil.DATA, true);
                    intent.putExtras(bundle);
                    context.sendBroadcast(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Intent intent = new Intent(ActionUtil.ACTION_CONNECT_RESULT);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(ActionUtil.DATA, false);
                    intent.putExtras(bundle);
                    context.sendBroadcast(intent);
                }
            }
        }).start();
    }


    public void pub(String payload, String mac) {
        //TOPIC_HEAD + mac + CMD
        MqttTopic mqttTopic = client.getTopic("5OHR236W3/heater/test01");
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(1);
        mqttMessage.setRetained(false);
        mqttMessage.setPayload(payload.getBytes());
        try {
            MqttDeliveryToken token;
            token = mqttTopic.publish(mqttMessage);
            token.waitForCompletion();
            System.out.println("publish----------+"+payload);
            Log.i("publish----------+",payload);

        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    static MqttConnectOptions options;
    //public void subscribe(String mac) {
    public void subscribe() {
        if (client.isConnected()) {
            try {
                //client.subscribe("2KWRR4CAL/things/test", QOS);
                client.subscribe("5OHR236W3/receive/test01", QOS);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            //this.mac = mac;
            try {
                client.connect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }


    }

    public void initMqttClient(final Context context) {
        try {
            client = new MqttClient(host, "test1", new MemoryPersistence());
            options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(userName);
            options.setPassword(passWord.toCharArray());
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(20);
            client.setCallback(new MqttCallback() {

                public void connectionLost(Throwable cause) {
                    // 连接丢失后，一般在这里面进行重连
                    System.out.println("connectionLost----------");
                    con(context);
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    // publish后会执行到这里
                    System.out.println("deliveryComplete---------"
                            + token.isComplete());
                }

                public void messageArrived(String topicName, MqttMessage message)
                        throws Exception {
                    System.out.println("receive---------"
                            + topicName + message.toString());
                    String[] topicBytes = topicName.split("\\/");
                    if (topicBytes.length > 2) {
                        MqttData mqttData = new MqttData(topicName, message.toString(), topicBytes[2]);
                        Intent intent = new Intent(ActionUtil.ACTION_RECE_DATA);
                        Bundle bundle = new Bundle();
                        Gson gson = new Gson();

                        bundle.putString(ActionUtil.DATA, gson.toJson(mqttData));
                        intent.putExtras(bundle);
                        context.sendBroadcast(intent);
                    }

                }

            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disConnect() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
}
