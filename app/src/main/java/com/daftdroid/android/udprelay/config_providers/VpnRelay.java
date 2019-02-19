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

public class VpnRelay extends ConfigurationActivity {

    private Ipv4 chanAloc, chanArem, chanBrem;

    // Generate uiComponents for each elements in the input form. The parameters are the
    // elements common to all forms - title and OK button.
    @Override
    protected List<UiComponent> getUiComponents(View titleText, View okButton) {

        List<UiComponent> uiComponents = new ArrayList<UiComponent>();

        UiComponent okButtonComp = new UiComponentView(okButton);
        UiComponent titleTextComp = new UiComponentView(titleText);

        chanAloc = new Ipv4(findViewById(R.id.chanALocip));
        chanArem = new Ipv4(findViewById(R.id.chanARemip));
        chanBrem = new Ipv4(findViewById(R.id.chanBRemip));

        // Add the components in focus order

        uiComponents.add(titleTextComp);
        uiComponents.add(chanAloc);
        uiComponents.add(chanArem);
        uiComponents.add(chanBrem);
        uiComponents.add(okButtonComp);

        return uiComponents;
    }
    @Override
    protected void processLoadedSpec(VpnSpecification loadedSpec) {
        RelaySpec rly = loadedSpec.getRelaySpec();

        chanAloc.setIpAddress(rly.getChanALocalIP());
        chanAloc.setPort(rly.getChanALocalPort());
        chanArem.setIpAddress(rly.getChanARemoteIP());
        chanArem.setPort(rly.getChanARemotePort());
        chanBrem.setIpAddress(rly.getChanBRemoteIP());
        chanBrem.setPort(rly.getChanBRemotePort());
    }
    @Override
    protected void initialiseBlankSpec() {

        // Default, assume any client can connect
        chanArem.setIpAddress(null);
        chanArem.setPort(0);

        //Default, assume OpenVPN standard port
        chanAloc.setPort(RelaySpec.WELL_KNOWN_PORT_OPENVPN);
        chanBrem.setPort(RelaySpec.WELL_KNOWN_PORT_OPENVPN);

        // Set the formatting correctly
        chanAloc.initBlank();
        chanArem.initBlank();
        chanBrem.initBlank();

    }
    @Override
    protected void prepareSpecForSave(VpnSpecification spec) {
        RelaySpec rly = new RelaySpec(
                chanAloc.getIpAddress(), chanAloc.getPort(),
                chanArem.getIpAddress(), chanArem.getPort(),
                "0.0.0.0", 0,
                chanBrem.getIpAddress(), chanBrem.getPort());
        spec.setSpec(rly);

    }
    @Override
    protected String getActivityTitle() {
        return "VPN Relay";
    }
    @Override
    protected int getActivityLayout() {
        return R.layout.vpn_relay;
    }
}
