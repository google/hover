package io.mattcarroll.hover.content;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * TODO
 */
public interface NavigatorContent {

    @NonNull
    View getView();

    void onShown(@NonNull Navigator navigator);

    void onHidden();

}
