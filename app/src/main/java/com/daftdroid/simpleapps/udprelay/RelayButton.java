package com.daftdroid.simpleapps.udprelay;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.widget.Button;

import java.io.IOException;


public class RelayButton extends AppCompatButton {

    private Relay relay;
    private final RelaySpec spec;
    private boolean running;

    public RelayButton(Context c, RelaySpec spec)
    {
        super(c);
        this.spec = spec;
        setText(spec.getName());
    }

    public void click()
    {
        try {

            if (!running) {
                if (relay == null) {
                    relay = new Relay(spec, NetworkThread.getNetworkThread().selector());
                }

                relay.startRelay();
                setText(spec.getName()+ " [RUNNING]");
                running = true;
            }
            else {
                relay.stopRelay();
                setText(spec.getName());
                running = false;
            }
        } catch (IOException e)
        {
            setText(e.getLocalizedMessage()); // TODO
        }
    }
}
