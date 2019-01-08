package com.daftdroid.simpleapps.udprelay;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;

import java.io.IOException;


public class RelayButton extends AppCompatButton {

    private Relay relay;
    private final RelaySpec spec;
    private final Context context;

    public RelayButton(Context c, RelaySpec spec)
    {
        super(c);
        this.spec = spec;
        setText(spec.getName());
        this.context = c;
    }

    public void click()
    {
        try {

            if (relay == null) {
                relay = new Relay(spec);
                relay.startRelay();
                NetworkService.uiAddRelay(context, relay);

                setText(spec.getName()+ " [RUNNING]");
            }
            else {
                relay.stopRelay();
                NetworkService.wakeup();
                relay = null;
                setText(spec.getName());
            }
        } catch (IOException e)
        {
            setText(e.getLocalizedMessage()); // TODO
        }
    }
}
