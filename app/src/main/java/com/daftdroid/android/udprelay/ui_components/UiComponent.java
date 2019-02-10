package com.daftdroid.android.udprelay.ui_components;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public abstract class UiComponent {

    private final ViewGroup viewGroup;

    public UiComponent(Activity act, int placerHolderId, int componentResource) {
        ViewGroup vg = act.findViewById(placerHolderId);
        LayoutInflater inflater = act.getLayoutInflater();
        viewGroup = (ViewGroup) inflater.inflate(componentResource, vg);
    }
    public abstract View getFocusFirst();
    public abstract View getFocusLast();



    /*
        Links this forward in the focus chain relative to the arg previous
     */
    public UiComponent linkFocusForward(UiComponent previous) {
        if (previous != null) {
            View prevView = previous.getFocusLast();

            // Make the previous view point at us as its next.
            int nextId = prevView.getNextFocusForwardId();
            prevView.setNextFocusForwardId(getFocusFirst().getId());

            // Now use the saved value of next, and use it as our next pointer
            getFocusLast().setNextFocusForwardId(nextId);
        }
        return previous; // for continuing the chain
    }

    protected void moveToNextFocus(View target) {
        View next = target.getRootView().findViewById(target.getNextFocusForwardId());
        if (next != null)
            next.requestFocus();
    }

    protected ViewGroup getViewGroup() {
            return viewGroup;
        }
    }
