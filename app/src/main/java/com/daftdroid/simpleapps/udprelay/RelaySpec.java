package com.daftdroid.simpleapps.udprelay;

public class RelaySpec {
    public static final String EPHEMERAL_IP = null;
    public static final int EPHEMERAL_PORT = 0;
    public static final int WELL_KNOWN_PORT = 1194; // Assuming OpenVPN

    /* Conventions
                         local ip        public ip
    client ---------------- Android Device -------------- Server

     */

    private final String name;
    private final String android_local_ip;
    private final int android_local_port;
    private final String android_public_ip;
    private final int android_public_port;
    private final String server_ip;
    private final int server_port;
    private final String client_ip;
    private final int client_port;

    /* Getters */
    public String getName() {return name;}
    public String getAndroidLocalIP() {return android_local_ip;}
    public int getAndroidLocalPort() {return android_local_port;}
    public String getAndroidPublicIP() {return android_public_ip;}
    public int getAndroidPublicPort() {return android_public_port;}
    public String getServerIP() {return server_ip;}
    public int getServerPort() {return server_port;}
    public String getClientIP() {return client_ip;}
    public int getClientPort() {return client_port;}

    /*
        Constructor which takes all of the parameters to full specify the
        relay
     */
    public RelaySpec(String name, String android_local_ip, int android_local_port,
                     String android_public_ip, int android_public_port,
                     String server_ip, int server_port,
                     String client_ip, int client_port) {
        this.name = name;
        this.android_local_ip = android_local_ip;
        this.android_local_port = android_local_port;
        this.android_public_ip = android_public_ip;
        this.android_public_port = android_public_port;
        this.server_ip = server_ip;
        this.server_port = server_port;
        this.client_ip = client_ip;
        this.client_port = client_port;
    }

    /*
        Constructor which assumes ephermal IP and port for outgoing traffic. UDP is connection-less
        so this implies there is a client-server relationship established within the higher
        levels of the prototcol. For OpenVPN over UDP, this relationship is established by the use
        of TLS (tls-server / tls-client in the config files). Non-TLS UDP connections are peer-to-peer
        and do not support multiple "clients".
     */
    public RelaySpec(String name, String android_local_ip, int android_local_port,
                     String server_ip, int server_port)
    {
        this (name, android_local_ip, android_local_port,
                EPHEMERAL_IP, EPHEMERAL_PORT,
                server_ip, server_port,
                EPHEMERAL_IP, EPHEMERAL_PORT);
    }

    /*
        Constructor assuming a client-server type set-up for OpenVPN (see above comments
        regarding TLS) - and both the server and the local replication of the server
        will listen on a well known port for OpenVPN
     */
    public RelaySpec (String name, String android_local_ip, String server_ip)
    {
        this (name, android_local_ip, WELL_KNOWN_PORT,
                server_ip, WELL_KNOWN_PORT);
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

        // Example where we specify everything, note that the parameter android_public_ip is
        // very unlikely to be anything other than RelaySpec.EPHEMERAL_IP, unless you are
        // specifying a particular gateway - if so you need to know its IP at runtime.

        new RelaySpec("demo 3", "192.168.42.129", 1195,
                RelaySpec.EPHEMERAL_IP, 7000,
                "203.0.113.10", 1196,
                "192.168.42.150", 7000)
    };

}
