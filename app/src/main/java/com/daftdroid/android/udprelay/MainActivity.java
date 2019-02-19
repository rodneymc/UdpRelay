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

package com.daftdroid.android.udprelay;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainActivity extends Activity {

    private final List<RelayButton> relays = new ArrayList<RelayButton>();
    private Storage storage;
    private Typeface fontAwesome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fontAwesome = Typeface.createFromAsset( getAssets(), "fa-regular-400.ttf" );

        /*
            Register for receiving status updates to the buttons in the event
            of network errors.
         */
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new NetServiceBroadcastReceiver(this),
                new IntentFilter(NetworkService.BROADCAST_ACTION));

        storage = new Storage(getFilesDir(), getCacheDir());
        List<VpnSpecification> vpns = storage.loadAll();

        for (VpnSpecification vpn: vpns) {
            addRelay(vpn);
        }

        Button button = new Button(this);
        button.setText("ADD [APLHA]");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GenericUDPrelay.class);
                //intent.putExtra(VpnSpecification.INTENT_ID, 1);
                startActivityForResult(intent, 1);
            }
        });

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LinearLayout ll = (LinearLayout)findViewById(R.id.layoutmain);
        ll.addView(button, lp);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            VpnSpecification newSpec = storage.load(data.getIntExtra(VpnSpecification.INTENT_ID, 0));

            if (newSpec != null) {
                for (RelayButton rb : relays) {
                    if (rb.getSpecId() == requestCode) {
                        rb.updateSpec(newSpec);
                        return;
                    }
                }
                // If we didn't have one to update, it must be a new one.
                addRelay(newSpec);
            }
        }
    }
    private void addRelay(VpnSpecification spec)
    {
        RelayButton myButton = new RelayButton(this, R.id.layoutmain, spec);
        relays.add (myButton);
    }

    public RelayButton getRelayButton(int id) {
        for (RelayButton rb: relays) {
            rb.updateRelay();
            if (rb.getSpecId() == id) {
                return rb;
            }
        }

        return null;
    }
    public Storage getStorage() {
        return storage;
    }

    Typeface getFontAwesome() {
        return fontAwesome;
    }

}
