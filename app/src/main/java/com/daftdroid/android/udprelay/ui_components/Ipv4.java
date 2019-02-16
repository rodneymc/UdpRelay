package com.daftdroid.android.udprelay.ui_components;

import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.daftdroid.android.udprelay.R;

import androidx.core.view.ViewCompat;

public class Ipv4 extends UiComponentViewGroup {

    private final EditText[] ipBoxes = new EditText[4];
    private EditText portBox;
    private CheckBox checkBox;
    private CheckBox portCheckBox;
    private boolean anyIP; // True for ADDR_ANY ie 0.0.0.0 or the "any" box ticked
    private boolean anyPort;
    private boolean saveable;

    public Ipv4(ViewGroup viewGroup) {

        super(viewGroup);
        findChildElements();
        enumerateChildren();

        focusLast = portBox;
        focusFirst = ipBoxes[3];


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
                    if (!blockListeners) {
                        ipByteEdited(target, false);
                    }
                }
            });
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!blockListeners) {
                    checkBoxChanged();
                }
            }
        });

        portBox.addTextChangedListener(new EditTextChangedListener(portBox) {
            @Override
            public void onTextChanged(EditText target, Editable s) {
                if (!blockListeners) {
                    portBoxChanged(false);
                }
            }
        });

        portCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!blockListeners) {
                    portCheckBoxChanged();
                }
            }
        });
    }

    private void ipByteEdited(EditText target, boolean anyIpUpdated) {

        if (anyIpUpdated) {
            if (anyIP) {
                editTextStatus(target, EditTextStatus.DISABLED);
            }
        }

        // Handle the text, unless anyIP is true in which case the text is invisible and irrelevant.
        if (!anyIP) {
            String txt = target.getText().toString();
            boolean moveNext = false;
            final int origlen = txt.length();;
            txt = txt.replace("?","");
            int len = txt.length();
            boolean questionMarkDetected = len != origlen;

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
                editTextStatus(target, EditTextStatus.SOFT_ERROR);
            } else {
                // This byte is valid, in the sense that it is at least in the range 0-255,
                // though actually the last byte shouldn't be zero but that's more about the
                // address value as a hole being invalid.

                editTextStatus(target, EditTextStatus.NORMAL);

                if (moveNext || len == 3) {
                    moveToNextFocus(target);
                }
            }

            // Update with any changes we made
            blockListeners = true; // block recursion
            target.getText().replace(0, origlen, txt, 0, len);
            blockListeners = false;

            if (questionMarkDetected) {
                target.setSelection(1);
            }
        }
    }

    private void findChildElements() {

        ViewGroup viewGroup = getViewGroup();
        ipBoxes[0] = viewGroup.findViewById(R.id.ip0);
        ipBoxes[1] = viewGroup.findViewById(R.id.ip1);
        ipBoxes[2] = viewGroup.findViewById(R.id.ip2);
        ipBoxes[3] = viewGroup.findViewById(R.id.ip3);
        checkBox = viewGroup.findViewById(R.id.checkBox);
        portBox = viewGroup.findViewById(R.id.port);
        portCheckBox = viewGroup.findViewById(R.id.portCheckBox);
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
            // When specifying a remote address, means literally accept from ANY
            // (the other end must initiate the "connection". When specifying a local address,
            // means auto select based on the route to the host. Explicit 0.0.0.0 means
            // bind to all.
            return null;
        } else {
            return ipBoxes[3].getText() +
                    "." + ipBoxes[2].getText() +
                    "." + ipBoxes[1].getText() +
                    "." + ipBoxes[0].getText();
        }

    }
    @Override
    public String getRawUserInputState() {
        return ipBoxes[3].getText() +
                "." + ipBoxes[2].getText() +
                "." + ipBoxes[1].getText() +
                "." + ipBoxes[0].getText() +
                "." + (checkBox.isChecked() ? 'y' : 'n') +
                "." + portBox.getText() +
                "." + (portCheckBox.isChecked() ? 'y' : 'n');
    }
    @Override
    public void putRawUserInputState(String state) {
        // TODO handle question marks on rotation
        String[] split = state.split("\\.", 7);
        blockListeners = true;
        ipBoxes[3].setText(split[0].replace("?", ""));
        ipBoxes[2].setText(split[1].replace("?", ""));
        ipBoxes[1].setText(split[2].replace("?", ""));
        ipBoxes[0].setText(split[3].replace("?", ""));
        portBox.setText(split[5].replace("?", ""));
        blockListeners = false; // setting the checkbox will cause the update
        checkBox.setChecked(split[4].equals("y"));
        portCheckBox.setChecked(split[6].equals("y"));
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

            // Don't allow the listener to run while we are updating the individual boxes,
            // they will all be updated when the checkbox is updated.

            blockListeners = true;

            if (split == null) {
                for (int i = 0; i < 4; i ++) {
                    // Fill with blanks
                    ipBoxes[i].setText("");
                }
                checkBox.setChecked(true);
            }

            else {
                for (int i = 0; i < 4; i++) {
                    // The split will have caused byteVals[0] to contain the MSbyte
                    ipBoxes[3-i].setText(Integer.toString(byteVals[i]));
                }
                checkBox.setChecked(false);
            }

            blockListeners = false;

            checkBoxChanged();
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
              editTextStatus(portBox, EditTextStatus.DISABLED);

            }

        }
        if (!anyPort) {
            String txt_orig = portBox.getText().toString();
            String txt = txt_orig.replace("?", "");
            int origlen = txt_orig.length();
            int len = txt.length();
            int val = (len == 0) ? 9999999 : Integer.parseInt(txt); // Digits only in this box, so should be safe

            if (val > 65535) {
                editTextStatus(portBox, EditTextStatus.SOFT_ERROR);
            } else {
                editTextStatus(portBox, EditTextStatus.NORMAL); 

                if (txt.length() == 5) {
                    moveToNextFocus(portBox);
                }
            }

            // See if a ? was stripped

            if (len != origlen) {
                blockListeners = true;
                portBox.getText().replace(0, origlen, txt, 0, len);
                blockListeners = false;
                portBox.setSelection(1);
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
            blockListeners = true;
            portBox.setText(Integer.toString(port));
            blockListeners = false;

            portCheckBox.setChecked(false);
        } else {
            // Port == 0 means any
            blockListeners = true;
            portBox.setText("");
            blockListeners = false;

            portCheckBox.setChecked(true);
        }
    }
    // Validate whether the config is saveable, not whether the IP configuratio
    // is actually sane. Flags up errors as hard which makes them more visible in the ui.
    // Do in reverse order, so the focus ends up on the first errored item.
    public void validate() {
        saveable = true;

        if (!anyPort) {
            String txt = portBox.getText().toString();
            // If it contains a ?, we already flagged it as error
            if (txt.equals("?")) {
                saveable = false;
            } else {
                int port = !txt.equals("") ? Integer.parseInt(txt) : 99999;
                if (port > 65535) {
                    saveable = false;
                    editTextStatus(portBox, EditTextStatus.HARD_ERROR);
                }
            }
        }

        if (!anyIP) {
            // We need a number 0-255 in all boxes
            for (int i = 0; i < 4; i ++) {
                String txt = ipBoxes[i].getText().toString();

                // If it contains a ?, we already flagged it as error
                if (txt.equals("?")) {
                    saveable = false;
                    continue;
                }

                int byteval = !txt.equals("") ? Integer.parseInt(txt) : 99999;
                if (byteval > 255) {
                    saveable = false;
                    editTextStatus(ipBoxes[i], EditTextStatus.HARD_ERROR);
                }
            }
        }
    }
    public boolean isSaveable() {
        return saveable;
    }
    // Update the status when blank, to be called if setIpAddress or setPort are not
    // called after creation.
    public void initBlank() {
        portCheckBoxChanged();
        checkBoxChanged();
    }

}
