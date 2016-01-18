package io.mattcarroll.hover.hoverdemo.menu;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import io.mattcarroll.hover.hoverdemo.R;

/**
 * View to show when a menu list has nothing in it.
 */
public class EmptyListView extends FrameLayout {

    public EmptyListView(Context context) {
        super(context);
        init();
    }

    public EmptyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_emptylist, this, true);
    }
}
