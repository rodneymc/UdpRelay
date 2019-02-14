package com.daftdroid.android.udprelay.ui_components;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class UiComponent {

    private final ViewGroup viewGroup;
    private UiComponent focusPrevious;
    private UiComponent focusNext;

    public UiComponent(View view) {
        focusLast = focusFirst = view;

        if (view instanceof ViewGroup) {
            viewGroup = (ViewGroup) view;
        } else {
            viewGroup = null;
        }
    }
    public UiComponent(Activity act, int placerHolderId, int componentResource) {
        ViewGroup vg = act.findViewById(placerHolderId);
        LayoutInflater inflater = act.getLayoutInflater();
        inflater.inflate(componentResource, vg);

        viewGroup = (ViewGroup) vg.getChildAt(vg.getChildCount() -1);
    }

    protected View focusFirst;
    protected View focusLast;



    /*
        Links this forward in the focus chain relative to the arg previous
     */
    public UiComponent linkFocusForward(UiComponent previous) {
        if (previous != null) {
            View prevView = previous.focusLast;

            // Make the previous view point at us as its next.
            int nextId = prevView.getNextFocusForwardId();
            prevView.setNextFocusForwardId(focusFirst.getId());

            // Now use the saved value of next, and use it as our next pointer
            focusLast.setNextFocusForwardId(nextId);

            // Link
            previous.focusNext = this;
            focusPrevious = previous;
        }
        return previous; // for continuing the chain
    }
    protected void updateFocusNextPrev() {

        System.out.println("update!");

        if (focusPrevious != null) {
            focusPrevious.focusLast.setNextFocusForwardId(focusFirst.getId());
            View v = focusPrevious.focusLast;
            View v2 = focusFirst;
            if (v instanceof EditText) {
                System.out.println(((EditText)v).getText());
            }
            if (v2 instanceof EditText) {
                System.out.println(((EditText)v2).getText());
            }
        }
        if (focusNext != null) {
            focusLast.setNextFocusForwardId(focusNext.focusFirst.getId());
        }
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
