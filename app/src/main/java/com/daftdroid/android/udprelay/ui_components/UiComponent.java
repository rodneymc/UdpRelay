package com.daftdroid.android.udprelay.ui_components;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

import com.daftdroid.android.udprelay.R;

import androidx.core.widget.TextViewCompat;

public abstract class UiComponent {

    private UiComponent focusPrevious;
    private UiComponent focusNext;
    protected enum EditTextStatus {NORMAL, SOFT_ERROR, HARD_ERROR, DISABLED};
    protected boolean blockListeners;
    private View erroredView;
    protected final int bg_greyedOut;
    protected final int bg_normal;

    public UiComponent(View c) {
        bg_greyedOut = c.getResources().getColor(R.color.colorGreyedOut);
        bg_normal = c.getResources().getColor(R.color.colorEditBg);

    }

    public void requestFocusToErroredView() {
        if (erroredView != null) {
            blockListeners = true;
            erroredView.requestFocus();
            blockListeners = false;
        }
    }

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
    protected void editTextStatus(final EditText e, final EditTextStatus s) {
        boolean enable = true;

        // TODO a bit of an assumption that this is the only place we need an
        // onfocus change listener

        switch (s) {
            case NORMAL:
                TextViewCompat.setTextAppearance(e, R.style.ipbox);
                e.setBackgroundColor(bg_normal);
                e.setOnFocusChangeListener(null);
                break;

            case SOFT_ERROR:
                TextViewCompat.setTextAppearance(e, R.style.ipbox_error);
                e.setBackgroundColor(bg_normal);
                break;
            case HARD_ERROR:

                erroredView = e;
                TextViewCompat.setTextAppearance(e, R.style.ipbox_error);
                e.setBackgroundColor(bg_normal);

                // If the text is empty, we need to put a ? in it to highlight it
                if (e.getText().toString().equals("")) {

                    blockListeners = true;
                    e.setText("?");
                    blockListeners = false;

                    if (!e.hasFocus()) {
                        // Remove the question mark when the box gets focus

                        e.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if (!blockListeners) {
                                    e.setText("");
                                    e.setOnFocusChangeListener(null);
                                }
                            }
                        });
                    }
                }
                break;

            case DISABLED:
                enable = false;

                if (e.getText().toString().equals("?")) {
                    blockListeners = true;
                    e.setText("");
                    blockListeners = false;
                }

                e.setTextColor(bg_greyedOut);
                e.setBackgroundColor(bg_greyedOut);

                break;
        }

        e.setEnabled(enable);
    }

    public abstract View getFocusLast();
    public abstract View getFocusFirst();
    public abstract String getRawUserInputState();
    public abstract void putRawUserInputState(String txt);
    public abstract int getId();
    public abstract int getChildFocusIndex();
    public abstract void setFocusToChild(int childIndex);
    public abstract void validate();
    public abstract boolean hasError();
}
