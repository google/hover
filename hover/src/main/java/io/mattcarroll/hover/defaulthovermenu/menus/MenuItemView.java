package io.mattcarroll.hover.defaulthovermenu.menus;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import io.mattcarroll.hover.R;


/**
 * View that represents a MenuItem as a list item.
 */
public class MenuItemView extends FrameLayout {

    private TextView mTitleTextView;

    private String mTitle;

    public MenuItemView(Context context) {
        super(context);
        init();
    }

    public MenuItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_menu_item, this, true);

        mTitleTextView = (TextView) findViewById(R.id.textview_title);
    }

    public void setTitle(String title) {
        mTitle = title;
        updateView();
    }

    private void updateView() {
        mTitleTextView.setText(mTitle);
    }
}
