package io.mattcarroll.hover;

import android.graphics.Point;
import android.support.annotation.NonNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * TODO
 */
public abstract class Dock {

    private final Set<Listener> mListeners = new CopyOnWriteArraySet<Listener>();

    @NonNull
    public abstract Point position();

    public void addListener(@NonNull Listener listener) {
        mListeners.add(listener);
    }

    public void removeListener(@NonNull Listener listener) {
        mListeners.remove(listener);
    }

    public void clearListeners() {
        mListeners.clear();
    }

    protected void notifyListeners() {
        for (Listener listener : mListeners) {
            listener.onDockChange(this);
        }
    }

    public interface Listener {
        void onDockChange(@NonNull Dock dock);
    }

}
