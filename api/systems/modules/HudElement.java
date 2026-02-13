package dev.anarchy.waifuhax.api.systems.modules;

import dev.anarchy.waifuhax.api.gui.WHWindow;
import dev.anarchy.waifuhax.api.settings.Vector2Setting;
import dev.anarchy.waifuhax.api.systems.modules.annotations.ManualInstanciating;
import dev.anarchy.waifuhax.client.WaifuHax;
import dev.anarchy.waifuhax.client.events.hud.DrawnWaifuHudEvent;
import dev.anarchy.waifuhax.client.events.hud.HudMouseClickedEvent;
import dev.anarchy.waifuhax.client.events.hud.HudMouseDragEvent;
import dev.anarchy.waifuhax.client.events.hud.HudMouseReleasedEvent;
import meteordevelopment.orbit.EventHandler;
import org.joml.Vector2f;

@ManualInstanciating
public abstract class HudElement extends AbstractModule {

    protected final WHWindow window = new WHWindow("", new Vector2f(200, 200));

    public Vector2Setting pos = new Vector2Setting("pos", "position of the ingame window", new Vector2f(0, 0), null);

    public HudElement(String displayName) {
        WaifuHax.EVENT_BUS.unsubscribe(window);
        window.setTitle(displayName);
    }

    @Override
    public String getDescription() {
        return "";
    }

    @EventHandler
    public void onHudBeingClicked(HudMouseClickedEvent event) {
        window.onHudBeingClicked(event);
    }

    @EventHandler
    public void onMouseDrag(HudMouseDragEvent event) {
        window.onMouseDrag(event);
    }

    @EventHandler
    public void onHudBeingReleased(HudMouseReleasedEvent event) {
        window.onHudBeingReleased(event);
        pos.setValue(new Vector2f(window.getRoot().getPos().x, window.getRoot().getPos().y));
        this.save();
    }

    @EventHandler
    public void onHudBeingRendered(DrawnWaifuHudEvent event) {
        onRender(event);
        window.onHudBeingDrawn(event);
    }

    public abstract void onRender(DrawnWaifuHudEvent event);

    @Override
    public void onActivate(boolean live) {
        window.getRoot().setPos(pos.getValue().x, pos.getValue().y);
    }
}
