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
    private final ViewGroup viewGroup;

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
        super(viewGroup);
        this.viewGroup = viewGroup;
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
