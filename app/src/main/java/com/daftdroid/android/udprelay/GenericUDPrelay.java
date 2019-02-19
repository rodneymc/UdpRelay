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
import com.daftdroid.android.udprelay.ui_components.UiComponentView;

import java.util.ArrayList;
import java.util.List;

public class GenericUDPrelay extends Activity {

    private int relayId;
    private Ipv4 chanAloc, chanArem, chanBloc, chanBrem;

    private List<UiComponent> uiComponents = new ArrayList<UiComponent>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Storage storage = new Storage(getFilesDir(), getCacheDir());

        setContentView(R.layout.generic_udp_relay);
        setTitle("Generic UDP Relay");

        final Button okButton = findViewById(R.id.okbutton);

        final EditText titleText = findViewById(R.id.configTitle);

        UiComponent okButtonComp = new UiComponentView(okButton);
        UiComponent titleTextComp = new UiComponentView(titleText);

        chanAloc = new Ipv4(findViewById(R.id.chanALocip));
        chanArem = new Ipv4(findViewById(R.id.chanARemip));
        chanBloc = new Ipv4(findViewById(R.id.chanBLocip));
        chanBrem = new Ipv4(findViewById(R.id.chanBRemip));

        // Add the components in focus order

        uiComponents.add(titleTextComp);
        uiComponents.add(chanAloc);
        uiComponents.add(chanArem);
        uiComponents.add(chanBloc);
        uiComponents.add(chanBrem);
        uiComponents.add(okButtonComp);

        // Link the focusable items together before initialising them with data,
        // or the linking will not work. // TODO this should work though...
        int componentCount = uiComponents.size();

        for (int i = 1; i < componentCount; i ++) {
            uiComponents.get(i).linkFocusForward(uiComponents.get(i-1));
        }


        if (savedInstanceState != null) {
            // Restore the edited state of the config, which might not be the same
            // as the actual stored config.

            relayId = savedInstanceState.getInt("id");
            int focusParent = savedInstanceState.getInt("focusParent");
            int focusChild = savedInstanceState.getInt("focusChild");

            for (UiComponent u: uiComponents) {
                u.putRawUserInputState(savedInstanceState.getString(Integer.toString(u.getId())));

                if (u.getId() == focusParent) {
                    u.setFocusToChild(focusChild);
                }
            }



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

            // Set the focus to the dummy element, to prevent the focus going somewhere annoying
            // and / or the keyboard auto opening which is also annoying
            findViewById(R.id.dummyfocus).requestFocus();

        }

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chanBrem.validate();
                chanBloc.validate();
                chanArem.validate();
                chanAloc.validate();

                // If there is an error, move the focus to the first errored view.
                if (!chanAloc.isSaveable()) {
                    chanAloc.requestFocusToErroredView();
                } else if (!chanArem.isSaveable()) {
                    chanArem.requestFocusToErroredView();
                } else if (!chanBloc.isSaveable()) {
                    chanBloc.requestFocusToErroredView();
                } else if (!chanBrem.isSaveable()) {
                    chanBrem.requestFocusToErroredView();
                } else {
                    String name = ((EditText) findViewById(R.id.configTitle)).getText().toString();

                    RelaySpec rly = new RelaySpec(
                            chanAloc.getIpAddress(), chanAloc.getPort(),
                            chanArem.getIpAddress(), chanArem.getPort(),
                            chanBloc.getIpAddress(), chanBloc.getPort(),
                            chanBrem.getIpAddress(), chanBrem.getPort());
                    VpnSpecification spec = new VpnSpecification(storage);
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
        boolean focusFound = false;
        int focusChildIndex = -1;
        int focusParentIndex = -1;

        for (UiComponent u: uiComponents) {
            outState.putString(Integer.toString(u.getId()), u.getRawUserInputState());

            if (!focusFound && (focusChildIndex = u.getChildFocusIndex()) != -1) {
                focusParentIndex = u.getId();
                focusFound = true;
            }
        }

        outState.putInt("focusParent", focusParentIndex);
        outState.putInt("focusChild", focusChildIndex);

    }
}
