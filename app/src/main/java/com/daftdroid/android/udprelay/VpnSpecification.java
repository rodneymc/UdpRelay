package com.daftdroid.android.udprelay;

import android.content.Context;

import java.io.Serializable;


public class VpnSpecification implements Serializable {

    static final long serialVersionUID = 1L;
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

    // So we can save error flag to non-volatile and keep track of an error condition
    // without keeping the service active.
    private Throwable error;

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
    public void setError(Throwable error) {
        this.error = error;
    }
    public Throwable error() {
        return error;
    }
}
