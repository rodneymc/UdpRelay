package com.daftdroid.android.udprelay.ui_components;

import android.view.View;
import android.widget.EditText;

public abstract class UiComponent {

    public abstract View getFocusFirst();
    public abstract View getFocusLast();

    /*
        Links this forward in the focus chain relative to the arg previous
     */
    public void linkFocusForward(UiComponent previous) {
        if (previous != null) {
            View prevView = previous.getFocusLast();

            // Make the previous view point at us as its next.
            int nextId = prevView.getNextFocusForwardId();
            prevView.setNextFocusForwardId(getFocusFirst().getId());

            // Now use the saved value of next, and use it as our next pointer
            getFocusLast().setNextFocusForwardId(nextId);
        }
    }

    protected void moveToNextFocus(View target) {
        View next = target.getRootView().findViewById(target.getNextFocusForwardId());
        if (next != null)
            next.requestFocus();
    }

    }
