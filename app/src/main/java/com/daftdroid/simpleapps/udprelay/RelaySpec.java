package com.daftdroid.simpleapps.udprelay;

import android.os.Build;

import java.util.Objects;

public class RelaySpec {
    public static final String EPHEMERAL_IP = null;
    public static final int EPHEMERAL_PORT = 0;
    public static final int WELL_KNOWN_PORT = 1194; // Assuming OpenVPN

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
                     String chanBlocalIP, int chanBlocalPort,
                     String chanBremoteIP, int chanBremotePort,
                     String chanAremoteIP, int chanAremotePort) {
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
        Constructor which assumes ephermal IP and port for outgoing traffic. UDP is connection-less
        so this implies there is a client-server relationship established within the higher
        levels of the prototcol. For OpenVPN over UDP, this relationship is established by the use
        of TLS (tls-server / tls-client in the config files). Non-TLS UDP connections are peer-to-peer
        and do not support multiple "clients".
     */
    public RelaySpec(String name, String chanAlocalIP, int chanAlocalPort,
                     String chanBremoteIP, int chanBremotePort)
    {
        this (name, chanAlocalIP, chanAlocalPort,
                EPHEMERAL_IP, EPHEMERAL_PORT,
                chanBremoteIP, chanBremotePort,
                EPHEMERAL_IP, EPHEMERAL_PORT);
    }

    /*
        Constructor assuming a client-server type set-up for OpenVPN (see above comments
        regarding TLS) - and both the server and the local replication of the server
        will listen on a well known port for OpenVPN
     */
    public RelaySpec (String name, String chanAlocalIP, String chanBremoteIP)
    {
        this (name, chanAlocalIP, WELL_KNOWN_PORT,
                chanBremoteIP, WELL_KNOWN_PORT);
    }

    /*
        Examples - you can hard-code some settings here in your private branch
        in order to get if you want
     */

    public static final RelaySpec[] exampleRelays=  {
        // Example using the simplist constructor
        new RelaySpec("demo 1", "192.168.42.129", "203.0.113.10"),

        // Example where we must specify the dest ports
        new RelaySpec("demo 2", "192.168.42.129",
                1195, "203.0.113.10", 1196),

        // Example where we specify everything, note that the parameter chanBlocalIP is
        // very unlikely to be anything other than RelaySpec.EPHEMERAL_IP, unless you are
        // specifying a particular gateway - if so you need to know its IP at runtime.

        new RelaySpec("demo 3", "192.168.42.129", 1195,
                RelaySpec.EPHEMERAL_IP, 7000,
                "203.0.113.10", 1196,
                "192.168.42.150", 7000)
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
