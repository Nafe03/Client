package dev.anarchy.waifuhax.client.events;

import dev.anarchy.waifuhax.api.BaseEvent;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.util.math.MatrixStack;

public class RenderEvent extends BaseEvent {

    public static class World extends RenderEvent {
        private final MatrixStack matrices;
        private final float tickDelta;
        private final Camera camera;
        private final Frustum frustum;

        public World(MatrixStack matrices, float tickDelta, Camera camera, Frustum frustum) {
            this.matrices = matrices;
            this.tickDelta = tickDelta;
            this.camera = camera;
            this.frustum = frustum;
        }

        public MatrixStack getMatrices() {
            return matrices;
        }

        public float getTickDelta() {
            return tickDelta;
        }

        public Camera getCamera() {
            return camera;
        }

        public Frustum getFrustum() {
            return frustum;
        }
    }
}