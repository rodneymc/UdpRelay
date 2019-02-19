package com.daftdroid.android.udprelay.config_providers;

import android.view.View;

import com.daftdroid.android.udprelay.R;
import com.daftdroid.android.udprelay.RelaySpec;
import com.daftdroid.android.udprelay.VpnSpecification;
import com.daftdroid.android.udprelay.ui_components.Ipv4;
import com.daftdroid.android.udprelay.ui_components.UiComponent;
import com.daftdroid.android.udprelay.ui_components.UiComponentView;
import com.daftdroid.android.udprelay.ConfigurationActivity;

import java.util.ArrayList;
import java.util.List;

public class GenericUDPrelay extends ConfigurationActivity {

    private Ipv4 chanAloc, chanArem, chanBloc, chanBrem;

    // Generate uiComponents for each elements in the input form. The parameters are the
    // elements common to all forms - title and OK button.
    protected List<UiComponent> getUiComponents(View titleText, View okButton) {

        List<UiComponent> uiComponents = new ArrayList<UiComponent>();

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

        return uiComponents;
    }
    protected void processLoadedSpec(VpnSpecification loadedSpec) {
        RelaySpec rly = loadedSpec.getRelaySpec();

        chanAloc.setIpAddress(rly.getChanALocalIP());
        chanAloc.setPort(rly.getChanALocalPort());
        chanArem.setIpAddress(rly.getChanARemoteIP());
        chanArem.setPort(rly.getChanARemotePort());
        chanBloc.setIpAddress(rly.getChanBLocalIP());
        chanBloc.setPort(rly.getChanBLocalPort());
        chanBrem.setIpAddress(rly.getChanBRemoteIP());
        chanBrem.setPort(rly.getChanBRemotePort());
    }
    protected void initialiseBlankSpec() {
        chanAloc.initBlank();
        chanBloc.initBlank();
        chanArem.initBlank();
        chanBrem.initBlank();
    }
    protected void prepareSpecForSave(VpnSpecification spec) {
        RelaySpec rly = new RelaySpec(
                chanAloc.getIpAddress(), chanAloc.getPort(),
                chanArem.getIpAddress(), chanArem.getPort(),
                chanBloc.getIpAddress(), chanBloc.getPort(),
                chanBrem.getIpAddress(), chanBrem.getPort());
        spec.setSpec(rly);

    }
    protected String getActivityTitle() {
        return "Generic UDP Relay";
    }
}
