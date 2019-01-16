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

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Relay outRelay, inRelay;

    private final List<RelayButton> relays = new ArrayList<RelayButton>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
            Register for receiving status updates to the buttons in the event
            of network errors.
         */
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new NetServiceBroadcastReceiver(this),
                new IntentFilter(NetworkService.BROADCAST_ACTION));

        // TODO using the demo configs, do something better than this, but if you want to get
        // going you can edit the demo configs to fit your system

        List<Relay> runningList = NetworkService.getActiveRelays();

        for (RelaySpec rs: RelaySpec.exampleRelays)
        {
            addRelay(rs, runningList);
        }
    }
    @Override
    public void onClick(View v) {
         RelayButton b = (RelayButton) v;
         b.click();
    }

    private void addRelay(RelaySpec rSpec, List<Relay> runningList)
    {
        Relay existing = null;
        // See if we can find a relay running already that matches the spec
        if (runningList != null) {
            for (Relay r: runningList) {
                if (r.getSpec().equals(rSpec)) {
                    existing = r;
                    break;
                }
            }
        }

        RelayButton myButton = new RelayButton(this, rSpec, existing);
        relays.add (myButton);
        myButton.setOnClickListener(this);

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LinearLayout ll = (LinearLayout)findViewById(R.id.layoutmain);
        ll.addView(myButton, lp);
    }

    public RelayButton getRelayButton(int id) {
        for (RelayButton rb: relays) {
            Relay r = rb.getRelay();

            if (r != null && r.getUniqueId() == id) {
                return rb;
            }
        }

        return null;
    }

}
