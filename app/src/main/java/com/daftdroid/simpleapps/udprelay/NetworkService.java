package com.daftdroid.simpleapps.udprelay;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
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

        if (singleton != null) {
            /*
                If it set already, then another instance's onCreate method must have
                been called.
             */
            throw new IllegalStateException("Single-instance-of-service logic failure");
        }
        singleton = this;
    }

    private List<Relay> registeredRelays = new ArrayList<Relay>();

    private Selector selector;

    /*
        Static data, modifiable from multiple threads by syncrhonizing on class.
     */
    private static NetworkService singleton;
    private static List<Relay> newRelays; // Static, incase a network service instance isn't created yet
    private static volatile boolean changed;

    private volatile boolean finishing;

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

        /*
            Use a notification to make it a foreground service
         */
        final String CHANNEL_ID = "com.daftdroid.udprelay";

        if (Build.VERSION.SDK_INT >= 26) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, "UDP Relay", importance);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        Notification.Builder builder = new Notification.Builder(getBaseContext())
                .setSmallIcon(R.drawable.notificationinert)
                .setContentTitle("UDP Relay")
                .setContentText("Relay service running")
                .setProgress(0, 0, false)
                .setDefaults(0); // No sound etc?

        if (Build.VERSION.SDK_INT >= 26) {
            builder.setChannelId(CHANNEL_ID);
        }

        if (Build.VERSION.SDK_INT >= 16) {
            startForeground(1, builder.build());
        } else {
            startForeground(1, builder.getNotification());
        }

        /*
            Make sure the first time round we get into the if (changed) block, where we might
            break and finish, if there is nothing to do.
         */
        changed = true;

        while (true) {
            try {

                List<Relay> newRelaysCpy = null;

                synchronized (NetworkService.class)
                {
                    if (changed) {
                        // See if any new relays have appeared


                        // Add all the new relays into the registeredRelays list, and create
                        // a new blank list of relays to register, however keep a reference to the
                        // old one (newRelays) - these are to be initialized outside the syncrhonized block)

                        if (newRelays != null && newRelays.size() > 0)
                        {
                            // Once we leave the syncrhonized block, newRelays could be set
                            // again by another thread.

                            newRelaysCpy = newRelays;
                            newRelays = null;

                            for (Relay r: newRelaysCpy) {
                                registeredRelays.add(r);
                            }
                        }

                        for (Iterator<Relay> itr = registeredRelays.iterator(); itr.hasNext();) {
                            Relay r = itr.next();
                            if (r.stopping()) {
                                itr.remove();
                                r.close();
                            }

                        }
                        changed = false;

                        // If there are no relays, we can quit
                        if (registeredRelays.size() == 0) {
                            finishing = true;
                            registeredRelays = null;
                            break;
                        }
                    }

                    if (newRelaysCpy != null)
                    {
                        for (Relay r: newRelaysCpy)
                        {
                            r.initialize(selector);
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

    public static synchronized  void uiAddRelay(Context ui, Relay r) {

        /*
            It was not possible to attach the relay to an existing service. There may be
            a new service creation already in the pipeline (as indicated by there already
            being an entry in newServiceRelayList) if not we need to start one.
         */

        if (newRelays == null && (singleton == null || singleton.finishing)) {
            // A new service needs to be created.

            ui.startService(new Intent(ui, NetworkService.class));
        } else {
            changed = true;
            singleton.selector.wakeup();
        }

        if (newRelays == null) {
            newRelays = new ArrayList<Relay>(1);
        }
        newRelays.add(r);
    }

    public static synchronized void wakeup() {

     if (singleton != null) {
           changed = true;
           singleton.selector.wakeup();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /*
            Don't pull the rug while static methods have the lock on newRelays otherwise we have
            a small window for NPE on code like if (singleton != null && !singleton.finishing)
         */

        synchronized (NetworkService.class) {
            if (singleton == null) {
                /*
                    If it was null already, then another instance's onDestroy method must have
                    been called.
                 */
                throw new IllegalStateException("Single-instance-of-service logic failure");
            }
            singleton = null;
        }
    }

    public static synchronized List<Relay> getActiveRelays() {
        if (singleton == null) {
            return null;
        } else {
            return new ArrayList<Relay>(singleton.registeredRelays);
        }
    }
}
