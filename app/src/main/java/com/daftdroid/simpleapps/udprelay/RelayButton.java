package com.daftdroid.simpleapps.udprelay;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.AppCompatButton;

import java.io.IOException;


public class RelayButton extends AppCompatButton {

    private Relay relay;
    private final RelaySpec spec;
    private final Context context;

    public RelayButton(Context c, RelaySpec spec, Relay existing) {
        super(c);
        this.spec = spec;
        setText(spec.getName());
        this.context = c;

        relay = existing;
        if (relay != null) {
            setText(spec.getName() + " [RUNNING]");
        }
    }

    public void click() {
        try {

            if (relay == null) {
                relay = new Relay(spec);
                relay.startRelay();
                NetworkService.uiAddRelay(context, relay);

                setText(spec.getName() + " [RUNNING]");
            } else {
                relay.stopRelay();
                NetworkService.wakeup();
                relay = null;
                setText(spec.getName());
            }
        } catch (IOException e) {
            setText(e.getLocalizedMessage()); // TODO
        }
    }

    public void errorCallback(int severity, final String msg) {
        post(new Runnable() {
            @Override
            public void run() {
                setText(spec.getName() + " [" + msg + "]");
            }

        });
    }

    public Relay getRelay() {
        return relay;
    }
}
