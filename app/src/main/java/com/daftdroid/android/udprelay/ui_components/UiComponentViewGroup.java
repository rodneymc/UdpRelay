package com.daftdroid.android.udprelay.ui_components;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.daftdroid.android.udprelay.R;

import java.util.ArrayList;
import java.util.List;

import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;

public abstract class UiComponentViewGroup extends UiComponent {

    protected View focusFirst;
    protected View focusLast;
    protected boolean blockListeners;
    private View erroredView;
    private final ViewGroup viewGroup;
    private final int bg_greyedOut;
    private final int bg_normal;

    public static ViewGroup doInflate(Activity act, int placerHolderId, int componentResource) {
        ViewGroup vg = act.findViewById(placerHolderId);
        LayoutInflater inflater = act.getLayoutInflater();
        inflater.inflate(componentResource, vg);

        return  (ViewGroup) vg.getChildAt(vg.getChildCount() -1);
    }
    public UiComponentViewGroup(Activity act, int placerHolderId, int componentResource) {
        this (doInflate(act, placerHolderId, componentResource));
    }
    public UiComponentViewGroup(ViewGroup viewGroup) {
        this.viewGroup = viewGroup;
        bg_greyedOut = viewGroup.getResources().getColor(R.color.colorGreyedOut);
        bg_normal = viewGroup.getResources().getColor(R.color.colorEditBg);
    }

    public void requestFocusToErroredView() {
        if (erroredView != null) {
            blockListeners = true;
            erroredView.requestFocus();
            blockListeners = false;
        }
    }
    @Override
    public int getChildFocusIndex() {
        int i = 0;

        for (View v: getAllChildren(viewGroup)) {
            if (v.hasFocus()) {
                return i;
            }

            i++;
        }

        return -1;
    }

    // Get all children that are not themselves ViewGroup, however we do recursively
    // get children of the children that are ViewGroups

    private List<View> getAllChildren(View v) {
        List<View> visited = new ArrayList<View>();
        List<View> unvisited = new ArrayList<View>();
        unvisited.add(v);

        while (!unvisited.isEmpty()) {
            View child = unvisited.remove(0);

            if (!(child instanceof ViewGroup)) {
                visited.add(child); // only add the last descendent
                continue;
            }
            ViewGroup group = (ViewGroup) child;
            final int childCount = group.getChildCount();
            for (int i=0; i<childCount; i++) unvisited.add(group.getChildAt(i));
        }

        return visited;
    }

    @Override
    public void setFocusToChild(int childIndex) {
        View v = getAllChildren(viewGroup).get(childIndex);
        v.requestFocus();
    }

    protected ViewGroup getViewGroup() {
        return viewGroup;
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

    /*
        Override the view IDs of the viewGroup's children with unique ones, this means we
        can reuse bits of xml containing view IDs and end up with unique IDs for all the views.
        Any code which relies on the original xml id must be run before this.
     */

    protected void enumerateChildren() {
        enumerateChildren(viewGroup);
    }

    private void enumerateChildren(ViewGroup v) {
        int childCount = v.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = v.getChildAt(i);

            child.setId(ViewCompat.generateViewId());

            if (child instanceof ViewGroup) {
                enumerateChildren((ViewGroup) child);
            }
        }
    }

    @Override
    public View getFocusLast() {
        return focusLast;
    }
    @Override
    public View getFocusFirst() {
        return focusFirst;
    }
    @Override
    public int getId() {
        return viewGroup.getId();
    }

}
