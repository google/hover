package io.mattcarroll.hover.defaulthovermenu.menus;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * View that displays all items in a given {@link MenuItem}.
 */
public class MenuListView extends FrameLayout {

    private ListView mListView;
    private MenuListAdapter mMenuListAdapter;
    private MenuItemSelectionListener mMenuItemSelectionListener;

    public MenuListView(Context context) {
        this(context, null);
    }

    public MenuListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mMenuListAdapter = new MenuListAdapter();

        mListView = new ListView(getContext());
        mListView.setAdapter(mMenuListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != mMenuItemSelectionListener) {
                    mMenuItemSelectionListener.onMenuItemSelected(mMenuListAdapter.getItem(position));
                }
            }
        });
        addView(mListView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setMenu(@Nullable MenuItem menu) {
        if (null == menu) {
            mMenuListAdapter.setMenuItems(new ArrayList<MenuItem>(0));
        } else {
            mMenuListAdapter.setMenuItems(menu.getItems());
        }
    }

    public void setMenuItemSelectionListener(@Nullable MenuItemSelectionListener listener) {
        mMenuItemSelectionListener = listener;
    }

    public interface MenuItemSelectionListener {
        void onMenuItemSelected(@NonNull MenuItem menuItem);
    }
}
