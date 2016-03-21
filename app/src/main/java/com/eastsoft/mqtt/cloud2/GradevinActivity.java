package com.eastsoft.mqtt.cloud2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.eastsoft.mqtt.cloud2.model.Gradevin;
import com.eastsoft.mqtt.cloud2.model.MqttData;
import com.eastsoft.mqtt.cloud2.util.ActionUtil;
import com.eastsoft.mqtt.cloud2.util.CmdUtil;
import com.eastsoft.mqtt.cloud2.util.CustomNumberPicker;
import com.eastsoft.mqtt.cloud2.util.HeaterActivity;
import com.google.gson.Gson;

/**
 * Created by ll on 2015/12/3.
 */
public class GradevinActivity extends BaseActivity implements View.OnClickListener {

    private TextView textLight, textDew, textLock, textRecLc, textRectLd, textRectFrt, textRecWin, textSetLc, textSetLd;
    private EditText editLc, editLd;
    private Button jump, btnSetLc, btnSetLd, btnOkLc, btnOkLd, btnLcAdd, btnLcJian, btnLdAdd, btnLdJian, jumpToHeater;
    private View lylcSet, lyLdSet;
    private String strSetTempLd, strSetTemLc;
    private MyApplication myApplication;
    private String mac = "112233445566";
    private BroadcastReceiver r2;
    private CustomNumberPicker pickerLc, pickerLd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        setContentView(R.layout.gradevin_layout);
        initView();
        myApplication = (MyApplication) getApplication();

        //myApplication.mqttManeger.subscribe(mac);
        myApplication.mqttManeger.subscribe();
        Toast.makeText(GradevinActivity.this, "已经订阅", Toast.LENGTH_LONG).show();
//        register1();

        registReceiver();
    }

//    private void register1() {
//        IntentFilter intentFilter1 = new IntentFilter();
//        intentFilter1.addAction(ActionUtil.ACTION_CONNECT_RESULT);
//        BroadcastReceiver r1 = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Bundle bundle = intent.getExtras();
//                Boolean result = bundle.getBoolean(ActionUtil.DATA);
//                if (result) {
//                    Toast.makeText(context, "连接成功", Toast.LENGTH_SHORT).show();
//                    myApplication.mqttManeger.subscribe(mac);
//                    Toast.makeText(GradevinActivity.this, "已经订阅", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(context, "连接失败，请重试", Toast.LENGTH_SHORT).show();
//
//                }
//            }
//        };
//        registerReceiver(r1, intentFilter1);
//    }

    private void registReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActionUtil.ACTION_RECE_DATA);
        r2 = new BroadcastReceiver() {
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
                Toast.makeText(GradevinActivity.this, "rec" + payload, Toast.LENGTH_LONG).show();
                Gradevin gradevin = gson.fromJson(payload, Gradevin.class);
                if (gradevin.getCmd() == null || gradevin.getCmd().equals("")) {
                    return;
                }
                int temp = gradevin.getDt() - 38;
                if (gradevin.getCmd().equals(CmdUtil.CMD_LIGHT)) {
                    Drawable a = getResources().getDrawable(R.drawable.lights_off);
                    a.setBounds(0, 0, a.getMinimumWidth(), a.getMinimumHeight());
                    Drawable a1 = getResources().getDrawable(R.drawable.lights_on);
                    a1.setBounds(0, 0, a1.getMinimumWidth(), a1.getMinimumHeight());

                    textLight.setCompoundDrawables(null, null, gradevin.getDt() == 0 ? a
                            : a1, null);
                    textLight.setTag(gradevin.getDt() == 1 ? "on" : "off");

                } else if (gradevin.getCmd().equals(CmdUtil.CMD_DEW)) {
                    Drawable a = getResources().getDrawable(R.drawable.remove_not);
                    a.setBounds(0, 0, a.getMinimumWidth(), a.getMinimumHeight());
                    Drawable a1 = getResources().getDrawable(R.drawable.remove);
                    a1.setBounds(0, 0, a1.getMinimumWidth(), a1.getMinimumHeight());

                    textDew.setCompoundDrawables(null, null, gradevin.getDt() == 0 ? a
                            : a1, null);

                    textDew.setTag(gradevin.getDt() == 1 ? "on" : "off");

                } else if (gradevin.getCmd().equals(CmdUtil.CMD_LOCK)) {
                    Drawable a = getResources().getDrawable(R.drawable.lockun);
                    a.setBounds(0, 0, a.getMinimumWidth(), a.getMinimumHeight());
                    Drawable a1 = getResources().getDrawable(R.drawable.lock_t);
                    a1.setBounds(0, 0, a1.getMinimumWidth(), a1.getMinimumHeight());
                    textLock.setCompoundDrawables(null, null, gradevin.getDt() == 0 ? a
                            : a1, null);
                    textLock.setTag(gradevin.getDt() == 1 ? "lock" : "unlock");

                } else if (gradevin.getCmd().equals(CmdUtil.CMD_ERFRT)) {
                    textRectFrt.setText("E4");
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_ERLC)) {
                    textRecLc.setText("E3");
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_ERWIN)) {
                    textRecWin.setText("E2");
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_ERLD)) {
                    textRectLd.setText("E1");
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_RECFRT)) {
                    textRectFrt.setText(temp + "");
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_RECLC)) {
                    textRecLc.setText(temp + "");
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_RECWIN)) {
                    textRecWin.setText(temp + "");
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_RECLD)) {
                    textRectLd.setText(temp + "");
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_SETLC)) {
                    strSetTemLc = temp + "";
                    btnSetLc.setText("设置温度:" + temp + "℃");
                    pickerLc.setValue(temp);
                    editLc.setText(temp + "");
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_SETLD)) {
                    strSetTempLd = temp + "";
                    btnSetLd.setText("设置温度:" + temp + "℃");
                    pickerLd.setValue(temp);
                    editLd.setText(String.valueOf(temp).substring(1));
                }

            }
        };
        registerReceiver(r2, intentFilter);
    }

    private void initView() {

        pickerLc = (CustomNumberPicker) findViewById(R.id.picker_lc);
        pickerLd = (CustomNumberPicker) findViewById(R.id.picker_ld);
        pickerLc.setMaxValue(15);
        pickerLc.setMinValue(1);
        try {
            pickerLc.setItemCount(4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            pickerLd.setItemCount(4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pickerLc.setValue(1);
        pickerLd.setValue(-21);
        textLock = (TextView) findViewById(R.id.lock);
        textDew = (TextView) findViewById(R.id.dew);
        textLight = (TextView) findViewById(R.id.light);

        textRectLd = (TextView) findViewById(R.id.text_temper_ld);
        textRecWin = (TextView) findViewById(R.id.text_temper_win);
        textRecLc = (TextView) findViewById(R.id.text_temper_lc);
        textRectFrt = (TextView) findViewById(R.id.text_temper_frt);

        btnSetLc = (Button) findViewById(R.id.btn_set_temp_lc);
        btnSetLd = (Button) findViewById(R.id.btn_set_temp_ld);

        btnOkLc = (Button) findViewById(R.id.btn_set_temp_lc_ok);
        btnOkLd = (Button) findViewById(R.id.btn_set_temp_ld_ok);
        btnLcAdd = (Button) findViewById(R.id.lc_add);
        btnLcJian = (Button) findViewById(R.id.lc_jian);
        btnLdAdd = (Button) findViewById(R.id.ld_add);
        btnLdJian = (Button) findViewById(R.id.ld_jian);

        lylcSet = findViewById(R.id.ly_set_lc);
        lyLdSet = findViewById(R.id.ly_set_ld);
        editLd = (EditText) findViewById(R.id.edit_ld);
        editLc = (EditText) findViewById(R.id.edit_lc);
        editLc.setText(1 + "");
        editLd.setText(21 + "");
        jumpToHeater = (Button) this.findViewById(R.id.jump_to_heater);


        textLock.setOnClickListener(this);
        textDew.setOnClickListener(this);
        textLight.setOnClickListener(this);

        textRectLd.setOnClickListener(this);
        textRecWin.setOnClickListener(this);
        textRecLc.setOnClickListener(this);
        textRectFrt.setOnClickListener(this);

        jumpToHeater.setOnClickListener(this);
        btnSetLd.setOnClickListener(this);
        btnSetLc.setOnClickListener(this);
        btnOkLd.setOnClickListener(this);
        btnOkLc.setOnClickListener(this);
        btnLdJian.setOnClickListener(this);
        btnLdAdd.setOnClickListener(this);
        btnLcJian.setOnClickListener(this);
        btnLcAdd.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!myApplication.mqttManeger.isConnected()) {
            Toast.makeText(GradevinActivity.this, "连接已经断开", Toast.LENGTH_SHORT).show();
            return;
        }
        String cmd = "";
        int data = 0;
        switch (v.getId()) {
            case R.id.jump_to_heater:
                Intent intent = new Intent(GradevinActivity.this, HeaterActivity.class);
                startActivity(intent);
            case R.id.lock:
                cmd = CmdUtil.CMD_LOCK;
                if (textLock.getTag().equals("lock")) {
                    data = 0;
//                    textLock.setTag("unLock");
                } else {
//                    textLock.setTag("lock");
                    data = 1;
                }
                break;
            case R.id.dew:
                if (textLock.getTag().equals("lock")) {
                    Toast.makeText(GradevinActivity.this, "请先解锁，再操作", Toast.LENGTH_SHORT).show();
                    return;
                }
                cmd = CmdUtil.CMD_DEW;
                if (textDew.getTag().equals("on")) {
                    data = 0;
//                    textDew.setTag("off");
                } else {
//                    textDew.setTag("on");
                    data = 1;
                }
                break;
            case R.id.light:
                if (textLock.getTag().equals("lock")) {
                    Toast.makeText(GradevinActivity.this, "请先解锁，再操作", Toast.LENGTH_SHORT).show();
                    return;
                }
                cmd = CmdUtil.CMD_LIGHT;
                if (textLight.getTag().equals("on")) {
//                    textLight.setTag("off");
                    data = 0;
                } else {
//                    textLight.setTag("on");
                    data = 1;
                }
                break;
            case R.id.btn_set_temp_lc:
                if (textLock.getTag().equals("lock")) {
                    Toast.makeText(GradevinActivity.this, "请先解锁，再操作", Toast.LENGTH_SHORT).show();
                    return;
                }

                lylcSet.setVisibility(View.VISIBLE);
                editLc.setText(strSetTemLc);

                break;
            case R.id.btn_set_temp_ld:
                if (textLock.getTag().equals("lock")) {
                    Toast.makeText(GradevinActivity.this, "请先解锁，再操作", Toast.LENGTH_SHORT).show();
                    return;
                }
                lyLdSet.setVisibility(View.VISIBLE);
                editLd.setText(strSetTempLd);
                break;
            case R.id.btn_set_temp_lc_ok:
                if (textLock.getTag().equals("lock")) {
                    Toast.makeText(GradevinActivity.this, "请先解锁，再操作", Toast.LENGTH_SHORT).show();
                    return;
                }
                cmd = CmdUtil.CMD_SETLC;
                data = 38 + pickerLc.getValue();
                lylcSet.setVisibility(View.INVISIBLE);
//                btnSetLc.setText("设置温度" + getSetLcText());

                break;
            case R.id.btn_set_temp_ld_ok:
                if (textLock.getTag().equals("lock")) {
                    Toast.makeText(GradevinActivity.this, "请先解锁，再操作", Toast.LENGTH_SHORT).show();
                    return;
                }

                cmd = CmdUtil.CMD_SETLD;
                data = 38 + pickerLd.getValue();

                lyLdSet.setVisibility(View.INVISIBLE);
//                btnSetLd.setText("设置温度-" + getSetLdText());
                break;
            case R.id.lc_add:
                if (textLock.getTag().equals("lock")) {
                    Toast.makeText(GradevinActivity.this, "请先解锁，再操作", Toast.LENGTH_SHORT).show();
                    return;
                }
                cmd = CmdUtil.CMD_SETLC;
                data = 38 + Integer.parseInt(getSetLcText()) + 1;
                if (Integer.parseInt(getSetLcText()) + 1 > 15) {
                    Toast.makeText(GradevinActivity.this, "已经是最大温度", Toast.LENGTH_SHORT).show();
                    return;
                }
//                editLc.setText(data+"");
                break;
            case R.id.lc_jian:
                if (textLock.getTag().equals("lock")) {
                    Toast.makeText(GradevinActivity.this, "请先解锁，再操作", Toast.LENGTH_SHORT).show();
                    return;
                }
                cmd = CmdUtil.CMD_SETLC;
                data = 38 + Integer.parseInt(getSetLcText()) - 1;
                if (Integer.parseInt(getSetLcText()) - 1 < 1) {
                    Toast.makeText(GradevinActivity.this, "已经是最小温度", Toast.LENGTH_SHORT).show();
                    return;
                }
//                editLc.setText(data+"");
                break;
            case R.id.ld_add:
                if (textLock.getTag().equals("lock")) {
                    Toast.makeText(GradevinActivity.this, "请先解锁，再操作", Toast.LENGTH_SHORT).show();
                    return;
                }
                cmd = CmdUtil.CMD_SETLD;
                data = 38 - Integer.parseInt(getSetLdText()) + 1;
                if (Integer.parseInt(getSetLdText()) - 1 < 12) {
                    Toast.makeText(GradevinActivity.this, "已经是最大温度", Toast.LENGTH_SHORT).show();
                    return;
                }
                int temp = -Integer.parseInt(getSetLdText()) + 1;
//                editLd.setText(temp+"");

                break;
            case R.id.ld_jian:
                if (textLock.getTag().equals("lock")) {
                    Toast.makeText(GradevinActivity.this, "请先解锁，再操作", Toast.LENGTH_SHORT).show();
                    return;
                }
                cmd = CmdUtil.CMD_SETLD;
                data = 38 - Integer.parseInt(getSetLdText()) - 1;
                if (Integer.parseInt(getSetLdText()) + 1 > 21) {
                    Toast.makeText(GradevinActivity.this, "已经是最小温度", Toast.LENGTH_SHORT).show();
                    return;
                }
                int temp2 = -Integer.parseInt(getSetLdText()) - 1;
//                editLd.setText(temp2+"");
                break;

        }
        if (cmd.equals("")) {
            return;
        }
        Gradevin gradevin = new Gradevin(cmd, data);
        Gson gson = new Gson();
        String payload = gson.toJson(gradevin);
        myApplication.mqttManeger.pub(payload, mac);
        Toast.makeText(GradevinActivity.this, "pub " + payload, Toast.LENGTH_LONG).show();

    }


    private String getSetLcText() {
        return editLc.getText().toString();
    }

    private String getSetLdText() {
        return editLd.getText().toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        myApplication.mqttManeger.disConnect();
    }



}


