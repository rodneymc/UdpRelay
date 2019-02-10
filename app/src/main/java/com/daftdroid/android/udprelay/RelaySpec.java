package com.daftdroid.android.udprelay;

import android.os.Build;

import java.io.Serializable;
import java.util.Objects;

public class RelaySpec implements Serializable {

    static final long serialVersionUID = 1L;

    public static final String EPHEMERAL_IP = null;
    public static final int EPHEMERAL_PORT = 0;
    public static final int WELL_KNOWN_PORT_OPENVPN = 1194;

    private final String name;
    private final String chanAlocalIP;
    private final int chanAlocalPort;
    private final String chanBlocalIP;
    private final int chanBlocalPort;
    private final String chanBremoteIP;
    private final int chanBremotePort;
    private final String chanAremoteIP;
    private final int chanAremotePort;

    /* Getters */
    public String getName() {return name;}
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
    public RelaySpec(String name, String chanAlocalIP, int chanAlocalPort,
                     String chanAremoteIP, int chanAremotePort,
                     String chanBlocalIP, int chanBlocalPort,
                     String chanBremoteIP, int chanBremotePort) {
        this.name = name;
        this.chanAlocalIP = chanAlocalIP;
        this.chanAlocalPort = chanAlocalPort;
        this.chanBlocalIP = chanBlocalIP;
        this.chanBlocalPort = chanBlocalPort;
        this.chanBremoteIP = chanBremoteIP;
        this.chanBremotePort = chanBremotePort;
        this.chanAremoteIP = chanAremoteIP;
        this.chanAremotePort = chanAremotePort;
    }


    /*
        Examples - you can hard-code some settings here in your private branch
        in order to get if you want
     */

    public static final RelaySpec[] exampleRelays=  {

        // Example where we specify everything, note that the parameter chanBlocalIP is
        // very unlikely to be anything other than RelaySpec.EPHEMERAL_IP, unless you are
        // specifying a particular gateway - if so you need to know its IP at runtime.

        new RelaySpec("demo", "192.168.42.129", 1195, // our LAN addr
                "192.168.42.150", 7000, // "client" on the LAN
                 RelaySpec.EPHEMERAL_IP, 7000, // our GSM address
                "203.0.113.10", 1196) // remote "server"
    };

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
