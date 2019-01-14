package com.daftdroid.simpleapps.udprelay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetServiceBroadcastReceiver extends BroadcastReceiver {

    private final MainActivity main;

    public NetServiceBroadcastReceiver(MainActivity main) {
        this.main = main;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        RelayButton rb = main.getRelayButton(intent.getIntExtra(NetworkService.BROADCAST_RLYNUM, 0));
        String errString = intent.getStringExtra(NetworkService.BROADCAST_ERRSTRING);
        int status = intent.getIntExtra(NetworkService.BROADCAST_ERRSTATUS, 0);

        if (rb != null) { // not sure it should ever be null..
            rb.setError(status, errString);
        }
    }
}
