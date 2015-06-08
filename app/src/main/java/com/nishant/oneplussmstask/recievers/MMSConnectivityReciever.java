package com.nishant.oneplussmstask.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class MMSConnectivityReciever extends BroadcastReceiver {
    public MMSConnectivityReciever() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
        if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            return;
        }

        NetworkInfo mNetworkInfo = intent.getParcelableExtra(
                ConnectivityManager.EXTRA_NETWORK_INFO);

        if ((mNetworkInfo == null) ||
                (mNetworkInfo.getType() != ConnectivityManager.TYPE_MOBILE_MMS)) {
            return;
        }

        if (!mNetworkInfo.isConnected()) {
            return;
        } else {
            //send mms
        }
    }
}
