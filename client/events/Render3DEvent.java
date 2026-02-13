package dev.anarchy.waifuhax.client.events;

import net.minecraft.client.util.math.MatrixStack;

/**
 * Render3DEvent - Posted when the world is being rendered
 * Use this for drawing 3D overlays in the world
 */
public class Render3DEvent {
    
    public final MatrixStack matrices;
    public final float tickDelta;
    
    public Render3DEvent(MatrixStack matrices, float tickDelta) {
        this.matrices = matrices;
        this.tickDelta = tickDelta;
    }
}