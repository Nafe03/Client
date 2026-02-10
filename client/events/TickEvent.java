package dev.anarchy.waifuhax.client.events;

import meteordevelopment.orbit.ICancellable;

public class TickEvent implements ICancellable {

    private static final TickEvent onTickEvent = new TickEvent();

    public static TickEvent get() {
        return onTickEvent;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean cancelled) {

    }
}
