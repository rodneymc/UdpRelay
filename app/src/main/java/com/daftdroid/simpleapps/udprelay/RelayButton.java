package com.daftdroid.simpleapps.udprelay;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.AppCompatButton;

import java.io.IOException;
import java.util.List;


public class RelayButton extends AppCompatButton {

    private Relay relay;
    private final RelaySpec spec;
    private final Context context;

    public RelayButton(Context c, RelaySpec spec) {
        super(c);
        this.spec = spec;
        setText(spec.getName());
        this.context = c;

        updateRelay();

        if (relay != null) {
            updateText();
        }
    }

    public void click() {
        try {

            if (relay == null) {
                relay = new Relay(spec);
                relay.startRelay();
                NetworkService.uiAddRelay(context, relay);
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
    public void updateText() {
        setError(relay.getErrorLevel(), relay.getStatusMessage());
    }

    /*
        Update our reference to relay. This is needed because either
        1) A new ui instance was created, while relays were already running
        2) The network service decided to replace the relay with a new one after an error.
     */
    public void updateRelay() {
        // A list of running, non errored relays
        List<Relay> runningList = NetworkService.getActiveRelays();
        // See if we can find a relay running already that matches the spec
        if (runningList != null) {
            for (Relay r: runningList) {
                if (r.getSpec().equals(spec)) {
                    relay = r;
                    break;
                }
            }
        }

    }

    private void setError(int severity, final String msg) {
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
