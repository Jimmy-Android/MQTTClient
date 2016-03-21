package com.eastsoft.mqtt.cloud2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eastsoft.mqtt.cloud2.model.DeviceInfo;
import com.eastsoft.mqtt.cloud2.model.LightGson;
import com.eastsoft.mqtt.cloud2.model.MyMessage;
import com.eastsoft.mqtt.cloud2.model.TemperatureGson;
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class CloudActivity3 extends Activity {

    private TextView resultTv;
    //	private Button light1Btn, light2Btn;
//    private ImageView light1, light2, light3, light4;
    private ImageView temp1, temp2, tempSwitch1, tempSwitch2;
    private GridView gridView;

    private String host = "tcp://129.1.99.4:1883";
    //	private String host = "tcp://129.1.11.111:1883";
    private String userName = "eastsoft";
    private String passWord = "es";

    private Handler handler;
    private MqttClient client;

    private String myTopic = "sensor";
    private final String LIGHT_BB_SUB = "eastsoft/things/BBBBBBBBBBBB";
    private final String LIGHT_BB_PUB = "eastsoft/things/BBBBBBBBBBBB/command";

    private final String LIGHT_B1_SUB = "eastsoft/things/BBBBBBBBBBB1";
    private final String LIGHT_B1_PUB = "eastsoft/things/BBBBBBBBBBB1/command";

    private final String LIGHT_B2_SUB = "eastsoft/things/BBBBBBBBBBB2";
    private final String LIGHT_B2_PUB = "eastsoft/things/BBBBBBBBBBB2/command";

    private final String LIGHT_CC_SUB = "eastsoft/things/CCCCCCCCCCCC";
    private final String LIGHT_CC_PUB = "eastsoft/things/CCCCCCCCCCCC/command";

    private final String LIGHT_9001_2332_SUB = "eastsoft/9001/1711282332";
    private final String LIGHT_9001_2332_PUB = "eastsoft/9001/1711282332/command";

    private final String LIGHT_9001_2436_SUB = "eastsoft/9001/1711282436";
    private final String LIGHT_9001_2436_PUB = "eastsoft/9001/1711282436/command";

    private final String LIGHT_11001_2858_SUB = "eastsoft/11001/1711282858";
    private final String LIGHT_11001_2858_PUB = "eastsoft/11001/1711282858/command";

    private final String LIGHT_11001_2799_SUB = "eastsoft/11001/1711282799";
    private final String LIGHT_11001_2799_PUB = "eastsoft/11001/1711282799/command";

    private final String THERMOSTAT_9001_3049_SUB = "eastsoft/9001/1711973049";
    private final String THERMOSTAT_9001_3049_PUB = "eastsoft/9001/1711973049/command";

    private final String THERMOSTAT_11001_8916_SUB = "eastsoft/11001/1695338916";
    private final String THERMOSTAT_11001_8916_PUB = "eastsoft/11001/1695338916/command";


    private Adapter adapter;
    private String[] deviceName = {"9001:1711282332", "9001:1711282436", "11001:1711282858", "11001:1711282799", "AID:BBBBBB", "AID:CCCCCC", "AID:BBBBBBBBBBB1", "AID:BBBBBBBBBBB2"};
    private String[] subTopicStringArray = {LIGHT_9001_2332_SUB, LIGHT_9001_2436_SUB, LIGHT_11001_2858_SUB, LIGHT_11001_2799_SUB, LIGHT_BB_SUB, LIGHT_CC_SUB, LIGHT_B1_SUB, LIGHT_B2_SUB};
    private String[] pubTopicStringArray = {LIGHT_9001_2332_PUB, LIGHT_9001_2436_PUB, LIGHT_11001_2858_PUB, LIGHT_11001_2799_PUB, LIGHT_BB_PUB, LIGHT_CC_PUB, LIGHT_B1_PUB, LIGHT_B2_PUB};

//    private List<MqttTopic> mqttTopicList = new LinkedList<>();
    private MqttConnectOptions options;

    private List<DeviceInfo> deviceInfoList = new LinkedList<>();

    private ScheduledExecutorService scheduler;
    //light pub state
//    private MqttTopic light1PubTopic;
//    private MqttTopic light2PubTopic;
//    private MqttTopic light3PubTopic;
//    private MqttTopic light4PubTopic;

    private MqttTopic temp1PubTopic;
    private MqttTopic temp2PubTopic;
    private ProgressDialog progressDialog;


    private static Map<String, Integer> tempMap = new HashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main3);
        initView();
        initTempMap();
        initMqttClient();
        startReconnect();
        initHander();
    }

    private void initHander() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case 1:
                        System.out.println("-----------------------------");
                        MyMessage payload = (MyMessage) msg.obj;
                        Toast.makeText(CloudActivity3.this, payload.toString(),
                                Toast.LENGTH_SHORT).show();
                        if (THERMOSTAT_9001_3049_SUB.equals(payload.getTopicName())) {
                            Gson gson = new Gson();
                            TemperatureGson temperatureGson = gson.fromJson(payload.getMsg(), TemperatureGson.class);

                            int temp = temperatureGson.getTemperature();
                            if (temp == 0) {
                                if (temperatureGson.isOn()) {
                                    tempSwitch1.setBackgroundResource(R.drawable.tem_on_default);
                                    tempSwitch1.setTag("on");
                                } else {
                                    tempSwitch1.setBackgroundResource(R.drawable.tem_off_default);
                                    tempSwitch1.setTag("off");
                                }
                            } else {
                                if (temp < 5) {
                                    temp = 5;
                                }
                                if (temp > 35) {
                                    temp = 35;
                                }
                                temp1.setBackgroundResource(tempMap.get(temp + ""));
                            }

                        } else if (THERMOSTAT_11001_8916_SUB.equals(payload.getTopicName())) {

                            Gson gson = new Gson();
                            TemperatureGson temperatureGson = gson.fromJson(payload.getMsg(), TemperatureGson.class);

                            int temp = temperatureGson.getTemperature();
                            if (temp == 0) {
                                if (temperatureGson.isOn()) {
                                    tempSwitch2.setBackgroundResource(R.drawable.tem_on_default);
                                    tempSwitch2.setTag("on");
                                } else {
                                    tempSwitch2.setBackgroundResource(R.drawable.tem_off_default);
                                    tempSwitch2.setTag("off");
                                }
                            } else {
                                if (temp < 5) {
                                    temp = 5;
                                }
                                if (temp > 35) {
                                    temp = 35;
                                }
                                temp2.setBackgroundResource(tempMap.get(temp + ""));
                            }

                        } else{
                            for (int i=0;i< deviceInfoList.size();i++){
                                if (payload.getTopicName().equals(deviceInfoList.get(i).getSubTopicString())){
                                    Gson gson = new Gson();
                                    LightGson lightGson = gson.fromJson(payload.getMsg(), LightGson.class);

                                    deviceInfoList.get(i).setOn(lightGson.isOn());
                                   adapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                        }

                        break;
                    case 2:
                        if (progressDialog != null && progressDialog.isShowing()) {

                            progressDialog.dismiss();
                        }
                        Toast.makeText(CloudActivity3.this, "连接成功",
                                Toast.LENGTH_SHORT).show();
                        try {
                            for (int i = 0; i <deviceInfoList.size(); i++) {
                               deviceInfoList.get(i).setMqttTopic(client.getTopic(deviceInfoList.get(i).getPubTopicString()));
                            }
                            for (int i = 0; i < subTopicStringArray.length; i++) {
                                client.subscribe(subTopicStringArray[i], 1);
                            }

                            temp1PubTopic = client.getTopic(THERMOSTAT_9001_3049_PUB);
                            temp2PubTopic = client.getTopic(THERMOSTAT_11001_8916_PUB);

                            client.subscribe(THERMOSTAT_9001_3049_SUB, 1);
                            client.subscribe(THERMOSTAT_11001_8916_SUB, 1);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 3:
                        if (progressDialog != null && progressDialog.isShowing()) {

                            progressDialog.dismiss();
                        }
                        Toast.makeText(CloudActivity3.this, "连接失败，系统正在重连",
                                Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }

            }
        };
    }

    private void initMqttClient() {
        try {

            // host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(host, "test136", new MemoryPersistence());
            // MQTT的连接设置
            options = new MqttConnectOptions();
            // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            // //设置连接的用户名
            options.setUserName(userName);
            // //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            // 设置回调
            client.setCallback(new MqttCallback() {

                public void connectionLost(Throwable cause) {
                    // 连接丢失后，一般在这里面进行重连
                    System.out.println("connectionLost----------");
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    // publish后会执行到这里
                    System.out.println("deliveryComplete---------"
                            + token.isComplete());
                }

                public void messageArrived(String topicName, MqttMessage message)
                        throws Exception {
                    // subscribe后得到的消息会执行到这里面
                    System.out.println("messageArrived-----" + topicName
                            + "-----" + message.toString());
                    Message msg = new Message();
                    msg.what = 1;
                    // msg.obj = topicName + "---" + message.toString();
                    MyMessage mmsg = new MyMessage();
                    mmsg.setMsg(message.toString());
                    mmsg.setTopicName(topicName);
                    msg.obj = mmsg;
                    handler.sendMessage(msg);
                }

            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void initView() {

        for (int i = 0; i < deviceName.length; i++) {
            deviceInfoList.add(new DeviceInfo(deviceName[i], false,pubTopicStringArray[i],subTopicStringArray[i]));
        }
        gridView = (GridView) findViewById(R.id.gridview);
        adapter = new Adapter();
        gridView.setAdapter(adapter);
        temp1 = (ImageView) findViewById(R.id.temperture_9001_3049);
        temp2 = (ImageView) findViewById(R.id.temperture_11001_8916);
        tempSwitch1 = (ImageView) findViewById(R.id.tem_switch_9001_3049);
        tempSwitch2 = (ImageView) findViewById(R.id.tem_switch_11001_8916);

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("正在连接，请稍后...");
        progressDialog.setMax(200);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    LightGson lightGson = new LightGson();
                    lightGson.setOn(!deviceInfoList.get(i).isOn());
                    Gson gson = new Gson();
                    MyMessage payload = new MyMessage();
                    payload.setMsg(gson.toJson(lightGson));
                    payload.setTopicName(deviceInfoList.get(i).getPubTopicString());

                    pub(payload, deviceInfoList.get(i).getMqttTopic());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        tempSwitch1.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                try {
                    TemperatureGson tem = new TemperatureGson();
                    if ("on".equals(tempSwitch1.getTag().toString())) {
                        tem.setOn(false);
                    } else {
                        tem.setOn(true);
                    }
                    Gson gson = new Gson();
                    MyMessage payload = new MyMessage();
                    payload.setMsg(gson.toJson(tem));
                    payload.setTopicName(THERMOSTAT_9001_3049_PUB);
                    pub(payload, temp1PubTopic);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        tempSwitch2.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                try {
                    TemperatureGson tem = new TemperatureGson();
                    if ("on".equals(tempSwitch2.getTag().toString())) {
                        tem.setOn(false);
                    } else {
                        tem.setOn(true);
                    }
                    Gson gson = new Gson();
                    MyMessage payload = new MyMessage();
                    payload.setMsg(gson.toJson(tem));
                    payload.setTopicName(THERMOSTAT_11001_8916_PUB);
                    pub(payload, temp2PubTopic);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.all_on).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allOnOff(true);
            }
        });

        findViewById(R.id.all_off).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              allOnOff(false);
            }
        });
    }

    private void allOnOff(boolean on) {
        for (DeviceInfo deviceInfo : deviceInfoList) {

            LightGson lightGson = new LightGson();
            lightGson.setOn(on);
            Gson gson = new Gson();
            MyMessage payload = new MyMessage();
            payload.setMsg(gson.toJson(lightGson));
            payload.setTopicName(deviceInfo.getPubTopicString());
            pub(payload, deviceInfo.getMqttTopic());
        }
        TemperatureGson tem = new TemperatureGson();
        tem.setOn(on);
        Gson gson = new Gson();
        MyMessage payload = new MyMessage();
        payload.setMsg(gson.toJson(tem));
        payload.setTopicName(THERMOSTAT_11001_8916_PUB);
        pub(payload, temp2PubTopic);

        TemperatureGson tem2 = new TemperatureGson();
        tem2.setOn(on);
        Gson gson2 = new Gson();
        MyMessage payload2 = new MyMessage();
        payload2.setMsg(gson2.toJson(tem2));
        payload2.setTopicName(THERMOSTAT_9001_3049_PUB);
        pub(payload, temp1PubTopic);
    }

    private void pub(MyMessage payload, MqttTopic myttTopic) {
        MqttMessage message = new MqttMessage();
        message.setQos(1);
        message.setRetained(true);
        System.out.println(message.isRetained() + "------ratained状态");
        message.setPayload(payload.getMsg().getBytes());
        try {
            MqttDeliveryToken token;
            token = myttTopic.publish(message);
            token.waitForCompletion();
            System.out.println(token.isComplete() + "========");
            Toast.makeText(CloudActivity3.this, payload.toString(),
                    Toast.LENGTH_SHORT).show();
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    private void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            public void run() {
                if (!client.isConnected()) {
                    connect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    private void connect() {
        new Thread(new Runnable() {

            public void run() {
                try {
                    client.connect(options);
                    Message msg = new Message();
                    msg.what = 2;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 3;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void initTempMap() {
        tempMap.put(5 + "", R.drawable.num_5);
        tempMap.put(6 + "", R.drawable.num_6);
        tempMap.put(7 + "", R.drawable.num_7);
        tempMap.put(8 + "", R.drawable.num_8);
        tempMap.put(9 + "", R.drawable.num_9);
        tempMap.put(10 + "", R.drawable.num_10);
        tempMap.put(11 + "", R.drawable.num_11);
        tempMap.put(12 + "", R.drawable.num_12);
        tempMap.put(13 + "", R.drawable.num_13);
        tempMap.put(14 + "", R.drawable.num_14);
        tempMap.put(15 + "", R.drawable.num_15);
        tempMap.put(16 + "", R.drawable.num_16);
        tempMap.put(17 + "", R.drawable.num_17);
        tempMap.put(18 + "", R.drawable.num_18);
        tempMap.put(19 + "", R.drawable.num_19);
        tempMap.put(20 + "", R.drawable.num_20);
        tempMap.put(21 + "", R.drawable.num_21);
        tempMap.put(22 + "", R.drawable.num_22);
        tempMap.put(23 + "", R.drawable.num_23);
        tempMap.put(24 + "", R.drawable.num_24);
        tempMap.put(25 + "", R.drawable.num_25);
        tempMap.put(26 + "", R.drawable.num_26);
        tempMap.put(27 + "", R.drawable.num_27);
        tempMap.put(28 + "", R.drawable.num_28);
        tempMap.put(29 + "", R.drawable.num_29);
        tempMap.put(30 + "", R.drawable.num_30);
        tempMap.put(31 + "", R.drawable.num_31);
        tempMap.put(32 + "", R.drawable.num_32);
        tempMap.put(33 + "", R.drawable.num_33);
        tempMap.put(34 + "", R.drawable.num_34);
        tempMap.put(35 + "", R.drawable.num_35);

    }



    class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return deviceInfoList.size();
        }

        @Override
        public Object getItem(int i) {
            return deviceInfoList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(CloudActivity3.this).inflate(R.layout.gridview_item, null);
                Holder holder = new Holder();
                holder.imageView = (ImageView) view.findViewById(R.id.light_img);
                holder.textView = (TextView) view.findViewById(R.id.light_name);
                view.setTag(holder);
            }
            Holder holder = (Holder) view.getTag();
            DeviceInfo deviceInfo = deviceInfoList.get(position);

            holder.imageView.setBackgroundResource(deviceInfo.isOn() ? R.drawable.light_on : R.drawable.light_off);
            holder.textView.setText(deviceInfo.getDeviceName());

            return view;
        }

        class Holder {
            private ImageView imageView;
            private TextView textView;
        }
    }


    @Override

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (client != null && keyCode == KeyEvent.KEYCODE_BACK) {
            try {
                client.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            scheduler.shutdown();
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
