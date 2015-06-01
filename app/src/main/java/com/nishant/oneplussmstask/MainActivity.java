package com.nishant.oneplussmstask;

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
import android.telephony.gsm.SmsManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String ACTION_SMS_SENT = "com.nishant.oneplussmstask.android.apis.os.SMS_SENT_ACTION";
	EditText contentTextEdit = null;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		String phoneNo = null;
		Uri uri = data.getData();
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		cursor.moveToFirst();

		int phoneIndex = cursor
				.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
		phoneNo = cursor.getString(phoneIndex);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Get GUI controls instance from here
		contentTextEdit = (EditText) this.findViewById(R.id.message);

		String sms_body = getIntent().getStringExtra("SMS");
		String sms_no = getIntent().getStringExtra("SMSNo");

		contentTextEdit.setText(sms_body);
		
		if(sms_body != null && !sms_body.equalsIgnoreCase("")
				|| sms_no != null && !sms_no.equalsIgnoreCase("")){
			sendSMS(sms_body, sms_no);
		}
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

				contentTextEdit.setText("");
				Toast.makeText(MainActivity.this, message,
						Toast.LENGTH_SHORT).show();
			}
		}, new IntentFilter(ACTION_SMS_SENT));

	}

	public void onClickSend(View v) {

		// Get content and check for null
		if (TextUtils.isEmpty(contentTextEdit.getText())) {
			Toast.makeText(MainActivity.this, "Please enter Message.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		// sms body coming from user input
		String strSMSBody = contentTextEdit.getText().toString();
		// sms recipient added by user from the activity screen
		String strReceipentsList = "";
		sendSMS(strSMSBody, strReceipentsList);
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
}
