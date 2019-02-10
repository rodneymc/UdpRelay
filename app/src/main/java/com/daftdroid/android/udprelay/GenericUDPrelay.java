package com.daftdroid.android.udprelay;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

        Button b = findViewById(R.id.okbutton);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vpnName = "ui_test1";
                RelaySpec rly = new RelaySpec (vpnName,
                        chanAloc.getIpAddress(), chanAloc.getPort(),
                        chanArem.getIpAddress(), chanArem.getPort(),
                        chanBloc.getIpAddress(), chanBloc.getPort(),
                        chanBrem.getIpAddress(), chanBrem.getPort());
                VpnSpecification spec = new VpnSpecification();
                spec.setTitle(vpnName);
                spec.setId(1); // TODO
                spec.setSpec(rly);

                new Storage(getFilesDir()).save(spec);
            }
        });
    }
}
