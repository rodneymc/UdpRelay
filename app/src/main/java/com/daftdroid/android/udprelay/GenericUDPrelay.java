package com.daftdroid.android.udprelay;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.daftdroid.android.udprelay.ui_components.Ipv4;

public class GenericUDPrelay extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generic_udp_relay);

        LayoutInflater inflater = getLayoutInflater();
        ViewGroup vg;
        View v;

        vg = findViewById(R.id.chanAip);
        v = inflater.inflate(R.layout.ipv4, vg);
        new Ipv4(v);

        vg = findViewById(R.id.chanBip);
        v = inflater.inflate(R.layout.ipv4, vg);

        //ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //LinearLayout ll = (LinearLayout) vg;
        //ll.addView(v, lp);

    }
}
