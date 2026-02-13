package dev.anarchy.waifuhax.client.events;

import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.ICancellable;

public class ChunkOcclusionEvent implements ICancellable {

    private static final ChunkOcclusionEvent EVENT = new ChunkOcclusionEvent();

    @Getter
    @Setter
    private boolean cancelled = false;

    public static ChunkOcclusionEvent get() {
        EVENT.setCancelled(false);
        return EVENT;
    }

}
