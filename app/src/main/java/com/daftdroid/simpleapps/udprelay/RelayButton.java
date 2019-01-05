package com.daftdroid.simpleapps.udprelay;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.widget.Button;

import java.io.IOException;


public class RelayButton extends AppCompatButton {

    private Relay relay;
    private final RelaySpec spec;

    public RelayButton(Context c, RelaySpec spec)
    {
        super(c);
        this.spec = spec;
        setText(spec.getName());
    }

    public void click()
    {
        try {

            if (relay == null) {
                relay = new Relay(spec, NetworkThread.getNetworkThread().selector());
            }
            else {
                cleanUp();
                setText(spec.getName());
            }
        } catch (IOException e)
        {
            setText(e.getLocalizedMessage()); // TODO
            cleanUp();
        }
    }

    /*
        //TODO
     */
    public void cleanUp()
    {
        relay = null;
    }

}
