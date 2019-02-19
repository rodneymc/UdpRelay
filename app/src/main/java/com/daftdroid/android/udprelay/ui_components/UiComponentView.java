package com.daftdroid.android.udprelay.ui_components;

import android.text.Editable;
import android.view.View;
import android.widget.EditText;

public class UiComponentView extends UiComponent {
    private final View view;
    private boolean errorListenerAdded;

    public UiComponentView(View view) {
        super(view);
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

    @Override
    public boolean hasError() {
        // For now, assume that if the view is an edit text, it must not be blank, also, if it
        // just contains "?" then we assume that was inserted by the app to indicate error.
        if (view instanceof EditText) {
            Editable ed = ((EditText) view).getText();
            int ed_len = ed.length();

            return (ed_len == 0 || ed_len == 1 && ed.toString().equals("?"));
        }

        // Other types don't have error
        return false;
    }
    @Override
    public void validate() {
        if (!errorListenerAdded && hasError()) {

            // hasError can return true only if view is an edit text.
            EditText e = (EditText) view;
            editTextStatus(e, EditTextStatus.HARD_ERROR);

            // Install a simple handler for getting rid of the ? when the user start to type
            e.addTextChangedListener(new EditTextChangedListener(e) {
                @Override
                public void onTextChanged(EditText target, Editable s) {
                        e.removeTextChangedListener(this);
                        errorListenerAdded = false;
                        Editable ed = e.getText();
                        String txt = ed.toString();
                        String newTxt = txt.replace("?", "");
                        ed.replace(0, txt.length(), newTxt, 0, newTxt.length());
                        e.setSelection(newTxt.length());
                        editTextStatus(e, EditTextStatus.NORMAL);
                }
            });

            errorListenerAdded = true;
        }
    }

}