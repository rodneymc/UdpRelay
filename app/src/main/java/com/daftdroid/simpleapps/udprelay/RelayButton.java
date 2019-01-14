package com.daftdroid.simpleapps.udprelay;

import android.content.Context;
import android.graphics.Color;
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
                setTextColor(Color.BLACK);
            } else {
                relay.stopRelay();
                NetworkService.wakeup();
                relay = null;
                setText(spec.getName());
                setTextColor(Color.BLACK);
            }
        } catch (IOException e) {
            setText(e.getLocalizedMessage()); // TODO
        }
    }

    public void setError(int severity, final String msg) {
        setText(spec.getName() + " [" + msg + "]");

        if (severity == Relay.ERROR_HARD) {
            setTextColor(Color.RED);
        } else if (severity == Relay.ERROR_SOFT) {
            setTextColor(Color.YELLOW);
        } else {
            setTextColor(Color.BLACK);
        }
    }

    public Relay getRelay() {
        return relay;
    }
}
