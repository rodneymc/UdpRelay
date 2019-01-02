package com.daftdroid.simpleapps.udprelay;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.widget.Button;

import java.io.IOException;


public class RelayButton extends AppCompatButton {

    private Relay inRelay, outRelay;
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
            if (outRelay == null) {
                outRelay = new Relay(spec.getAndroidLocalIP(), spec.getAndroidLocalPort(),
                        spec.getAndroidPublicIP(), spec.getAndroidPublicPort(),
                        spec.getServerIP(), spec.getServerPort());
                inRelay = new Relay(outRelay, spec.getClientIP(), spec.getClientPort());
                inRelay.setName("In relay");
                inRelay.start();
                outRelay.setName("Out relay");
                outRelay.start();
                setText("stop");
            } else {
                cleanUp();
                setText("start");
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            cleanUp();
            setText(e.getLocalizedMessage());
        }
    }

    /*
    Cleanup function terminates the threads and nulls the references to them.
    Nulling the refs allows GC, also a null reference is used to indicate that
    we are not currently running.
 */
    public void cleanUp()
    {
        if (outRelay != null)
            outRelay.terminate();

        if (inRelay != null)
            inRelay.terminate();

        outRelay = null;
        inRelay = null;

    }

}
