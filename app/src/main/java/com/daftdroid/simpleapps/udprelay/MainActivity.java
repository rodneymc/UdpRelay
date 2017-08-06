/*
This file is part of UdpRelay.

    Copyright 2017 rodney@daftdroid.com

    UdpRelay is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UdpRelay is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UdpRelay.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.daftdroid.simpleapps.udprelay;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Relay outRelay, inRelay;

    /*
        Hard coded addresses. TODO this should be replaced with a user interface!
     */

    private final String EPHEMERAL_IP = null;
    private final int EPHEMERAL_PORT = 0;
    private final String ANDROID_LOCAL_IP = "192.168.42.129";
    private final int ANDROID_LOCAL_PORT = 1195;
    private final String ANDROID_PUBLIC_IP = EPHEMERAL_IP;
    private final int ANDROID_PUBLIC_PORT = EPHEMERAL_PORT;
    private final String SERVER_IP = "203.0.113.10";
    private final int SERVER_PORT = 1194;
    private final String CLIENT_IP = "192.168.42.10";
    private final int CLIENT_PORT = ANDROID_LOCAL_PORT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b = (Button) findViewById(R.id.button);
        b.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        try {
            Button b = (Button) v;
            if (outRelay == null) {
                outRelay = new Relay(ANDROID_LOCAL_IP, ANDROID_LOCAL_PORT, ANDROID_PUBLIC_IP,
                        ANDROID_PUBLIC_PORT, SERVER_IP, SERVER_PORT);
                inRelay = new Relay(outRelay, CLIENT_IP, CLIENT_PORT);
                inRelay.setName("In relay");
                inRelay.start();
                outRelay.setName("Out relay");
                outRelay.start();
                b.setText("stop");
            } else {
                cleanUp();
                b.setText("start");
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            cleanUp();
        }
    }
    /*
        Cleanup function terminates the threads and nulls the references to them.
        Nulling the refs allows GC, also a null reference is used to indicate that
        we are not currently running.
     */
    private void cleanUp()
    {
        if (outRelay != null)
            outRelay.terminate();

        if (inRelay != null)
            inRelay.terminate();

        outRelay = null;
        inRelay = null;
    }
    /*
        Handler for the activity being destroyed. As it stands we basically shut down
        however in a better version of this code the app would behave as a "service" in the
        sense that it would continue to relay traffic even when the activity is destroyed.
        As it stands, this would leak since destroying the activity removes the references to
        our threads. TODO implement service
     */
    @Override
    public void onDestroy()
    {
        cleanUp();
        super.onDestroy();
    }

}
