package com.daftdroid.android.udprelay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.daftdroid.android.udprelay.ui_components.UiComponent;

import java.io.IOException;
import java.util.List;

public class RelayButton extends UiComponent {

    private Relay relay;
    private VpnSpecification spec;
    private final Button startStopButton;
    private final Context context;

    public RelayButton(Activity act, int placeHolderId, VpnSpecification spec) {

        super(act, placeHolderId, R.layout.relaybutton);
        this.context = act;

        this.spec = spec;

        startStopButton = (Button) getViewGroup().findViewById(R.id.rly_main_button);

        updateStartStopButton();
        updateRelay();

        if (relay != null) {
            updateText();
        }

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickStartStop(v);
            }
        });

        Button b = (Button) getViewGroup().findViewById(R.id.delete_rly);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO don't allow while relay is running

                ViewGroup thisView = getViewGroup();
                ((ViewGroup) thisView.getParent()).removeView(thisView);

                new Storage(act.getFilesDir()).delete(spec);
            }
        });

        b = (Button) getViewGroup().findViewById(R.id.edit_rly);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO don't allow while relay is running
                Intent intent = new Intent(act, GenericUDPrelay.class);
                intent.putExtra(VpnSpecification.INTENT_ID, spec.getId());
                act.startActivityForResult(intent, spec.getId());
            }
        });

    }

    public void onClickStartStop(View v) {
        try {

            if (relay == null) {
                relay = new Relay(spec.getRelaySpec());
                relay.startRelay();
                NetworkService.uiAddRelay(context, relay);
            } else {
                relay.stopRelay();
                NetworkService.wakeup();
                relay = null;
                startStopButton.setText(spec.getTitle());
                startStopButton.setTextColor(Color.BLACK);
            }
        } catch (IOException e) {
            startStopButton.setText(e.getLocalizedMessage()); // TODO
        }
    }


    public void updateText() {
        setError(relay.getErrorLevel(), relay.getStatusMessage());
    }

    /*
        Update our reference to relay. This is needed because either
        1) A new ui instance was created, while relays were already running
        2) The network service decided to replace the relay with a new one after an error.
     */
    public void updateRelay() {
        // A list of running, non errored relays
        List<Relay> runningList = NetworkService.getActiveRelays();
        // See if we can find a relay running already that matches the spec
        if (runningList != null) {
            for (Relay r: runningList) {
                if (r.getSpec().equals(spec.getRelaySpec())) {
                    relay = r;
                    break;
                }
            }
        }
    }

    public void updateSpec(VpnSpecification spec) {
        this.spec = spec;
        updateStartStopButton();
    }

    private void updateStartStopButton() {
        startStopButton.setText(spec.getTitle());
    }

    private void setError(int severity, final String msg) {
        startStopButton.setText(spec.getTitle() + " [" + msg + "]");

        if (severity == Relay.ERROR_HARD) {
            startStopButton.setTextColor(Color.RED);
        } else if (severity == Relay.ERROR_SOFT) {
            startStopButton.setTextColor(0xffffa500);
        } else {
            startStopButton.setTextColor(Color.BLACK);
        }
    }

     public Relay getRelay() {
        return relay;
    }

    // TODO not sure if we need to return something meaningful here?
    public int getSpecId() {
        return spec.getId();
    }

}
