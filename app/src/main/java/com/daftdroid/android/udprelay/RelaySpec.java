package com.daftdroid.android.udprelay;

import android.os.Build;

import java.io.Serializable;
import java.util.Objects;

public class RelaySpec implements Serializable {

    static final long serialVersionUID = 1L;

    public static final String EPHEMERAL_IP = null;
    public static final int EPHEMERAL_PORT = 0;
    public static final int WELL_KNOWN_PORT_OPENVPN = 1194;

    private final String chanAlocalIP;
    private final int chanAlocalPort;
    private final String chanBlocalIP;
    private final int chanBlocalPort;
    private final String chanBremoteIP;
    private final int chanBremotePort;
    private final String chanAremoteIP;
    private final int chanAremotePort;

    public String getChanALocalIP() {return chanAlocalIP;}
    public int getChanALocalPort() {return chanAlocalPort;}
    public String getChanBLocalIP() {return chanBlocalIP;}
    public int getChanBLocalPort() {return chanBlocalPort;}
    public String getChanBRemoteIP() {return chanBremoteIP;}
    public int getChanBRemotePort() {return chanBremotePort;}
    public String getChanARemoteIP() {return chanAremoteIP;}
    public int getChanARemotePort() {return chanAremotePort;}

    /*
        Constructor which takes all of the parameters to full specify the
        relay
     */
    public RelaySpec(String chanAlocalIP, int chanAlocalPort,
                     String chanAremoteIP, int chanAremotePort,
                     String chanBlocalIP, int chanBlocalPort,
                     String chanBremoteIP, int chanBremotePort) {
        this.chanAlocalIP = chanAlocalIP;
        this.chanAlocalPort = chanAlocalPort;
        this.chanBlocalIP = chanBlocalIP;
        this.chanBlocalPort = chanBlocalPort;
        this.chanBremoteIP = chanBremoteIP;
        this.chanBremotePort = chanBremotePort;
        this.chanAremoteIP = chanAremoteIP;
        this.chanAremotePort = chanAremotePort;
    }


     @Override
    public boolean equals(Object other) {
        if (!(other instanceof RelaySpec)) {
            return false;
        }
        RelaySpec o = (RelaySpec) other;

        return o == this ||
                        o.chanAlocalIP == chanAlocalIP &&
                        o.chanAlocalPort == chanAlocalPort &&
                        o.chanAremoteIP == chanAremoteIP &&
                        o.chanAremotePort == chanAremotePort &&
                        o.chanBlocalIP == chanBlocalIP &&
                        o.chanBlocalPort == chanBlocalPort &&
                        o.chanBremoteIP == chanBlocalIP &&
                        o.chanBremotePort == chanBremotePort;
    }
    @Override
    public int hashCode() {

        if (Build.VERSION.SDK_INT >= 19) {
            return Objects.hash(chanAremoteIP, chanAremotePort, chanAlocalIP, chanAlocalPort,
                    chanBremoteIP, chanBremotePort, chanBlocalIP, chanBlocalPort);
        }

        return chanAremoteIP.hashCode() + chanAremotePort + chanAlocalIP.hashCode() + chanAlocalPort +
                chanBremoteIP.hashCode() + chanBremotePort + chanBlocalIP.hashCode() + chanBremotePort;
    }
}
