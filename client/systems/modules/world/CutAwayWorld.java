package dev.anarchy.waifuhax.client.systems.modules.world;

import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.events.ChunkOcclusionEvent;
import meteordevelopment.orbit.EventHandler;

// CutAwayWorld is a ForgeHax module ported from forge 1.12.2 to
// fabric 1.21.x. see <class>GameRendererMixin</class>
public class CutAwayWorld extends AbstractModule {

    public final FloatSetting nearPlaneDistance = new FloatSetting("Near Plane Distance", "How far the nearplane is set", 5.5f, 0.05f, 10);

    @Override
    public String getDescription() {
        return "Fuck with nearplane to see through the world";
    }

    @EventHandler
    public void onChunkOcclusion(ChunkOcclusionEvent event) {
        event.setCancelled(true);
    }
}
