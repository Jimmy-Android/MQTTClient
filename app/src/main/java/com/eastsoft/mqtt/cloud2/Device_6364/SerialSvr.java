package com.eastsoft.mqtt.cloud2.Device_6364;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.D2xxManager.D2xxException;
import com.ftdi.j2xx.FT_Device;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

/**
 * 绑定服务器的方式，实现 Serial串口 通信.
 *
 * @author xuky
 */

public class SerialSvr extends Service {

    public static final int CONNETED = 0;
    public static final int UNCONNECT = 1;
    public static final int RECEIVEDATE = 2;

    // 串口对象变量
    FT_Device ftDev = null;

    // 通信控件的参数
    int baudRate = 115200; /* baud rate */
    byte stopBit = 1; /* 1:1stop bits, 2:2 stop bits */
    byte dataBit = 8; /* 8:8bit, 7: 7bit */
    byte parity = 2; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
    byte flowControl = 0; /* 0:none, 1: flow control(CTS,RTS) */
    int portNumber = 0; /* port number */

    Context CommInformationContext;

    int readLength = 2048;

    // 当前工作状况
    // 串口是否进行了参数设置，false表示，建立了连接，但是没有参数设置
    boolean uart_configured = false;

    final int SERIAL_CLOSE = 0;
    final int SERIAL_OPEN = 1;

    // 当前状态 0 关闭，1打开，默认是关闭状态
    int currentstatus = SERIAL_CLOSE;

    // 定义onBinder方法所返回的对象
    private MyBinder binder = new MyBinder();
    public static String tag = "test6364";

    // 通过继承Binder来实现IBinder类 相当于内部类的方式实现多重继承？
    public class MyBinder extends Binder // ①
    {
        // 外部可以调用的方法写在这里

        // 定义发送方法
        public void write(byte[] bytes) {
            Log.d(tag, "seri send" + Activity6463.getStr(bytes));
            new WriteToServerTask().execute(bytes);

        }

        // 定义断开socket方法
        public void close() {
            // 异步方式退出
            new CloseSocketTask().execute();
        }

    }

    // 内部类，向服务器发送数据（后台进行，异步执行，不会锁住）
    // 继承了AsyncTask,是android提供的轻量级的异步类
    private class WriteToServerTask extends AsyncTask<byte[], Void, Void> {
        //		private int len;
        public WriteToServerTask() {
//		this.len=len;
        }


        protected Void doInBackground(byte[]... data) {

            // 判断串口的状态
            if (ftDev == null || !ftDev.isOpen()) {
                Log.e("j2xx", "SendMessage: device not open");
                return null;
            }
            try {
                ftDev.setLatencyTimer((byte) 16);
                // 写数据
                ftDev.write(data[0], data[0].length);
            } catch (Exception ex) {
                // 如果写输出出现错误，执行断开连接操作
//				disconnect();
            }
            return null;
        }

    }

    // 内部类，停止SOCKET（后台进行，异步执行，不会锁住）
    // 继承了AsyncTask,是android提供的轻量级的异步类
    private class CloseSocketTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }
    }

    // 必须实现的方法，绑定该Service时回调该方法
    @Override
    public IBinder onBind(Intent intent) {

        System.out.println("Service is Binded");

        if (!initService())
            return binder;

        // 启动一个线程、动态地修改count状态值，接收数据并将消息转发下去
        new Thread() {

            @Override
            public void run() { // ---buffer store for the stream---
                // ---keep listening to the InputStream until an
                // exception occurs---
                while (true) {

                    // 在线程中执行的时候，在此暂停一下，降低CPU占用率
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }

                    // 如果未连接到服务器，创建连接
                    if (ftDev == null) {
                        initService();
                    }

                    // 获取 Queue（缓冲区大小） 信息，如果小于0，不执行
                    int iavailable = ftDev.getQueueStatus();
                    if (iavailable > 0) {

                        // 如果缓冲区数据太多，不全部接收，只接收设定大小的数据
                        if (iavailable > readLength) {
                            iavailable = readLength;
                        }

                        // 接收数据的长度为iavailable（实际大小，或是最大设定值）
                        byte[] readData;
                        readData = new byte[readLength];
                        ftDev.read(readData, iavailable);

                        // 实际接收到的数据
                        byte[] currReceiveByte = null;
                        currReceiveByte = new byte[iavailable];

                        // 将实际接收到的数据放到数组中
                        for (int i = 0; i < iavailable; i++) {
                            currReceiveByte[i] = readData[i];
                        }

                        // 如果接收到了数据，进行如下处理
                        if (currReceiveByte != null) {
                            String msg = Activity6463.getStr(currReceiveByte);

                            Log.d(tag, "seri receive" + msg);
                            Intent intent = new Intent("RECVFRAME_ACTION");
                            Bundle bundle = new Bundle();
                            bundle.putString("recv", msg);
                            bundle.putByteArray("recvbyte", currReceiveByte);
                            intent.putExtras(bundle);
                            sendBroadcast(intent);

                        }
                    }
                }
            }
        }.start();

        // 返回IBinder对象
        return binder;
    }

    public void SetConfig(int baud, byte dataBits, byte stopBits, byte parity,
                          byte flowControl) {

        if (ftDev == null || !ftDev.isOpen()) {
            Log.e("j2xx", "SetConfig: device not open");
            return;
        }

        // configure our port
        // reset to UART mode for 232 devices
        // 设置串口为RS232模式
        ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
        // 设置波特率
        ftDev.setBaudRate(baud);
        // 设置数据位
        switch (dataBits) {
            case 7:
                dataBits = D2xxManager.FT_DATA_BITS_7;
                break;
            case 8:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
            default:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
        }
        // 设置停止位
        switch (stopBits) {
            case 1:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
            case 2:
                stopBits = D2xxManager.FT_STOP_BITS_2;
                break;
            default:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
        }
        // 设置校验方式
        switch (parity) {
            case 0:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
            case 1:
                parity = D2xxManager.FT_PARITY_ODD;
                break;
            case 2:
                parity = D2xxManager.FT_PARITY_EVEN;
                break;
            case 3:
                parity = D2xxManager.FT_PARITY_MARK;
                break;
            case 4:
                parity = D2xxManager.FT_PARITY_SPACE;
                break;
            default:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
        }
        // 向设备中写入通信参数
        ftDev.setDataCharacteristics(dataBits, stopBits, parity);

        // 设置流控方式
        short flowCtrlSetting;
        switch (flowControl) {
            case 0:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
            case 1:
                flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
                break;
            case 2:
                flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
                break;
            case 3:
                flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
                break;
            default:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
        }

        // TODO : flow ctrl: XOFF/XOM
        ftDev.setFlowControl(flowCtrlSetting, (byte) 0x0b, (byte) 0x0d);

        // 参数设置完成，标记标志
        uart_configured = true;
        Toast.makeText(this, "Config done", Toast.LENGTH_SHORT).show();
    }

    private boolean initService() {

        // 获取设备信息
        D2xxManager ftdid2xx = null;
        try {
            ftdid2xx = D2xxManager.getInstance(this);
        } catch (D2xxException e) {
            e.printStackTrace();
        }

        int devCount = ftdid2xx.createDeviceInfoList(this);

        // Toast.makeText(this, "openSerial devCount:" + devCount,
        // Toast.LENGTH_LONG).show();

        int portIndex = 0;
        if (devCount > 0) {
            // 判断设备是否RS232设备
            D2xxManager.FtDeviceInfoListNode[] deviceList = new D2xxManager.FtDeviceInfoListNode[devCount];
            ftdid2xx.getDeviceInfoList(devCount, deviceList);
            if (deviceList[0].type == D2xxManager.FT_DEVICE_2232H)
//				Toast.makeText(this, "device type = FT2232H ",
//						Toast.LENGTH_LONG).show();

                portIndex = deviceList[0].iSerialNumber;

            try {
                ftdid2xx = D2xxManager.getInstance(this);
            } catch (D2xxException e) {
                e.printStackTrace();
            }
            ftDev = ftdid2xx.openByIndex(this, portIndex);
        }
        int tmpProtNumber = portIndex + 1;

        boolean flag = false;
        try {
            // 根据串口的状态信息，进行操作
            if (currentstatus == SERIAL_CLOSE) {
                if (ftDev == null) {
                    // 1、连接方式1 建立与某个端口的串口的连接ftdid2xx.openByIndex
                    ftDev = ftdid2xx.openByIndex(null, portNumber);
                    // 2、连接方式2 添加了连接参数的“与某个端口的串口建立连接”
                    // ftDev =
                    // ftdid2xx.openByIndex(CommInformationContext,portNumber,
                    // d2xxDrvParameter);
                } else {
                    synchronized (ftDev) {
                        // ftDev = ftdid2xx.openByIndex(this,portNumber);
                    }
                    // 建立了连接，但是没有进行参数设置
                    uart_configured = false;
                }
            } else {
                // 提示用户，当前串口已经连接了
//				Toast.makeText(this,
//						"Device port " + tmpProtNumber + " is already opened",
//						Toast.LENGTH_LONG).show();
                return flag;
            }

            // 如果经过了前面的步骤，串口依然是null状态或是not isOpen，表示串口打开NG
            // NG表示没有通过
            if (ftDev == null || !ftDev.isOpen()) {
                Toast.makeText(
                        this,
                        "open device port(" + tmpProtNumber
                                + ") NG, ftDev == null", Toast.LENGTH_LONG)
                        .show();
                return flag;
            }
            // 串口正常打开了
            currentstatus = SERIAL_OPEN;
            flag = true;
//			Toast.makeText(this, "open device port(" + tmpProtNumber + ") OK",
//					Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            return flag;
        }
        SetConfig(baudRate, dataBit, stopBit, parity, flowControl);

        return flag;
    }

    // Service被创建时回调该方法。
    @Override
    public void onCreate() {
        super.onCreate();

    }

    // Service被断开连接时回调该方法
    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("Service is Unbinded");
        return true;
    }

    // Service被关闭之前回调该方法。
    @Override
    public void onDestroy() {
        super.onDestroy();
        new CloseSocketTask().execute();
        System.out.println("Service is Destroyed");
    }

}