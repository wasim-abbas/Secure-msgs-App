package com.wasim.messegeingApp.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.wasim.messegeingApp.R;
import com.wasim.messegeingApp.activities.SmsDetailedView;
import com.wasim.messegeingApp.constants.Constants;
import com.wasim.messegeingApp.services.SaveSmsService;
import com.wasim.messegeingApp.utils.AESUtils;
import com.wasim.messegeingApp.utils.FunctionsClass;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SmsReceiver extends BroadcastReceiver {
    private String decryptedString;
    private static int len_msg;


    private String TAG = SmsReceiver.class.getSimpleName();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            // Get the SMS message received

            Log.e(TAG, "smsReceiver");

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                // A PDU is a "protocol data unit". This is the industrial standard for SMS message
                Object[] pdu_Objects = (Object[]) bundle.get("pdus");
                if (pdu_Objects != null) {
                    // This will create an SmsMessage object from the received pdu

                    for (Object aObject : pdu_Objects) {

                        SmsMessage smsMessage = getIncomingMessage(aObject, bundle);

                        // Get sender phone number
                        String senderNo = smsMessage.getDisplayOriginatingAddress();

                        //check here by passing reciever number statically

                        String message = smsMessage.getMessageBody();

                        issueNotification(context, senderNo, message);
                        saveSmsInInbox(context, smsMessage);


                    }
                    this.abortBroadcast();
                    // End of loop
                }
            }
        } // bundle null
    }

    private void saveSmsInInbox(Context context, SmsMessage sms) {

        Intent serviceIntent = new Intent(context, SaveSmsService.class);
        serviceIntent.putExtra("sender_no", sms.getDisplayOriginatingAddress());
        serviceIntent.putExtra("message", sms.getMessageBody());
        serviceIntent.putExtra("date", sms.getTimestampMillis());
        context.startService(serviceIntent);

    }


    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
    }

    public static byte[] returnMessege(String message)
    {

        FunctionsClass functionsClass = new FunctionsClass();

        Log.e("ok", "received messge :" + message);

        ///////// get Length of key
        int keyLength = 45;//functionsClass.lastChar(message);
        System.out.println("keylenth\t" + keyLength);
        Log.e("ok", "key lenght lastChar: " + keyLength);


        ///////// getmessage from key
        int first = Integer.parseInt(message.substring(0, 2));
        Log.e("ok","first key lenght:"+first);

        if (first == 12) {
            len_msg = 128;
        } else if (first == 32) {
            len_msg = 32;
        } else if (first == 64) {
            len_msg = 64;
        } else if (first == 19) {
            len_msg = 192;
        } else if (first == 25) {
            len_msg = 256;
        } else if (first == 96) {
            len_msg = 96;
        }
        ////get msg with lenght
        String get_msg_raw = functionsClass.getMessage(message, len_msg+2);///end pint of messsage
        Log.e("ok", "lenght of msg: " + get_msg_raw);
        ////get msg without lenght
        String get_msg = get_msg_raw.substring(2,len_msg+2);
        System.out.println("message\t" + get_msg);
        Log.e("ok", "extract messge cancate string: " + get_msg);
        ////change hexadecimal String to byte
        byte[] enc = toByte(get_msg);
        Log.e("ok", "Encrypted byte string" + enc);
        return enc;
    }

    public static byte[] returnKey(String message)
    {
        FunctionsClass functionsClass=new FunctionsClass();
        int keyLength = 45;
        ///// get key with length
        String get_key = message.substring(message.length() - keyLength - 2);///give starting point of string
        System.out.println("my key with lenhtt" + get_key);
        Log.e("ok", "Received get key with length: " + get_key);


        /////// Get key
        String mykey = functionsClass.getKey(get_key, keyLength);
        //String finalKey = "javax.crypto.spec.SecretKeySpec" + mykey;
        System.out.println("my key\t" + mykey);
        //  System.out.println("my key\t" + finalKey);

        Log.e("ok", "Received mykey: " + mykey);
        //Log.e("ok", "Received my final key: " + finalKey.length());


        byte[] encodedKey = Base64.decode(mykey, Base64.DEFAULT);
        Log.e("ok", "decoded key in bytes: " + encodedKey);

        SecretKey originalKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        byte[] yourKey=originalKey.getEncoded();

//
        Log.e("ok", "secreet key: " + originalKey);
        Log.e("ok", "Key Encoded: " + originalKey.getEncoded());
        return yourKey;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void issueNotification(Context context, String senderNo, String message) {

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_launcher);

        try {

            decryptedString = new String(AESUtils.decodeFile(returnKey(message), returnMessege(message)));

        } catch (
                Exception e) {
            e.printStackTrace();
            decryptedString = message;
            Toast.makeText(context, "The Encrypted string is incomplte.....", Toast.LENGTH_SHORT).show();
            Log.e("ok", "error in msg receiver");
        }


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setLargeIcon(icon)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(senderNo)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(decryptedString))
                        .setAutoCancel(true)
                        .setContentText(decryptedString);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);


        Intent resultIntent = new Intent(context, SmsDetailedView.class);
        resultIntent.putExtra(Constants.CONTACT_NAME, senderNo);
        resultIntent.putExtra(Constants.FROM_SMS_RECIEVER, true);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int mNotificationId = 101;
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }

    private SmsMessage getIncomingMessage(Object aObject, Bundle bundle) {
        SmsMessage smsMessage;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            String format = bundle.getString("format");
            smsMessage = SmsMessage.createFromPdu((byte[]) aObject, format);

        } else {
            smsMessage = SmsMessage.createFromPdu((byte[]) aObject);
        }
        return smsMessage;
    }
}
