package com.daftdroid.simpleapps.udprelay;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NetworkThread extends Thread {

    private static final NetworkThread singleton;
    private volatile boolean done;
    private List<Relay> registeredRelays = new ArrayList<Relay>();
    private List<Relay> relaysToRegister = new ArrayList<Relay>();

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

                // See if any new relays have appeared

                List<Relay> newRelays = null;

                synchronized (this)
                {
                    // Add all the new relays into the registeredRelays list, and create
                    // a new blank list of relays to register, however keep a reference to the
                    // old one (newRelays) - these are to be initialized outside the syncrhonized block)

                    if (relaysToRegister.size() > 0)
                    {
                        newRelays = relaysToRegister;
                        for (Relay r: newRelays)
                        {
                            if (!registeredRelays.contains(r)) {
                                registeredRelays.add(r);
                            }
                        }

                        relaysToRegister = new ArrayList<Relay>(1);
                    }
                }

                if (newRelays != null)
                {
                    for (Relay r: newRelays)
                    {
                        r.initialize();
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

    public synchronized void addRelay(Relay relay)
    {
        relaysToRegister.add(relay);
        selector.wakeup();
    }
}
