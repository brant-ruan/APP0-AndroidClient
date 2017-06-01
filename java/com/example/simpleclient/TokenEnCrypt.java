package com.example.simpleclient;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by acer on 2017/6/1.
 */

public class TokenEnCrypt {
    public static String MD5(String token){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(token.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException nsaEx) {
            nsaEx.printStackTrace();
        }
        return "";
    }
}
