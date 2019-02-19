package com.daftdroid.android.udprelay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.daftdroid.android.udprelay.R;
import com.daftdroid.android.udprelay.Storage;
import com.daftdroid.android.udprelay.VpnSpecification;
import com.daftdroid.android.udprelay.ui_components.UiComponent;

import java.util.List;

public abstract class ConfigurationActivity extends Activity {

    private int relayId;
    private List<UiComponent> uiComponents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Storage storage = new Storage(getFilesDir(), getCacheDir());

        setContentView(getActivityLayout());
        setTitle(getActivityTitle());

        final Button okButton = findViewById(R.id.okbutton);

        final EditText titleText = findViewById(R.id.configTitle);

        uiComponents = getUiComponents(titleText, okButton);

        // Link the focusable items together before initialising them with data,
        // or the linking will not work. // TODO this should work though...
        int componentCount = uiComponents.size();

        for (int i = 1; i < componentCount; i ++) {
            uiComponents.get(i).linkFocusForward(uiComponents.get(i-1));
        }


        if (savedInstanceState != null) {
            // Restore the edited state of the config, which might not be the same
            // as the actual stored config.

            relayId = savedInstanceState.getInt("id");
            int focusParent = savedInstanceState.getInt("focusParent");
            int focusChild = savedInstanceState.getInt("focusChild");

            for (UiComponent u: uiComponents) {
                u.putRawUserInputState(savedInstanceState.getString(Integer.toString(u.getId())));

                if (u.getId() == focusParent) {
                    u.setFocusToChild(focusChild);
                }
            }



        } else {
            // Load the saved config, if there is one

            relayId = getIntent().getIntExtra(VpnSpecification.INTENT_ID, 0);
            final VpnSpecification loadedSpec;

            loadedSpec = storage.load(relayId);

            if (loadedSpec != null) {

                processLoadedSpec(loadedSpec);

                titleText.setText(loadedSpec.getTitle());
            } else {
                relayId = storage.getNewSpecId();
                initialiseBlankSpec();

            }

            // Set the focus to the dummy element, to prevent the focus going somewhere annoying
            // and / or the keyboard auto opening which is also annoying
            findViewById(R.id.dummyfocus).requestFocus();

        }

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (UiComponent u: uiComponents) {
                    u.validate();
                }

                // If there is an error, move the focus to the first errored view.
                boolean hasError = false;
                for (UiComponent u: uiComponents) {
                    if (u.hasError()) {
                        u.requestFocusToErroredView();
                        hasError = true;
                        break;
                    }
                }

                if (!hasError) {
                    String name = ((EditText) findViewById(R.id.configTitle)).getText().toString();
                    VpnSpecification spec = new VpnSpecification(storage);
                    spec.setTitle(name);
                    spec.setId(relayId);

                    prepareSpecForSave(spec);

                    storage.save(spec);

                    Intent retData = new Intent();
                    retData.putExtra(VpnSpecification.INTENT_ID, relayId);
                    setResult(RESULT_OK, retData);
                    finish();
                }
            }
        });
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt("id", relayId);
        boolean focusFound = false;
        int focusChildIndex = -1;
        int focusParentIndex = -1;

        for (UiComponent u: uiComponents) {
            outState.putString(Integer.toString(u.getId()), u.getRawUserInputState());

            if (!focusFound && (focusChildIndex = u.getChildFocusIndex()) != -1) {
                focusParentIndex = u.getId();
                focusFound = true;
            }
        }

        outState.putInt("focusParent", focusParentIndex);
        outState.putInt("focusChild", focusChildIndex);
    }

    protected abstract List<UiComponent> getUiComponents(View titleText, View okButton);
    protected abstract void processLoadedSpec(VpnSpecification loadedSpec);
    protected abstract void initialiseBlankSpec();
    protected abstract void prepareSpecForSave(VpnSpecification spec);
    protected abstract String getActivityTitle();
    protected abstract int getActivityLayout();
}
