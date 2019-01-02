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
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Relay outRelay, inRelay;

    private final List<RelayButton> relays = new ArrayList<RelayButton>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO using the demo configs, do something better than this, but if you want to get
        // going you can edit the demo configs to fit your system

        for (RelaySpec rs: RelaySpec.exampleRelays)
        {
            addRelay(rs);
        }
    }
    @Override
    public void onClick(View v) {
         RelayButton b = (RelayButton) v;
         b.click();
    }

    private void addRelay(RelaySpec rSpec)
    {
        RelayButton myButton = new RelayButton(this, rSpec);
        relays.add (myButton);
        myButton.setOnClickListener(this);

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LinearLayout ll = (LinearLayout)findViewById(R.id.layoutmain);
        ll.addView(myButton, lp);

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
        for (RelayButton rb: relays)
        {
            rb.cleanUp();
        }
        super.onDestroy();
    }

}
