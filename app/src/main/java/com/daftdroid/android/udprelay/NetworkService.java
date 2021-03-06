package com.daftdroid.android.udprelay;

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

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NetworkService extends IntentService {

    public static final String BROADCAST_ACTION =
            "com.daftdroid.android.udprelay.NETSERVICE_BROADCAST";
    public static final String BROADCAST_RLYNUM =
            "com.daftdroid.android.udprelay.NETSERVICE_RLYNUM";


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
    private boolean finishing;

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
            while (processRegisteredRelays()) {
            }

            if (finishing) {
                /*
                    If the above call decided we were done, then we are done, there may be a new
                    one being launched (it's an unlikely race but it could happen) but this instance
                    is done.
                 */
                break;
            }

            try {
                selector.select();
                Set<SelectionKey> keySet = selector.selectedKeys();

                for (SelectionKey key : keySet) {
                    Object attachment;

                    if ((attachment = key.attachment()) instanceof Relay) {
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

    private boolean processRegisteredRelays() {
        List<Relay> newRelaysCpy = null;

        synchronized(NetworkService.class) {

            if (changed) {
                // See if any new relays have appeared


                // Add all the new relays into the registeredRelays list, and create
                // a new blank list of relays to register, however keep a reference to the
                // old one (newRelays) - these are to be initialized outside the syncrhonized block)

                if (newRelays != null && newRelays.size() > 0) {
                    // Once we leave the syncrhonized block, newRelays could be set
                    // again by another thread.

                    newRelaysCpy = newRelays;
                    newRelays = null;

                    for (Relay r : newRelaysCpy) {
                        registeredRelays.add(r);
                    }
                }
                changed = false;
            }
        }

        if (newRelaysCpy != null) {
            for (Relay r: newRelaysCpy) {
                r.initialize(selector);
                broadcastStatus(r, Relay.ERROR_NONE, "RUNNING");
            }
        }

        for (Iterator<Relay> itr = registeredRelays.iterator(); itr.hasNext();) {
            Relay r = itr.next();

            boolean hasError = r.hasError();

            if (hasError) {
                if (r.softError()) {
                    new ErrorCountdown(this, r).run();
                } else {
                    r.stopRelay();
                    broadcastStatus(r, Relay.ERROR_HARD, "Error");
                }
            }
            if (hasError || r.stopping()) {
                itr.remove();
                r.close();
            }
        }

        // If all the relays are gone, and the UI hasn't asyncronously requested some new ones
        // (by setting "changed"), then we can quit, indicated by setting singleton null.

        synchronized (NetworkService.class) {
            if (registeredRelays.size() == 0 && !changed) {
                singleton = null; // tell async threads
                finishing = true; // tell the caller
                registeredRelays = null;
            }
            return changed; // return the value of changed while we were syncrhonized
        }
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
    public void addRelay(Relay r) {
        uiAddRelay(this, r);
    }

    public static synchronized void wakeup() {

     if (singleton != null) {
           changed = true;
           singleton.selector.wakeup();
        }
    }

    public static synchronized List<Relay> getActiveRelays() {
        if (singleton == null) {
            return null;
        } else {
            return new ArrayList<Relay>(singleton.registeredRelays);
        }
    }

    void broadcastStatus(Relay r, int errlevel, String msg) {

        Intent localIntent = new Intent(BROADCAST_ACTION)
                .putExtra(BROADCAST_RLYNUM, r.getUniqueId());

        r.setErrorLevel(errlevel);
        r.setStatusMessage(msg);

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
