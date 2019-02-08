package com.daftdroid.android.udprelay.ui_components;

import android.graphics.Color;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;

import com.daftdroid.android.udprelay.R;

public class Ipv4 {

    private final View view;

    public Ipv4(View view) {
        this.view = view;

        for (EditText e: getIpBoxes()) {
            e.addTextChangedListener(new EditTextChangedListener(e) {
                private boolean recursion;

                @Override
                public void onTextChanged(EditText target, Editable s) {
                    if (!recursion) {
                        recursion = true;
                        ipByteEdited((EditText) target);
                        recursion = false;
                    }

                }
            });
        }
    }

    private void ipByteEdited(EditText target) {
        String txt = target.getText().toString();
        boolean moveNext = false;
        int len = txt.length();

        if (txt.endsWith(".")) {
            if (len == 1) {
                // The user entered just a dot on its own. Disallow this.
                txt = "";
                len = 0;
            }
            else {
                moveNext = true;
                txt = txt.substring(0, txt.length() - 1); // Strip the trailing dot
            }
        }

        int dotindex;
        if ((dotindex = txt.indexOf(".")) >= 0) {
            // The user tried to put a dot in the middle of the number, disallow this
            // by setting it back to what it was before.

            txt = txt.substring(0, dotindex) + txt.substring(dotindex+1, len);
        }

        // Now validate
        int val;
        if (len == 0) {
            val = 999; // an arbitrary invalid value
        } else {
            val = Integer.parseInt(txt); // there should be no way this is not an int now
        }

        if (val > 255) {
            target.setBackgroundColor(Color.YELLOW);
        }
        else {
            // This byte is valid, in the sense that it is at least in the range 0-255,
            // though actually the last byte shouldn't be zero but that's more about the
            // address value as a hole being invalid.

            target.setBackgroundColor(Color.WHITE);

            if (moveNext) {
                moveToNextFocus();
            }
        }

        // Update with any changes we made
        target.getText().replace(0, len, txt);
    }

    private void portEdited(EditText target) {

    }
    private EditText[] getIpBoxes() {
        EditText e[] = new EditText[4];
        e[0] = view.findViewById(R.id.IPbyte0);
        e[1] = view.findViewById(R.id.IPbyte1);
        e[2] = view.findViewById(R.id.IPbyte2);
        e[3] = view.findViewById(R.id.IPbyte3);

        return e;
    }

    private void moveToNextFocus() {
        // TODO
    }
}
