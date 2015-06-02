package com.nishant.oneplussmstask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;

public class IncomingMessageReciever extends BroadcastReceiver {

    // Get the object of SmsManager
    final SmsManager sms = SmsManager.getDefault();
    private MainActivity activity;

    public void onReceive(Context context, Intent intent) {

        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();
        activity = MainActivity.getInstance();
        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    String senderNum = phoneNumber;
                    String text = currentMessage.getDisplayMessageBody();

                    Message message = new Message();
                    message.setId(122);//dummy
                    message.setMessage(text);
                    message.setDateTime(DateFormat.getDateTimeInstance().format(new Date()));
                    message.setMe(false);
                    activity.displayMessage(message);

                    Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + text);

                }
            }

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);

        }
    }

    public IncomingMessageReciever() {
    }
}
