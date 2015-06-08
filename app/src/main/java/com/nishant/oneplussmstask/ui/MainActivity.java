package com.nishant.oneplussmstask.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.gsm.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.Toast;

import com.nishant.oneplussmstask.R;
import com.nishant.oneplussmstask.Utils;
import com.nishant.oneplussmstask.mms.APNHelper;
import com.nishant.oneplussmstask.mms.PhoneEx;
import com.nishant.oneplussmstask.model.Message;
import com.nishant.oneplussmstask.nokia.IMMConstants;
import com.nishant.oneplussmstask.nokia.MMContent;
import com.nishant.oneplussmstask.nokia.MMEncoder;
import com.nishant.oneplussmstask.nokia.MMMessage;
import com.nishant.oneplussmstask.nokia.MMResponse;
import com.nishant.oneplussmstask.nokia.MMSender;
import com.nishant.oneplussmstask.ui.adapter.MessageAdapter;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private EditText contentTextEdit = null;
    private EditText reciepients = null;
    private Button sendText = null;
    private ListView messagesContainer;
    private MessageAdapter adapter;
    private ArrayList<Message> chatHistory;
    private static MainActivity activity;
    private ConnectivityManager mConnMgr;
    private PowerManager.WakeLock mWakeLock;
    private ConnectivityBroadcastReceiver mReceiver;
    private SMSSentReciever mSMSSentReciever;
    private Message mMessage;
    private NetworkInfo mNetworkInfo;
    private NetworkInfo mOtherNetworkInfo;
    private boolean isMMS = false;
    private Uri selectedImageUri;

    public Message getMessage() {
        return mMessage;
    }

    public void setMessage(Message mMessage) {
        this.mMessage = mMessage;
    }

    public enum State {
        UNKNOWN,
        CONNECTED,
        NOT_CONNECTED
    }

    private State mState;
    private boolean mListening;
    private boolean mSending;

    public static MainActivity getInstance() {
        return activity;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                selectedImageUri = data.getData();
                showPopup(selectedImageUri);
                isMMS = true;
            }
        }
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

        if (Utils.isStringEmpty(sms_no)) {
            reciepients.setText(sms_no);
        }

        sendText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSend();
            }
        });

        mListening = true;
        mSending = false;
        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mReceiver = new ConnectivityBroadcastReceiver();
        mSMSSentReciever = new SMSSentReciever();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);

        // Register broadcast receivers for SMS sent and delivered intents
        registerReceiver(mSMSSentReciever, new IntentFilter(ACTION_SMS_SENT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true; // handled
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
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

    public void onClickSend() {
        // sms body coming from user input
        String strSMSBody = contentTextEdit.getText().toString();
        // sms recipient added by user from the activity screen
        String strReceipentsList = reciepients.getText().toString();
        Message message = null;
        if (validateInput()) {
            if (isMMS) {
                message = createMMSMessage(reciepients.getText().toString(), contentTextEdit.getText().toString(), selectedImageUri, isMMS);
                sendMMS();
            } else {
                message = createMMSMessage(reciepients.getText().toString(), contentTextEdit.getText().toString(), null, isMMS);
                sendSMS(strSMSBody, strReceipentsList);
            }
        }
        if (message != null) {
            displayMessage(message);
            setMessage(message);
            contentTextEdit.setText("");
        }
        if(popup != null && popup.isShowing()){
            popup.dismiss();
        }
        Utils.hideKeyboard(MainActivity.this, contentTextEdit);
    }

    public void displayMessage(Message message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
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

    private void sendMMS() {

        try {

            // Ask to start the connection to the APN. Pulled from Android source code.
            int result = beginMmsConnectivity();

            if (result != PhoneEx.APN_ALREADY_ACTIVE) {
                Log.v(TAG, "Extending MMS connectivity returned " + result + " instead of APN_ALREADY_ACTIVE");
                // Just wait for connectivity startup without
                // any new request of APN switch.
                return;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void endMmsConnectivity() {
        // End the connectivity
        try {
            Log.v(TAG, "endMmsConnectivity");
            if (mConnMgr != null) {
                mConnMgr.stopUsingNetworkFeature(
                        ConnectivityManager.TYPE_MOBILE,
                        PhoneEx.FEATURE_ENABLE_MMS);
            }
        } finally {
            releaseWakeLock();
        }
    }

    protected int beginMmsConnectivity() throws IOException {
        // Take a wake lock so we don't fall asleep before the message is downloaded.
        createWakeLock();

        int result = mConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, PhoneEx.FEATURE_ENABLE_MMS);

        Log.v(TAG, "beginMmsConnectivity: result=" + result);

        switch (result) {
            case PhoneEx.APN_ALREADY_ACTIVE:
            case PhoneEx.APN_REQUEST_STARTED:
                acquireWakeLock();
                return result;
        }

        throw new IOException("Cannot establish MMS connectivity");
    }

    private synchronized void createWakeLock() {
        // Create a new wake lock if we haven't made one yet.
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MMS Connectivity");
            mWakeLock.setReferenceCounted(false);
        }
    }

    private void acquireWakeLock() {
        // It's okay to double-acquire this because we are not using it
        // in reference-counted mode.
        mWakeLock.acquire();
    }

    private void releaseWakeLock() {
        // Don't release the wake lock if it hasn't been created and acquired.
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION) || mListening == false) {
                Log.w(TAG, "onReceived() called with " + mState.toString() + " and " + intent);
                return;
            }

            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            if (noConnectivity) {
                mState = State.NOT_CONNECTED;
            } else {
                mState = State.CONNECTED;
            }

            mNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            mOtherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

//			mReason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
//			mIsFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);


            // Check availability of the mobile network.
            if ((mNetworkInfo == null) || (mNetworkInfo.getType() != ConnectivityManager.TYPE_MOBILE_MMS)) {
                Log.v(TAG, "   type is not TYPE_MOBILE_MMS, bail");
                return;
            }

            if (!mNetworkInfo.isConnected()) {
                Log.v(TAG, "   TYPE_MOBILE_MMS not connected, bail");
                return;
            } else {
                Log.v(TAG, "connected..");

                if (mSending == false) {
                    mSending = true;
                    Message mMessage = getMessage();
                    sendMMSUsingNokiaAPI(mMessage.getPhone_number(), mMessage.getMessage(), mMessage.getImageUri());
                }
            }
        }
    }

    ;

    class SMSSentReciever extends BroadcastReceiver {
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

            contentTextEdit.setText("");
            Toast.makeText(MainActivity.this, message,
                    Toast.LENGTH_SHORT).show();
        }
    }

    ;

    private void sendMMSUsingNokiaAPI(String recipient, String text, Uri uri) {
        // Magic happens here.

        MMMessage mm = new MMMessage();
        SetMessage(mm, recipient, text);
        AddContents(mm, uri);

        MMEncoder encoder = new MMEncoder();
        encoder.setMessage(mm);

        try {
            encoder.encodeMessage();
            byte[] out = encoder.getMessage();

            MMSender sender = new MMSender();
            APNHelper apnHelper = new APNHelper(this);
            List<APNHelper.APN> results = apnHelper.getMMSApns();

            if (results.size() > 0) {

                final String MMSCenterUrl = results.get(0).MMSCenterUrl;
                final String MMSProxy = results.get(0).MMSProxy;
                final int MMSPort = Integer.valueOf(results.get(0).MMSPort);
                final Boolean isProxySet = (MMSProxy != null) && (MMSProxy.trim().length() != 0);

                sender.setMMSCURL(MMSCenterUrl);
                sender.addHeader("X-NOKIA-MMSC-Charging", "100");

                MMResponse mmResponse = sender.send(out, isProxySet, MMSProxy, MMSPort);
                Log.d(TAG, "Message sent to " + sender.getMMSCURL());
                Log.d(TAG, "Response code: " + mmResponse.getResponseCode() + " " + mmResponse.getResponseMessage());

                Enumeration keys = mmResponse.getHeadersList();
                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    String value = (String) mmResponse.getHeaderValue(key);
                    Log.d(TAG, (key + ": " + value));
                }

                if (mmResponse.getResponseCode() == 200) {
                    // 200 Successful, disconnect and reset.
                    endMmsConnectivity();
                    mSending = false;
                    mListening = false;
                } else {
                    // kill dew :D hhaha
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void SetMessage(MMMessage mm, String recipient, String text) {
        mm.setVersion(IMMConstants.MMS_VERSION_10);
        mm.setMessageType(IMMConstants.MESSAGE_TYPE_M_SEND_REQ);
        mm.setTransactionId("0000000066");
        mm.setDate(new Date(System.currentTimeMillis()));
        mm.setFrom(String.format("%s/TYPE=PLMN", recipient));
        mm.addToAddress(String.format("%s/TYPE=PLMN", recipient));
        mm.setDeliveryReport(true);
        mm.setReadReply(false);
        mm.setSenderVisibility(IMMConstants.SENDER_VISIBILITY_SHOW);
        mm.setSubject("This is a nice message!!" + text);
        mm.setMessageClass(IMMConstants.MESSAGE_CLASS_PERSONAL);
        mm.setPriority(IMMConstants.PRIORITY_LOW);
        mm.setContentType(IMMConstants.CT_APPLICATION_MULTIPART_MIXED);

//	    In case of multipart related message and a smil presentation available
//	    mm.setContentType(IMMConstants.CT_APPLICATION_MULTIPART_RELATED);
//	    mm.setMultipartRelatedType(IMMConstants.CT_APPLICATION_SMIL);
//	    mm.setPresentationId("<A0>"); // where <A0> is the id of the content containing the SMIL presentation

    }

    private void AddContents(MMMessage mm, Uri uri) {
        /*Path where contents are stored*/
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Bitmap b;

        try {
            b = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            b.compress(Bitmap.CompressFormat.JPEG, 90, os);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Adds text content
        MMContent part1 = new MMContent();
        byte[] buf1 = os.toByteArray();
        part1.setContent(buf1, 0, buf1.length);
        part1.setContentId("<0>");
        part1.setType(IMMConstants.CT_IMAGE_JPEG);
        mm.addContent(part1);

    }
}
