package com.daftdroid.android.udprelay.ui_components;

import android.view.View;

public abstract class UiComponent {

    private UiComponent focusPrevious;
    private UiComponent focusNext;
    protected enum EditTextStatus {NORMAL, SOFT_ERROR, HARD_ERROR, DISABLED};

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

            // Link
            previous.focusNext = this;
            focusPrevious = previous;
        }
        return previous; // for continuing the chain
    }

    protected void updateFocusNextPrev() {

        if (focusPrevious != null) {
            focusPrevious.getFocusLast().setNextFocusForwardId(getFocusFirst().getId());
        }
        if (focusNext != null) {
            getFocusLast().setNextFocusForwardId(focusNext.getFocusFirst().getId());
        }
    }

    protected void moveToNextFocus(View target) {
        View next = target.getRootView().findViewById(target.getNextFocusForwardId());
        if (next != null)
            next.requestFocus();
    }

    public abstract View getFocusLast();
    public abstract View getFocusFirst();
    public abstract String getRawUserInputState();
    public abstract void putRawUserInputState(String txt);
    public abstract int getId();
    public abstract int getChildFocusIndex();
    public abstract void setFocusToChild(int childIndex);
}
