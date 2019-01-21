package com.daftdroid.android.udprelay;

import java.io.IOException;

public class ErrorCountdown extends Thread {
    private final NetworkService service;
    private final Relay relay;

    public ErrorCountdown(NetworkService service, Relay relay) {
        this.service = service;
        this.relay = relay;
    }

    /*
        Executes a 5 second countdown, then restarts the relay
     */
    @Override
    public void run() {
        int countdown = 5;

        do {
            service.broadcastStatus(relay, Relay.ERROR_SOFT, "ERROR ("+countdown+")");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        } while (--countdown > 0);

        Relay replacement;

        try {
            // To restart a relay, we create a replacement from it
            replacement = new Relay(relay);

            // Now "stop" the old one, which will allow it to be GCd
            // TODO this is not intuitive whats going on here
            relay.stopRelay();

        } catch (IOException e) {
            // We don't expect an IO exception to actually occur?
            service.broadcastStatus(relay, Relay.ERROR_HARD, "BUG");
            return;
        }

        service.addRelay(replacement);
    }
}
