package com.eastsoft.mqtt.cloud2.Protocal;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.IllegalFormatCodePointException;

/**
 * Created by Admin on 2015/11/28.
 */
public class MqttManeger {
    public enum CloudStatus {
        unConnected, connected
    }

    private static final String UTF_8 = "UTF-8";


    //hearder  固定头部+可变头部+消息体
    //bit        7|6|5|4|        3|       2|1|    0
    //byte1  messageType| Dup flag| Qos level| Retain
    //byte2             Remaining Length

    public static final short keepAlive =2*60*60;//单位是秒 2小时

    //heard1
    public static final String MQTT_RESERVL = "0000";//保留
    public static final String MQTT_CONNECT = "0001";//请求连接
    public static final String MQTT_CONNACK = "0010";//请求应答

    public static final String MQTT_PUBLISH = "0011";//发布消息
    public static final String MQTT_PUBACK = "0100";//发布应答

    public static final String MQTT_PUBREC = "0101";//发布已接受，保证传递1
    public static final String MQTT_PUBREL = "0110";//发布释放，保证传递2
    public static final String MQTT_PUBCOMP = "0111";//发布完成，保证传递3

    public static final String MQTT_SUBSCRIBE = "1000";//订阅请求
    public static final String MQTT_SUBACK = "1001";//订阅应答

    public static final String MQTT_UNSUBSCRIBE = "1010";//取消订阅
    public static final String MQTT_UNSUBACK = "1011";//取消订阅应答

    public static final String MQTT_PING = "1100";//PING请求
    public static final String MQTT_PINGACK = "1101";//PING回复

    public static final String MQTT_DISCONNECT = "1110";//断开连接


    public static final String MQTT_DUP = "1";//客户端或服务器重发publish pubrel subscribe unsubscribe  适用于QOS>0 可变头部有消息ID
    public static final String MQTT_UNDUP = "0";

    public static final String MQTT_QOS_0 = "00";
    public static final String MQTT_QOS_1 = "01";

    public static final String MQTT_RETAIN = "1";
    public static final String MQTT_UNRETAIN = "0";
    //heard2  length

    //mqtt_connect

    //variable header
    //variable header
    public static final String PROTOCALNAME = "MQTT";
    public static final byte PROTOCALVERSION = 4;
    public static byte[] connect(String clientId, String userName, String psd) {

        byte b1 = (byte) Integer.parseInt(MQTT_CONNECT + MQTT_UNDUP + MQTT_QOS_0 + MQTT_UNRETAIN, 2);

        //variable header
        ByteBuffer byteBuffer = ByteBuffer.allocate(127);
        try {
            byteBuffer.putShort((short) PROTOCALNAME.getBytes(UTF_8).length);
            byteBuffer.put(PROTOCALNAME.getBytes(UTF_8));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        byteBuffer.put(PROTOCALVERSION);

        byte connecFlag = (byte) Integer.parseInt("11000010", 2);
        byteBuffer.put(connecFlag);
        byteBuffer.putShort(keepAlive);

        //payload
        try {
            byte[] clientIdBytes = clientId.getBytes(UTF_8);
            byteBuffer.putShort((short) clientIdBytes.length);
            byteBuffer.put(clientIdBytes);

            if(userName!=null&&!userName.equals("")){

                byte[] nameBytes = userName.getBytes(UTF_8);
                byteBuffer.putShort((short) nameBytes.length);
                byteBuffer.put(nameBytes);

                byte[] psdBytes = psd.getBytes(UTF_8);
                byteBuffer.putShort((short) psdBytes.length);
                byteBuffer.put(psdBytes);
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byteBuffer.flip();
        ByteBuffer data = ByteBuffer.allocate(byteBuffer.limit() + 2);
        data.put(b1);
        data.put((byte) byteBuffer.limit());
        data.put(byteBuffer);
//        System.out.println("connect" + getStr(data.array()));

        return data.array();
    }

    public static ReceiveCloudData handleReceivedCloudData(byte[] data) {

        ReceiveCloudData receiveCloudData = new ReceiveCloudData();

        int b1=data[0]&0xff;
        b1=b1>>4;
        if (b1 == 2) {//连接
            receiveCloudData.receiveMessageEnum = ReceivedMessageTypeEnum.connect;
            if (data.length >= 4) {
                byte reCode = data[3];
                if (reCode == 0) {
                    receiveCloudData.connectFlag = true;
                } else {
                    receiveCloudData.connectFlag = false;
                }
            }
//    		System.out.println("connect"+Arrays.toString(data.array()));

        } else if (b1 == 13) {//
            receiveCloudData.receiveMessageEnum = ReceivedMessageTypeEnum.ping;
            receiveCloudData.connectFlag = true;
        } else if (b1 == 3) {

            receiveCloudData.receiveMessageEnum = ReceivedMessageTypeEnum.publishData;
            ByteBuffer byteBuffer=ByteBuffer.wrap(data);
            byteBuffer.get();
            byteBuffer.get();
            short topicLen=byteBuffer.getShort();
            byte[] topicBytes=new byte[topicLen];
            byteBuffer.get(topicBytes);
            receiveCloudData.topicName=new String(topicBytes);

            int payloadLen=byteBuffer.limit()-byteBuffer.position();
            byte[] payloadBytes=new byte[payloadLen];
            byteBuffer.get(payloadBytes);
            receiveCloudData.message=new String(payloadBytes);

        }else if(b1==9){
            receiveCloudData.receiveMessageEnum = ReceivedMessageTypeEnum.suback;

        }else if(b1==8){//63 订阅
            receiveCloudData.receiveMessageEnum = ReceivedMessageTypeEnum.subscribe;
            ByteBuffer byteBuffer=ByteBuffer.wrap(data);
            byteBuffer.get();
            byteBuffer.get();
            byteBuffer.getShort();
            short len=byteBuffer.getShort();
            byte[] subBytes=new byte[len];
            byteBuffer.get(subBytes);
            receiveCloudData.topicName=new String(subBytes);
        }

        return receiveCloudData;
    }

    public static byte[] publish(int qos, String topicName, String paylod) {
        String qosStr = MQTT_QOS_0;
        if (qos == 1) {
            qosStr = MQTT_QOS_1;
        }
        byte b1 = (byte) Integer.parseInt(MQTT_PUBLISH + MQTT_UNDUP + qosStr + MQTT_RETAIN, 2);

//        int len=2+
        ByteBuffer byteBuffer = null;
        try {
            byte[] topic = topicName.getBytes(UTF_8);
            byte[] paylodBytes = paylod.getBytes(UTF_8);

            int len=topic.length+2+paylodBytes.length;
//            int len=2+topic.length+2+paylodBytes.length;
            byteBuffer= ByteBuffer.allocate(len+1+1);
            byteBuffer.put(b1);
            byteBuffer.put((byte) len);

            byteBuffer.putShort((short) topic.length);
            byteBuffer.put(topic);

//            byteBuffer.putShort((short) paylodBytes.length);
            byteBuffer.put(paylodBytes);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        if (messageId != -1) {
//            byteBuffer.putShort(messageId);
//        }
//        System.out.println("publish"+getStr(byteBuffer.array()));

        return byteBuffer.array();

    }
    public static byte[] subscribe(int qos, String topicName,short messageId) {
        byte b1 = (byte) Integer.parseInt(MQTT_SUBSCRIBE + MQTT_UNDUP + MQTT_QOS_1 + MQTT_UNRETAIN, 2);
        byte[] topicBytes = null;
        try {
            topicBytes= topicName.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        int len=2+2+topicBytes.length+ 1;
        ByteBuffer data = ByteBuffer.allocate(len+ 2);
        data.put(b1);
        data.put((byte) (len));
        data.putShort(messageId);
        data.putShort((short) topicBytes.length);
        data.put(topicBytes);
        data.put((byte) qos);
//        System.out.println("sub"+getStr(data.array()));

        return data.array();
    }

    public static byte[] ping() {
        byte[] data = new byte[2];
        data[0] = (byte) Integer.parseInt(MQTT_PING + MQTT_UNDUP + MQTT_QOS_0 + MQTT_UNRETAIN, 2);
        data[1] = 0;
        return data;
    }

    public static byte[] disconnect(){
        byte[] data = new byte[2];
        data[0] = (byte) Integer.parseInt(MQTT_DISCONNECT + MQTT_UNDUP + MQTT_QOS_0 + MQTT_UNRETAIN, 2);
        data[1] = 0;
        return data;
    }

    public static class ReceiveCloudData {
        public ReceivedMessageTypeEnum receiveMessageEnum = ReceivedMessageTypeEnum.other;
        public boolean connectFlag = false;

        public String topicName;
        public String message;
    }

    public enum ReceivedMessageTypeEnum {
        connect, ping, publishData,suback,subscribe,other;
    }

}
