package com.eastsoft.mqtt.cloud2;

/**
 * Created by Administrator on 2015/11/30 0030.
 */
public class MqttNativeInterface {

//    static {
//        System.loadLibrary("mqtt");
//    }
    //63
    public native byte[] sdk_get_ver_req();//读版本信息

    public native byte[] sdk_get_mac_req();//读mac地址

    public native byte[] sdk_get_ge_req();

    public native byte[] sdk_set_psk_req(byte[] psk, byte len);//设置密钥

    public native byte[] sdk_tx_plc_req(PlcData plcData);//64发送mqtt数据给63 或63发送mqtt数据至plc64

    public native byte[] sdk_search_att_resp_req(SearchResp searchResp);//63 回复设备属性搜索

    public native byte[] sdk_rmtpsk_resp_req(PskReq req);
//    {
//        u8			srcmod; 3
//        u8			dstmod;1
//        u8			prio;0
//        u8			dst[6];64 mac
//        u8			state; 0拒绝 1同意
//    } rmt_psk_resp_req_t;

    //64
    public native byte[] sdk_link_start_req(TcpData link);//开始连接

    //    {
//        byte type=02;
//        byte [] loc_ip;
//        short loc_port;
//        byte [] rmt_ip;
//        short rmt_port;
//    }
    public native byte[] sdk_tx_ge_req(TcpData mqttData);//向云发送数据

    public native byte[] sdk_search_start_req(Search search);//开始搜索

    public native byte[] sdk_rmtpsk_req_req(PskReq pskReq); //64请求改密钥

    public native ReceiveData sdk_recv(byte[] data, int len);

    public class PskReq {
        public byte srcmod = 1;
        public byte dstmod = 3;
        public byte prio = 0;
        public byte[] dstmac;//63mac
        public byte[] oldKey;
        public byte oldKeyLen;
        public byte[] newKey;
        public byte newKeyLen;
        public byte state = 1;
    }

    public class SearchResp {
        public byte srcmod = 3;
        public byte dstmod = 1;
        public byte state = 1;
        public byte[] src;//64的mac
        public byte taskid;//64的
        public byte[] data;//用户输入的63的data
        public byte len;
    }

    public class Search {
        public byte srcmod = 1;
        public byte dstmod = 3;
        public byte radius = 0;
        public byte rule = 0;
        public byte attrData[];
        public byte len;
    }

    public class TcpData {
        public byte type = 02;
        public byte[] data;
        public int len;
        public byte[] loc_ip;
        public short loc_port;
        public byte[] rmt_ip;
        public short rmt_port;

    }

    public class PlcData {
        public byte srcmod = 3;
        public byte dstmod = 1;
        public byte prio = 0;
        public byte multi = 0;
        public byte[] dst;//64mac
        public byte[] data;
        public int len;
    }

    public class DeviceInfo {
        public byte[] id46_mac;
        public byte id46_depth;
        public byte id46_netstate;//网络状态  00不在网 01 同属于自己网络 02 属于其他网络
        public byte id46_mode; //3-63/64  2-云 1网关
        public byte[] id46_attr;
        public byte id46_attrLen;
    }

    public class ReceiveData {
        public int totalLen;//==-1表示不正确
        public byte id;
        public byte status;

        //63   id=22 sdk_ver_t 获取版本信息返回
        public short id22_conf;//id==bit4=1 表示是64（版本信息，判断64、63）

        //63 id=23 读mac地址
        public byte[] id23_mac;

        public byte[] id28_ip;
        public byte[] id28_mask;
        public byte[] id28_gw;
        public byte[] id28_firdns;
        public byte[] id28_secdns;

        //64 id=43 sdk_ge_link_state_t
        public byte id43_status;//0-无法连接 1-连接丢失 2-连接完成
        public byte[] id43_locIp;
        public short id43_locPort;
        public byte[] id43_rmtIp;
        public short id43_rmtPort;

        // 64  id=44 网卡接收 sdk_ge_data_t
        public byte[] id44_data;
//        public int id44_len;

        //63 64 id=45 电力线接收 sdk_plc_rx_t
        public byte srcmod;//1 mqtt 3mesage 显示开关灯
        public byte[] id45_data;
//        public int id45_len;
        public byte dstmod;

        //64 id=46  sdk_search_indi_t
        public byte id46_cnt;//几个63
        public DeviceInfo[] deviceInfos;

        //63 id=47 sdk_search_att_req_indi_t
        public byte[] id47_srcMac;//64 mac 记住mac地址
        public byte id47_taskid;
        public byte[] id47_data;
//        public int id47_len;

        //63 id=48  sdk_rmtpsk_req_indi_t
        public byte id48_state;//==00？错误   ==01 ==02已经改过
        public byte[] id48_newKey;
        public byte[] id48_srcmac;
//        public int id48_newKeyLen;

        //64 id=49 sdk_rmtpsk_resp_indi_t
        public byte id49_statusPsk;//0 拒绝 1同意
    }

}
