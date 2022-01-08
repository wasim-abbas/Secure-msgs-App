package com.wasim.messegeingApp.utils;

public class FunctionsClass {
    public int lastChar(String str){

        int last=Integer.parseInt(String.valueOf(str.charAt(str.length() - 2)));
        return last;
    }

    public String getKey(String str, int n){

        String key_get=str.substring(0,n);
        return key_get;
    }

    public String getMessage(String msg,int n){

        String msg_get=msg.substring(0,n);
        return msg_get;

    }
}
