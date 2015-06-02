package com.nishant.oneplussmstask;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by NISHAnT on 6/2/2015.
 */
public class Utils {
    public static boolean isStringEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    public static void hideKeyboard(Context c, View v) {
        InputMethodManager imm = (InputMethodManager) c.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
