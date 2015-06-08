package com.nishant.oneplussmstask.ui;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.nishant.oneplussmstask.R;
import com.nishant.oneplussmstask.Utils;

public class ContactsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        showContacts();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            try {
                Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
                cursor.moveToFirst();
                int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String phone_number = cursor.getString(column);
                if (Utils.isStringEmpty(phone_number)) {
                    getWindow().setExitTransition(new Explode());
                    Intent intent = new Intent(ContactsActivity.this, MainActivity.class);
                    intent.putExtra("phone_number", phone_number);
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                    finish();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
