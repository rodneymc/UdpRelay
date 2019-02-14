package com.daftdroid.android.udprelay.ui_components;

import android.app.Activity;
import android.graphics.Color;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.daftdroid.android.udprelay.R;

import androidx.core.view.ViewCompat;

public class Ipv4 extends UiComponent {

    private final EditText[] ipBoxes = new EditText[4];
    private EditText portBox;
    private CheckBox checkBox;
    private CheckBox portCheckBox;
    private boolean anyIP; // True for ADDR_ANY ie 0.0.0.0 or the "any" box ticked
    private boolean anyPort;
    private boolean blockTxtUpdateListener;

    public Ipv4(Activity act, int placeHolderId) {

        super(act, placeHolderId, R.layout.ipv4);
        findChildElements();

        focusLast = portBox;
        focusFirst = ipBoxes[3];

        portBox.setId(ViewCompat.generateViewId());
        checkBox.setId(ViewCompat.generateViewId());
        portCheckBox.setId(ViewCompat.generateViewId());
        View boxToRight = portBox;
        focusLast = portBox;

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
                @Override
                public void onTextChanged(EditText target, Editable s) {
                    if (!blockTxtUpdateListener) {
                        ipByteEdited(target, false);
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

        portBox.addTextChangedListener(new EditTextChangedListener(portBox) {
            @Override
            public void onTextChanged(EditText target, Editable s) {
                if (!blockTxtUpdateListener) {
                    portBoxChanged(false);
                }
            }
        });

        portCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                portCheckBoxChanged();
            }
        });
    }

    private void ipByteEdited(EditText target, boolean anyIpUpdated) {

        if (anyIpUpdated) {
            if (anyIP) {
                target.setBackgroundColor(Color.GRAY);
                target.setTextColor(Color.GRAY);
                target.setEnabled(false);
            } else {
                target.setEnabled(true);
            }
        }

        // Handle the text, unless anyIP is true in which case the text is invisible and irrelevant.
        if (!anyIP) {
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
                        txt = new String(new char[]{txt.charAt(0), txt.charAt(2)});
                        len -= 1;
                    } else {
                        // Just the first character
                        txt = new String(new char[]{txt.charAt(0)});
                        len = 1;
                        moveNext = true; // because the dot's at the end
                    }
                } else { // must be 2
                    // The user has entered eg 23. ie a two digit byte value which is valid. Strip the
                    // dot and move on.
                    txt = txt.substring(0, 2);
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
            } else {
                // This byte is valid, in the sense that it is at least in the range 0-255,
                // though actually the last byte shouldn't be zero but that's more about the
                // address value as a hole being invalid.

                target.setBackgroundColor(Color.WHITE);

                if (moveNext || len == 3) {
                    moveToNextFocus(target);
                }
            }

            // Update with any changes we made
            blockTxtUpdateListener = true; // block recursion
            target.getText().replace(0, origlen, txt, 0, len);
            blockTxtUpdateListener = false;
        }
    }

    private void findChildElements() {

        ViewGroup viewGroup = getViewGroup();
        ViewGroup layout0 = (ViewGroup) viewGroup.getChildAt(0);
        ViewGroup layout1 = (ViewGroup) viewGroup.getChildAt(1);
        ipBoxes[0] = (EditText) layout0.getChildAt(7);
        ipBoxes[1] = (EditText) layout0.getChildAt(5);
        ipBoxes[2] = (EditText) layout0.getChildAt(3);
        ipBoxes[3] = (EditText) layout0.getChildAt(1);
        checkBox = (CheckBox) layout0.getChildAt(8);
        portBox = (EditText) layout1.getChildAt(1);
        portCheckBox = (CheckBox) layout1.getChildAt(2);
    }

    private void checkBoxChanged() {
        anyIP = checkBox.isChecked();
        updateCheckBoxFocusLinks();

        for (EditText v: ipBoxes) {
            ipByteEdited(v, true);
        }

    }
    private void updateCheckBoxFocusLinks() {
        if (anyIP) {
            // If the user tabs in, take them to the checkbox, as the IP boxes are greyed out.
            focusFirst = checkBox;
        } else {
            // If the user tabs in, take them to the MSB, on the left.
            focusFirst = ipBoxes[3];
        }

        // When the user tabs out of the IP section they should land on the port box, unless
        // the port checkbox is ticked, in which case land on that. Either way, the target
        // will be the last focus components of this Ipv4 ui component.

        focusLast = anyPort ? portCheckBox : portBox;
        int targetId = focusLast.getId();

        if (anyIP) {
            checkBox.setNextFocusForwardId(targetId);
        } else {
            ipBoxes[0].setNextFocusForwardId(targetId);
        }

        updateFocusNextPrev();

    }

    public String getIpAddress() {
        if (checkBox.isChecked()) {
            // ADDR_ANY. When specifying a remote address, means literally accept from ANY
            // (the other end must initiate the "connection". When specifying a local address,
            // means bind to all.
            return null;
        } else {
            return ipBoxes[3].getText() +
                    "." + ipBoxes[2].getText() +
                    "." + ipBoxes[1].getText() +
                    "." + ipBoxes[0].getText();
        }

    }
    public String getRawUserInputState() {
        return ipBoxes[3].getText() +
                "." + ipBoxes[2].getText() +
                "." + ipBoxes[1].getText() +
                "." + ipBoxes[0].getText() +
                "." + (checkBox.isChecked() ? 'y' : 'n') +
                "." + portBox.getText();
    }
    public void putRawUserInputState(String state) {
        String[] split = state.split("\\.", 6);
        blockTxtUpdateListener = true;
        ipBoxes[3].setText(split[0]);
        ipBoxes[2].setText(split[1]);
        ipBoxes[1].setText(split[2]);
        ipBoxes[0].setText(split[3]);
        blockTxtUpdateListener = false; // setting the checkbox will cause the update
        checkBox.setChecked(split[4].equals("y"));
        portBox.setText(split[5]);
    }
    public boolean setIpAddress(String ipv4Addr) {

        boolean error = false;


        String[] split = null;
        int byteVals[] = new int[4];

        if (ipv4Addr != null) {
            split = ipv4Addr.split("\\.");
            if (split == null || split.length != 4) {
                split = null;
                error = true; // text supplied but not splitabble into 4
            }
        }

        if (split == null) {
            // in Java null is ADDR_ANY
            // nothing to do, byteVals will be {0,0,0,0} already

        } else {

            try {
                for (int i = 0; i < 4; i++) {
                    int val = Integer.parseInt(split[i]);

                    if (val < 0 || val > 255) {
                        error = true;
                        break;
                    }

                    byteVals[i] = val;
                }
            } catch (NumberFormatException e) {
                error = true;
            }
        }

        if (!error) {
            int total = 0;

            // Don't allow the listener to run while we are updating the individual boxes,
            // they will all be updated when the checkbox is updated.

            blockTxtUpdateListener = true;

            for (int i = 0; i < 4; i++) {
                // The split will have caused byteVals[0] to contain the MSbyte
                ipBoxes[3-i].setText(Integer.toString(byteVals[i]));
                total += byteVals[i]; // Detect any non-zero byte val
            }

            blockTxtUpdateListener = false;


            if (total == 0) {
                // The value was "0.0.0.0"

                if (!anyIP) {
                    checkBox.setChecked(true);
                } else {
                    // checked already but we need to run the whole update thing
                    checkBoxChanged();
                }
            } else {
                if (anyIP) {
                    checkBox.setChecked(false);
                } else {
                    // unchecked already but we need to run the whole update thing
                    checkBoxChanged();
                }
            }
        }

        return !error;
    }

    public void setADDR_ANYcheckboxText(String s) {
        // What does the caller want to call ADDR_ANY (eg "all", "ephemeral", "any"...)
        checkBox.setText(s);
    }

    private void portBoxChanged(boolean chkBoxChanged) {
        if (chkBoxChanged) {
            if (anyPort) {
                portBox.setBackgroundColor(Color.GRAY);
                portBox.setTextColor(Color.GRAY);
                portBox.setEnabled(false);
            } else {
                portBox.setTextColor(Color.BLACK);
                portBox.setEnabled(true);
            }

        }
        if (!anyPort) {
            String txt = portBox.getText().toString();
            int val = (txt.length() == 0) ? 9999999 : Integer.parseInt(txt); // Digits only in this box, so should be safe

            if (val > 65535) {
                portBox.setBackgroundColor(Color.YELLOW);
            } else {
                portBox.setBackgroundColor(Color.WHITE);

                if (txt.length() == 5) {
                    moveToNextFocus(portBox);
                }
            }
        }
    }
    private void portCheckBoxChanged() {
        anyPort = portCheckBox.isChecked();
        portBoxChanged(true);
        updateCheckBoxFocusLinks();
    }

    public int getPort() {
        String txt = portBox.getText().toString();
        int val = (txt.length() == 0) ? 0 : Integer.parseInt(txt); // Digits only in this box, so should be safe

        return val;
    }
    public void setPort(int port) {
        if (port > 0) {
            blockTxtUpdateListener = true;
            portBox.setText(Integer.toString(port));
            blockTxtUpdateListener = false;

            portCheckBox.setChecked(false);
        } else {
            // Port == 0 means any
            blockTxtUpdateListener = true;
            portBox.setText("");
            blockTxtUpdateListener = false;

            portCheckBox.setChecked(true);
        }
    }
    // Validate whether the config is saveable, not whether the IP configuratio
    // is actually sane.
    public boolean isSaveable() {
        try {
            if (!anyIP) {
                // We need a number 0-255 in all boxes
                for (int i = 0; i < 4; i ++) {
                    int byteval = Integer.parseInt(ipBoxes[i].getText().toString());
                    if (byteval > 255) {
                        return false;
                    }
                }
            }
            if (!anyPort) {
                Integer port = Integer.parseInt(portBox.getText().toString());
                if (port > 65535) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            // blank fields
            return false;
        }

        return true;
    }
    // Update the status when blank, to be called if setIpAddress or setPort are not
    // called after creation.
    public void initBlank() {
        portCheckBoxChanged();
        checkBoxChanged();
    }

}
