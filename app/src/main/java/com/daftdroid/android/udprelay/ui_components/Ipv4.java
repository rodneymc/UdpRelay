package com.daftdroid.android.udprelay.ui_components;

import android.graphics.Color;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.daftdroid.android.udprelay.R;

import androidx.core.view.ViewCompat;

public class Ipv4 extends UiComponent {

    private final ViewGroup viewGroup;
    private final View focusLast;
    private final View focusFirst;

    public Ipv4(ViewGroup viewGroup) {
        this.viewGroup = viewGroup;

        EditText[] ipBoxes = getIpBoxes();

        focusLast = ipBoxes[0];
        focusFirst = ipBoxes[3];

        View boxToRight = null;

        for (EditText e: getIpBoxes()) {

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
                // The user has entered 23. ie a two digit byte value which is valid. Strip the
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

    private void portEdited(EditText target) {

    }
    private EditText[] getIpBoxes() {
        EditText e[] = new EditText[4];

        ViewGroup childView = (ViewGroup) viewGroup.getChildAt(0);
        e[0] = (EditText) childView.getChildAt(7);
        e[1] = (EditText) childView.getChildAt(5);
        e[2] = (EditText) childView.getChildAt(3);
        e[3] = (EditText) childView.getChildAt(1);

        return e;
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
