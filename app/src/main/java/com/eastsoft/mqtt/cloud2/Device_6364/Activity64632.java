package com.eastsoft.mqtt.cloud2.Device_6364;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.eastsoft.mqtt.cloud2.MqttNativeInterface;
import com.eastsoft.mqtt.cloud2.Protocal.MqttManeger;
import com.eastsoft.mqtt.cloud2.R;
import com.eastsoft.mqtt.cloud2.model.LightGson;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Admin on 2015/11/30.
 */
public class Activity64632 extends Activity implements View.OnClickListener {
    // 保持所启动的Service的IBinder对象
    private SerialSvr.MyBinder serialSvrBinder;
    //    private TextView dataText;
    private MqttNativeInterface mqttNativeInterface;
    private View ly64;
    //    private String strAttr;
    private EditText editAttr;
    private static final int CLOUD_CONNECTED = 3;
    private static final int CLOUD_UNCONNECT = 1;
    private static final int CLOUD_LOST = 2;
    private static final int CLOUD_NOT_EXIST = 0;


    private static final String MAC = "mac";
    private static final String DEPTH = "depth";
    private static final String NETSTATE = "netstate";
    private static final String MODE = "mode";
    private static final String ATTR = "attr";

    private View imageLight;
    private SimpleAdapter simpleAdapter;
    private LinkedList<HashMap<String, String>> maps = new LinkedList<>();

    private byte[] mac64;
//    private String mac63Str = null;
//    private boolean is64 = false;


    private String clientId = "t1";
    private String userName = "eastsoft";
    private String psd = "es";
    private static final String TOPIC_TITLE = "eastsoft/things/";
    private static final String TOPIC_TAIL = "/command";

    public static final byte[] rmtIp = new byte[]{(byte) 129, 1, 99, 4};
    public static final short rmtPort = 1883;
    private byte[] locIp;
    private short locPort;
    private short subMessageId = 1;
    private TextView textLightName;
    private boolean mqttConnected = false;
    private TextView textLink;
    private Timer timer = new Timer();
    private boolean tcpConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_6364);
        mqttNativeInterface = new MqttNativeInterface();
//        dataText = (TextView) findViewById(R.id.textdata);
        ly64 = findViewById(R.id.ly64);
        editAttr = (EditText) findViewById(R.id.editattr);

        imageLight = findViewById(R.id.light_on);
        textLightName = (TextView) findViewById(R.id.light_name);
        textLink = (TextView) findViewById(R.id.text_link);

        imageLight.setOnClickListener(this);
        findViewById(R.id.btnattr).setOnClickListener(this);
        findViewById(R.id.btn_open).setOnClickListener(this);
        findViewById(R.id.btn_search).setOnClickListener(this);

        // 添加过滤器的条件
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("RECVFRAME_ACTION");
        registerReceiver(intentReceiver, intentFilter);

        simpleAdapter = new SimpleAdapter(Activity64632.this, maps, R.layout.list_item, new String[]{MAC, DEPTH, NETSTATE, MODE, ATTR},
                new int[]{R.id.text_mac, R.id.text_depth, R.id.text_netstate, R.id.text_mode, R.id.text_attr});
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                final Dialog dialog = new Dialog(Activity64632.this);
                View dialogView = LayoutInflater.from(Activity64632.this).inflate(R.layout.dialog, null);
                dialog.setTitle("更改密钥");
                final EditText editOld = (EditText) dialogView.findViewById(R.id.edit_old);
                final EditText editNew = (EditText) dialogView.findViewById(R.id.edit_new);
                dialogView.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MqttNativeInterface.PskReq pskReq = new MqttNativeInterface().new PskReq();
                        pskReq.dstmac = hexStringToByte(maps.get(i).get(MAC).substring(4));
                        pskReq.oldKey = editOld.getText().toString().getBytes();
                        pskReq.oldKeyLen = (byte) pskReq.oldKey.length;
                        pskReq.newKey = editNew.getText().toString().getBytes();
                        pskReq.newKeyLen = (byte) pskReq.newKey.length;
                        if (serialSvrBinder == null) {
                            showToast("serialSvrBinder=null");
                            return;
                        }
                        if (i == 0) {
                            serialSvrBinder.write(mqttNativeInterface.sdk_set_psk_req(pskReq.newKey, (byte) pskReq.newKey.length));

                        } else {
                            byte[] req = mqttNativeInterface.sdk_rmtpsk_req_req(pskReq);
                            serialSvrBinder.write(req);
                        }
                        dialog.dismiss();

                    }
                });
                dialogView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.setContentView(dialogView);
                dialog.show();
            }
        });

    }


    private byte[] id48_newKey;
    private Handler handler = new Handler();
    // 定义内部类，用于接收广播者发送的消息
    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String str = bundle.getString("recv");
            byte[] dataSrc = bundle.getByteArray("recvbyte");
            int totalLen = 0;
            while (dataSrc.length > totalLen) {
                while (totalLen < dataSrc.length && dataSrc[totalLen] != 0x79) {
                    totalLen++;
                }
                if (totalLen>=dataSrc.length){
                    return;
                }
                byte[] data = Arrays.copyOfRange(dataSrc, totalLen, dataSrc.length);
                MqttNativeInterface.ReceiveData receiveData = mqttNativeInterface.sdk_recv(data, data.length);
                Log.d(SerialSvr.tag, "id=" + receiveData.id);
                totalLen = receiveData.totalLen;
                if (totalLen == -1) {
                    totalLen = 0;
                    data[totalLen] = (byte) 0xff;
                    dataSrc = data;
                    continue;
                }
                if (serialSvrBinder == null) {
                    showToast("serialSvrBinder=null");
                    return;
                }
                switch (receiveData.id) {
                    case 23:
                        handleMac(receiveData);
                        break;
                    case 27://设置密钥返回
                        showToast("设置密钥成功");
                        break;
                    case 28:
                        handleGe(receiveData);
                        break;
                    case 39://远程同步密钥回复
                        Log.d(SerialSvr.tag, "set psk:");
                        serialSvrBinder.write(mqttNativeInterface.sdk_set_psk_req(id48_newKey, (byte) id48_newKey.length));
                        break;
                    case 43://64 连接云结果返回
                        handleTcpConnect(receiveData);
                        break;
                    case 44://网卡接收
                        handleMqttData(receiveData);
                        break;
                    case 45://电力线接收
                        handlePlcData(receiveData);
                        break;
                    case 46://63设备搜索上报
                        handleSearchReport(receiveData);
                        break;
                    case 47://搜索属性请求 63、
                        handleSearchAtt(receiveData);
                        break;
                    case 48://64 请求更改密钥， 63返回新的密钥
                        handleRtpsk(receiveData);
                        break;
                    case 49:
                        byte statusPsk = receiveData.id49_statusPsk;
                        Log.d(SerialSvr.tag, "state=" + statusPsk);
                        showToast(statusPsk == 0 ? "拒绝更改密钥" : "同意更改密钥");
                        break;
                    default:
                        break;
                }
                dataSrc = data;
            }
        }
    };

    private void handleGe(MqttNativeInterface.ReceiveData receiveData) {
        if (receiveData.id28_ip[0] != 0) {
            locIp = receiveData.id28_ip;
            //64连接云
            if (!tcpConnected){
                sendTcpLink();
            }
        } else {
            textLink.setText("ip=0");
            Log.d(SerialSvr.tag, "ge ip=0");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(SerialSvr.tag, "get ge req");
                    serialSvrBinder.write(mqttNativeInterface.sdk_get_ge_req());
                }
            }, 2000);

        }
    }

    private void handleRtpsk(MqttNativeInterface.ReceiveData receiveData) {
        byte state = receiveData.id48_state;
        if (state == 0) {
            showToast("state=00，错误");
            Log.d(SerialSvr.tag, "state=00 error");
            return;
        }
        if (state == 2) {
            Log.d(SerialSvr.tag, "state=2 has changed");
            showToast("state=2,已经改过");
            return;
        }

        MqttNativeInterface.PskReq pskReq = new MqttNativeInterface().new PskReq();
        pskReq.srcmod = 3;
        pskReq.dstmod = 1;
        pskReq.prio = 0;
        pskReq.dstmac = receiveData.id48_srcmac;
        pskReq.state = 1;
        Log.d(SerialSvr.tag, "agree change psk");
        serialSvrBinder.write(mqttNativeInterface.sdk_rmtpsk_resp_req(pskReq));
        id48_newKey = receiveData.id48_newKey;

    }


    private void handleSearchAtt(MqttNativeInterface.ReceiveData receiveData) {

        MqttNativeInterface.SearchResp searchResp = new MqttNativeInterface().new SearchResp();
        searchResp.src = receiveData.id47_srcMac;
        searchResp.taskid = receiveData.id47_taskid;
        searchResp.data = editAttr.getText().toString().getBytes();
        searchResp.len = (byte) searchResp.data.length;
        byte[] attr = mqttNativeInterface.sdk_search_att_resp_req(searchResp);
//        showToast("收到搜索请求并返回属性" + getStr(attr));
        Log.d(SerialSvr.tag, " send attr");
        serialSvrBinder.write(attr);
    }

    private void handleSearchReport(MqttNativeInterface.ReceiveData receiveData) {
        byte count = receiveData.id46_cnt;
//        Log.e(tag + "46:设备搜索上报,count=", count + "");
        MqttNativeInterface.DeviceInfo[] deviceInfos = receiveData.deviceInfos;
        device:
        for (MqttNativeInterface.DeviceInfo deviceInfo : deviceInfos) {
            HashMap<String, String> map = new HashMap<>();
            String mac = getMacStr(deviceInfo.id46_mac);
            String netStr = "";
            switch (deviceInfo.id46_netstate) {
                case 0:
                    netStr = "不在网";
                    break;
                case 1:
                    netStr = "同属于自己网络";
                    break;
                case 2:
                    netStr = "属于其他网络";
                    break;
            }
            String mode = "";
            switch (deviceInfo.id46_mode) {
                case 1:
                    mode = "网关";
                    break;
                case 2:
                    mode = "云";
                    break;
                case 3:
                    mode = "63/64";
                    break;
            }
            for (HashMap map1 : maps) {
                if (map1.get(MAC).equals("mac:" + mac)) {
//                    map1.put(MAC, "mac:" + mac);
                    map1.put(DEPTH, "depth:" + deviceInfo.id46_depth);
                    map1.put(NETSTATE, "netstate:" + netStr);
                    map1.put(MODE, "mode:" + mode);
                    map1.put(ATTR, "attr:" + new String(deviceInfo.id46_attr));
                    continue device;
                }
            }

            map.put(MAC, "mac:" + mac);
            map.put(DEPTH, "depth:" + deviceInfo.id46_depth);
            map.put(NETSTATE, "netstate:" + netStr);
            map.put(MODE, "mode:" + mode);
            map.put(ATTR, "attr:" + new String(deviceInfo.id46_attr));
            maps.add(map);
        }
        simpleAdapter.notifyDataSetChanged();
    }

    private void handlePlcData(MqttNativeInterface.ReceiveData receiveData) {
//        if (is64) {
        if (receiveData.dstmod == 1) {
            MqttNativeInterface.TcpData tcpData = new MqttNativeInterface().new TcpData();
            if (locIp == null) {
                showToast("没有建立TCP连接，不能转发报文");
                return;
            }
            if (!mqttConnected) {
                showToast("mqtt没有连接成功");
                return;
            }
            if (!tcpConnected) {
                showToast("连接已经断开");
                return;
            }
            tcpData.loc_ip = locIp;
            tcpData.loc_port = locPort;
            tcpData.rmt_ip = rmtIp;
            tcpData.rmt_port = rmtPort;
            tcpData.data = receiveData.id45_data;
            tcpData.len = receiveData.id45_data.length;

            byte[] data63 = mqttNativeInterface.sdk_tx_ge_req(tcpData);
            showToast("转发63的mqtt报文到云端");
            Log.d(SerialSvr.tag, "send 63mqtt data to server");
            serialSvrBinder.write(data63);

//                MqttManeger.ReceiveCloudData receiveCloudData = MqttManeger.handleReceivedCloudData(receiveData.id45_data);
//                if (receiveCloudData.receiveMessageEnum == MqttManeger.ReceivedMessageTypeEnum.subscribe) {
//                    mac63Str = receiveCloudData.topicName.split("\\/")[2];
//                }
        } else if (receiveData.dstmod == 3) {
            String dataStr = new String(receiveData.id45_data);
            showToast("本地报文" + dataStr);
            changeViewStatus(getMacStr(mac64), dataStr);
        }

//        } else {
//            byte srcmod = receiveData.srcmod;
//            if (srcmod == 1) {
//                MqttManeger.ReceiveCloudData rcData = MqttManeger.handleReceivedCloudData(receiveData.id45_data);
//                if (rcData.receiveMessageEnum == MqttManeger.ReceivedMessageTypeEnum.publishData) {
////                    showToast("63接收到message" + new String(receiveData.id45_data));
//
//                    String topicName = rcData.topicName;
//                    String message = rcData.message;
//                    String[] topc = topicName.split("\\/");
//                    if (mac63 != null && getMacStr(mac63).equals(topc[2])) {
//                        showToast("63接收到message" + new String(receiveData.id45_data));
//                        changeViewStatus(getMacStr(mac63), message);
//                    }
//                }
//            } else if (srcmod == 3) {
//                String dataStr = new String(receiveData.id45_data);
//                showToast("本地报文" + dataStr);
//                changeViewStatus(getMacStr(mac63), dataStr);
//            }
//        }
    }

    private void handleMqttData(MqttNativeInterface.ReceiveData receiveData) {
        byte[] reData = receiveData.id44_data;
        int len = reData.length;
        if (len <= 0) {
            showToast("接收到数据为空");
            return;
        }
        MqttManeger.ReceiveCloudData receiveCloudData = MqttManeger.handleReceivedCloudData(reData);
        switch (receiveCloudData.receiveMessageEnum) {
            case connect:
                if (receiveCloudData.connectFlag) {
                    mqttConnected = true;
                    showToast("mqtt-connect连接云成功");
                    Log.d(SerialSvr.tag, "64 mqtt connect success and  subscribe");

                    byte[] subBytes = MqttManeger.subscribe(1, TOPIC_TITLE + getMacStr(mac64) + TOPIC_TAIL, (short) (subMessageId + 1));
                    MqttNativeInterface.TcpData tcpData = new MqttNativeInterface().new TcpData();
                    tcpData.data = subBytes;
                    tcpData.len = tcpData.data.length;
                    tcpData.loc_ip = locIp;
                    tcpData.loc_port = locPort;
                    tcpData.rmt_ip = rmtIp;
                    tcpData.rmt_port = rmtPort;
                    serialSvrBinder.write(mqttNativeInterface.sdk_tx_ge_req(tcpData));
//                    keepAlive();
                } else {
                    mqttConnected = false;
                    showToast("mqtt-connect连接云失败");
                    Log.d(SerialSvr.tag, "64 mqtt connect failed reconnect");
                    mqttConnect();

                }
                break;
            case ping:
                if (receiveCloudData.connectFlag) {
                    mqttConnected = true;
                    showToast("mqtt-ping返回成功");
                } else {
                    mqttConnected = false;
                    showToast("mqtt-ping返回失败");
                }
                break;
            case publishData:

                String topicName = receiveCloudData.topicName;
                String message = receiveCloudData.message;
                String[] topc = topicName.split("\\/");
                if (!topc[2].equals(getMacStr(mac64))) {
                    showToast("转发mqtt报文给63");
                    Log.d(SerialSvr.tag, "receive publish data and send to " + topc[2]);
                    sendMqttTo63(reData, hexStringToByte(topc[2]));

                } else {
                    showToast("receive publish data " + "topinc="+topicName+"message="+message);
                    Log.d(SerialSvr.tag, "receive publish data");
                    changeViewStatus(getMacStr(mac64), message);

                }
                break;
            case suback:
                String mmac = getMacStr(mac64);
                Log.d(SerialSvr.tag, "receive 64 suback data and publish " + mmac);

                LightGson lightGson = new LightGson();
                lightGson.setOn(imageLight.getTag().equals("on"));
                Gson gson = new Gson();
                String data = gson.toJson(lightGson, LightGson.class);
                publish(mmac, data);
                break;
        }
    }

    private void mqttConnect() {
        byte[] connect = MqttManeger.connect(getMacStr(mac64), userName, psd);
        MqttNativeInterface.TcpData tcp = new MqttNativeInterface().new TcpData();

        tcp.loc_ip = locIp;
        tcp.loc_port = locPort;
        tcp.rmt_ip = rmtIp;
        tcp.rmt_port = rmtPort;
        tcp.data = connect;
        tcp.len = connect.length;
        serialSvrBinder.write(mqttNativeInterface.sdk_tx_ge_req(tcp));
    }

    private void sendMqttTo63(byte[] reData, byte[] dstmac) {
        MqttNativeInterface.PlcData plcData = new MqttNativeInterface().new PlcData();
        plcData.srcmod = 1;
        plcData.dstmod = 3;
        plcData.data = reData;
        plcData.len = reData.length;
        plcData.dst = dstmac;
        serialSvrBinder.write(mqttNativeInterface.sdk_tx_plc_req(plcData));
    }

    private void keepAlive() {
        timer.cancel();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                MqttManeger.ping();
            }
        }, 1000, MqttManeger.keepAlive * 900);
    }

    private void handleTcpConnect(MqttNativeInterface.ReceiveData receiveData) {
        byte status = receiveData.id43_status;
        switch (status) {
            case CLOUD_CONNECTED:
                textLink.setText("tcp连接成功");
//                showToast("tcp连接云成功");
                locIp = receiveData.id43_locIp;
                locPort = receiveData.id43_locPort;
                //发送mqtt connect报文
                mqttConnect();
                tcpConnected = true;
                break;
            case CLOUD_LOST:
                textLink.setText("tcp连接丢失");
                tcpConnected = false;
//                showToast("tcp连接云lost");
                sendTcpLink();
                break;
            case CLOUD_UNCONNECT:
                tcpConnected = false;
                textLink.setText("tcp连接断开");
                sendTcpLink();
                break;
            case CLOUD_NOT_EXIST:
                tcpConnected = false;
                textLink.setText("tcp连接不存在");
                sendTcpLink();
        }
    }

    private void sendTcpLink() {
        MqttNativeInterface.TcpData tcpData = new MqttNativeInterface().new TcpData();
        tcpData.loc_ip = locIp;
        tcpData.loc_port = 0;
        tcpData.rmt_ip = rmtIp;
        tcpData.rmt_port = rmtPort;
        serialSvrBinder.write(mqttNativeInterface.sdk_link_start_req(tcpData));
    }

    private void handleMac(MqttNativeInterface.ReceiveData receiveData) {
        textLightName.setText(getMacStr(receiveData.id23_mac));

        mac64 = receiveData.id23_mac;
        addCurDevice();

        serialSvrBinder.write(mqttNativeInterface.sdk_get_ge_req());
    }

    private void addCurDevice() {
        if (maps.size() == 0) {
            HashMap<String, String> map = new HashMap<>();
            map.put(MAC, "mac:" + getMacStr(mac64));
            map.put(DEPTH, "depth:" + "当前设备");
            map.put(NETSTATE, "netstate:" + "当前设备");
            map.put(MODE, "mode:" + "当前设备");
            map.put(ATTR, "attr:" + editAttr.getText().toString());
            maps.add(map);
        } else {
            maps.get(0).put(ATTR, "attr:" + editAttr.getText().toString());
            simpleAdapter.notifyDataSetChanged();
        }
    }

    private void changeViewStatus(String mac, String message) {
        Log.d(SerialSvr.tag, message);
        Gson gson = new Gson();
        LightGson lightGson = gson.fromJson(message, LightGson.class);
        if (lightGson.isOn()) {
            imageLight.setTag("on");
            imageLight.setBackgroundResource(R.drawable.light_on);
        } else {
            imageLight.setTag("off");
            imageLight.setBackgroundResource(R.drawable.light_off);
        }
        publish(mac, message);

    }

    private void publish(String mac, String message) {
        byte[] pubBytes = MqttManeger.publish(0, TOPIC_TITLE + mac, message);

        if (locIp == null) {
            showToast("没有建立tcp连接");
            return;
        }
        if (!mqttConnected) {
            showToast("mqtt没有连接成功");
            return;
        }
        MqttNativeInterface.TcpData tcpData = new MqttNativeInterface().new TcpData();
        tcpData.data = pubBytes;
        tcpData.len = tcpData.data.length;
        tcpData.loc_ip = locIp;
        tcpData.loc_port = locPort;
        tcpData.rmt_ip = rmtIp;
        tcpData.rmt_port = rmtPort;
        serialSvrBinder.write(mqttNativeInterface.sdk_tx_ge_req(tcpData));

    }


    public void openSerial() {
        if (serialSvrBinder != null) {
            unbindService(serialConn);
        }

        // 为Intent设置Action属性
        // 1、确保服务名准确；2、确保服务在manifest.xml文件中有定义
        Intent serialIntent = new Intent("com.eastsoft.plcandroid.SerialSvr");
        bindService(serialIntent, serialConn, BIND_AUTO_CREATE);//Service.
    }

    private ServiceConnection serialConn = new ServiceConnection() {
        // 当该Activity与Service连接成功时回调该方法
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 获取Service的onBind方法所返回的MyBinder对象
            serialSvrBinder = (SerialSvr.MyBinder) service; // ①
        }

        // 当该Activity与Service断开连接时回调该方法
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(Activity64632.this, "--Service Disconnected--", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serialSvrBinder.write(MqttManeger.disconnect());
        if (serialSvrBinder != null) {
            serialSvrBinder.close();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_open:
//                dataText.setText("");
                openSerial();
                break;
            case R.id.btnattr:
                clickAttr();
                break;
            case R.id.btn_search:
                clickSearch();

                break;
            case R.id.light_on:
                clickLight();
                break;

        }
    }

    private void clickLight() {
        if (mac64 == null) {
            showToast("mac为空");
            return;
        }
        LightGson lightGson = new LightGson();
        lightGson.setOn(imageLight.getTag().equals("on"));
        Gson gson = new Gson();
        String message = gson.toJson(lightGson, LightGson.class);
        publish(getMacStr(mac64), message);
    }

    private void clickSearch() {
        if (serialSvrBinder == null) {
            showToast("serialSvrBinder=null");
            return;
        }
        maps.clear();
        simpleAdapter.notifyDataSetChanged();
        addCurDevice();
        simpleAdapter.notifyDataSetChanged();
        MqttNativeInterface.Search search = new MqttNativeInterface().new Search();
        search.attrData = editAttr.getText().toString().getBytes();
        search.len = (byte) search.attrData.length;
        byte[] s = mqttNativeInterface.sdk_search_start_req(search);
//        showToast(getStr(s));
        serialSvrBinder.write(s);
    }

    private void clickAttr() {
        if (serialSvrBinder == null) {
            showToast("serialSvrBinder=null");
            return;
        }
        byte[] getmac = mqttNativeInterface.sdk_get_mac_req();
        serialSvrBinder.write(getmac);
//        if (tcpConnected){
//            textLink.setText("未连接");
//        }else{
//
//        }
    }

    private void showToast(String str) {
        Toast.makeText(Activity64632.this, str, Toast.LENGTH_SHORT).show();
    }

    public static String getStr(byte[] data) {
        String str = "";
        for (byte b : data) {
            str += String.format("%02x ", b);
        }
        return str;
    }

    private String getMacStr(byte[] data) {
        String str = "";
        for (byte b : data) {
            str += String.format("%02x", b);
        }
        return str;
    }

    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789abcdef".indexOf(c);
        return b;
    }
}
