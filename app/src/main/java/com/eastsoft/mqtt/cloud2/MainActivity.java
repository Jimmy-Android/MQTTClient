package com.eastsoft.mqtt.cloud2;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.eastsoft.mqtt.cloud2.model.Gradevin;
import com.eastsoft.mqtt.cloud2.model.MqttData;
import com.eastsoft.mqtt.cloud2.util.ActionUtil;
import com.eastsoft.mqtt.cloud2.util.CmdUtil;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.MqttException;


public class MainActivity extends Activity implements OnClickListener {

    private TextView textLight, textDew, textLock, textRecLc, textRectLd, textRectFrt, textRecWin, textSetLc, textSetLd;
    private EditText editLc, editLd;
    private Button btnSetLc, btnSetLd, btnOkLc, btnOkLd;
    private View lylcSet, lyLdSet;
    private MqttManeger mqttManeger;
    private BroadcastReceiver r1,r2;

    public   String pubTopic = "eastsoft/things/112233445566/cmd";
    public   String subTopic = "eastsoft/things/112233445566";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gradevin_layout);
        initView();



        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActionUtil.ACTION_RECE_DATA);
        r2=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                String data = bundle.getString(ActionUtil.DATA);
                Gson gson = new Gson();
                MqttData mqttData = gson.fromJson(data, MqttData.class);
                String payload = mqttData.getPayload();
                Gradevin gradevin = gson.fromJson(payload, Gradevin.class);
                if (gradevin.getCmd() == null || gradevin.getCmd().equals("")) {
                    return;
                }
                int temp = gradevin.getDt() - 38;
                if (gradevin.getCmd().equals(CmdUtil.CMD_LIGHT)) {
                    textLight.setCompoundDrawables(null, null, gradevin.getDt() == 0 ? getResources().getDrawable(R.drawable.i72)
                            : getResources().getDrawable(R.drawable.i64), null);

                } else if (gradevin.getCmd().equals(CmdUtil.CMD_DEW)) {
                    textDew.setCompoundDrawables(null, null, gradevin.getDt() == 0 ? getResources().getDrawable(R.drawable.i72)
                            : getResources().getDrawable(R.drawable.i64), null);
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_LOCK)) {
                    textLight.setCompoundDrawables(null, null, gradevin.getDt() == 0 ? getResources().getDrawable(R.drawable.i72)
                            : getResources().getDrawable(R.drawable.i64), null);
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_ERFRT)) {
                    textRectFrt.setText("E4");
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_ERLC)) {
                    textRecLc.setText("E3");
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_RECWIN)) {
                    textRecWin.setText("E2");
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_ERLD)) {
                    textRectLd.setText("E1");
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_RECFRT)) {
                    textRectFrt.setText(temp + "");
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_RECLC)) {
                    textRecLc.setText(temp + "");
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_RECWIN)) {
                    textRecWin.setText(temp + "");
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_ERLD)) {
                    textRectLd.setText(temp + "");
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_SETLC)) {
                    btnSetLc.setText("设置温度" + temp);
                } else if (gradevin.getCmd().equals(CmdUtil.CMD_SETLD)) {
                    btnOkLd.setText("设置温度" + temp);
                }
            }
        };
        registerReceiver(r2, intentFilter);

    }


    private void initView() {
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

        lylcSet = findViewById(R.id.ly_set_lc);
        lyLdSet = findViewById(R.id.ly_set_ld);
        editLd = (EditText) findViewById(R.id.edit_ld);
        editLc = (EditText) findViewById(R.id.edit_lc);

        textLock.setOnClickListener(this);
        textDew.setOnClickListener(this);
        textLight.setOnClickListener(this);

        textRectLd.setOnClickListener(this);
        textRecWin.setOnClickListener(this);
        textRecLc.setOnClickListener(this);
        textRectFrt.setOnClickListener(this);

        btnSetLd.setOnClickListener(this);
        btnSetLc.setOnClickListener(this);
        btnOkLd.setOnClickListener(this);
        btnOkLc.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (!mqttManeger.client.isConnected()) {
            Toast.makeText(MainActivity.this, "连接已经断开", Toast.LENGTH_SHORT).show();
            return;
        }
        String cmd = "";
        int data = 0;
        switch (v.getId()) {
            case R.id.lock:
                cmd = CmdUtil.CMD_LOCK;
                if (textLock.getTag().equals("lock")) {
                    data = 0;
                    textLock.setTag("unLock");
                } else {
                    textLock.setTag("lock");
                    data = 1;
                }
                break;
            case R.id.dew:
                if (textLock.getTag().equals("lock")) {
                    Toast.makeText(MainActivity.this, "请先解锁，再操作", Toast.LENGTH_SHORT).show();
                    return;
                }
                cmd = CmdUtil.CMD_DEW;
                if (textDew.getTag().equals("on")) {
                    data = 0;
                    textDew.setTag("off");
                } else {
                    textDew.setTag("on");
                    data = 1;
                }
                break;
            case R.id.light:
                if (textLock.getTag().equals("lock")) {
                    Toast.makeText(MainActivity.this, "请先解锁，再操作", Toast.LENGTH_SHORT).show();
                    return;
                }
                cmd = CmdUtil.CMD_LIGHT;
                if (textLight.getTag().equals("on")) {
                    textLight.setTag("off");
                    data = 0;
                } else {
                    textLight.setTag("on");
                    data = 1;
                }
                break;
            case R.id.btn_set_temp_lc:
                if (textLock.getTag().equals("lock")) {
                    Toast.makeText(MainActivity.this, "请先解锁，再操作", Toast.LENGTH_SHORT).show();
                    return;
                }

                lylcSet.setVisibility(View.VISIBLE);

                break;
            case R.id.btn_set_temp_ld:
                if (textLock.getTag().equals("lock")) {
                    Toast.makeText(MainActivity.this, "请先解锁，再操作", Toast.LENGTH_SHORT).show();
                    return;
                }
                lyLdSet.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_set_temp_lc_ok:
                if (textLock.getTag().equals("lock")) {
                    Toast.makeText(MainActivity.this, "请先解锁，再操作", Toast.LENGTH_SHORT).show();
                    return;
                }
                cmd = CmdUtil.CMD_SETLC;
                data = 38 + Integer.parseInt(getSetLcText());
                lylcSet.setVisibility(View.INVISIBLE);
                btnSetLc.setText("设置温度" + getSetLcText() + "℃");

                break;
            case R.id.btn_set_temp_ld_ok:
                if (textLock.getTag().equals("lock")) {
                    Toast.makeText(MainActivity.this, "请先解锁，再操作", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!getSetLdText().contains("-")) {
                    Toast.makeText(MainActivity.this, "输入不合法，请重新输入", Toast.LENGTH_SHORT).show();
                }
                cmd = CmdUtil.CMD_SETLD;
                data = 38 - Integer.parseInt(getSetLdText().substring(1));

                lyLdSet.setVisibility(View.INVISIBLE);
                btnSetLd.setText("设置温度" + getSetLdText() + "℃");
                break;
        }
        if (cmd.equals("")) {
            return;
        }
        Gradevin gradevin = new Gradevin(cmd, data);
        Gson gson = new Gson();
        String payload = gson.toJson(gradevin);
//        mqttManeger.pub(MainActivity.this, payload);
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
        mqttManeger.disConnect();
        unregisterReceiver(r1);
        unregisterReceiver(r2);

    }

}
