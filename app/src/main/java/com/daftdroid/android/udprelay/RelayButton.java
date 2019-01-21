package com.daftdroid.android.udprelay;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.List;


public class RelayButton implements View.OnClickListener {

    private Relay relay;
    private final RelaySpec spec;
    private final Context context;
    private final Button button;

    public RelayButton(Context c, RelaySpec spec) {

        this.spec = spec;
        this.context = c;

        button = new Button(context);


        button.setText(spec.getName());

        updateRelay();

        if (relay != null) {
            updateText();
        }

        button.setOnClickListener(this);
    }

    public void onClick(View v) {
        try {

            if (relay == null) {
                relay = new Relay(spec);
                relay.startRelay();
                NetworkService.uiAddRelay(context, relay);
            } else {
                relay.stopRelay();
                NetworkService.wakeup();
                relay = null;
                button.setText(spec.getName());
                button.setTextColor(Color.BLACK);
            }
        } catch (IOException e) {
            button.setText(e.getLocalizedMessage()); // TODO
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
        button.setText(spec.getName() + " [" + msg + "]");

        if (severity == Relay.ERROR_HARD) {
            button.setTextColor(Color.RED);
        } else if (severity == Relay.ERROR_SOFT) {
            button.setTextColor(0xffffa500);
        } else {
            button.setTextColor(Color.BLACK);
        }
    }

    public Button getButton() {
        return button;
    }
    public Relay getRelay() {
        return relay;
    }
}
