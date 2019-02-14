package com.daftdroid.android.udprelay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.daftdroid.android.udprelay.ui_components.Ipv4;
import com.daftdroid.android.udprelay.ui_components.UiComponent;

public class GenericUDPrelay extends Activity {

    private int relayId;
    private Ipv4 chanAloc, chanArem, chanBloc, chanBrem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Storage storage = new Storage(getFilesDir());

        setContentView(R.layout.generic_udp_relay);
        final Button okButton = findViewById(R.id.okbutton);

        final EditText titleText = findViewById(R.id.configTitle);

        chanAloc = new Ipv4(this, R.id.chanALocip);
        chanArem = new Ipv4(this, R.id.chanARemip);
        chanBloc = new Ipv4(this, R.id.chanBLocip);
        chanBrem = new Ipv4(this, R.id.chanBRemip);

        // Link the focusable items together before initialising them with data,
        // or the linking will not work. // TODO this should work though...

        new UiComponent(okButton).linkFocusForward(chanBrem).
                linkFocusForward(chanBloc).
                linkFocusForward(chanArem).
                linkFocusForward(chanAloc).
                linkFocusForward(new UiComponent(titleText));

        if (savedInstanceState != null) {
            // Restore the edited state of the config, which might not be the same
            // as the actual stored config.

            relayId = savedInstanceState.getInt("id");
            chanAloc.putRawUserInputState(savedInstanceState.getString("aloc"));
            chanBloc.putRawUserInputState(savedInstanceState.getString("bloc"));
            chanArem.putRawUserInputState(savedInstanceState.getString("arem"));
            chanBrem.putRawUserInputState(savedInstanceState.getString("brem"));
            titleText.setText(savedInstanceState.getString("name"));

        } else {
            // Load the saved config, if there is one

            relayId = getIntent().getIntExtra(VpnSpecification.INTENT_ID, 0);
            final VpnSpecification loadedSpec;

            loadedSpec = storage.load(relayId);

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

                titleText.setText(loadedSpec.getTitle());
            } else {
                relayId = storage.getNewSpecId();
                chanAloc.initBlank();
                chanBloc.initBlank();
                chanArem.initBlank();
                chanBrem.initBlank();
            }
        }

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (chanAloc.isSaveable() && chanBloc.isSaveable() && chanArem.isSaveable()
                 && chanBrem.isSaveable()) {
                    String name = ((EditText) findViewById(R.id.configTitle)).getText().toString();

                    RelaySpec rly = new RelaySpec(name,
                            chanAloc.getIpAddress(), chanAloc.getPort(),
                            chanArem.getIpAddress(), chanArem.getPort(),
                            chanBloc.getIpAddress(), chanBloc.getPort(),
                            chanBrem.getIpAddress(), chanBrem.getPort());
                    VpnSpecification spec = new VpnSpecification();
                    spec.setTitle(name);
                    spec.setId(relayId);
                    spec.setSpec(rly);

                    storage.save(spec);

                    Intent retData = new Intent();
                    retData.putExtra(VpnSpecification.INTENT_ID, relayId);
                    setResult(RESULT_OK, retData);
                    finish();
                }
            }
        });
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("id", relayId);
        outState.putString("name", ((EditText) findViewById(R.id.configTitle)).getText().toString());
        outState.putString("aloc", chanAloc.getRawUserInputState());
        outState.putString("bloc", chanBloc.getRawUserInputState());
        outState.putString("arem", chanArem.getRawUserInputState());
        outState.putString("brem", chanBrem.getRawUserInputState());
    }
}
