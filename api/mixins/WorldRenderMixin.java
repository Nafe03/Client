package dev.anarchy.waifuhax.api.mixins;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import dev.anarchy.waifuhax.client.WaifuHax;
import dev.anarchy.waifuhax.client.events.Render3DEvent;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * WorldRenderMixin - Posts Render3DEvent for 3D world rendering
 */
@Mixin(WorldRenderer.class)
public class WorldRenderMixin {

    @Inject(
        method = "render",
        at = @At("TAIL")
    )
    private void onRender(CallbackInfo ci) {
        MatrixStack matrices = new MatrixStack();
        WaifuHax.EVENT_BUS.post(new Render3DEvent(matrices, 0.0f));
    }
}