package com.wasim.messegeingApp.utils;


import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

    private static String algorithm = "AES";
    //static SecretKey yourKey = null;



    public static SecretKey generateKey(char[] passphraseOrPin, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Number of PBKDF2 hardening rounds to use. Larger values increase
        // computation time. You should select a value that causes computation
        // to take >100ms.
        final int iterations = 1000;

        // Generate a 256-bit key
        final int outputKeyLength = 256;

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(passphraseOrPin, salt, iterations, outputKeyLength);
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        return secretKey;
    }

    public static SecretKey generateKey() throws NoSuchAlgorithmException {
        // Generate a 256-bit key
        final int outputKeyLength = 256;
        SecureRandom secureRandom = new SecureRandom();
        // Do *not* seed secureRandom! Automatically seeded from system entropy.
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

      //  SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();

        keyGenerator.init(outputKeyLength, secureRandom);
        SecretKey yourKey = keyGenerator.generateKey();
        Log.e("ok", "key in generate key metod:  " + yourKey);
        int keyLenght = yourKey.getAlgorithm().length();
        Log.e("ok", "lenght of key :" + keyLenght);

        return yourKey;
    }


    public static String encodeFile(SecretKey key, byte[] fileData)
            throws Exception {
        byte[] encrypted = null;
        Log.e("ok", "key before get encoded :" + key);
        byte[] data = key.getEncoded();
        Log.e("ok", "key AFTER get encoded :" + key);
        SecretKeySpec skeySpec = new SecretKeySpec(data, 0, data.length, algorithm);
        Cipher cipher = Cipher.getInstance(algorithm);
        Log.e("ok", "key in encodefile " + key);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        encrypted = cipher.doFinal(fileData);
        Log.e("ok", "encrypted  msg in encodefile :" + encrypted);
        return toHex(encrypted);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String decodeFile(byte[] rawKey, byte[] fileData)
            throws Exception {
        byte[] decrypted = null;
        Cipher cipher = Cipher.getInstance(algorithm);
        SecretKeySpec skeySpec = new SecretKeySpec(rawKey, 0, rawKey.length, algorithm);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        Log.e("ok", "key in Decodefile " + rawKey);

        decrypted = cipher.doFinal(fileData);
        Log.e("ok", "decrypted Msg in Decodefile: " + decrypted);

        String string = new String(decrypted, StandardCharsets.UTF_8);
        Log.e("ok", "hexadecima Msg in Decodefile: " + string);


        return string;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }
}


////////////////////////////////////////////below is static key method example/////////////////////////////////

//public class AESUtils {
//
//    private static final byte[] keyValue =
//            new byte[]{'w', 'a', 's', 'i', 'm', 'k', 'h', 'y', 'b', 'e', 'r', 'c', 'o', 'd', 'e', 'd'};
//
//
//    public static String encrypt( String cleartext)
//            throws Exception {
//        byte[] rawKey = getRawKey();
//        Log.e("ok","key in encrypt methd :"+rawKey);
//        byte[] result = encrypt(rawKey, cleartext.getBytes());
//        Log.e("ok","key in encrypt methd after return from hexadecimal method :"+result);
//
//        return toHex(result);
//    }
//
//    public static String decrypt(String encrypted)
//            throws Exception {
//        Log.e("ok","decrypt mesg received in decrypt methd :"+encrypted);
//
//        byte[] enc = toByte(encrypted);
//        Log.e("ok","decrypt string in decrypt methd :"+enc);
//        byte[] rawKey = getRawKey();
//        Log.e("ok","key key key lkey in decrypt string in decrypt methd :"+enc);
//        byte[] result = decrypt(rawKey,enc);
//        return new String(result);
//    }
//
//    public static byte[] getRawKey() throws Exception {
//        SecretKey key = new SecretKeySpec(keyValue, "AES");
//        Log.e("ok","key in getKey methd :"+key);
//        byte[] raw = key.getEncoded();
//        Log.e("ok","key in getkey methd after encoded :"+raw);
//
//        return raw;
//    }
//
//    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
//        SecretKey skeySpec = new SecretKeySpec(raw, "AES");
//        Log.e("ok","key in encrypt methdraw 2nd :"+skeySpec);
//        Cipher cipher = Cipher.getInstance("AES");
//        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
//        byte[] encrypted = cipher.doFinal(clear);
//
//        return encrypted;
//    }
//
//    private static byte[] decrypt(byte[] rawKey,byte[] encrypted)
//            throws Exception {
//        SecretKey skeySpec = new SecretKeySpec(rawKey, "AES");
//        Cipher cipher = Cipher.getInstance("AES");
//        Log.e("ok","key in decrypt methd 2nd:"+skeySpec);
//        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
//        byte[] decrypted = cipher.doFinal(encrypted);
//        return decrypted;
//    }
//
//    public static byte[] toByte(String hexString) {
//        int len = hexString.length() / 2;
//        byte[] result = new byte[len];
//        for (int i = 0; i < len; i++)
//            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
//                    16).byteValue();
//        return result;
//    }
//
//    public static String toHex(byte[] buf) {
//        if (buf == null)
//            return "";
//        StringBuffer result = new StringBuffer(2 * buf.length);
//        for (int i = 0; i < buf.length; i++) {
//            appendHex(result, buf[i]);
//        }
//        return result.toString();
//    }
//
//    private final static String HEX = "0123456789ABCDEF";
//
//    private static void appendHex(StringBuffer sb, byte b) {
//        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
//    }
//
//}
