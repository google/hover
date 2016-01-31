package io.mattcarroll.hover.hoverdemo;

import de.greenrobot.event.EventBus;

/**
 * Globally accessible EventBus.
 */
public class Bus {

    private static EventBus sBus = new EventBus();

    public static EventBus getInstance() {
        return sBus;
    }

}
