package com.eastsoft.mqtt.cloud2.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.eastsoft.mqtt.cloud2.BaseActivity;
import com.eastsoft.mqtt.cloud2.MyApplication;
import com.eastsoft.mqtt.cloud2.R;
import com.eastsoft.mqtt.cloud2.model.Gradevin;
import com.eastsoft.mqtt.cloud2.model.MqttData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mr Wang on 2016/2/23.
 */
public class HeaterActivity extends BaseActivity implements View.OnClickListener {
    private ImageView shower,watering,smartHome,fire,OL,tmicon;
    private ImageButton decrease,add,switchIcon;
    private TextView temperature;
    private ImageButton[] buttons;
    private MyApplication myApplication;
    private String mac="test01";
    private BroadcastReceiver broadcastReceiver;
    private int currentTemp=0;
    private String heterSt="off",priority="on";
    private String heaterStatus="off",fireIcon="off";
    private String currentTp,upWater;
    private boolean model=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.heater);
        myApplication = (MyApplication) getApplication();
        myApplication.mqttManeger.subscribe();
        Toast.makeText(HeaterActivity.this, "已经订阅", Toast.LENGTH_LONG).show();
        initData();
        registReceiver();
        pullDeviceState();
    }

    private void  initData(){
        shower=(ImageView) this.findViewById(R.id.shower);
        //watering=(ImageView) this.findViewById(R.id.watering);
        //smartHome=(ImageView) this.findViewById(R.id.smart_home);
        fire=(ImageView) this.findViewById(R.id.fire);
        //OL=(ImageView) this.findViewById(R.id.ol);
        tmicon=(ImageView) this.findViewById(R.id.icon);
        decrease=(ImageButton) this.findViewById(R.id.decrease);
        add=(ImageButton) this.findViewById(R.id.add);
        switchIcon=(ImageButton) this.findViewById(R.id.switch_on);
        temperature=(TextView)this.findViewById(R.id.temperature);
        buttons=new ImageButton[]{decrease,add,switchIcon};
        for (int i=0;i<buttons.length;i++){
            buttons[i].setOnClickListener(this);
        }
        //smartHome.setBackgroundResource(R.drawable.smart_gray);
        shower.setBackgroundResource(R.drawable.shower_gray);
        //watering.setBackgroundResource(R.drawable.up_water_gray);
        fire.setBackgroundResource(R.drawable.fire_gray);
        //OL.setBackgroundResource(R.drawable.ol_gray);
        tmicon.setBackgroundResource(R.drawable.tem_gray);
        switchIcon.setBackground(getResources().getDrawable(R.drawable.switch_selected));
        decrease.setBackgroundResource(R.drawable.decrease_selected);
        add.setBackgroundResource(R.drawable.add_selected);
        temperature.setTextColor(Color.GRAY);

    }


    private void registReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActionUtil.ACTION_RECE_DATA);
        broadcastReceiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle bundle = intent.getExtras();
                String data = bundle.getString(ActionUtil.DATA);
                Gson gson = new Gson();
                MqttData mqttData = gson.fromJson(data, MqttData.class);
                if (mqttData.getAid() == null || !mqttData.getAid().equals(mac)) {
                    return;
                }
                String payload = mqttData.getPayload();
                Log.e("Json格式",payload);
                //Toast.makeText(HeaterActivity.this,payload,Toast.LENGTH_LONG).show();
                try {
                    JSONObject jsonObject = new JSONObject(payload);
                    if (jsonObject != null) {
                        String jasonObj = jsonObject.getString(JsonKey.FUNCTION);
                        JSONObject jsonObj = new JSONObject(jasonObj);
                        if (jasonObj != null) {
                            if (jasonObj.indexOf(JsonKey.POWER)!=-1){
                                heaterStatus = jsonObj.get(JsonKey.POWER).toString();
                            }
                            if (jasonObj.indexOf(JsonKey.CURRENT_TEMP)!=-1){
                                currentTp = jsonObj.get(JsonKey.CURRENT_TEMP).toString();
                            }
                            if (jasonObj.indexOf(JsonKey.FIRE)!=-1){
                                fireIcon = jsonObj.getString(JsonKey.FIRE);
                            }
                            if (jasonObj.indexOf(JsonKey.AMOUNT_ADJUST)!=-1){
                                upWater = jsonObj.get(JsonKey.AMOUNT_ADJUST).toString();
                            }
                            if(jasonObj.indexOf(JsonKey.PRIORITY)!=-1){
                                priority=jsonObj.getString(JsonKey.PRIORITY).toString();
                            }
                            //boolean waterStatus = jsonObj.getBoolean(JsonKey.WATER);
                            //String showerIcon = jsonObj.get(JsonKey.PRIORITY).toString();
                            heterSt=heaterStatus;
                            if (heterSt.equals("on")) {
                                //smartHome.setBackgroundResource(R.drawable.smart_default);
                                shower.setBackgroundResource(R.drawable.shower_default);
                                //watering.setBackgroundResource(R.drawable.up_water_default);
                                fire.setBackgroundResource(R.drawable.fire_default);
                               // OL.setBackgroundResource(R.drawable.ol_default);
                                tmicon.setBackgroundResource(R.drawable.tem_default);
                                switchIcon.setBackground(getResources().getDrawable(R.drawable.switc));
                                decrease.setBackgroundResource(R.drawable.decrease);
                                add.setBackgroundResource(R.drawable.add);
                                temperature.setTextColor(Color.WHITE);

                                if (priority.equals("off")){
                                     model=false;
                                     shower.setBackgroundResource(R.drawable.shower_default);
                                }else if (priority.equals("on")){
                                     model=true;
                                    shower.setBackground(getResources().getDrawable(R.drawable.shower_selected));
                                }

                                if (currentTp!=null){
                                    currentTemp =Integer.parseInt(currentTp);
                                    temperature.setText(String.valueOf(currentTemp));
                                }
                                if (fireIcon.equals("on")) {
                                    fire.setBackgroundResource(R.drawable.fire_selected);
                                } else {
                                    fire.setBackgroundResource(R.drawable.fire_default);
                                }
                                /*if (upWater!=null){
                                    if (upWater.equals("inc") || upWater.equals("dec")) {
                                        watering.setBackgroundResource(R.drawable.up_water_selected);
                                        OL.setBackgroundResource(R.drawable.ol_selected);
                                    } else {
                                        watering.setBackgroundResource(R.drawable.up_water_default);
                                        OL.setBackgroundResource(R.drawable.ol_default);
                                    }
                                }else{
                                    watering.setBackgroundResource(R.drawable.up_water_default);
                                    OL.setBackgroundResource(R.drawable.ol_default);
                                }*/

                            } else {
                                //smartHome.setBackgroundResource(R.drawable.smart_gray);
                                shower.setBackgroundResource(R.drawable.shower_gray);
                                //watering.setBackgroundResource(R.drawable.up_water_gray);
                                fire.setBackgroundResource(R.drawable.fire_gray);
                                //OL.setBackgroundResource(R.drawable.ol_gray);
                                tmicon.setBackgroundResource(R.drawable.tem_gray);
                                switchIcon.setBackground(getResources().getDrawable(R.drawable.switch_selected));
                                decrease.setBackgroundResource(R.drawable.decrease_selected);
                                add.setBackgroundResource(R.drawable.add_selected);
                                temperature.setTextColor(Color.GRAY);
                                if (currentTp!=null){
                                    temperature.setText(String.valueOf(currentTemp));
                                }

                            }

                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(HeaterActivity.this, "解析Json出错，有可能是Json格式有误。", Toast.LENGTH_SHORT).show();
                }

            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onClick(View view) {
        if (model){
            if (view.getId()==decrease.getId()){
                if (heterSt.equals("on")){
                    int temp=currentTemp-1;
                    currentTemp=temp;
                    String tempStr="";
                    if (temp<=0){
                        temp=0;
                        tempStr=String.valueOf(temp);
                    }else{
                        tempStr=String.valueOf(temp);
                    }
                    String str;
                    try {
                        str = new JSONObject("{'cmd':'write','function':{'temp_sub':'"+tempStr+"'}}").toString();
                        myApplication.mqttManeger.pub(str,mac);
                        Toast.makeText(HeaterActivity.this,"设置温度为:"+tempStr,Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (view.getId()==add.getId()){
                if (heterSt.equals("on")){
                    int temp=currentTemp+1;
                    currentTemp=temp;
                    String tempStr="";
                    if (temp>=70){
                        temp=70;
                        tempStr=String.valueOf(temp);
                    }else{
                        tempStr=String.valueOf(temp);
                    }
                    String str;
                    try {
                        str = new JSONObject("{'cmd':'write','function':{'temp_add':'"+tempStr+"'}}").toString();
                        myApplication.mqttManeger.pub(str,mac);
                        Toast.makeText(HeaterActivity.this,"设置温度为:"+tempStr,Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
            if (view.getId()==switchIcon.getId()){
                if (heterSt.equals("on")){
                    String str;
                    try {
                        str=new JSONObject("{'cmd':'write','function':{'power':'off'}}").toString();
                        myApplication.mqttManeger.pub(str,mac);
                        if (heterSt.equals("on")){
                            heterSt="off";
                        }else{
                            heterSt="on";
                        }
                        Toast.makeText(HeaterActivity.this,"热水器状态:"+"关闭",Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    String str;
                    try {
                        str=new JSONObject("{'cmd':'write','function':{'power':'on'}}").toString();
                        myApplication.mqttManeger.pub(str,mac);
                        if (heterSt.equals("on")){
                            heterSt="off";
                        }else{
                            heterSt="on";
                        }
                        Toast.makeText(HeaterActivity.this,"热水器状态:"+"开启",Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


            }
        }else{
            JSONObject jsonObject=new JSONObject();
            if (view.getId()==decrease.getId()){
                try {
                    String payload=jsonObject.put("cmd","read").toString();
                    myApplication.mqttManeger.pub(payload, mac);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (view.getId()==add.getId()){
                try {
                    String payload=jsonObject.put("cmd","read").toString();
                    myApplication.mqttManeger.pub(payload, mac);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            if (view.getId()==switchIcon.getId()){
                if (heterSt.equals("on")){
                    try {
                        String payload=jsonObject.put("cmd","read").toString();
                        myApplication.mqttManeger.pub(payload, mac);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                      String  str=new JSONObject("{'cmd':'write','function':{'power':'on'}}").toString();
                        myApplication.mqttManeger.pub(str,mac);
                        if (heterSt.equals("on")){
                            heterSt="off";
                        }else{
                            heterSt="on";
                        }
                        Toast.makeText(HeaterActivity.this,"热水器状态:"+"开启",Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        pullDeviceState();
    }

    //进入界面的时候拉取设备的状态
    private void pullDeviceState(){
        JSONObject jsonObject=new JSONObject();
        try {
         String payload=jsonObject.put("cmd","read").toString();
            myApplication.mqttManeger.pub(payload, mac);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
