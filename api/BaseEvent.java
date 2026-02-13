package dev.anarchy.waifuhax.api;

import meteordevelopment.orbit.ICancellable;

public class BaseEvent implements ICancellable {

    private boolean cancelled = false;

    @Override
    public void cancel() {
        ICancellable.super.cancel();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
