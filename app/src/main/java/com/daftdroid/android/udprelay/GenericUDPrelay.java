package com.daftdroid.android.udprelay;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.daftdroid.android.udprelay.ui_components.Ipv4;

public class GenericUDPrelay extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Storage storage = new Storage(getFilesDir());

        setContentView(R.layout.generic_udp_relay);

        Ipv4 chanAloc = new Ipv4(this, R.id.chanALocip);
        Ipv4 chanArem = new Ipv4(this, R.id.chanARemip);
        Ipv4 chanBloc = new Ipv4(this, R.id.chanBLocip);
        Ipv4 chanBrem = new Ipv4(this, R.id.chanBRemip);

        int id = getIntent().getIntExtra(VpnSpecification.INTENT_ID, 0);
        final VpnSpecification loadedSpec;

        loadedSpec = storage.load(id);

        if (loadedSpec != null) {
            RelaySpec rly = loadedSpec.getRelaySpec();

            chanAloc.setIpAddress(rly.getChanALocalIP());
            chanAloc.setPort(rly.getChanALocalPort());
            chanArem.setIpAddress(rly.getChanARemoteIP());
            chanArem.setPort(rly.getChanARemotePort());
            chanBloc.setIpAddress(rly.getChanBLocalIP());
            chanBloc.setPort(rly.getChanBLocalPort());
            chanBrem.setIpAddress(rly.getChanBRemoteIP());
            chanBrem.setPort(rly.getChanBRemotePort());

            ((EditText) findViewById(R.id.configTitle)).setText(loadedSpec.getTitle());
        }
        else {
            id = storage.getNewSpecId();
        }

        final int finalid = id;

        chanBrem.linkFocusForward(chanBloc).
                linkFocusForward(chanArem).
                linkFocusForward(chanAloc);

        Button b = findViewById(R.id.okbutton);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = ((EditText) findViewById(R.id.configTitle)).getText().toString();

                RelaySpec rly = new RelaySpec (name,
                        chanAloc.getIpAddress(), chanAloc.getPort(),
                        chanArem.getIpAddress(), chanArem.getPort(),
                        chanBloc.getIpAddress(), chanBloc.getPort(),
                        chanBrem.getIpAddress(), chanBrem.getPort());
                VpnSpecification spec = new VpnSpecification();
                spec.setTitle(name);
                spec.setId(finalid);
                spec.setSpec(rly);

                storage.save(spec);

                finish();
            }
        });
    }
}
