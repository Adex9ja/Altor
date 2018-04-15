package com.altor.android.altor.utils;

import android.util.Base64;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ADEOLU on 3/26/2017.
 */


public class EncryptionTechnology {
    private static String myKey = "aLtORADE";
    public static String Encrypt(String str)
    {
        String encryString = null;
        try {
            Key key = new SecretKeySpec(myKey.getBytes(), "DES");
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[]enctyptedByte = cipher.doFinal(str.getBytes("utf-8"));
            encryString = Base64.encodeToString(enctyptedByte,Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryString;
    }

    public static String Decrypt(String str)
    {
        String deString =null;
        try {
            Key key = new SecretKeySpec(myKey.getBytes(), "DES");
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedByte = Base64.decode(str,Base64.NO_WRAP);
            byte[] decrpted  =cipher.doFinal(decodedByte);
            deString = new String(decrpted,"utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deString;
    }


}
