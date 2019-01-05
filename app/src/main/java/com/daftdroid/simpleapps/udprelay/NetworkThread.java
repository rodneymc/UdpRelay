package com.daftdroid.simpleapps.udprelay;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

public class NetworkThread extends Thread {

    private static final NetworkThread singleton;
    private volatile boolean done;

    static
    {
        NetworkThread n = null;
        try {
            n = new NetworkThread();
        } catch (IOException e) {
            // I don't know what would cause us to get here.
            throw new IllegalStateException("Network thread cannot start");
        }
        finally {
            singleton = n;
        }
    }

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
        return singleton;
    }

    @Override
    public void run()
    {
        while (!done) {
            try {
                selector.select();
                Set<SelectionKey> keySet = selector.selectedKeys();

                for (SelectionKey key: keySet)
                {
                    Object attachment;

                    if ((attachment = key.attachment()) instanceof Relay)
                    {
                        Relay r = (Relay) attachment;
                        r.select(key);
                    }
                }

            } catch (IOException e) {
                throw new IllegalStateException("IOException here is a TODO", e); // TODO
            }
        }
    }

    public void terminate()
    {
        done = true;
        interrupt();
        try {selector.close();}
        catch (IOException e) {}
    }
}
