package com.daftdroid.android.udprelay.ui_components;

import android.view.View;
import android.widget.EditText;

public class UiComponentView extends UiComponent {
    public final View view;

    public UiComponentView(View view) {
        this.view = view;
    }

    @Override
    public String getRawUserInputState() {
        if (view instanceof EditText) {
            return ((EditText) view).getText().toString();
        }
        return null;
    }

    @Override
    public void putRawUserInputState(String txt) {
        if (view instanceof EditText) {
            ((EditText) view).setText(txt);
        }
    }

    @Override
    public int getId() {
        return view.getId();
    }

    @Override
    public View getFocusFirst() {
        return view;
    }

    @Override
    public View getFocusLast() {
        return view;
    }

    @Override
    public int getChildFocusIndex() {
        return view.hasFocus() ? 0 : -1;
    }
    @Override
    public void setFocusToChild(int childIndex) {
        view.requestFocus();
    }
}