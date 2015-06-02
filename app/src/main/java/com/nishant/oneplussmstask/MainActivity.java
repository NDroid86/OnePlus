package com.nishant.oneplussmstask;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.gsm.SmsManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

    public static final String ACTION_SMS_SENT = "com.nishant.oneplussmstask.android.apis.os.SMS_SENT_ACTION";
    private EditText contentTextEdit = null;
    private EditText reciepients = null;
    private Button sendText = null;
    private ListView messagesContainer;
    private MessageAdapter adapter;
    private ArrayList<Message> chatHistory;
    private static MainActivity activity;
    private static final int SELECT_PICTURE = 1;

    public static MainActivity getInstance() {
        return activity;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                String selectedImagePath = getPath(selectedImageUri);
                Toast.makeText(MainActivity.this,"Image selected",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getPath(Uri uri) {
        // just some safety built in
        if (uri == null) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        // Get GUI controls instance from here
        contentTextEdit = (EditText) findViewById(R.id.message);
        reciepients = (EditText) findViewById(R.id.recipient);
        sendText = (Button) findViewById(R.id.SendButton);
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);


        adapter = new MessageAdapter(MainActivity.this, new ArrayList<Message>());
        messagesContainer.setAdapter(adapter);

        String sms_no = getIntent().getStringExtra("phone_number");

        if(Utils.isStringEmpty(sms_no)){
            reciepients.setText(sms_no);
        }
        /*String sms_body = getIntent().getStringExtra("SMS");
        String sms_no = getIntent().getStringExtra("SMSNo");

        contentTextEdit.setText(sms_body);

        if (sms_body != null && !sms_body.equalsIgnoreCase("")
                || sms_no != null && !sms_no.equalsIgnoreCase("")) {
            sendSMS(sms_body, sms_no);
        }*/
        sendText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSend();
            }
        });
        // Register broadcast receivers for SMS sent and delivered intents
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = null;
                boolean error = true;
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        message = "Message sent!";
                        error = false;
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        message = "Error.";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        message = "Error: No service.";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        message = "Error: Null PDU.";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        message = "Error: Radio off.";
                        break;
                }

//                reciepients.setText("");
                contentTextEdit.setText("");
                Toast.makeText(MainActivity.this, message,
                        Toast.LENGTH_SHORT).show();
            }
        }, new IntentFilter(ACTION_SMS_SENT));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_attachment:
                pickImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
    }

    public void onClickSend() {
        // sms body coming from user input
        String strSMSBody = contentTextEdit.getText().toString();
        // sms recipient added by user from the activity screen
        String strReceipentsList = reciepients.getText().toString();
        if (validateInput()) {
            Message message = new Message();
            message.setId(122);//dummy
            message.setMessage(strSMSBody);
            message.setDateTime(DateFormat.getDateTimeInstance().format(new Date()));
            message.setMe(true);
            contentTextEdit.setText("");
            Utils.hideKeyboard(MainActivity.this, contentTextEdit);
            displayMessage(message);
            sendSMS(strSMSBody, strReceipentsList);
        }
    }

    public void displayMessage(Message message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    public void sendSMS(String strSMSBody, String strReceipentsList) {
        SmsManager sms = SmsManager.getDefault();
        List<String> messages = sms.divideMessage(strSMSBody);
        for (String message : messages) {
            sms.sendTextMessage(strReceipentsList, null, message, PendingIntent
                            .getBroadcast(this, 0, new Intent(ACTION_SMS_SENT), 0),
                    null);
        }
    }

    private boolean validateInput() {
        if (!Utils.isStringEmpty(contentTextEdit.getText().toString())) {
            Toast.makeText(MainActivity.this, "Please enter message.", Toast.LENGTH_SHORT).show();
        } else if (!Utils.isStringEmpty(reciepients.getText().toString())) {
            Toast.makeText(MainActivity.this, "Please enter reciepient.", Toast.LENGTH_SHORT).show();
        } else {
            return true;
        }
        return false;
    }

    /*private void loadDummyHistory() {

        chatHistory = new ArrayList<Message>();

        Message msg = new Message();
        msg.setId(1);
        msg.setMe(false);
        msg.setMessage("Hi");
        msg.setDateTime(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg);

        Message msg1 = new Message();
        msg1.setId(2);
        msg1.setMe(false);
        msg1.setMessage("How r u doing???");
        msg1.setDateTime(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg1);

        adapter = new MessageAdapter(MainActivity.this, new ArrayList<Message>());
        messagesContainer.setAdapter(adapter);

        for (int i = 0; i < chatHistory.size(); i++) {
            Message message = chatHistory.get(i);
            displayMessage(message);
        }
    }*/
}
