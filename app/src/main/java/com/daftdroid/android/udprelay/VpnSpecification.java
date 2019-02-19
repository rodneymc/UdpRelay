package com.daftdroid.android.udprelay;

import android.content.Context;

import java.io.Serializable;


public class VpnSpecification implements Serializable, RelayConfiguration {

    static final long serialVersionUID = 1L;
    public VpnSpecification(Storage loader) {
        this.loader = loader;
    }

    public static final String INTENT_ID =
            VpnSpecification.class.getCanonicalName().concat(".ID");

    private int id;

    private String title;

    // The relay specification. This is allowed to be null, in the case where we are just using
    // the app to generate OpenVPN config files, and not actually using the app to relay
    // the connection to the VPN.

    private RelaySpec relaySpec;

    // The openVPN configuration. Used for generating the OpenVPN config.

    private String vpn; // The common name of the hub

    // Error status saved separately so we don't have to resave the whole class on error.
    // Also so we can save an error async, if there is no activity running.
    private transient Throwable error;
    private transient Storage loader;

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
    public void setSpec(RelaySpec relaySpec) {
        this.relaySpec = relaySpec;
    }
    public RelaySpec getRelaySpec() {
        return relaySpec;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return this.title;
    }
    public void setVpn(String vpn) {
        this.vpn = vpn;
    }
    public String getVpn() {
        return vpn;
    }
    public void setError(Throwable error, boolean saveNow) {
        this.error = error;
        if (saveNow && loader != null) {
            loader.saveError(this);
        }
    }
    public Throwable error() {
        return error;
    }
    public void setLoader(Storage loader) {
        this.loader = loader;
    }
    public Storage getStorage() {
        return loader;
    }
}
