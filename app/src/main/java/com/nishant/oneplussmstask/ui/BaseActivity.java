package com.nishant.oneplussmstask.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.nishant.oneplussmstask.R;
import com.nishant.oneplussmstask.model.Message;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by NISHAnT on 6/8/2015.
 */
public class BaseActivity extends Activity {
    protected static final int PICK_CONTACT = 1;
    protected static final int SELECT_PICTURE = 2;
    protected static final String ACTION_SMS_SENT = "com.nishant.oneplussmstask.android.apis.os.SMS_SENT_ACTION";
    protected PopupWindow popup;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // enable transitions
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
    }

    protected void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
    }

    protected void sendSMS(String strSMSBody, String strReceipentsList){
        SmsManager sms = SmsManager.getDefault();
        List<String> messages = sms.divideMessage(strSMSBody);
        for (String message : messages) {
            sms.sendTextMessage(strReceipentsList, null, message, PendingIntent
                            .getBroadcast(this, 0, new Intent(ACTION_SMS_SENT), 0),
                    null);
        }
    }

    protected Message createMMSMessage(String recipient, String text, Uri uri, boolean isMMS) {
        Message message = new Message();
        message.setPhone_number(recipient);
        message.setId(122);//dummy
        message.setMessage(text);
        if (isMMS) {
            message.setImageUri(uri);
            message.setMMS(true);
        }
        message.setDateTime(DateFormat.getDateTimeInstance().format(new Date()));
        message.setMe(true);
        return message;
    }

    protected void showPopup(final Uri imageUri) {
        LayoutInflater layoutInflater = (LayoutInflater)
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout viewGroup = (LinearLayout) findViewById(R.id.popup);
        View layout = layoutInflater.inflate(R.layout.popup_bg, viewGroup);

        popup = new PopupWindow(BaseActivity.this);
        popup.setContentView(layout);
        popup.setFocusable(false);
        popup.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        popup.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        popup.setBackgroundDrawable(new BitmapDrawable());
        popup.setOutsideTouchable(false);
        popup.setTouchable(true);
        popup.setTouchInterceptor(customPopUpTouchListenr);
        popup.update();

        ImageView mImageView = (ImageView) layout.findViewById(R.id.img);

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mImageView.setImageBitmap(bitmap);

        ImageButton btn_remove = (ImageButton) layout.findViewById(R.id.btn_remove);
        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
            }
        });
        popup.showAtLocation(layout, Gravity.TOP, 0, 200);
    }

    View.OnTouchListener customPopUpTouchListenr = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View arg0, MotionEvent arg1) {
            Log.d("POPUP", "Touch false");
            return false;
        }

    };

    protected void showContacts(){
        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(i, PICK_CONTACT);
    }
}
