package com.daftdroid.android.udprelay.ui_components;

import android.graphics.Color;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

public class Ipv4 extends UiComponent {

    private final ViewGroup viewGroup;
    private final EditText[] ipBoxes = new EditText[4];
    private final View focusLast;
    private final View focusFirst;
    private CheckBox checkBox;

    public Ipv4(ViewGroup viewGroup) {
        this.viewGroup = viewGroup;

        findChildElements();

        focusLast = ipBoxes[0];
        focusFirst = ipBoxes[3];

        View boxToRight = null;

        for (EditText e: ipBoxes) {

            // Assign view IDs, needed for focus navigation.
            e.setId(ViewCompat.generateViewId());


            // Link the focus chain throughout the 4 boxes and.
            // Note that the loop runs right to left
            if (boxToRight != null) {
                e.setNextFocusForwardId(boxToRight.getId());
            }

            boxToRight = e; // for next time round the loop

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

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkBoxChanged();
            }
        });
    }

    private void ipByteEdited(EditText target) {
        String txt = target.getText().toString();
        boolean moveNext = false;
        int len = txt.length();
        final int origlen = len;

        /* Handle the user putting a dot in. We need to get rid of it, but it depends,
          where they put it.
         */
        int dotindex;
        if ((dotindex = txt.indexOf(".")) >= 0) {

            if (dotindex == 0) {
                /*  Dot is at the beginning. Get rid of it. If there are any more characters, keep
                    them
                 */
                if (len == 1) {
                    /* The dot was the only thing, string is now to be made empty */
                    txt = "";
                    len = 0;
                } else {
                    /* Just strip the first char (the dot) */
                    txt = txt.substring(1);
                    len -= 1;
                }
            } else if (dotindex == 1) {
                /* The dot is the second character of three. Two possibilities, a 3
                   character string eg 2.3 (invalid - remove the dot), or a 2 character
                   string (2.) - valid, remove the dot and advance to the next byte window
                 */
                if (len == 3) {
                    txt = new String(new char[] {txt.charAt(0),txt.charAt(2)});
                    len -= 1;
                } else {
                    // Just the first character
                    txt = new String(new char[] {txt.charAt(0)});
                    len = 1;
                    moveNext = true; // because the dot's at the end
                }
            } else { // must be 2
                // The user has entered eg 23. ie a two digit byte value which is valid. Strip the
                // dot and move on.
                txt = txt.substring(0,2);
                len = 2;
                moveNext = true;
            }
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

            if (moveNext || len == 3) {
                moveToNextFocus(target);
            }
        }

        // Update with any changes we made
        target.getText().replace(0, origlen, txt, 0, len);
    }

    private void findChildElements() {

        ViewGroup childView = (ViewGroup) viewGroup.getChildAt(0);
        ipBoxes[0] = (EditText) childView.getChildAt(7);
        ipBoxes[1] = (EditText) childView.getChildAt(5);
        ipBoxes[2] = (EditText) childView.getChildAt(3);
        ipBoxes[3] = (EditText) childView.getChildAt(1);
        checkBox = (CheckBox) childView.getChildAt(8);
    }

    private void checkBoxChanged() {
        if (checkBox.isChecked()) {
            for (TextView v: ipBoxes) {
                v.setBackgroundColor(Color.GRAY);
                v.setTextColor(Color.GRAY);
            }
        } else {
            for (EditText v: ipBoxes) {
                ipByteEdited(v);
            }
        }
    }

    public String getIpAddress() {
        if (checkBox.isChecked()) {
            // ADDR_ANY. When specifying a remote address, means literally accept from ANY
            // (the other end must initiate the "connection". When specifying a local address,
            // means bind to all.
            return "0.0.0.0";
        } else {
            return ipBoxes[3].getText() + "." + ipBoxes[2] + "." + ipBoxes[1] + "." + ipBoxes[0];
        }

    }
    public boolean setIpAddress(String ipv4Addr) {

        if (ipv4Addr == null) {
            return false;
        }
        String[] split = ipv4Addr.split(".");

        if (split == null || split.length != 4) {
            return false;
        }

        int byteVals[] = new int[4];
        try {
            for (int i = 0; i < 4; i ++) {
                int val = Integer.parseInt(split[i]);

                if (val < 0 || val > 255) {
                    return false;
                }

                byteVals[i] = val;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        // If we got here, we got a valid IP address string
        int total = 0;
        for (int i = 0; i < 4; i++) {
            // The split will have caused byteVals[0] to contain the MSbyte
            ipBoxes[3-i].setText(Integer.toString(byteVals[i]));
            total += byteVals[i]; // Detect any non-zero byte val
        }

        if (total == 0) {
            // The value was "0.0.0.0"
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }
        checkBoxChanged();

        return true;
    }

    public void setADDR_ANYcheckboxText(String s) {
        // What does the caller want to call ADDR_ANY (eg "all", "ephemeral", "any"...)
        checkBox.setText(s);
    }

    @Override
    public View getFocusLast() {
        return focusLast;
    }
    @Override
    public View getFocusFirst() {
        return focusFirst;
    }
}
