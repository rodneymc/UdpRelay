package com.daftdroid.simpleapps.udprelay;

import android.app.IntentService;
import android.content.Intent;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NetworkService extends IntentService {


    /**
     * From https://developer.android.com/guide/components/services#java
     * A constructor is required, and must call the super <code><a href="/reference/android/app/IntentService.html#IntentService(java.lang.String)">IntentService(String)</a></code>
     * constructor with a name for the worker thread.
     */
    public NetworkService() {
        super("UDP Relay Service");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }

    private List<Relay> registeredRelays = new ArrayList<Relay>();
    private List<Relay> newRelays = new ArrayList<Relay>();

    private Selector selector;

    public Selector selector()
    {
        return selector;
    }

    private static NetworkService singleton;
    private volatile boolean changed;
    private volatile boolean running; // Set before launching intent, cleared before exiting intent
    public static NetworkService getNetworkThread()
    {
        return singleton;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            selector = Selector.open();
        } catch (IOException e) {
            // Not sure what would cause this, apart from a complete network failure or running
            // out of handles.

            e.printStackTrace();
            return;
        }

        while (true) {
            try {

                if (changed) {
                    // See if any new relays have appeared

                    List<Relay> newRelaysCpy = null;

                    synchronized (this)
                    {
                        // Add all the new relays into the registeredRelays list, and create
                        // a new blank list of relays to register, however keep a reference to the
                        // old one (newRelays) - these are to be initialized outside the syncrhonized block)

                        if (this.newRelays.size() > 0)
                        {
                            newRelaysCpy = this.newRelays;
                            this.newRelays = new ArrayList<Relay>(1);
                        }
                        changed = false;

                        // If there are no relays, we can quit
                        if (registeredRelays.size() == 0)
                            break;
                    }

                    if (newRelaysCpy != null)
                    {
                        for (Relay r: newRelaysCpy)
                        {
                            r.initialize();
                        }
                    }
                }
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

        try {selector.close();}
        catch (IOException e) {}
    }

    public synchronized void addRelay(Relay relay)
    {
        /*
            If there are no registered relays, queue up a new work request, which will remain
            active until this drops to zero again
         */
        if (registeredRelays.size() == 0)
        {
            // Intent is just a dummy we don't use it to communicate with righ tnow
            Intent i = new Intent(this, NetworkService.class);
            startService(i);
        }

        newRelays.add(relay);
        registeredRelays.add(relay);
        changed = true;
        selector.wakeup();
    }
    public synchronized void removeRelay(Relay r)
    {
        registeredRelays.remove(r);
        changed = true;
        selector.wakeup();
    }
}
