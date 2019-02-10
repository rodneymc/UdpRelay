package com.daftdroid.android.udprelay;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.daftdroid.android.udprelay.ui_components.Ipv4;

public class GenericUDPrelay extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generic_udp_relay);

        Ipv4 chanAloc = new Ipv4(this, R.id.chanALocip);
        Ipv4 chanArem = new Ipv4(this, R.id.chanARemip);
        Ipv4 chanBloc = new Ipv4(this, R.id.chanBLocip);
        Ipv4 chanBrem = new Ipv4(this, R.id.chanBRemip);

        chanBrem.linkFocusForward(chanBloc).
                linkFocusForward(chanArem).
                linkFocusForward(chanAloc);
    }
}
