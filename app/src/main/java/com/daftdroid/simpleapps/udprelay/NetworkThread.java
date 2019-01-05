package com.daftdroid.simpleapps.udprelay;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

public class NetworkThread extends Thread {
    private final Selector selector;

    public NetworkThread() throws IOException {
        selector = Selector.open();
    }
    public Selector selector()
    {
        return selector;
    }

    public static NetworkThread getNetworkThread()
    {
        return null; // TODO
    }

}
