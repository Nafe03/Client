package dev.anarchy.waifuhax.client.events.hud;

import lombok.AccessLevel;
import lombok.Getter;
import meteordevelopment.orbit.ICancellable;
import net.minecraft.client.gui.DrawContext;

public class DrawnWaifuHudEvent implements ICancellable {

    private static final DrawnWaifuHudEvent onTickEvent = new DrawnWaifuHudEvent();

    @Getter(AccessLevel.PUBLIC)
    private DrawContext graphics;

    public static DrawnWaifuHudEvent get(DrawContext graphics) {
        onTickEvent.graphics = graphics;
        return onTickEvent;
    }

    @Override
    public void setCancelled(boolean b) {

    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
