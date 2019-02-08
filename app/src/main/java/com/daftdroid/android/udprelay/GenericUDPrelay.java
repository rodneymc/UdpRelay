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
        ViewGroup placeHolder;
        ViewGroup inflated;

        placeHolder = findViewById(R.id.chanAip);
        inflated = (ViewGroup) inflater.inflate(R.layout.ipv4, placeHolder);
        Ipv4 chanAIP = new Ipv4(inflated, null);

        placeHolder = findViewById(R.id.chanBip);
        inflated = (ViewGroup) inflater.inflate(R.layout.ipv4, placeHolder);
        Ipv4 chanBIP = new Ipv4(inflated, chanAIP.getFocusLast());
    }
}
