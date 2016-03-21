package com.eastsoft.mqtt.cloud2.Device_6364;

/**
 * Created by Admin on 2015/11/30.
 */
public class DataConvert {
    public static String bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            ret += hex;
        }
        ret = ret.toUpperCase();
        return ret;
    }
}
