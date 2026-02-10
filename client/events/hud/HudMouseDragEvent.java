package dev.anarchy.waifuhax.client.events.hud;

import meteordevelopment.orbit.ICancellable;

public class HudMouseDragEvent implements ICancellable {

    public double mouseX, mouseY;
    public double deltaX, deltaY;
    public int button;

    private static final HudMouseDragEvent event = new HudMouseDragEvent();

    public static HudMouseDragEvent get(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        event.mouseX = mouseX;
        event.mouseY = mouseY;
        event.deltaX = deltaX;
        event.deltaY = deltaY;
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
