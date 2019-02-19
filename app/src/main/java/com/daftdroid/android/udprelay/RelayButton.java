package com.daftdroid.android.udprelay;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.daftdroid.android.udprelay.config_providers.GenericUDPrelay;
import com.daftdroid.android.udprelay.ui_components.UiComponentViewGroup;

import java.util.List;

public class RelayButton {

    private Relay relay;
    private VpnSpecification spec;
    private final Button titleText;
    private final MainActivity context;
    private boolean hardError;
    private final Button playStopButton;

    public RelayButton(MainActivity act, int placeHolderId, VpnSpecification spec) {

        final ViewGroup viewGroup;

        viewGroup = UiComponentViewGroup.doInflate(act, placeHolderId, R.layout.relaybutton);
        this.context = act;

        this.spec = spec;

        titleText = (Button) viewGroup.findViewById(R.id.rly_main_button);
        playStopButton = viewGroup.findViewById(R.id.startstop_rly);
        Typeface fontAwesome = act.getFontAwesome();

        updateRelay();

        updateTextAndButtons();

        playStopButton.setTypeface(fontAwesome);
        playStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickStartStop(v);
            }
        });

        Button b = (Button) viewGroup.findViewById(R.id.delete_rly);
        b.setTypeface(fontAwesome);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO don't allow while relay is running

                ((ViewGroup) viewGroup.getParent()).removeView(viewGroup);

                act.getStorage().delete(spec);
            }
        });

        b = (Button) viewGroup.findViewById(R.id.edit_rly);
        b.setTypeface(fontAwesome);
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

            if (!running()) {
                spec.getStorage().deleteError(spec);
                relay = new Relay(spec);
                relay.startRelay();
                NetworkService.uiAddRelay(context, relay);
            } else {
                relay.stopRelay();
                NetworkService.wakeup();
                relay = null;
                titleText.setText(spec.getTitle());
                titleText.setTextColor(Color.WHITE);
            }
        } catch (RelayException e) {
            context.getStorage().saveError(spec.getId(), e);
            setError(Relay.ERROR_HARD, e.getMessage());
        }
        updateTextAndButtons();
    }


    public void updateTextAndButtons() {

        if (relay != null) {
            setError(relay.getErrorLevel(), relay.getStatusMessage());
        } else {
            // Update the error status from the storage
            Throwable error = spec.error();

            if (error == null) {
                setError(Relay.ERROR_NONE, null);
            } else {
                setError(Relay.ERROR_HARD, error.getMessage());
            }
        }

        // Update the play / pause button
        if (running()) {
            playStopButton.setText(context.getResources().getString(R.string.icon_pause));
        } else {
            playStopButton.setText(context.getResources().getString(R.string.icon_play));
        }

    }
    private boolean running() {
        return !(hardError || relay == null || relay.stopping());
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
            int id = spec.getId();
            for (Relay r: runningList) {
                if (r.getConfigId() == id) {
                    relay = r;
                    break;
                }
            }
        }
    }

    public void updateSpec(VpnSpecification spec) {
        this.spec = spec;

        // For an update, we must stop any existing relay, the user can start a new one
        // with the new config by pressing play.

        if (relay != null) {
            relay.stopRelay();
            relay = null;
        }
        updateTextAndButtons();
    }

    private void setError(int severity, final String msg) {
        String title = spec.getTitle();

        if (msg != null) {
            title += " [" + msg + "]";
        }
        titleText.setText(title);

        hardError = false;

        if (severity == Relay.ERROR_HARD) {
            titleText.setTextColor(Color.RED);
            hardError = true;
        } else if (severity == Relay.ERROR_SOFT) {
            titleText.setTextColor(0xffffa500);
        } else {
            titleText.setTextColor(Color.WHITE);
        }
    }

    // TODO not sure if we need to return something meaningful here?
    public int getSpecId() {
        return spec.getId();
    }

    public VpnSpecification getVpnSpec() {
        return spec;
    }

}
