package dev.anarchy.waifuhax.client.events.hud;

import meteordevelopment.orbit.ICancellable;

public class HudMouseReleasedEvent implements ICancellable {

    public double mouseX, mouseY;
    public int button;

    private static final HudMouseReleasedEvent event = new HudMouseReleasedEvent();

    public static HudMouseReleasedEvent get(double mouseX, double mouseY, int button) {
        event.mouseX = mouseX;
        event.mouseY = mouseY;
        event.button = button;
        return event;
    }

    @Override
    public void setCancelled(boolean b) {

    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
