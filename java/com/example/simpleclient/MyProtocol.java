package com.example.simpleclient;

/**
 * Created by acer on 2017/6/1.
 */

public class MyProtocol {
    public static byte CS_H = (byte)0x91;
    public static byte SC_H = (byte)0x11;
    public static byte LOGIN_L = (byte)0x01;
    public static byte SIGNUP_L = (byte)0x02;
    public static byte QRIDEN_L = (byte)0x04;
    public static byte LOGOUT_L = (byte)0x08;

    public static byte LOGIN_LEN = (byte)0x34;
    public static byte LOGIN_DATA_LEN = (byte)0x30;

    public static byte SIGNUP_LEN = (byte)0x34;
    public static byte SIGNUP_DATA_LEN = (byte)0x30;

    public static byte LOGOUT_LEN = (byte)4;
    public static byte LOGOUT_DATA_LEN = (byte)0;

    public static byte QRIDEN_LEN = (byte)20;
    public static byte QRIDEN_DATA_LEN = (byte)16;

    public static byte HEADER_LEN = (byte)4;
    public static byte USERNAME_LEN = (byte)16;
    public static byte PASSWORD_LEN = (byte)32;
    public static byte RESPONSE_LEN = (byte)8;

    public static int ERR_ALREADY = 2;
    public static int ERR_NONE = 0;
    public static int ERR_DUP = 1;
    public static int ERR_WRONG = -1;


    public static int bytesToInt(byte[] ary, int offset) {
        int value;
        value = (int) ((ary[offset+3]&0xFF)
                | ((ary[offset+2]<<8) & 0xFF00)
                | ((ary[offset+1]<<16)& 0xFF0000)
                | ((ary[offset]<<24) & 0xFF000000));
        return value;
    }
}
